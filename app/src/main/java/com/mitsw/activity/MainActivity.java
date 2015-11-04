package com.mitsw.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mitsw.download.listener.DownloadObserver;
import com.mitsw.download.listener.Downloads;
import com.mitsw.util.log.DebugMode;

public class MainActivity extends AppCompatActivity {


    //private final static String DOWNLOAD_URL = "http://img.meilishuo.net/css/images/AndroidShare/Meilishuo_3.6.1_10006.apk";

    private final static String DOWNLOAD_URL ="https://drive.google.com/uc?export=download&id=0B4EUMVYZzs_HdHdUZmxHS2RfQ28"; // file size : 1 mb
//    private final static String DOWNLOAD_URL ="https://drive.google.com/uc?export=download&id=0B4EUMVYZzs_HS0VEeDJ6OHlhdFU"; // file size : 10 mb
//    private final static String DOWNLOAD_URL = "https://drive.google.com/uc?export=download&id=0B4EUMVYZzs_HTzc3RUE0U1JwNFU"; // file size : 20 mb

    private long mDownloadId = -1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_CURRENT_BYTES = "current_bytes";
    public static final String COLUMN_TOTAL_BYTES = "total_bytes";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_MIME_TYPE = "mimetype";
    public static final Uri ALL_DOWNLOADS_CONTENT_URI = Uri.parse("content://downloads/all_downloads");
    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");


    private FileObserver mFileObserver;
    private DownloadObserver mDownloadObserver;
    private final boolean mIsFileObserverEnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        startDownload();

                        //queryDownloadProvider();

                    }
                }.start();
            }
        });

        findViewById(R.id.btn_download_observer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null != mDownloadObserver) {
                    mDownloadObserver.stopDownloadObserver();
                    mDownloadObserver = null;
                    ((Button) view).setText("Start DownloadObserver");
                } else {
                    mDownloadObserver = new DownloadObserver(getApplicationContext());
                    mDownloadObserver.startDownloadObserver();
                    ((Button) view).setText("Stop DownloadObserver");
                }
            }
        });

    }

    private void queryDownloadProvider(){

        Cursor c = getContentResolver().query(
                Downloads.Impl.CONTENT_URI,
                new String[]{
                        COLUMN_ID,
                        COLUMN_CURRENT_BYTES,
                        COLUMN_TOTAL_BYTES,
                        COLUMN_STATUS},
                null, null, null);


        if (c == null) {
            return;
        }

        // Columns match projection in query above
        final int idColumn = 0;
        final int currentBytesColumn = 1;
        final int totalBytesColumn = 2;
        final int statusColumn = 3;

        c.moveToFirst();
        long totalBytes = c.getLong(totalBytesColumn);
        long currentBytes = c.getLong(currentBytesColumn);

        Log.d("MainActivity", String.format("totalBytes : %d, currentBytes : %d", totalBytes, currentBytes));

        c.close();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mDownloadObserver){
            mDownloadObserver.stopDownloadObserver();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mReceiver);

        synchronized (this) {
            long id = mDownloadId;
            if (id != -1) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                downloadManager.remove(id);
                mDownloadId = -1;
            }
        }
        Toast.makeText(this, "Stop Download", Toast.LENGTH_SHORT).show();

    }


    private void startDownload() {
        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DOWNLOAD_URL));
        request.setMimeType("application/vnd.android.package-archive");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "drweb.apk");

        synchronized (MainActivity.this) {
            mDownloadId = downloadManager.enqueue(request);
            DebugMode.Log("@#","id:"+mDownloadId);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Start Download", Toast.LENGTH_SHORT).show();
            }
        });

    }



    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            synchronized(MainActivity.this) {
                mDownloadId = -1;
            }
            Toast.makeText(context, "Donwload complete", Toast.LENGTH_LONG).show();
        }
    };

}
