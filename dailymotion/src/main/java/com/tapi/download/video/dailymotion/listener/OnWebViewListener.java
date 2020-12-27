package com.tapi.download.video.dailymotion.listener;

public interface OnWebViewListener {
    void onPageLoaded();

    void onViewLinkChanged(String url);
}
