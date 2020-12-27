package com.tapi.downloader.report.listener;

import com.tapi.downloader.database.elements.Task;

/**
 * Created by Majid Golshadi on 4/20/2014.
 */
public interface DownloadManagerListener {

    void onDownloadStateChanged(Task task);

    void onDownloadProcess(long taskId, double percent, long downloadedLength);

}
