package com.mitsw.download.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.mitsw.download.listener.DownloadInfo;
import com.mitsw.download.listener.DownloadObserver;
import com.mitsw.download.listener.Downloads;
import com.mitsw.util.log.DebugMode;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Hill on 15/8/28.
 */
public class DownloadReportInfoDao {
    public static final String TAG = DownloadObserver.TAG;
    public static final String UNKNOWN = "unknown";

    private PrimeKey mKey;

    private int mStatus;
    private long mTimestamp;
    private String mFileExt;
    private String mMimeType;
    private long mTotalByte;

    public PrimeKey getPrimeKey(){
        return mKey;
    }

    public long getTotalByte() {
        return mTotalByte;
    }
    public void setTotalByte(long bytes) {
        mTotalByte = bytes;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public String getFileExt() {
        return mFileExt;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        mTimestamp = timestamp;
    }

    private static String getDownloadInfoFileExtension(String hint, String filePath){
        if(!TextUtils.isEmpty(hint)){
            return getFileExtension(hint);
        }else if(!TextUtils.isEmpty(filePath)){
            return getFileExtension(filePath);
        }

        return UNKNOWN;
    }

    private static String getFileExtension(String filePath){
        String extName;
        String[] afterSplit = filePath.split("\\.");
        if(afterSplit.length > 0) {
            extName = afterSplit[afterSplit.length-1];
        }else{
            extName = UNKNOWN;
        }
        return extName;
    }

    public static DownloadReportInfoDao createFromDownloadInfo(DownloadInfo downloadinfo, long timestamp) {
        DownloadReportInfoDao dao = new DownloadReportInfoDao();
        dao.mKey = new PrimeKey(downloadinfo);
        dao.mStatus = downloadinfo.mStatus;
        dao.mMimeType = downloadinfo.mMimeType;
        dao.mFileExt = getDownloadInfoFileExtension(downloadinfo.mHint, downloadinfo.mFileName);
        dao.mTotalByte = downloadinfo.mTotalBytes;
        dao.mTimestamp = timestamp;
        return dao;
    }

    public static DownloadReportInfoDao createFromDatabase(PrimeKey key) {
        SQLiteDatabase db = DownloadInfoDBHelper.getInstance().getWritableDatabase();
        DownloadReportInfoDao dao = null;
        Cursor c = null;

        try {
            c = db.query(DownloadInfoDBHelper.DownloadInfoTable.TABLE_NAME, null, key.getWhereClause(), key.getWhereArgs(), null, null, null);
            if (null != c && c.getCount() > 0) {
                Builder builder = new Builder(c);
                c.moveToFirst();
                dao = builder.createFromCursor(c);
            }
        } catch (RuntimeException e) {
            if (DebugMode.mEnableLog) {
                DebugMode.Log(TAG, "Fail to query data for key:" + key, e);
            }
            e.printStackTrace();
        } finally {
            if (null != c) c.close();
        }
        return dao;
    }

    public static boolean checkIfExist(PrimeKey key) {

        SQLiteDatabase db = DownloadInfoDBHelper.getInstance().getWritableDatabase();
        Cursor c = null;

        try {
            c = db.query(DownloadInfoDBHelper.DownloadInfoTable.TABLE_NAME, null, key.getWhereClause(), key.getWhereArgs(), null, null, null);
            if (null != c && c.getCount() > 0) {
                return true;
            }
        } catch (RuntimeException e) {
            if (DebugMode.mEnableLog) {
                DebugMode.Log(TAG, "Fail to query data for key:" + key, e);
            }
            e.printStackTrace();
        } finally {
            if (null != c) c.close();
        }
        return false;
    }

    private DownloadReportInfoDao(){}

    public static class PrimeKey {
        private long mId;
        private String mUri;
        private String mPackage;

        public PrimeKey(){
        }

        public PrimeKey(DownloadInfo downloadinfo) {
            mId = downloadinfo.mId;
            mUri = downloadinfo.mUri;
            mPackage = downloadinfo.mPackage;
            if (null == mPackage) mPackage = "";
        }
        public String getWhereClause() {
            return DownloadInfoDBHelper.DownloadInfoTable.COLUMN_ID+" = ? AND "
                    +DownloadInfoDBHelper.DownloadInfoTable.COLUMN_URI+" = ? AND "
                    +DownloadInfoDBHelper.DownloadInfoTable.COLUMN_NOTIFICATION_PACKAGE+" = ?";
        }

        public String[] getWhereArgs() {
            return new String[]{Long.toString(mId), mUri, mPackage};
        }

        @Override
        public boolean equals(Object obj) {

            if (obj == this) {
                return true;
            }

            if (obj == null || !(obj instanceof PrimeKey)) {
                return false;
            }

            PrimeKey primeKey = (PrimeKey) obj;

            if(primeKey.mId == this.mId
                    && primeKey.mUri.equalsIgnoreCase(this.mUri)
                    && primeKey.mPackage.equalsIgnoreCase(this.mPackage)){
                return true;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return (int) mId;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            return sb.append("id:")
                    .append(mId)
                    .append(", url:")
                    .append(mUri)
                    .append(", pkg:")
                    .append(mPackage)
                    .toString();
        }
    }

    public static class Builder {
        private int mIndexForId;
        private int mIndexForUri;
        private int mIndexForPackage;
        private int mIndexForTimesamp;
        private int mIndexForStatue;
        private int mIndexForMimeType;
        private int mIndexForFileExt;
        private int mIndexForTotalByte;

        Builder(Cursor c) {
            mIndexForId = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_ID);
            mIndexForUri = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_URI);
            mIndexForPackage = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_NOTIFICATION_PACKAGE);
            mIndexForStatue = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_STATUS);
            mIndexForMimeType = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_MIME_TYPE);
            mIndexForFileExt = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_FILE_EXT);
            mIndexForTotalByte = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_TOTAL_BYTES);
            mIndexForTimesamp = c.getColumnIndex(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_CREATE_TIME);
        }

        private String getString(Cursor c, int index) {
            String str = c.getString(mIndexForPackage);
            return str != null ? str : "";
        }

        public DownloadReportInfoDao createFromCursor(Cursor c) {
            DownloadReportInfoDao dao = new DownloadReportInfoDao();
            PrimeKey key = new PrimeKey();
            key.mId = c.getLong(mIndexForId);
            key.mUri = c.getString(mIndexForUri);
            key.mPackage = getString(c, mIndexForPackage);
            dao.mKey = key;
            dao.mStatus = c.getInt(mIndexForStatue);
            dao.mMimeType = c.getString(mIndexForMimeType);
            dao.mFileExt = c.getString(mIndexForFileExt);
            dao.mTotalByte = c.getLong(mIndexForTotalByte);
            dao.mTimestamp = c.getLong(mIndexForTimesamp);
            return dao;
        }
    }

    public static HashMap<PrimeKey, DownloadReportInfoDao> getAllDataFromDb() {
        HashMap<PrimeKey, DownloadReportInfoDao> map = new HashMap<>();
        SQLiteDatabase db = DownloadInfoDBHelper.getInstance().getWritableDatabase();

        Cursor c = null;
        try {
            c = db.query(DownloadInfoDBHelper.DownloadInfoTable.TABLE_NAME, null, null, null, null, null, null);

            if (c.getCount() == 0) return map;

            Builder builder = new Builder(c);
            while(c.moveToNext()) {
                DownloadReportInfoDao dao = builder.createFromCursor(c);
                map.put(dao.mKey, dao);
            }
        } catch (Exception e) {
        } finally {
            if (null != c) {
                c.close();
            }
        }

        return map;

    }

    public static boolean deleteItemByKey(PrimeKey key) {
        SQLiteDatabase db = DownloadInfoDBHelper.getInstance().getWritableDatabase();
        String whereClause = key.getWhereClause();
        String[] whereArgs = key.getWhereArgs();
        db.delete(DownloadInfoDBHelper.DownloadInfoTable.TABLE_NAME, whereClause, whereArgs);
        return true;
    }


    public boolean deleteItem() {
        if (DebugMode.mEnableLog) {
            DebugMode.Log(TAG, "deleteItem:" + this);
        }
        SQLiteDatabase db = DownloadInfoDBHelper.getInstance().getWritableDatabase();
        PrimeKey key = mKey;
        String whereClause = key.getWhereClause();
        String[] whereArgs = key.getWhereArgs();
        db.delete(DownloadInfoDBHelper.DownloadInfoTable.TABLE_NAME, whereClause, whereArgs);
        return true;
    }

    public boolean insertToDatabase() {
        try {
            SQLiteDatabase db = DownloadInfoDBHelper.getInstance().getWritableDatabase();
            return db.insert(DownloadInfoDBHelper.DownloadInfoTable.TABLE_NAME, null, convertToContetnValues()) > 0;
        } catch (RuntimeException e) {
            if(DebugMode.mEnableLog) {
                DebugMode.Log(TAG, "Fail to insert");
            }
        }
        return false;
    }

    public boolean updateToDatabase() {
        try {
            SQLiteDatabase db = DownloadInfoDBHelper.getInstance().getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_STATUS, mStatus);
            cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_TOTAL_BYTES, mTotalByte);
            cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_CREATE_TIME, mTimestamp);
            return db.update(DownloadInfoDBHelper.DownloadInfoTable.TABLE_NAME, cv
                    , mKey.getWhereClause(), mKey.getWhereArgs()) > 0;
        } catch (RuntimeException e) {
            if(DebugMode.mEnableLog) {
                DebugMode.Log(TAG, "Fail to insert");
            }
        }
        return false;
    }

    private ContentValues convertToContetnValues() {
        ContentValues cv = new ContentValues();
        PrimeKey key = mKey;
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_ID, key.mId);
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_URI, key.mUri);
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_NOTIFICATION_PACKAGE, key.mPackage);
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_STATUS, mStatus);
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_MIME_TYPE, mMimeType);
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_FILE_EXT, mFileExt);
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_TOTAL_BYTES, mTotalByte);
        cv.put(DownloadInfoDBHelper.DownloadInfoTable.COLUMN_CREATE_TIME, mTimestamp);
        return cv;
    }

    public static void handleOnChanged(Cursor cursor, ContentResolver resolver, Context context, HashMap<PrimeKey, DownloadReportInfoDao> unfinishedDownloadReportKeys) {
        if (null == cursor || cursor.getCount() == 0) return;
        HashSet<PrimeKey> disapearedKey = new HashSet<>(unfinishedDownloadReportKeys.keySet());
        DownloadInfo.Reader reader = new DownloadInfo.Reader(resolver, cursor);
        while (cursor.moveToNext()) {
            DownloadInfo info = reader.newDownloadInfo(context);
            DownloadReportInfoDao currentDao = DownloadReportInfoDao.createFromDownloadInfo(info, System.currentTimeMillis());
            PrimeKey key = currentDao.mKey;
            DownloadReportInfoDao oldRecordedDao = unfinishedDownloadReportKeys.get(key);

            if (null != oldRecordedDao) {
                disapearedKey.remove(key);
            }

            if ((null == oldRecordedDao && !Downloads.Impl.isStatusCompleted((currentDao.getStatus())))
                    || (oldRecordedDao != null && (currentDao.getStatus() != oldRecordedDao.getStatus()))) {
                if (handleOnChanged(currentDao, context)) {
                    unfinishedDownloadReportKeys.put(key, currentDao);
                }
            }
            if (Downloads.Impl.isStatusCompleted((currentDao.getStatus()))) {
                unfinishedDownloadReportKeys.remove(key);
            }
        }

        for(PrimeKey key: disapearedKey) {
            DownloadReportInfoDao disapearedDao = unfinishedDownloadReportKeys.get(key);
            disapearedDao.setStatus(Downloads.Impl.STATUS_UNKNOWN_ERROR);
            disapearedDao.deleteItem();
        }
    }

    public static boolean handleOnChanged(DownloadReportInfoDao currentDownloadReportDao, Context context) {
        switch (currentDownloadReportDao.getStatus()) {
            case Downloads.Impl.STATUS_PENDING: {
                currentDownloadReportDao.setTimestamp(System.currentTimeMillis());
                currentDownloadReportDao.insertToDatabase();
                return true;
            }
            case Downloads.Impl.STATUS_RUNNING: {
                DownloadReportInfoDao oldDownloadReportInfoDao =
                        DownloadReportInfoDao.createFromDatabase(currentDownloadReportDao.mKey);
                if (null == oldDownloadReportInfoDao) {
                    if (DebugMode.mEnableLog) {
                        DebugMode.Log(TAG, "insert record that we never receive its pending event : " + currentDownloadReportDao);
                    }
                    if (currentDownloadReportDao.getTotalByte() < 0) {
                        currentDownloadReportDao.setTimestamp(System.currentTimeMillis());
                    }
                    currentDownloadReportDao.setStatus(Downloads.Impl.STATUS_PENDING);
                    if (currentDownloadReportDao.getTotalByte() >= 0) {
                        currentDownloadReportDao.setStatus(Downloads.Impl.STATUS_RUNNING);
                    }
                    currentDownloadReportDao.insertToDatabase();
                    return true;
                } else if (currentDownloadReportDao.getTotalByte() >= 0 && oldDownloadReportInfoDao.getStatus() == Downloads.Impl.STATUS_PENDING) {
                    currentDownloadReportDao.setTimestamp(oldDownloadReportInfoDao.getTimestamp());
                    currentDownloadReportDao.updateToDatabase();
                    return true;
                }
            }
            default: {
                if(Downloads.Impl.isStatusCompleted(currentDownloadReportDao.getStatus())) {
                    DownloadReportInfoDao oldDownloadReportInfoDao =
                            DownloadReportInfoDao.createFromDatabase(currentDownloadReportDao.mKey);
                    if (null != oldDownloadReportInfoDao) {
                        currentDownloadReportDao.setTimestamp(oldDownloadReportInfoDao.getTimestamp());
                    }
                    currentDownloadReportDao.deleteItem();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("PrimeKey: {")
                .append(mKey)
                .append("}, status:")
                .append(mStatus)
                .append(", size:")
                .append(mTotalByte)
                .append(", ext:")
                .append(mFileExt)
                .append(", mime:")
                .append(mMimeType)
                .append(" start time:")
                .append(mTimestamp).toString();
    }
}
