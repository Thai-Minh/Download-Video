package com.tapi.download.video.facebook.core;

import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;


public class FaceBookClient extends WebViewClient {
    private static final String TAG = "FaceBookClient";
    private OnLogoutAccountListener listener;

    public FaceBookClient(OnLogoutAccountListener listener) {
        this.listener = listener;
    }


    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.e(TAG, "shouldOverrideUrlLoading: " + url);
        if (url.contains("https://m.facebook.com/logout.php?") || url.contains("https://m.facebook.com/?stype")) {
            CookieManager instance = CookieManager.getInstance();
            instance.removeAllCookie();
            instance.removeSessionCookie();
            AppPreferences.INSTANCE.putString(PreferencesContains.COOKIE, "");
            if (listener != null)
                listener.onLogout();
        }

        if (!url.contains("fb-messenger") && !url.startsWith("intent")) {
            if (url.equals("https://www.facebook.com/home.php#_=_") ||
                    url.contains(" https://m.facebook.com/login/save-device/cancel/") ||
                    url.contains("https://m.facebook.com/?_rdr")) {
                String linkDefault = "https://m.facebook.com/?_rdr";
                view.loadUrl(linkDefault);
                if (listener != null)
                    listener.onLoading();
                return super.shouldOverrideUrlLoading(view, linkDefault);
            } else {
                return super.shouldOverrideUrlLoading(view, url);
            }
        }else {
            return true;
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        String cookies = CookieManager.getInstance().getCookie(url);
        if (cookies != null)
            AppPreferences.INSTANCE.putString(PreferencesContains.COOKIE, cookies);
        view.loadUrl("javascript:window.HtmlViewer.showHTML" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        view.loadUrl("javascript:(function() { "
                + "var el = document.querySelectorAll('div[data-sigil]');"
                + "for(var i=0;i<el.length; i++)"
                + "{"
                + "var sigil = el[i].dataset.sigil;"
                + "if(sigil.indexOf('inlineVideo') > -1){"
//                + "delete el[i].dataset.sigil;"
                + "var jsonData = JSON.parse(el[i].dataset.store);"
                + "el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\");');"
                + "}" + "}" + "})()");

        view.loadUrl("javascript:(function display() {"
                + "document.getElementById('MStoriesTray').style.display = 'none';"
                + "document.getElementById('MComposer').style.display = 'none';"
                + "document.getElementsByClassName('_4g34')[2].style.display = 'none';"
                + "document.getElementsByClassName('_4g34')[3].style.display = 'none';"
                + "document.getElementsByClassName('_4g34')[4].style.display = 'none';"
                + "})()");

        view.loadUrl("javascript:(function prepareVideo() {"
                + "var a = document.querySelectorAll('div._56be');"
                + "for(var i =0; i < a.length; i++) {var into = a[i].querySelector('video._1_uo'); if(into) {var obj = a[i];"
                + "var video = obj.querySelector('video._1_uo');"
                + "var mLink = video.src;"
                + "var objId = obj.querySelector('div._59e9._1-ut._2a_g.async_composer');"
                + "var mId = objId.id;"
                + "obj.setAttribute('onClick','FBVideoLoader.loadVideo(\"'+mLink+'\",\"'+mId+'\")');"
                + "video.onplay = function(){FBVideoLoader.loadVideo(mLink,mId);console.log(mLink);};"
                + " }}"
                + "})()");
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        view.loadUrl("javascript:(function prepareVideo() { "
                + "var el = document.querySelectorAll('div[data-sigil]');"
                + "for(var i=0;i<el.length; i++)"
                + "{"
                + "var sigil = el[i].dataset.sigil;"
                + "if(sigil.indexOf('inlineVideo') > -1){"
//                + "delete el[i].dataset.sigil;"
                + "var jsonData = JSON.parse(el[i].dataset.store);"
                + "el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');"
                + "}" + "}" + "})()");


        view.loadUrl("javascript:(function display() {"
                + "document.getElementById('MStoriesTray').style.display = 'none';"
                + "document.getElementById('MComposer').style.display = 'none';"
                + "document.getElementsByClassName('_4g34')[2].style.display = 'none';"
                + "document.getElementsByClassName('_4g34')[3].style.display = 'none';"
                + "document.getElementsByClassName('_4g34')[4].style.display = 'none';"
                + "})()");

        view.loadUrl("javascript:(function prepareVideo() {"
                + "var a = document.querySelectorAll('div._56be');"
                + "for(var i =0; i < a.length; i++) {var into = a[i].querySelector('video._1_uo'); if(into) {var obj = a[i];"
                + "var video = obj.querySelector('video._1_uo');"
                + "var mLink = video.src;"
                + "var objId = obj.querySelector('div._59e9._1-ut._2a_g.async_composer');"
                + "var mId = objId.id;"
                + "obj.setAttribute('onClick','FBVideoLoader.loadVideo(\"'+mLink+'\",\"'+mId+'\")');"
                + "video.onplay = function(){FBVideoLoader.loadVideo(mLink,mId);console.log(mLink);};"
                + " }}"
                + "})()");

        view.loadUrl("javascript:( window.onload=prepareVideo;"
                + ")()");
    }

    public interface OnLogoutAccountListener {
        void onLogout();

        void onLoading();
    }
}
