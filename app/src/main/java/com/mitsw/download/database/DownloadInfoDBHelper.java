package com.mitsw.download.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mitsw.application.MitswApplication;

/**
 * Created by Hill on 15/8/28.
 */
public class DownloadInfoDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "download_report.db";

    private static DownloadInfoDBHelper sInstance;

    public static class DownloadInfoTable {

        public static final String TABLE_NAME = "download_report";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_MIME_TYPE = "mimetype";
        public static final String COLUMN_FILE_EXT = "file_ext";
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";
        public static final String COLUMN_CREATE_TIME = "create_time"; // 加到 DB 的時間
        /*
        public static final String COLUMN_FILE_NAME_HINT = "hint";
        public static final String COLUMN_DATA = "_data";
        public static final String COLUMN_DESTINATION = "destination";
        public static final String COLUMN_VISIBILITY = "visibility";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_LAST_MODIFICATION = "lastmod";
        public static final String COLUMN_TOTAL_BYTES = "total_bytes";
        public static final String COLUMN_CURRENT_BYTES = "current_bytes";
        public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_CONTROL = "control";
        */

        private static final String CREATE_TABLE = new StringBuilder("CREATE TABLE ").append(TABLE_NAME).append(" ( ")
                .append(COLUMN_ID).append(" INTEGER").append(",")
                .append(COLUMN_URI).append(" TEXT").append(",")
                .append(COLUMN_NOTIFICATION_PACKAGE).append(" TEXT").append(",")
                .append(COLUMN_STATUS).append(" INTEGER").append(",")
                .append(COLUMN_MIME_TYPE).append(" TEXT").append(",")
                .append(COLUMN_FILE_EXT).append(" TEXT").append(",")
                .append(COLUMN_TOTAL_BYTES).append(" INTEGER").append(",")
                .append(COLUMN_CREATE_TIME).append(" INTEGER").append(",")
                .append("PRIMARY KEY (").append(COLUMN_ID).append(",")
                                        .append(COLUMN_URI).append(",")
                                        .append(COLUMN_NOTIFICATION_PACKAGE).append(")")
                .append(" );").toString();

    }

    private DownloadInfoDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DownloadInfoTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static DownloadInfoDBHelper getInstance() {
        if (sInstance == null) {
            Context context = MitswApplication.getInstance().getApplicationContext();
            synchronized (DownloadInfoDBHelper.class) {
                if (sInstance == null) {
                    sInstance = new DownloadInfoDBHelper(context);
                }
            }
        }
        return sInstance;
    }
}
