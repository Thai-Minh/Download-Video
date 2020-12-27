package com.tapi.download.video.core.listener;

import android.content.Context;

import com.tapi.download.video.core.Video;

public interface OnDownloadVideo {
    void startDownload (Context context, Video videoDownload, int position);
}
