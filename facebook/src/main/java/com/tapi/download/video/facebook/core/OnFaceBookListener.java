package com.tapi.download.video.facebook.core;

import com.tapi.download.video.core.Video;


public interface OnFaceBookListener {
    void onLoadingCatchUrl();

    void onWebLoading(int progress);

    void onVideoLoaded(Video video);

    void onPageFinish(String html);

    void onLogoutAccount();

    void onLoadingWeb();
}
