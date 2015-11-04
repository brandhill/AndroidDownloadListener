package com.mitsw.filelistener;

import android.os.FileObserver;
import android.util.Log;

import com.mitsw.service.FileListenerService;

/**
 * Created by Hill on 15/8/26.
 */
public class BaseFileObserver extends FileObserver {

    public static final int CREATE_DIR = 1073742080;
    public static final int DELETE_DIR = 1073741840;

    protected static final String TAG = FileListenerService.TAG;

    public BaseFileObserver(String path, int mask) {
        super(path, mask);
    }

    @Override
    public void onEvent(int event, String path) {

        String operation;

        switch(event & FileObserver.ALL_EVENTS){

            case FileObserver.CREATE:
                //文件被创建
                /**
                 * 相关操作
                 */

                operation = "CREATE";
                break;
            case FileObserver.OPEN :
                //文件被打开
                /**
                 * 相关操作
                 */

                operation = "OPEN";
                break;
            case FileObserver.ACCESS:
                //打开文件后，读文件内容操作
                /**
                 * 相关操作
                 */

                operation = "ACCESS";
                break;
            case FileObserver.MODIFY:
                //文件被修改
                /**
                 * 相关操作
                 */

                operation = "MODIFY";
                break;
            case FileObserver.ATTRIB:
                //未明操作
                /**
                 * 相关操作
                 */

                operation = "ATTRIB";
                break;
            case FileObserver.CLOSE_NOWRITE:
                //没有编辑文件，关闭
                /**
                 * 相关操作
                 */

                operation = "CLOSE_NOWRITE";
                break;
            case FileObserver.CLOSE_WRITE:
                //编辑完文件，关闭
                /**
                 * 相关操作
                 */

                operation = "CLOSE_WRITE";
                break;
            case FileObserver.DELETE:
                //文件被删除
                /**
                 * 相关操作
                 */

                operation = "DELETE";
                break;
            case FileObserver.MOVED_FROM:
                //文件被移动
                /**
                 * 相关操作
                 */

                operation = "MOVED_FROM";
                break;



            case CREATE_DIR:


                operation = "CREATE_DIR";
                break;

            case DELETE_DIR:

                operation = "DELETE_DIR";
                break;

            default:

                operation = "default ?!";
                break;
        }

        Log.d(TAG, String.format("event : %d, operation : %s, path : %s", event, operation, path));

    }
}

