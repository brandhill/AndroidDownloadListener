/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mitsw.download.listener;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Details about a specific download. Fields should only be mutated by updating
 * from database query.
 */
public class DownloadInfo {
    private final static String TAG = "DownloadInfo";

    // TODO: move towards these in-memory objects being sources of truth, and
    // periodically pushing to provider.

    public static class Reader {
        private ContentResolver mResolver;
        private Cursor mCursor;

        public Reader(ContentResolver resolver, Cursor cursor) {
            mResolver = resolver;
            mCursor = cursor;
        }

        public DownloadInfo newDownloadInfo( Context context) {
            final DownloadInfo info = new DownloadInfo(context);
            updateFromDatabase(info);
//            readRequestHeaders(info);
            return info;
        }

        public void updateFromDatabase(DownloadInfo info) {

            info.mId = getLong(Downloads.Impl._ID);
            info.mUri = getString(Downloads.Impl.COLUMN_URI);
            info.mHint = getString(Downloads.Impl.COLUMN_FILE_NAME_HINT);
            info.mFileName = getString(Downloads.Impl._DATA);
            info.mMimeType = getString(Downloads.Impl.COLUMN_MIME_TYPE);
            info.mDestination = getInt(Downloads.Impl.COLUMN_DESTINATION);
            info.mVisibility = getInt(Downloads.Impl.COLUMN_VISIBILITY);
            info.mStatus = getInt(Downloads.Impl.COLUMN_STATUS);
            info.mLastMod = getLong(Downloads.Impl.COLUMN_LAST_MODIFICATION);
            info.mPackage = getString(Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE);
            info.mClass = getString(Downloads.Impl.COLUMN_NOTIFICATION_CLASS);
            info.mTotalBytes = getLong(Downloads.Impl.COLUMN_TOTAL_BYTES);
            info.mCurrentBytes = getLong(Downloads.Impl.COLUMN_CURRENT_BYTES);
            info.mDeleted = getInt(Downloads.Impl.COLUMN_DELETED) == 1;
            info.mMediaProviderUri = getString(Downloads.Impl.COLUMN_MEDIAPROVIDER_URI);
            info.mTitle = getString(Downloads.Impl.COLUMN_TITLE);
            info.mDescription = getString(Downloads.Impl.COLUMN_DESCRIPTION);
            info.mControl = getInt(Downloads.Impl.COLUMN_CONTROL); // 原本用 synchronized (this) 包住


            //info.mNumFailed = getInt(Downloads.Impl.COLUMN_FAILED_CONNECTIONS); // does not exist
            //info.mNoIntegrity = getInt(Downloads.Impl.COLUMN_NO_INTEGRITY) == 1;  //does not exist
            //info.mMimeType = Intent.normalizeMimeType(getString(Downloads.Impl.COLUMN_MIME_TYPE)); //does not exist
            //int retryRedirect = getInt(Constants.RETRY_AFTER_X_REDIRECT_COUNT); // does not exist
            //info.mRetryAfter = retryRedirect & 0xfffffff;
            //info.mExtras = getString(Downloads.Impl.COLUMN_NOTIFICATION_EXTRAS); // does not exist
            //info.mCookies = getString(Downloads.Impl.COLUMN_COOKIE_DATA); // does not exist
            //info.mcookiedata = getString(Downloads.Impl.COLUMN_USER_AGENT); // does not exist
            //info.mReferer = getString(Downloads.Impl.COLUMN_REFERER); // does not exist
            //info.mETag = getString(Constants.ETAG); // does not exist
            //info.mUid = getInt(Constants.UID); // does not exist
            //info.mMediaScanned = getInt(Downloads.Impl.COLUMN_MEDIA_SCANNED); // does not exist
            //info.mIsPublicApi = getInt(Downloads.Impl.COLUMN_IS_PUBLIC_API) != 0; // does not exist
            //info.mAllowedNetworkTypes = getInt(Downloads.Impl.COLUMN_ALLOWED_NETWORK_TYPES); // does not exist
            //info.mAllowRoaming = getInt(Downloads.Impl.COLUMN_ALLOW_ROAMING) != 0; // does not exist
            //info.mAllowMetered = getInt(Downloads.Impl.COLUMN_ALLOW_METERED) != 0; // does not exist
            //info.mBypassRecommendedSizeLimit = getInt(Downloads.Impl.COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT); // does not exist


        }

        private void readRequestHeaders(DownloadInfo info) {
            info.mRequestHeaders.clear();
            Uri headerUri = Uri.withAppendedPath(
                    info.getAllDownloadsUri(), Downloads.Impl.RequestHeaders.URI_SEGMENT);
            Cursor cursor = mResolver.query(headerUri, null, null, null, null);
            try {
                int headerIndex =
                        cursor.getColumnIndexOrThrow(Downloads.Impl.RequestHeaders.COLUMN_HEADER);
                int valueIndex =
                        cursor.getColumnIndexOrThrow(Downloads.Impl.RequestHeaders.COLUMN_VALUE);
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    addHeader(info, cursor.getString(headerIndex), cursor.getString(valueIndex));
                }
            } finally {
                cursor.close();
            }

            if (info.mCookies != null) {
                addHeader(info, "Cookie", info.mCookies);
            }
            if (info.mReferer != null) {
                addHeader(info, "Referer", info.mReferer);
            }
        }

        private void addHeader(DownloadInfo info, String header, String value) {
            info.mRequestHeaders.add(Pair.create(header, value));
        }

        private String getString(String column) {

            //Log.d(TAG, "getString column : "+column);

            int index;
            String data;
            try {
                index = mCursor.getColumnIndexOrThrow(column);
                data = mCursor.getString(index);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            //Log.d(TAG, String.format("data : %s", data));

            return (TextUtils.isEmpty(data)) ? null : data;
        }

        private Integer getInt(String column) {

            //Log.d(TAG, "getInt column : "+column);

            int data;
            int index;
            try {
                index = mCursor.getColumnIndexOrThrow(column);
                data = mCursor.getInt(index);

            } catch (Exception e) {
                e.printStackTrace();
                data = -1;
                index = -1;
            }

            //Log.d(TAG, String.format("data : %d", data));

            return data;
        }

        private Long getLong(String column) {

            //Log.d(TAG, "getLong column : "+column);

            long data;
            int index;
            try {
                index = mCursor.getColumnIndexOrThrow(column);
                data = mCursor.getLong(index);

            } catch (Exception e) {
                e.printStackTrace();
                data = -1;
                index = -1;
            }

            //Log.d(TAG, String.format("data : %d", data));

            return data;
        }

        private void dumpCursorContent(Cursor c){

            c.moveToFirst();

            if (c.getCount() > 0) {
                int colummnCount = c.getColumnCount();

                String[] columns = c.getColumnNames();

                while (c.moveToNext()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < colummnCount; i++) {
                        if (sb.length() != 0) sb.append(", ");
                        sb.append(columns[i]).append(": ").append(c.getString(i));
                    }
                    Log.d(TAG, sb.toString());
                }
            } else {
                Log.d(TAG, "no data");
            }
        }
    }



    /**
     * For intents used to notify the user that a download exceeds a size threshold, if this extra
     * is true, WiFi is required for this download size; otherwise, it is only recommended.
     */
    public static final String EXTRA_IS_WIFI_REQUIRED = "isWifiRequired";

    public long mId;
    public String mUri;
    @Deprecated
    public boolean mNoIntegrity;
    public String mHint;
    public String mFileName;
    public String mMimeType;
    public int mDestination;
    public int mVisibility;
    public int mControl;
    public int mStatus;
    public int mNumFailed;
    public int mRetryAfter;
    public long mLastMod;
    public String mPackage;
    public String mClass;
    public String mExtras;
    public String mCookies;
    public String mUserAgent;
    public String mReferer;
    public long mTotalBytes;
    public long mCurrentBytes;
    public String mETag;
    public int mUid;
    public int mMediaScanned;
    public boolean mDeleted;
    public String mMediaProviderUri;
    public boolean mIsPublicApi;
    public int mAllowedNetworkTypes;
    public boolean mAllowRoaming;
    public boolean mAllowMetered;
    public String mTitle;
    public String mDescription;
    public int mBypassRecommendedSizeLimit;


    private List<Pair<String, String>> mRequestHeaders = new ArrayList<Pair<String, String>>();
    private final Context mContext;


    private DownloadInfo(Context context) {
        mContext = context;
    }

    public Collection<Pair<String, String>> getHeaders() {
        return Collections.unmodifiableList(mRequestHeaders);
    }

    public String getUserAgent() {
        if (mUserAgent != null) {
            return mUserAgent;
        } else {
            return Constants.DEFAULT_USER_AGENT;
        }
    }

    public Uri getMyDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI, mId);
    }

    public Uri getAllDownloadsUri() {
        return ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, mId);
    }

    @Override
    public String toString() {
        return dumpInfoData();
    }

    public String dumpInfoData() {

        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("mId", mId);
            jsonObj.put("mUri", mUri);
            jsonObj.put("mHint", mHint);
            jsonObj.put("mStatus", Downloads.Impl.statusToString(mStatus) );
            jsonObj.put("mLastMod", mLastMod);
            jsonObj.put("mFileName", mFileName);
            jsonObj.put("mMimeType", mMimeType);
            jsonObj.put("mDestination", mDestination);
            jsonObj.put("mVisibility", mVisibility);
            jsonObj.put("mPackage", mPackage);
            jsonObj.put("mClass", mClass);
            jsonObj.put("mTotalBytes", mTotalBytes);
            jsonObj.put("mCurrentBytes", mCurrentBytes);
            jsonObj.put("mDeleted", mDeleted);
            jsonObj.put("mMediaProviderUri", mMediaProviderUri);
            jsonObj.put("mTitle", mTitle);
            jsonObj.put("mDescription", mDescription);
            jsonObj.put("mControl", mControl);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return jsonObj.toString();

    }


    /**
     * Query and return status of requested download.
     */
    public static int queryDownloadStatus(ContentResolver resolver, long id) {
        final Cursor cursor = resolver.query(
                ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id),
                new String[] { Downloads.Impl.COLUMN_STATUS }, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            } else {
                // TODO: increase strictness of value returned for unknown
                // downloads; this is safe default for now.
                return Downloads.Impl.STATUS_PENDING;
            }
        } finally {
            cursor.close();
        }
    }
}
