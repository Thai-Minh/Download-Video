package com.tapi.downloader.report.listener;

import com.tapi.downloader.database.elements.Task;

/**
 * Created by Majid Golshadi on 4/21/2014.
 */
public class DownloadManagerListenerModerator {

    private DownloadManagerListener downloadManagerListener;

    public DownloadManagerListenerModerator(DownloadManagerListener listener){
        downloadManagerListener = listener;
    }

    public void OnDownloadStateChanged(Task task) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadStateChanged(task);
        }
    }

    public void onDownloadProcess(long taskId, double percent, long downloadedLength) {
        if (downloadManagerListener != null) {
            downloadManagerListener.onDownloadProcess(taskId, percent, downloadedLength);
        }
    }

}
