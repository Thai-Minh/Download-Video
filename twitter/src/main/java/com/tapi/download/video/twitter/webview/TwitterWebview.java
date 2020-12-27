package com.tapi.download.video.twitter.webview;

import android.annotation.SuppressLint;
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


import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.twitter.listener.TwitterListener;
import com.tapi.download.video.twitter.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class TwitterWebview extends WebView implements TwitterWebviewChrome.OnProgressLoading, View.OnTouchListener {

    private static String TAG = TwitterWebview.class.getSimpleName();

    private Context mContext;
    private WebView webView;
    private TwitterListener listener;
    private boolean isPageFinish;
    private String url;

    public TwitterWebview(Context context) {
        this(context, null);
    }

    public TwitterWebview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwitterWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
        createWebView();
    }

    public void setListener(TwitterListener listener) {
        this.listener = listener;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void createWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebChromeClient(new TwitterWebviewChrome(this));
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUserAgentString(USER_AGENT_DEFAULT);
        webView.addJavascriptInterface(this, "Html");
        webView.addJavascriptInterface(this, "TwDownloader");
        webView.setWebViewClient(new TwitterWebviewClient(new TwitterWebviewClient.OnLogoutAccountListener() {
            @Override
            public void onLogout() {
                if (listener != null)
                    listener.onLogoutAccount();
                isPageFinish = true;
            }
        }));

        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieSyncManager.getInstance().startSync();
        webView.loadUrl("https://twitter.com/");
    }

    @JavascriptInterface
    public void show(String html) {
        Log.e(TAG, "show: " );

        Document document = null;
        try {
            document = Jsoup.connect("https://twitter.com/")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                    .header("Cookie", AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, ""))
                    .get();
        } catch (IOException e) {

        }
        if (listener != null && AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "").contains("auth_token")) {
            listener.onPageFinish(document.toString());
            isPageFinish = true;
        }
    }

    private void initView() {
        inflate(mContext, R.layout.twitter_webview, this);
        webView = findViewById(R.id.twitter_web_view);
    }

    public void loadVideoPrivate(String url) {
        this.url = url;
        webView.loadUrl(url);
    }

    public void reloadWeb() {
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl("https://twitter.com/");
        }
    }

    @Override
    public void onProgress(int progress) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return !isPageFinish;
    }
}
