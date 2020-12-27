package com.tapi.download.video.twitter.webview;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class TwitterWebviewChrome extends WebChromeClient {
    private OnProgressLoading loading;

    public TwitterWebviewChrome(OnProgressLoading loading) {
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
