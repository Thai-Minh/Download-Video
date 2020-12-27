package com.tapi.download.video.dailymotion.util;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utils {

    public final static String URL = "url";

    public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params) {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
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

    public static void openDailyApp(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.dailymotion.dailymotion");
            context.startActivity(intent);
        } catch (Exception e) {
            // returns null if application is not installed
            Toast.makeText(context, "Application is not installed", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getDurationString(int millisecond) {
        Date date = new Date(millisecond);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);

    }

    public static int convertDpToPixel(float dp, Context context) {
        return (int) dp * (context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    // Get full file name
    public static String getFullName(String downloadUrl, String name) {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(downloadUrl);
        return name.concat(".").concat(fileExtension);
    }
}
