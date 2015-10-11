package com.mitsw.download.listener;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.mitsw.download.database.DownloadReportInfoDao;
import com.mitsw.util.log.DebugMode;

import java.util.ArrayList;
import java.util.HashMap;

public class DownloadObserver {

    public final static String TAG = "DownloadObserver";
    private static final long TIMESTAMP_UNKNOWN = -1;
    private static final boolean NOTIFY_CHANGE_WITHOUT_URI = Build.VERSION.SDK_INT < 16;

    private Context mContext;
    private HandlerThread mThread;
    private DownloadProviderObserver mDownloadProviderObserver;

    public DownloadObserver(Context ctx) {
        mContext = ctx;
    }

    public void startDownloadObserver(){
        mThread = new HandlerThread("DownloadObserver");
        mThread.start();
        Handler handler = new Handler(mThread.getLooper());
        mDownloadProviderObserver = new DownloadProviderObserver(handler);
        final ContentResolver resolver = mContext.getContentResolver();
        handler.post(new Runnable() {
            @Override
            public void run() {
                resolver.registerContentObserver(Downloads.Impl.CONTENT_URI, true, mDownloadProviderObserver);
                doInitDownloadObserver(resolver);
            }
        });
    }

    private void doInitDownloadObserver(ContentResolver resolver) {
        Context ctx = mContext;
        HashMap<DownloadReportInfoDao.PrimeKey, DownloadReportInfoDao> daoMap = DownloadReportInfoDao.getAllDataFromDb();
        ArrayList<DownloadInfo> infos = null;
        HashMap<DownloadReportInfoDao.PrimeKey, DownloadReportInfoDao> unfinishedDownloadReportKeys = null;
        if (NOTIFY_CHANGE_WITHOUT_URI) {
            unfinishedDownloadReportKeys = new HashMap<>();
            mDownloadProviderObserver.setDownloadReportKeys(unfinishedDownloadReportKeys);
        }

        if (DebugMode.mEnableLog) {
            DebugMode.Log(TAG, "doInitDownloadObserver");
        }

        if (daoMap.size() <= 0) return;
        String[] projection = null;
        String selection = null;
        String[] selectionArgs = null;
        String orderBy = null;

        Cursor cursor = null;

        try {
            cursor = resolver.query(Downloads.Impl.CONTENT_URI, projection, selection, selectionArgs, orderBy);
            int count = cursor.getCount();

            if (count == 0) return;

            infos = new ArrayList<>(count);
            DownloadInfo.Reader reader = new DownloadInfo.Reader(resolver, cursor);
            while (cursor.moveToNext()) {
                DownloadInfo info = reader.newDownloadInfo(ctx);
                DownloadReportInfoDao.PrimeKey key = new DownloadReportInfoDao.PrimeKey(info);

                DownloadReportInfoDao currentReportDao = DownloadReportInfoDao.createFromDownloadInfo(info,
                        TIMESTAMP_UNKNOWN);

                if (NOTIFY_CHANGE_WITHOUT_URI && !Downloads.Impl.isStatusCompleted(info.mStatus)) {
                    unfinishedDownloadReportKeys.put(key, currentReportDao);
                }

                DownloadReportInfoDao oldReportDao = daoMap.get(key);
                if (null != oldReportDao) {
                    daoMap.remove(key);
                    int currentStatus = currentReportDao.getStatus();
                    if (currentStatus == Downloads.Impl.STATUS_RUNNING) {
                        if (currentReportDao.getTotalByte() > 0 && oldReportDao.getStatus() == Downloads.Impl.STATUS_PENDING) {
                            if (DebugMode.mEnableLog) {
                                DebugMode.Log(TAG, "Report for missed state:"+info);
                            }
                            currentReportDao.setTimestamp(oldReportDao.getTimestamp());
                            currentReportDao.updateToDatabase();
                        }
                    } else if (Downloads.Impl.isStatusCompleted(currentReportDao.getStatus())) {
                        oldReportDao.deleteItem();
                    }

                } else {
                    infos.add(info);
                }
            }
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }

        // 處理 downloadinfo 有出現在 我們的 database 裡面，但是沒有出現在 DownLoad Provider 裡面
        for (DownloadReportInfoDao dao : daoMap.values()) {
            dao.setStatus(Downloads.Impl.STATUS_UNKNOWN_ERROR);
            dao.setTimestamp(TIMESTAMP_UNKNOWN);
            if (DebugMode.mEnableLog) {
                DebugMode.Log(TAG, "Report download that we don't know when to finish:"+dao);
            }

            DownloadReportInfoDao.deleteItemByKey(dao.getPrimeKey());
        }
        updateMissedInfo(infos);
        if (NOTIFY_CHANGE_WITHOUT_URI) {
            mDownloadProviderObserver.setDownloadReportKeys(unfinishedDownloadReportKeys);
        }
    }


    private void handleMissingDownload(DownloadInfo info, DownloadReportInfoDao dao, boolean isNew) {

        if (DebugMode.mEnableLog) {
            DebugMode.Log(TAG, "handle missed download, is new:"+isNew+", info:"+info);
        }

        if (isNew) {
            dao.insertToDatabase();
        } else {
            dao.deleteItem();
        }
    }

    // 處理 downloadinfo 有出現在 DownLoad Provider 裡面，但是沒有出現在我們的 database 裡面
    private void updateMissedInfo(ArrayList<DownloadInfo> infos) {
        for (DownloadInfo info: infos) {
            DownloadReportInfoDao dao = DownloadReportInfoDao.createFromDownloadInfo(info, -1);
            int status = info.mStatus;

            if(DownloadReportInfoDao.checkIfExist(dao.getPrimeKey())){
                if(DebugMode.mEnableLog) {
                    Log.w(TAG, "already exist, dao : "+dao.toString());
                }
            }

            switch (status) {
                case Downloads.Impl.STATUS_PENDING:
                case Downloads.Impl.STATUS_RUNNING:
                    handleMissingDownload(info, dao, true);
                    return;
            }

            if (Downloads.Impl.isStatusError(status) || Downloads.Impl.isStatusSuccess(status)) {
                handleMissingDownload(info, dao, false);
            }
        }
    }


    public void stopDownloadObserver(){
        if (null != mDownloadProviderObserver) {
            mContext.getContentResolver().unregisterContentObserver(mDownloadProviderObserver);
            mDownloadProviderObserver = null;
        }
    }

    public class DownloadProviderObserver extends ContentObserver {
        private HashMap<DownloadReportInfoDao.PrimeKey, DownloadReportInfoDao> mUnfinishedDownloadReportKeys;

        private DownloadProviderObserver(Handler handler) {
            super(handler);
        }

        public void setDownloadReportKeys(HashMap<DownloadReportInfoDao.PrimeKey, DownloadReportInfoDao> downloadReportKeys) {
            this.mUnfinishedDownloadReportKeys = downloadReportKeys;
        }


        private void insertAllPendingDownloads() {
            ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = resolver.query(Downloads.Impl.CONTENT_URI, null, null, null, null);

            try {
                if (cursor == null || cursor.getCount() == 0) return;
                DownloadInfo.Reader reader = new DownloadInfo.Reader(resolver, cursor);
                while (cursor.moveToNext()) {
                    DownloadInfo info = reader.newDownloadInfo(mContext);
                    if (info.mStatus == Downloads.Impl.STATUS_PENDING) {
                        if (DebugMode.mEnableLog) {
                            DebugMode.Log(TAG,"insert pending download task");
                        }
                        DownloadReportInfoDao downloadReportInfoDao = DownloadReportInfoDao.createFromDownloadInfo(info, System.currentTimeMillis());
                        downloadReportInfoDao.insertToDatabase();
                    }
                }
            } finally {
                if (null != cursor) cursor.close();
            }

        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (DebugMode.mEnableLog){
                DebugMode.Log(TAG, "onChange : " + uri);
            }

            String lastPath = uri.getLastPathSegment();

            try {
                Integer.parseInt(lastPath);
            } catch (RuntimeException e) {
                insertAllPendingDownloads();
                return;
            }

            ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, null, null, null, null);
                if (null == cursor || cursor.getCount() == 0) return;
                cursor.moveToNext();
                DownloadInfo.Reader reader = new DownloadInfo.Reader(resolver, cursor);
                DownloadInfo info = reader.newDownloadInfo(mContext);
                DownloadReportInfoDao currentDownloadReportDao =
                        DownloadReportInfoDao.createFromDownloadInfo(info, TIMESTAMP_UNKNOWN);
                DownloadReportInfoDao.handleOnChanged(currentDownloadReportDao, mContext);
            } catch (RuntimeException e) {
                Log.d(TAG, "exception", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        @Override
        public void onChange(boolean selfChange) {
            if (!NOTIFY_CHANGE_WITHOUT_URI) {
                return;
            }

            if (DebugMode.mEnableLog){
                DebugMode.Log(TAG, "onChange no uri");
            }
            ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;
            try {
                cursor = resolver.query(Downloads.Impl.CONTENT_URI, null, null, null, null);
                DownloadReportInfoDao.handleOnChanged(cursor, resolver, mContext, mUnfinishedDownloadReportKeys);
            } catch (RuntimeException e) {
                Log.d(TAG, "exception", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
}
