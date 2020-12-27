package com.tapi.download.video.instagram.core.webview;

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
import android.webkit.WebViewClient;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.instagram.core.InstagramCatchVideo;
import com.tapi.download.video.instagram.core.InstagramListener;
import com.tapi.download.video.instagram.R;
import com.tapi.download.video.instagram.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

public class InstagramWebview extends WebView implements InstaWebviewChrome.OnProgressLoading, View.OnTouchListener {

    private static final String TAG = "InstagramWebview";
    private static final String USER_AGENT_DEFAULT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";

    private Context mContext;
    private WebView webView;
    private InstagramListener listener;
    private OnLoadingNewFeedListener onLoadingNewFeedListener;
    private boolean isPageFinish, isGoback;
    private String url;

    public InstagramWebview(Context context) {
        this(context, null);
    }

    public InstagramWebview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InstagramWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
        createWebView();
    }

    public void setListener(InstagramListener listener) {
        this.listener = listener;
    }

    private void initView() {
        inflate(mContext, R.layout.insta_webview, this);
        webView = findViewById(R.id.instagram_web_view);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void createWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebChromeClient(new InstaWebviewChrome(this));
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUserAgentString(USER_AGENT_DEFAULT);
        webView.addJavascriptInterface(this, "HtmlViewer");
        webView.addJavascriptInterface(this, "InsDownloader");
        webView.addJavascriptInterface(this, "InsLogout");
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);
        webView.setWebViewClient(new InstagramWebviewClient(new InstagramWebviewClient.OnLoadingAccountListener() {

            @Override
            public void onLoading() {
                onLoadingNewFeedListener.OnLoading();
            }
        }));
        webView.setOnTouchListener(this);

        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieSyncManager.getInstance().startSync();
        webView.loadUrl("https://www.instagram.com/");
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

        try {
            Document document = Jsoup.connect("https://www.instagram.com")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                    .header("Cookie", AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, ""))
                    .maxBodySize(0)
                    .get();
        } catch (IOException e) {

        }

        if (listener != null && AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "").contains("ds_user_id") && Utils.checkLoadFinishInsta(html)) {
            if (!isGoback) {
                listener.onPageFinish(html);
                isGoback = true;
            } else {
                isGoback = false;
            }
        }

        isPageFinish = true;
    }

    @JavascriptInterface
    public void processVideo(String url) {

        String oldLink = AppPreferences.INSTANCE.getString("URL", "");

        Log.e(TAG, "processVideo: new url: " + url);
        Log.e(TAG, "processVideo: old url: " + oldLink);

        if (!url.equals(oldLink)) {

            InstagramCatchVideo.getInstance().getVideoDownloadLink(url, new OnCatchVideoListener() {

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
                    if (listener != null) {
                        ArrayList<DownloadLink> links = new ArrayList<>();
                        links.add(new DownloadLink("", 0));
                        Video video = new Video("unknown", "unknown", "unknown", "unknown", "unknown", 0, links);
                        listener.onVideoLoaded(video);
                        Log.e(TAG, "onPrivateLink: " + link);
                    }
                }
            });
        }
    }

    @JavascriptInterface
    public void logOut(String html) {

        Log.e(TAG, "logOut: " + html);

        CookieManager instance = CookieManager.getInstance();
        instance.removeAllCookie();
        instance.removeSessionCookie();
        AppPreferences.INSTANCE.putString(PreferencesContains.COOKIE, "");
        if (listener != null) {
            listener.onLogoutAccount();
            String cookie = AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "");
            Log.e(TAG, "logOut: cookie " + cookie);
        }
    }

    public void loadVideoPrivate(String url) {
        this.url = url;
        webView.loadUrl(url);
    }

    public void reloadWeb() {
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl("https://www.instagram.com/");
        }
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
