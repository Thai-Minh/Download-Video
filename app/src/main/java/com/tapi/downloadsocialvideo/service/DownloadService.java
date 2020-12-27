package com.tapi.downloadsocialvideo.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.function.downloader.DownloadManagerImpl;
import com.tapi.downloadsocialvideo.function.downloader.OnDownloadListener;

import java.util.ArrayList;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private static final int NOTIFICATION_FOREGROUND_ID = 32141;
    private boolean isFirstTime;

    private DownloadManagerImpl mDownloadManager;

    public DownloadManagerImpl getDownloadManagerImpl() {
        return mDownloadManager;
    }

    @Override
    public void onCreate() {
        mDownloadManager = new DownloadManagerImpl(this);
        mDownloadManager.addOnDownloadListener(new OnDownloadListener() {
            @Override
            public void onDownloadTaskListChange(ArrayList<Task> downloadTasks) {
                updateForegroundNotification();
                if (!isFirstTime) {
                    for (Task task : downloadTasks) {
                        if (task.state != TaskStates.END) {
                            if (mDownloadManager != null) {
                                mDownloadManager.showNotification(task.getFullName(), task.id);
                                if (task.state == TaskStates.DOWNLOADING || task.state == TaskStates.INIT) {
                                    mDownloadManager.resumeDownload(task.id);
                                }
                                else if (task.state == TaskStates.MERGE) {
                                    if (task.extension.contains("mp4")) {
                                        mDownloadManager.mergeFile(task);
                                    } else {
                                        mDownloadManager.mergeFileFFmpeg(task);
                                    }
                                }
                            }
                        }
                    }
                    isFirstTime = true;
                }
            }


            @Override
            public void onDownloadStateChange(Task downloadTask) {
                if (downloadTask.state == TaskStates.END) {
                    updateForegroundNotification();
                }
            }

            @Override
            public void onDownloadProgressChange(Task downloadTask) {

            }

        });
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    private void updateForegroundNotification() {
        int numberDownloading = mDownloadManager.getNumberTaskProgress();
        updateForegroundNotification(numberDownloading);
    }

    private void updateForegroundNotification(int numberDownload) {
        if (numberDownload > 0) {
//            DownloadServiceNotification.createNotification(this,numberDownload);
            startForeground(NOTIFICATION_FOREGROUND_ID, DownloadServiceNotification.createNotification(this, numberDownload));
        } else {
            stopForeground(true);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadServiceBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
    }

    public class DownloadServiceBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}
