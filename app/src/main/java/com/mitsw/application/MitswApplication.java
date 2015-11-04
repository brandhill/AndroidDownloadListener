package com.mitsw.application;

import android.app.Application;
import android.content.Intent;

import com.mitsw.service.FileListenerService;


public class MitswApplication extends Application {
    private static MitswApplication theApp;

    public MitswApplication() {
        theApp = this;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent = new Intent(this, FileListenerService.class);
        startService(intent);
    }

    public static MitswApplication getInstance() {
        return theApp;
    }
}
