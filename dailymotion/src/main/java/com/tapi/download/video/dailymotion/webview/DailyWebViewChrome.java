package com.tapi.download.video.dailymotion.webview;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class DailyWebViewChrome extends WebChromeClient {
    private OnProgressLoading loading;

    public DailyWebViewChrome(OnProgressLoading loading) {
        this.loading = loading;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (loading!=null)
            loading.onProgress(newProgress);
    }

    public interface OnProgressLoading {
        void onProgress(int progress);
    }
}
