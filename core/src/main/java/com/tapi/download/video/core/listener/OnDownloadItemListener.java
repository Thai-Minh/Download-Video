package com.tapi.download.video.core.listener;

import com.tapi.downloader.database.elements.Task;

public interface OnDownloadItemListener {

    void onDownloadItemCancelClick(Task downloadTask);

    void onDownloadItemDeleteClick(Task downloadTask);

    void onDownloadItemPauseClick(Task downloadTask);

    void onDownloadItemResumeClick(Task downloadTask);

    void onDownloadItemCompleteClick(Task downloadTask);
}
