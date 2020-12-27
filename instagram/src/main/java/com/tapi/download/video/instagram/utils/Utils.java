package com.tapi.download.video.instagram.utils;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.tapi.download.video.instagram.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import static android.content.Context.CLIPBOARD_SERVICE;

public class Utils {

    private static final String TAG = "Utils";

    public static final String INSTAGRAM_URL = "https://www.instagram.com";

    public static String[] USER_AGENT = new String[]{
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"
    };

    public static boolean checkLinkLive(String url) {
        return url.contains("live-dash");
    }

    public static int getDuration(String url) {
        if (!Utils.checkLinkLive(url)) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(url, new HashMap<String, String>());
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInMillisec = Long.parseLong(time);
            retriever.release();
            return (int) timeInMillisec;
        }
        return 0;
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

    private static boolean storageAllowed(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return permission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static String getUserAgent() {
        Random random = new Random();
        int i = random.nextInt(USER_AGENT.length);
        return USER_AGENT[i];
    }

    public static boolean checkLoadFinishInsta(String html) {
        return (html.contains("es6") || html.contains("metro"));
    }

    public static int getWidthScreen(Context context) {
        WindowManager window = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        window.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    public static int convertDpToPixel(float dp, Context context) {
        return (int) dp * (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static String getStringClipBoard(Context context) {
        ClipboardManager clipBoard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = clipBoard.getPrimaryClip();
        if (clipData != null) {
            ClipData.Item item = clipData.getItemAt(0);
            return item.getText().toString();
        }
        return null;
    }

    public static void openInstagramApp(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            context.startActivity(intent);
        } catch (Exception e) {
            // returns null if application is not installed
            Toast.makeText(context, "Application is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public static void loadThumbnail(Context context, String urlThumbnail, ImageView imgView) {
        Glide.with(context)
                .load(urlThumbnail)
                .into(imgView);
    }

    public static String getDurationString(int millisecond) {
        Date date = new Date(millisecond);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);

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

    public static int convertSpToPixel(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static void changeColorSeekbar(Context context, SeekBar mSeekBar) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            LayerDrawable progressDrawable = (LayerDrawable) mSeekBar.getProgressDrawable();
            Drawable processDrawable = progressDrawable.findDrawableByLayerId(android.R.id.progress);
            processDrawable.setColorFilter(context.getResources().getColor(R.color.insta_preview_seekbar_color), PorterDuff.Mode.SRC_IN);
            Drawable backgroundDrawable = progressDrawable.findDrawableByLayerId(android.R.id.background);
            backgroundDrawable.setColorFilter(context.getResources().getColor(R.color.insta_preview_seekbar_line), PorterDuff.Mode.SRC_IN);
            mSeekBar.getThumb().setColorFilter(context.getResources().getColor(R.color.insta_preview_seekbar_color), PorterDuff.Mode.SRC_IN);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawableProgress = DrawableCompat.wrap(mSeekBar.getIndeterminateDrawable());
            DrawableCompat.setTint(drawableProgress, ContextCompat.getColor(context, R.color.insta_preview_seekbar_line));
            mSeekBar.setProgressDrawable(DrawableCompat.unwrap(drawableProgress));
        } else {
            mSeekBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(context, R.color.insta_preview_seekbar_line), PorterDuff.Mode.SRC_IN);
        }
    }

}
