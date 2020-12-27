package com.tapi.download.video.instagram.core.webview;

import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;

public class InstagramWebviewClient extends WebViewClient {

    private static final String TAG = "InstagramWebviewClient";

    private static final String ARTICLE = "article";
    private static final String A_CLASS_NAME = "c-Yi7";
    private static final String DIV_VIDEO_CLASS_NAME = "Yi5aA";
    private static final String DIV_VIDEO_INDEX_CLASS_NAME = "Yi5aA XCodT";
    private OnLoadingAccountListener listener;

    public InstagramWebviewClient(OnLoadingAccountListener listener) {
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
        Log.e(TAG, "shouldOverrideUrlLoading: cookie1 " + cookie);

        if (!url.contains("https://m.facebook.com/login.php?") || !url.contains("https://www.instagram.com/accounts/onetap/?")) {
            view.loadUrl(url);
        }

        if (url.contains("https://www.instagram.com/accounts/signup/")) {
            Log.e(TAG, "shouldOverrideUrlLoading: loading ");
            listener.onLoading();
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {

        Log.e(TAG, "onPageFinished: " + url);
        String cookies = CookieManager.getInstance().getCookie(url);

        if (cookies != null) {
            AppPreferences.INSTANCE.putString(PreferencesContains.COOKIE, cookies);
            Log.e(TAG, "onPageFinished: cookie0 " + cookies);
        }

        view.loadUrl("javascript:(function display() {"
                + "document.getElementsByClassName('Z_Gl2')[0].style.display = 'none';"
                + "document.getElementsByClassName('zGtbP  ')[0].style.display = 'none';"
                + "document.getElementsByClassName('q02Nz _0TPg')[0].style.display = 'none';"
                + "document.getElementsByClassName('b5itu')[0].style.display = 'none';"
                + "})()");

        view.loadUrl("javascript:window.InsLogout.logOut" +
                "('<html>'+document.getElementsByClassName('aOOlW  bIiDR  ')[0].innerHTML+'</html>');");

        view.loadUrl("javascript:window.HtmlViewer.show" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element = document.querySelectorAll('" + ARTICLE + "');"
                + "if (element || element.length > 0) {"
                + "for (var i = 0; i < element.length; i++) {"
                + "var eleVideo = element[i].querySelector('span.videoSpritePlayButton');"
                + "if(eleVideo) {"
                + "var url = element[i].querySelector('a." + A_CLASS_NAME + "').href;"
                + "element[i].setAttribute('onClick', 'InsDownloader.processVideo(\"'+ url +'\");');"
                + "}" + "}" + "}"
                + "})()");

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element = document.querySelectorAll('" + ARTICLE + "');"
                + "if (element || element.length > 0) {"
                + "for (var i = 0; i < element.length; i++) {"
                + "var eleVideo = element[i].querySelector('span.videoSpritePlayButton');"
                + "if(eleVideo) {"
                + "var url = element[i].querySelector('a." + A_CLASS_NAME + "').href;"
                + "var moreVideo = element[i].querySelectorAll('div." + DIV_VIDEO_CLASS_NAME + "');"
                + "if (moreVideo || moreVideo.length > 0) {"
                + "for (var j = 0; j < moreVideo.length; j++) {"
                + "if (moreVideo[j].className == ('" + DIV_VIDEO_INDEX_CLASS_NAME + "')) {"
                + "element[i].setAttribute('onClick', 'InsDownloader.processVideo(\"'+ url +'\" + \"'+ j +'\");');"
                + "}}}}}}})()");
    }

    @Override
    public void onLoadResource(WebView view, String url) {

        view.loadUrl("javascript:(function display() {"
                + "document.getElementsByClassName('Z_Gl2')[0].style.display = 'none';"
                + "document.getElementsByClassName('zGtbP  ')[0].style.display = 'none';"
                + "document.getElementsByClassName('q02Nz _0TPg')[0].style.display = 'none';"
                + "document.getElementsByClassName('b5itu')[0].style.display = 'none';"
                + "})()");

        view.loadUrl("javascript:window.InsLogout.logOut" +
                "('<html>'+document.getElementsByClassName('aOOlW  bIiDR  ')[0].innerHTML+'</html>');");

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element = document.querySelectorAll('" + ARTICLE + "');"
                + "if (element || element.length > 0) {"
                + "for (var i = 0; i < element.length; i++) {"
                + "var eleVideo = element[i].querySelector('span.videoSpritePlayButton');"
                + "if(eleVideo) {"
                + "var url = element[i].querySelector('a." + A_CLASS_NAME + "').href;"
                + "element[i].setAttribute('onClick', 'InsDownloader.processVideo(\"'+ url +'\");');"
                + "}" + "}" + "}"
                + "})()");

        view.loadUrl("javascript:(function prepareVideo() { "
                + "var element = document.querySelectorAll('" + ARTICLE + "');"
                + "if (element || element.length > 0) {"
                + "for (var i = 0; i < element.length; i++) {"
                + "var eleVideo = element[i].querySelector('span.videoSpritePlayButton');"
                + "if(eleVideo) {"
                + "var url = element[i].querySelector('a." + A_CLASS_NAME + "').href;"
                + "var moreVideo = element[i].querySelectorAll('div." + DIV_VIDEO_CLASS_NAME + "');"
                + "if (moreVideo || moreVideo.length > 0) {"
                + "for (var j = 0; j < moreVideo.length; j++) {"
                + "if (moreVideo[j].className == ('" + DIV_VIDEO_INDEX_CLASS_NAME + "')) {"
                + "element[i].setAttribute('onClick', 'InsDownloader.processVideo(\"'+ url +'\" + \"'+ j +'\");');"
                + "}}}}}}})()");

        view.loadUrl("javascript:( window.onload=prepareVideo;"
                + ")()");
    }

    public interface OnLoadingAccountListener {

        void onLoading();
    }

}
