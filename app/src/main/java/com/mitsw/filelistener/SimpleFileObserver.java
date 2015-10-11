package com.mitsw.filelistener;

/**
 * Created by Hill on 15/8/26.
 */
public class SimpleFileObserver extends BaseFileObserver {


    public SimpleFileObserver(String path, int mask) {
        super(path, mask);
    }

    @Override
    public void onEvent(int event, String path) {
        super.onEvent(event, path);

    }
}
