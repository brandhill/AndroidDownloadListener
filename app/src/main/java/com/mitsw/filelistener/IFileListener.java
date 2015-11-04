package com.mitsw.filelistener;

/**
 * Created by Hill on 15/9/1.
 */
public interface IFileListener {

    void onFileOpened(String path);

    void onFileClosed(String path);

    void onFileCreated(String path);

    void onFileDeleted(String path);

    void onFileRenamed(String path, boolean isMovedFrom);

    void onUnknownOperation(String path);
}