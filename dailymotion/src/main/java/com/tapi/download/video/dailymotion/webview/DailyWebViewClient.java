package com.tapi.download.video.dailymotion.webview;

import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;

public class DailyWebViewClient extends WebViewClient {

    private static final String DIV_PARENT = "div.Card__card___2FbPd.VideoCard__videoCard___3RIZ9.Card__nohover___FjJE7.Card__noshadow___1M4s1";
    private static final String DIV_PARENT2 = "div.col.medium-4.large-3.xlarge-2.xsmall-12.small-6";
    private static final String DIV_DURATION = "VideoDurationTag__videoDuration___3XMUb";

    private static final String TAG = "DailyWebViewClient";

    private OnLoadingListener listener;

    public DailyWebViewClient(OnLoadingListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        Log.e(TAG, "shouldOverrideUrlLoading: " + url);

        String cookie = AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "");

        if (url.contains("https://www.dailymotion.com/")) {
            Log.e(TAG, "shouldOverrideUrlLoading: loading ");
            listener.onLoading();
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        String cookies = CookieManager.getInstance().getCookie(url);

        if (cookies != null)
            AppPreferences.INSTANCE.putString(PreferencesContains.COOKIE, cookies);

        view.loadUrl("javascript:window.HtmlViewer.show" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element = document.querySelectorAll('" + DIV_PARENT + "');"
                + "if (element || element.length > 0) {"
                + "for (var i = 0; i < element.length; i++) {"
                + "var eleVideo = element[i].querySelector('div." + DIV_DURATION + "');"
                + "if(eleVideo) {"
                + "var url = element[i].querySelector('a').href;"
                + "element[i].setAttribute('onClick', 'DailyDownloader.processVideo(\"'+ url +'\");');"
                + "}" + "}" + "}"
                + "})()");

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element2 = document.querySelectorAll('" + DIV_PARENT2 + "');"
                + "for (var j = 0; j < element2.length; j++) {"
                + "var url2 = element2[j].querySelector('a').href;"
                + "element2[j].setAttribute('onClick', 'DailyDownloader.processVideo(\"'+ url2 +'\");');"
                + "}" + "}"
                + ")()");
    }

    @Override
    public void onLoadResource(WebView view, String url) {

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element = document.querySelectorAll('" + DIV_PARENT + "');"
                + "if (element || element.length > 0) {"
                + "for (var i = 0; i < element.length; i++) {"
                + "var eleVideo = element[i].querySelector('div." + DIV_DURATION + "');"
                + "if(eleVideo) {"
                + "var url = element[i].querySelector('a').href;"
                + "element[i].setAttribute('onClick', 'DailyDownloader.processVideo(\"'+ url +'\");');"
                + "}" + "}" + "}"
                + "})()");

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element2 = document.querySelectorAll('" + DIV_PARENT2 + "');"
                + "for (var j = 0; j < element2.length; j++) {"
                + "var url2 = element2[j].querySelector('a').href;"
                + "element2[j].setAttribute('onClick', 'DailyDownloader.processVideo(\"'+ url2 +'\");');"
                + "}" + "}"
                + ")()");

        view.loadUrl("javascript:( window.onload=prepareVideo;"
                + ")()");
    }

    public interface OnLoadingListener {

        void onLoading();
    }
}
