package com.tapi.downloadsocialvideo.function.downloader;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;

interface IDownloadManager {
    int startDownload(Video video, DownloadLink downloadLink);

    void pauseDownload(long downloadId);

    void resumeDownload(long downloadId);

    void cancelDownload(long downloadId); // downloading -> pause -> delete

    boolean deleteDownload(long downloadId); // delete

    void addOnDownloadListener(OnDownloadListener onDownloadListener);

    void removeOnDownloadListener(OnDownloadListener onDownloadListener);
}
