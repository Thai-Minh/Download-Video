package com.tapi.download.video.dailymotion.listener;

import com.tapi.download.video.core.Video;

public interface DailyListener {
    void onLoadingUrl();

    void onVideoLoaded(Video video);

    void onPageFinish(String html);
}
