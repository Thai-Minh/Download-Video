package com.tapi.download.video.instagram.core;


import com.tapi.download.video.core.Video;

public interface InstagramListener {
    void onLoadingUrl();

    void onVideoLoaded(Video video);

    void onPageFinish(String html);

    void onLogoutAccount();
}
