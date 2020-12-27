package com.tapi.download.video.facebook.function.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
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
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.core.FaceBookClient;
import com.tapi.download.video.facebook.core.FaceBookWebChrome;
import com.tapi.download.video.facebook.core.FacebookCatchVideo;
import com.tapi.download.video.facebook.core.OnFaceBookListener;
import com.tapi.download.video.facebook.core.StoriesFunction;
import com.tapi.download.video.facebook.utils.Utils;

import java.util.ArrayList;

public class FaceBookWebView extends WebView implements FaceBookWebChrome.OnProgressLoading, View.OnTouchListener {
    private static final String TAG = "FaceBookWebView";
    private static final String USER_AGENT_DEFAULT = "Mozilla/5.0 (Linux; Android 4.2.2; GT-I9505 Build/JDQ39) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.59 Mobile Safari/537.36";
    public static final String LINK_DEFAULT = "https://m.facebook.com/";

    private Context mContext;
    private WebView webView;
    private OnFaceBookListener listener;
    private FaceBookClient client;

    private boolean isPageFinish, isGoback;
    private String url;

    public FaceBookWebView(Context context) {
        this(context, null);
    }

    public FaceBookWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceBookWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
        createWebView();
    }

    public void setListener(OnFaceBookListener listener) {
        this.listener = listener;
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void createWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebChromeClient(new FaceBookWebChrome(this));
        webView.getSettings().setUserAgentString(USER_AGENT_DEFAULT);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.addJavascriptInterface(this, "FBDownloader");
        webView.addJavascriptInterface(this, "HtmlViewer");
        webView.addJavascriptInterface(this, "FBVideoLoader");
        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);

        client = new FaceBookClient(new FaceBookClient.OnLogoutAccountListener() {
            @Override
            public void onLogout() {
                if (listener != null) {
                    listener.onLogoutAccount();
                    webView.clearHistory();
                    isPageFinish = true;
                }
            }

            @Override
            public void onLoading() {
                if (listener!=null)
                    listener.onLoadingWeb();
                webView.clearHistory();
            }
        });
        webView.setWebViewClient(client);
        webView.setOnTouchListener(this);
        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        CookieSyncManager.getInstance().startSync();
        webView.loadUrl(LINK_DEFAULT);
    }

    @Override
    public boolean canGoBack() {
        if (webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return false;
    }

    private void initView() {
        inflate(mContext, R.layout.facebook_webview, this);
        webView = findViewById(R.id.faceBook_web_view);
    }

    @JavascriptInterface
    public void loadVideo(final String videoUrl, final String videoId) {
        String mId = videoId.replace("ufi_", "");
        catchVideo(mId, videoUrl);
    }

    @JavascriptInterface
    public void processVideo(final String videoUrl, final String videoID) {
        catchVideo(videoID, videoUrl);
    }

    private void catchVideo(String videoId, String videoUrl) {
        if (listener != null)
            listener.onLoadingCatchUrl();

        FacebookCatchVideo.getInstance().getVideoDownloadLink(Utils.BASE_FACEBOOK_VIDEO + videoId, new OnCatchVideoListener() {
            @Override
            public void onStartCatch() {

            }

            @Override
            public void onCatchedLink(Video video) {
                if (listener != null)
                    listener.onVideoLoaded(video);
            }

            @Override
            public void onPrivateLink(String link) {
                final int[] duration = new int[1];
                final int[] videoSize = new int[1];
                if (listener != null) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            videoSize[0] = Utils.getVideoSize(videoUrl);
                            duration[0] = StoriesFunction.getDuration(videoUrl);
                        }
                    });
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ArrayList<DownloadLink> links = new ArrayList<>();
                    links.add(new DownloadLink(videoUrl, videoSize[0], com.tapi.download.video.core.utils.Utils.VIDEO_SD));
                    Video video = new Video("unknow", "unknow", links);
                    video.setDuration(duration[0]);
                    listener.onVideoLoaded(video);
                }
            }
        });
    }


    public void loadVideoPrivate(String url) {
        this.url = url;
        webView.loadUrl(url);
    }

    public void reloadWeb() {
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else {
            webView.loadUrl(LINK_DEFAULT);
        }
    }

    @Override
    public void onProgress(int progress) {
        if (listener!=null)
            listener.onWebLoading(progress);
    }

    @JavascriptInterface
    public void showHTML(String html) {
        if (listener != null && AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "").contains("c_user")
                && Utils.checkLoadFinishFb(html)) {
            if (!isGoback)
                listener.onPageFinish(html);
            else isGoback = false;
        }
        isPageFinish = true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return !isPageFinish;
    }
}
