package com.tapi.download.video.dailymotion;

public interface OnWebViewListener {
    void onPageLoaded();

    void onViewLinkChanged(String url);
}
