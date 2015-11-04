package com.mitsw.filelistener;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Hill on 15/9/2.
 */
public class FileListenerImpl implements IFileListener {


    private static final String TAG = "FileListenerImpl";
    public static final long FILE_SIZE_UNKNOWN = -1;

    public static final String CHROME_TEMP_FILE_EXTENSION_SUFFIX = ".crdownload";


    @Override
    public void onFileCreated(String path) {
        Log.i(TAG, "File Created : " + path);
    }


    @Override
    public void onFileOpened(String path) {
        Log.i(TAG, "File Opened : " + path);
    }

    @Override
    public void onFileClosed(String path) {
        Log.i(TAG, "File Closed : " + path);
    }


    @Override
    public void onFileDeleted(String path) {
        Log.i(TAG, "File Deleted : " + path);
    }

    @Override
    public void onFileRenamed(String path, boolean isMovedFrom) {
        Log.i(TAG, "File Renamed : " + path);
    }

    @Override
    public void onUnknownOperation(String path) {
        Log.i(TAG, "Unknown operation : " + path);
    }

    public static String getSkipPathName(String path) {
        String[] array = path.split(CHROME_TEMP_FILE_EXTENSION_SUFFIX);
        if (array.length >= 1) {
            return array[0];
        } else {
            return "";
        }

    }

    public static long getFileSize(String path) {
        String targetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

        try {
            File file = new File(targetDirectory + File.separator + path);

            Log.d(TAG, "file.isDirectory() : " + file.isDirectory() + " , file.exists() : " + file.exists());

            return Integer.parseInt(String.valueOf(file.length()));
        } catch (NumberFormatException e) {
            Log.i(TAG, "getFileSize fails ", e);
        }

        return FILE_SIZE_UNKNOWN;

    }

}
