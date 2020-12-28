package com.tapi.download.video.twitter.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.twitter.task.TwitterCatchVideo;
import com.tapi.download.video.twitter.task.youtube.SOService;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.SSLContext;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Utils {

    private static final String TAG = "Utils";

    public static final String TWITTER_URL = "https://twitter.com/";

    public static final String BASE_URL = "http://192.168.0.186:5000/";

    public static SOService getSOService() {
        return getClient(BASE_URL).create(SOService.class);
    }
    private static Retrofit retrofit = null;
    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static void initializeSSLContext(Context mContext){
        try {
            SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            ProviderInstaller.installIfNeeded(mContext.getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private static boolean storageAllowed(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int permission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            return permission == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    public static void openTwitterApp(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.twitter.android");
            context.startActivity(intent);
        } catch (Exception e) {
            // returns null if application is not installed
            Toast.makeText(context, "Application is not installed", Toast.LENGTH_SHORT).show();
        }
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

    public static String getDurationString(int millisecond) {
        Date date = new Date(millisecond);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);

    }

    public static void sortDownloadList(ArrayList<DownloadLink> downloadLinks) {
        Collections.sort(downloadLinks, new Comparator<DownloadLink>() {
            @Override
            public int compare(DownloadLink link1, DownloadLink link2) {
                return link2.getResolution() - link1.getResolution();
            }
        });
    }
}
