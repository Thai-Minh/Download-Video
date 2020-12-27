package com.tapi.download.video.instagram.core.webview;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class InstaWebviewChrome extends WebChromeClient {
    private static final String TAG = "InstaWebviewChrome";
    private OnProgressLoading loading;

    public InstaWebviewChrome(OnProgressLoading loading) {
        this.loading = loading;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        Log.e(TAG, "onProgressChanged: " + newProgress );
        super.onProgressChanged(view, newProgress);
        Log.e(TAG, "onProgressChanged: "+newProgress );
        if (loading!=null)
            loading.onProgress(newProgress);
    }

    public interface OnProgressLoading {
        void onProgress(int progress);
    }
}
