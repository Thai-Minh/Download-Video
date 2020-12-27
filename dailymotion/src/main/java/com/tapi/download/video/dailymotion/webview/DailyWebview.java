package com.tapi.download.video.dailymotion.webview;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.listener.IWebViewCore;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.dailymotion.DailyCatchVideo;
import com.tapi.download.video.dailymotion.GetVideoDownloadTask;
import com.tapi.download.video.dailymotion.listener.DailyListener;
import com.tapi.download.video.dailymotion.R;
import com.tapi.download.video.dailymotion.util.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

public class DailyWebview extends WebView implements DailyWebViewChrome.OnProgressLoading, View.OnTouchListener {

    public static final String DAILY_HOME = "https://www.dailymotion.com/";

    private Context mContext;
    private static final String TAG = "DailyWebView";
    private OnLoadingNewFeedListener onLoadingNewFeedListener;
    private DailyListener listener;
    private boolean isPageFinish;
    private WebView webView;

    public DailyWebview(Context context) {
        this(context, null);
    }

    public DailyWebview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DailyWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
        initWebView();
    }

    private void initView() {
        inflate(mContext, R.layout.daily_webview, this);
        webView = findViewById(R.id.daily_web_view);
    }

    public void reloadWeb() {
        String url = AppPreferences.INSTANCE.getString(Utils.URL, "");
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl(DAILY_HOME);
        }
    }

    public void setListener(DailyListener dailyListener) {
        this.listener = dailyListener;
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void initWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(this, "HtmlViewer");
        webView.addJavascriptInterface(this, "DailyDownloader");
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        webView.setWebChromeClient(new DailyWebViewChrome(this));
        webView.setWebViewClient(new DailyWebViewClient(new DailyWebViewClient.OnLoadingListener() {
            @Override
            public void onLoading() {
                onLoadingNewFeedListener.OnLoading();
                Log.e(TAG, "onLoading: ");
            }
        }));

        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieSyncManager.getInstance().startSync();
        webView.loadUrl(DAILY_HOME);
    }

    @Override
    public boolean canGoBack() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    @JavascriptInterface
    public void show(String html) {
        listener.onPageFinish(html);

        isPageFinish = true;
    }

    @JavascriptInterface
    public void processVideo(String url) {

        Log.e(TAG, "processVideo: " + url);

        AppPreferences.INSTANCE.putString(Utils.URL, url);

        DailyCatchVideo.getInstance().getVideoDownloadLink(url, new OnCatchVideoListener() {
            @Override
            public void onStartCatch() {
                if (listener != null)
                    listener.onLoadingUrl();
            }

            @Override
            public void onCatchedLink(Video video) {
                if (listener != null)
                    listener.onVideoLoaded(video);
                Log.e(TAG, "onCatchedLink: " + video.getImgUser());
            }

            @Override
            public void onPrivateLink(String link) {

            }
        });
    }


    @Override
    public void onProgress(int progress) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return !isPageFinish;
    }

    public void setOnLoadingNewFeedListener(OnLoadingNewFeedListener onLoadingNewFeedListener) {
        this.onLoadingNewFeedListener = onLoadingNewFeedListener;
    }

    public interface OnLoadingNewFeedListener {
        void OnLoading();
    }
}
