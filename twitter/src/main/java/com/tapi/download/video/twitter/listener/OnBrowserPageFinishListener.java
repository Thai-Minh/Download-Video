package com.tapi.download.video.twitter.listener;

public interface OnBrowserPageFinishListener {
    void onBrowserPageFinish(String html);

    void onLogoutPage();
}
