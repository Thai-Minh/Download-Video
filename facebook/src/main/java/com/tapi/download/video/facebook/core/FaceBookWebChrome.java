package com.tapi.download.video.facebook.core;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class FaceBookWebChrome extends WebChromeClient {
    private OnProgressLoading loading;

    public FaceBookWebChrome(OnProgressLoading loading) {
        this.loading = loading;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        Log.e("Tag", "onProgressChanged: "+newProgress );
        if (loading!=null)
            loading.onProgress(newProgress);
    }

    public interface OnProgressLoading {
        void onProgress(int progress);
    }
}
