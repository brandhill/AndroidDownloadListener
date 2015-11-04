package com.mitsw.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.mitsw.filelistener.BaseFileObserver;
import com.mitsw.filelistener.FileListenerImpl;
import com.mitsw.filelistener.SimpleFileObserver;

public class FileListenerService extends Service {

    public final static String TAG = "FileListener";
    private BaseFileObserver mFileObserver;
    public FileListenerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        startFileObserver();
        Log.i(TAG, "Service is starting ...");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFileObserver();

    }



    private void startFileObserver(){
        if(null == mFileObserver) {
            String targetDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            mFileObserver = new SimpleFileObserver(targetDirectory, BaseFileObserver.FILE_DOWNLOADING);
            mFileObserver.setFileListener(new FileListenerImpl());
            mFileObserver.startWatching();

            Log.i(TAG, "File listener is started, target directory : " + targetDirectory);
        }
    }

    private void stopFileObserver(){
        if(null != mFileObserver){
            mFileObserver.stopWatching();

            Log.i(TAG, "File listener is stopped.");
        }
    }
}
