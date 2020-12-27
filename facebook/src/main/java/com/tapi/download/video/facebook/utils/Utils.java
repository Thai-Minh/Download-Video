package com.tapi.download.video.facebook.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.facebook.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class Utils {
    public static final String FACEBOOK_URL = "https://www.facebook.com";
    public static final String BASE_FACEBOOK_VIDEO = "https://www.facebook.com/video.php?v=";
    public static final String COOKIE = "Cookie";
    public static final String BASE_LOAD_MORE = "https://m.facebook.com/stories/cursor/nextPage/?start_cursor=" + "%s" + "&prev_bucket_count=24&tray_session_id=" + "%s";
    public static final String BASE_STORIES_VIEW = "https://m.facebook.com/story/view/?bucket_id=" + "%s" + "&thread_id=" + "%s" + "&source=permalink&_rdr";
    public static final String SEND_URL = "url";
    private static final String TAG = "Utils";
    public static String[] USER_AGENT = new String[]{
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"
    };


    public static void setPaddingOverStatusBar(Context context, View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int statusBarHeight = getStatusBarHeight(context);
            view.setPadding(0, statusBarHeight, 0, 0);
        }
    }


    public static boolean checkLoadFinishFb(String html) {
        return !html.contains("<html><head><title>Facebook") && !html.contains("<html><head></head><body></body></html>");
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getHeightScreen(Context context, boolean isNav) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels + (isNav ? getNavigationBarHeight(context, window) : 0);
    }

    private static int getNavigationBarHeight(Context context, WindowManager windowManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            windowManager.getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight)
                return realHeight - usableHeight;
            else
                return 0;
        }
        return 0;
    }

    public static String getUserAgent() {
        Random random = new Random();
        int i = random.nextInt(USER_AGENT.length);
        return USER_AGENT[i];
    }

    public static String getCookie() {
        return AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "");
    }

    public static int getWidthScreen(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int convertSpToPixel(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }



    public static int convertDpToPixel(float dp, Context context) {
        return (int) dp * (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void openFaceBookApp(Context context) {
        Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
        facebookIntent.setData(Uri.parse(FACEBOOK_URL));
        context.startActivity(facebookIntent);
    }

    public static void changeColorSeekbar(Context context, SeekBar mSeekBar) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            LayerDrawable progressDrawable = (LayerDrawable) mSeekBar.getProgressDrawable();
            Drawable processDrawable = progressDrawable.findDrawableByLayerId(android.R.id.progress);
            processDrawable.setColorFilter(context.getResources().getColor(R.color.fb_preview_seekbar_color), PorterDuff.Mode.SRC_IN);
            Drawable backgroundDrawable = progressDrawable.findDrawableByLayerId(android.R.id.background);
            backgroundDrawable.setColorFilter(context.getResources().getColor(R.color.fb_preview_seekbar_line), PorterDuff.Mode.SRC_IN);
            mSeekBar.getThumb().setColorFilter(context.getResources().getColor(R.color.fb_preview_seekbar_color), PorterDuff.Mode.SRC_IN);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawableProgress = DrawableCompat.wrap(mSeekBar.getIndeterminateDrawable());
            DrawableCompat.setTint(drawableProgress, ContextCompat.getColor(context, R.color.fb_preview_seekbar_line));
            mSeekBar.setProgressDrawable(DrawableCompat.unwrap(drawableProgress));
        } else {
            mSeekBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, R.color.fb_preview_seekbar_line), PorterDuff.Mode.SRC_IN);
        }
    }

    public static void animationChangeAlpha(final View view, float from, float to, final boolean isShow) {
        AlphaAnimation animation1 = new AlphaAnimation(from, to);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animation1.setDuration(150);
        animation1.setFillAfter(true);
        view.startAnimation(animation1);
    }

    public static void loadThumbnail(Context context, String urlThumbnail, ImageView imgView, RequestListener<Drawable> listener) {
        Glide.with(context)
                .load(urlThumbnail)
                .listener(listener)
//                .placeholder(R.drawable.fb_loading_iv)
                .error(R.drawable.fb_loading_fail_iv)
                .into(imgView);
    }

    public static void loadThumbnail(Context context, String urlThumbnail, ImageView imgView) {
        Glide.with(context)
                .load(urlThumbnail)
//                .placeholder(R.drawable.item_image_loading_icon)
                .error(R.drawable.fb_loading_fail_profile_iv)
                .into(imgView);
    }

    public static int getVideoSize(String url) {
        try {
            URL url1 = new URL(url);
            HttpURLConnection a2 = (HttpURLConnection) url1.openConnection();
            int size = a2.getContentLength();
            a2.disconnect();
            return size;
        } catch (Exception e) {
            Log.e(TAG, "getVideoSize: " + e.getMessage());
        }
        return -1;
    }

    public static boolean checkLinkLive(String url) {
        return url.contains("live-dash");
    }
    public static boolean checkCookiesFb() {
        String cookies = AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "");
        return cookies != null && !cookies.isEmpty() && cookies.contains("c_user");
    }
}
