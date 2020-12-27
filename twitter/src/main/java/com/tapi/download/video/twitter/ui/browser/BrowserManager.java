package com.tapi.download.video.twitter.ui.browser;

import com.tapi.download.video.twitter.listener.OnBrowserPageFinishListener;

public class BrowserManager {
    public static BrowserManager instance;
    private OnBrowserPageFinishListener pageFinishListeners;
    private String html;

    public static BrowserManager getInstance() {
        if (instance == null)
            instance = new BrowserManager();
        return instance;
    }

    public void setPageFinishListeners(OnBrowserPageFinishListener pageFinishListeners) {
        this.pageFinishListeners = pageFinishListeners;
        if (html != null && !html.isEmpty()) {
            pageFinishListeners.onBrowserPageFinish(html);
        }
    }

    public void sendPageFinish(String html) {
        if (pageFinishListeners != null)
            pageFinishListeners.onBrowserPageFinish(html);
        else {
            this.html = html;
        }
    }

    public void sendLogoutPage() {
        if (pageFinishListeners != null)
            pageFinishListeners.onLogoutPage();
    }
}
