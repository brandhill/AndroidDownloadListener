package com.mitsw.application;

import android.app.Application;


public class MitswApplication extends Application {
    private static MitswApplication theApp;

    public MitswApplication() {
        theApp = this;
    }


    public static MitswApplication getInstance() {
        return theApp;
    }
}
