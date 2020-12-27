package com.tapi.downloadsocialvideo.function.downloader;

import com.tapi.downloader.database.elements.Task;

import java.util.ArrayList;

public interface OnDownloadListener {
    void onDownloadTaskListChange(ArrayList<Task> downloadTasks);

    void onDownloadStateChange(Task downloadTask);

    void onDownloadProgressChange(Task downloadTask);
}
