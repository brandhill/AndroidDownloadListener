package com.mitsw.service;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import com.mitsw.filelistener.RecursiveFileObserver;
import com.mitsw.filelistener.SimpleFileObserver;

public class FileListenerService extends Service {

    public final static String TAG = "FileListener";
    private FileObserver mFileObserver;
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
            mFileObserver = new SimpleFileObserver("/sdcard", FileObserver.ALL_EVENTS);
            mFileObserver.startWatching();

            Log.i(TAG, "File listener is starting ...");
        }
    }

    private void stopFileObserver(){
        if(null != mFileObserver){
            mFileObserver.stopWatching();

            Log.i(TAG, "File listener is stopped.");
        }
    }
}
