package com.tapi.download.video.core.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.tapi.download.video.core.R;
import com.tapi.download.video.core.Video;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class Utils {

    public static final String NO_INTERNET = "no_internet";
    public static final String ACTION_PRIVATE_LINK = "action_private_link";
    public static final String PRIVATE_LINK = "private_link";

    public static final int VIDEO_HD = -1;
    public static final int VIDEO_SD = -2;

    public static final String ACTION_CHECK_ID = "action_check_id";
    public static final String ACTION_SHOW_BOTTOM_SHEET = "action_show_bottom_sheet";
    public static final String INTENT_ACTION_CHECK_ID = "intent_action_check_id";

    public static final String ACTION_RESUME_DOWNLOAD = "action_resume";
    public static final String INTENT_ACTION_RESUME_DOWNLOAD = "intent_action_resume";

    public static final String BOTTOM_SHEET_ACTION_DOWNLOAD_START = "bottom_sheet_action_download_start";
    public static final String BOTTOM_SHEET_ACTION_DOWNLOAD_CANCEL = "bottom_sheet_action_download_cancel";
    public static final String BOTTOM_SHEET_ACTION_DOWNLOAD_PAUSE = "bottom_sheet_action_download_pause";

    public static final String CALLBACK_ACTION_DOWNLOAD_CANCEL = "callback_action_download_cancel";
    public static final String CALLBACK_ACTION_DOWNLOAD_PAUSE = "callback_action_download_pause";
    public static final String CALLBACK_ACTION_DOWNLOAD_DOWNLOADING = "callback_action_download_downloading";
    public static final String CALLBACK_ACTION_DOWNLOAD_END = "callback_action_download_complete";
    public static final String CALLBACK_ACTION_DOWNLOAD_STATE = "callback_action_update";

    public static final String BOTTOM_SHEET_VIDEO = "bottom_sheet_video";
    public static final String BOTTOM_SHEET_LINK_POSITION = "bottom_sheet_position_link";
    public static final String BOTTOM_SHEET_PERCENT = "bottom_sheet_percent";

    public static final String TWITTER_KEY = "YbsTtOlXJZOY6G8yzXLR7fObk";
    public static final String TWITTER_SECRET = "aXwPL8r00rok639o2ayFNZDLE2Ujh9d6spDH3ImUV7ZTyERojn";

    public static String[] USER_AGENT = new String[]{
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"
    };

    public static void loadThumbnail(Context context, String urlThumbnail, ImageView imgView, RequestListener<Drawable> listener) {
        Glide.with(context)
                .load(urlThumbnail)
                .listener(listener)
                .into(imgView);
    }

    public static void loadThumbnail(Context context, String urlThumbnail, ImageView imgView) {
        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                .skipMemoryCache(true)
                .centerInside();

        Glide.with(context)
                .load(urlThumbnail)
                .apply(requestOptions)
                .centerCrop()
                .into(imgView);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
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

    public static int getVideoSize(String url) {
        try {
            URL url1 = new URL(url);
            HttpURLConnection a2 = (HttpURLConnection) url1.openConnection();
            int size = a2.getContentLength();
            a2.disconnect();
            return size; // Byte
        } catch (Exception e) {
            Log.e("Utils", "getVideoSize: " + e.getMessage());
        }
        return -1;
    }

    public static String getDurationString(int millisecond) {
        Date date = new Date(millisecond);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public static void sendActionStartDownload(Context context, Video video, int position, String actionName) {
        Intent intent = new Intent(actionName);
        intent.putExtra(Utils.BOTTOM_SHEET_VIDEO, video);
        intent.putExtra(Utils.BOTTOM_SHEET_LINK_POSITION, position);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendTaskLocalBroadcast(Context context, String actionName) {
        Intent intent = new Intent(actionName);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static String getUserAgent() {
        Random random = new Random();
        int i = random.nextInt(USER_AGENT.length);
        return USER_AGENT[i];
    }

    public static float getWidthTextPlayController(String str, Context context) {
        Paint paint = new Paint();
        paint.setTextSize(Utils.convertSpToPixel(12, context));
        Typeface typeface = ResourcesCompat.getFont(context, R.font.san_francisco_regular);
        paint.setTypeface(typeface);
        Rect result = new Rect();
        paint.getTextBounds(str, 0, str.length(), result);

        return result.width();
    }

    public static void setPaddingLeftView(Context context, View view, int dp) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setPadding(convertDpToPixel(dp, context), 0, 0, 0);
        }
    }

    public static void checkInitialized(Context context) {
        TwitterConfig config = new TwitterConfig.Builder(context)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(Utils.TWITTER_KEY, Utils.TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    public static String getVideoId(String link) {
        if (link.endsWith("/")) {
            String substring = link.substring(0, link.length() - 1);
            int i = substring.lastIndexOf("/");
            return substring.substring(i + 1);
        } else {
            int i = link.lastIndexOf("=");
            return link.substring(i + 1);
        }
    }

    public static String parseVideoIdDaily(String link) {
        String link2 = link.substring(link.lastIndexOf("/") + 1);

        if (link2.contains("?")) {
            link2 = link2.substring(0, link2.indexOf("?"));
        }

        Log.e("TAG", "parseVideoIdDaily: " + link2);
        return link2;
    }
}
