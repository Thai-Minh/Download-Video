package com.tapi.download.video.twitter.webview;

import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;

public class TwitterWebviewClient extends WebViewClient {

    private static final String TAG = "TwitterWebviewClient";

    private OnLogoutAccountListener listener;

    public TwitterWebviewClient(OnLogoutAccountListener listener) {
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
        view.loadUrl(url);

        if (url.contains("https://mobile.twitter.com/?logout") && cookie != null && cookie.contains("guest_id")) {
            CookieManager instance = CookieManager.getInstance();
            instance.removeAllCookie();
            instance.removeSessionCookie();
            AppPreferences.INSTANCE.putString(PreferencesContains.COOKIE, "");
            if (listener != null)
                listener.onLogout();

            Log.e(TAG, "shouldOverrideUrlLoading: clear " + AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, ""));
        }
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        String cookies = CookieManager.getInstance().getCookie(url);
        if (cookies != null)

            AppPreferences.INSTANCE.putString(PreferencesContains.COOKIE, cookies);

        view.loadUrl("javascript:(function display() {"
                + "document.getElementsByClassName('css-1dbjc4n r-173mn98 r-1bo11z6 r-2ues7x')[0].style.display = 'none';"
                + "})()");

        view.loadUrl("javascript:window.Html.show" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

    }

    @Override
    public void onLoadResource(WebView view, String url) {

        view.loadUrl("javascript:(function display() {"
                + "document.getElementsByClassName('css-1dbjc4n r-173mn98 r-1bo11z6 r-2ues7x')[0].style.display = 'none';"
                + "})()");

        view.loadUrl("javascript:( window.onload=prepareVideo;"
                + ")()");
    }

    public interface OnLogoutAccountListener {
        void onLogout();
    }
}
