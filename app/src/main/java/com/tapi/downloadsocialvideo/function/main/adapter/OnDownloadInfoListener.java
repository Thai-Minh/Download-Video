package com.tapi.downloadsocialvideo.function.main.adapter;

import com.tapi.download.video.core.DownloadLink;

public interface OnDownloadInfoListener {
    void onDownloadVideo(DownloadLink link);

    void onWatchVideo();
}
