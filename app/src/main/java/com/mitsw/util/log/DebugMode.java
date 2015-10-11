package com.mitsw.util.log;

import android.util.Log;

/**
 * Created by leo_lu on 2015/8/28.
 */
public class DebugMode {
    public static final boolean mEnableLog = true;
    public static void Log(String TAG, String log) {
        Log.d(TAG, log);
    }

    public static void Log(String TAG, String log, Throwable throwable) {
        Log.d(TAG, log, throwable);

    }
}
