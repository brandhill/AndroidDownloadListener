package com.mitsw.filelistener;

import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

import com.mitsw.service.FileListenerService;

/**
 * Created by Hill on 15/8/26.
 */
public class BaseFileObserver extends FileObserver {


    public static final int IS_DIR_BIT = 0x40000100;
    public static final  int FILE_DOWNLOADING = CREATE | DELETE | MOVED_FROM | MOVED_TO | CLOSE_WRITE | CLOSE_NOWRITE | OPEN;
    protected static final String TAG = "BaseFileObserver";

    private String IGNORE_PREFIX_PATH_NAME = ".com.google.Chrome";

    IFileListener mFileListener;

    public BaseFileObserver(String path, int mask) {
        super(path, mask);
    }

    public void setFileListener(IFileListener fl){
        mFileListener = fl;
    }

    @Override
    public void onEvent(int event, String path) {

        if(TextUtils.isEmpty(path) || path.startsWith(IGNORE_PREFIX_PATH_NAME)){
            return;
        }

        String operation;
        int eventMask = event & FileObserver.ALL_EVENTS;
        boolean isDir = ((event & IS_DIR_BIT) != 0);
        switch(eventMask){
            case CREATE:
                //文件被创建

                if(null != mFileListener){
                    mFileListener.onFileCreated(path);
                }

                operation = "CREATE";
                break;
            case OPEN :
                //文件被打开

                if(null != mFileListener){
                    mFileListener.onFileOpened(path);
                }

                operation = "OPEN";
                break;
            case ACCESS:
                //打开文件后，读文件内容操作
                operation = "ACCESS";
                break;
            case MODIFY:
                //文件被修改
                operation = "MODIFY";
                break;
            case ATTRIB:
                operation = "ATTRIB";
                break;
            case CLOSE_NOWRITE:
                //没有编辑文件，关闭

                if(null != mFileListener){
                    mFileListener.onFileClosed(path);
                }

                operation = "CLOSE_NOWRITE";
                break;
            case CLOSE_WRITE:

                if(null != mFileListener){
                    mFileListener.onFileClosed(path);
                }

                operation = "CLOSE_WRITE";
                break;
            case DELETE:

                if(null != mFileListener){
                    mFileListener.onFileDeleted(path);
                }

                operation = "DELETE";
                break;

            case DELETE_SELF:
                if(null != mFileListener){
                    mFileListener.onFileDeleted(path);
                }

                operation = "DELETE_SELF";
                break;

            case MOVED_TO:

                if(null != mFileListener){
                    mFileListener.onFileRenamed(path, false);
                }

                operation = "MOVED_TO";
                break;

            case MOVED_FROM:

                if(null != mFileListener){
                    mFileListener.onFileRenamed(path, true);
                }

                operation = "MOVED_FROM";
                break;

            default:
                if(null != mFileListener){
                    mFileListener.onUnknownOperation(path);
                }

                operation = "Unknow:"+ Integer.toHexString(eventMask);
                break;
        }


//            Log.i(TAG, String.format("event : %#x, isDir: %b, operation : %s, path : %s", event, isDir, operation, path));


    }
}

