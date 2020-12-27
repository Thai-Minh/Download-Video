package com.tapi.download.video.twitter.listener;

import com.tapi.download.video.core.Video;

public interface TwitterListener {
    void onLoadingUrl();

    void onVideoLoaded(Video video);

    void onPageFinish(String html);

    void onLogoutAccount();
}
