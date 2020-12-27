package com.tapi.downloadsocialvideo.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.activities.PlayVideoActivity;
import com.tapi.downloadsocialvideo.function.main.SocialManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

public class Utils {
    public static final String URL_PRIVACY_POLICY = "http://www.google.com";
    public static String LINK_APP_STORE = "https://play.google.com/store/apps/details?id=%s";
    public static final String TASK_DOWNLOAD = "task_download";
    public static final String VIDEO_URL = "video_url";

    // Convert byte size into human-readable (MB, GB...)
    public static String formatFileSize(Context context, long bytes) {
        return Formatter.formatShortFileSize(context, bytes);
    }

    // Formats an elapsed time in the form "MM:SS" or "H:MM:SS"
    public static String formatDuration(long second) {
        return DateUtils.formatElapsedTime(second);
    }

    public static boolean isFileDeleted(String filePath) {
        File file = new File(filePath);
        return !file.exists();
    }

    public static void startPlayVideoActivity(Context context, Task downloadTask, String filePath) {
        Intent intent = new Intent(context, PlayVideoActivity.class);
        intent.putExtra(TASK_DOWNLOAD, downloadTask);
        intent.putExtra(VIDEO_URL, filePath);
        context.startActivity(intent);
    }

    public static void loadThumbnail(Context context, String urlThumbnail, ImageView imgView) {
        Glide.with(context)
                .load(urlThumbnail)
                .placeholder(R.drawable.fb_loading_iv)
                .error(R.drawable.fb_loading_fail_iv)
                .into(imgView);
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


    public static String convertTime(String duration) {
        if (duration.equalsIgnoreCase("P0D")) {
            return "0:00";
        }
        String s = duration.replace("T", "");

        if (s.contains("H")) {
            s = s.replace("H", ":");
            if (s.contains("M")) {
                s = s.replace("M", ":");
                if (s.contains("S")) {
                    s = s.replace("S", "");
                    String[] spilt = s.split(":");
                    return String.format(Locale.ENGLISH, "%s:%02d:%02d", spilt[0], Integer.valueOf(spilt[1]), Integer.valueOf(spilt[2]));
                } else {
                    String[] spilt = s.split(":");
                    return String.format(Locale.ENGLISH, "%s:%02d:%02d", spilt[0], Integer.valueOf(spilt[1]), 0);
                }
            } else if (s.contains("S")) {
                s = s.replace("S", "");
                String[] spilt = s.split(":");
                return String.format(Locale.ENGLISH, "%s:%02d:%02d", spilt[0], 0, Integer.valueOf(spilt[1]));
            } else {
                String[] spilt = s.split(":");
                return String.format(Locale.ENGLISH, "%s:%02d:%02d", spilt[0], 0, 0);
            }


        } else if (s.contains("M")) {
            s = s.replace("M", ":");
            if (s.contains("S")) {
                s = s.replace("S", "");
            } else {
                s = s + "00";
            }
            String[] spilt = s.split(":");
            return String.format(Locale.ENGLISH, "%s:%02d", spilt[0], Integer.valueOf(spilt[1]));
        } else {
            s = s.replace("S", "");
            return String.format(Locale.ENGLISH, "%s:%02d", "0", Integer.valueOf(s));
        }
    }

    public static void shareVideo(Context context, String path) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (!Utils.isFileDeleted(path)) {
            Uri uri = Uri.fromFile(new File(path));
            Intent videoshare = new Intent(Intent.ACTION_SEND);
            videoshare.setType("video/mp4");
            videoshare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            videoshare.putExtra(Intent.EXTRA_STREAM, uri);
            context.startActivity(videoshare);
        }

    }

    public static String getSaveNameVideo(Video video, int resolution) {
        String title = getVideoTitle(video);
        return title + "_" + (resolution < 0 ? (resolution == com.tapi.download.video.core.utils.Utils.VIDEO_HD ? "HD" : "SD") : resolution);
    }

    public static String getVideoTitle(Video video) {
        String title = video.getTitle();
        if (title == null || title.isEmpty()) {
            title = "Unknown";
        }
        return title;
    }

    public static boolean checkVideoLive(Video video) {
        if (SocialManager.getInstance().getmState() == SocialManager.StateApp.FACE_BOOK){
            ArrayList<DownloadLink> links = video.getLinks();
            for (DownloadLink link : links) {
                if (checkLinkLive(link.getLink()))
                    return true;
            }
        }
        return false;
    }

    public static boolean checkLinkLive(String url) {
        return url.contains("live-dash");
    }

    public static boolean isInternetConnected(Context ctx) {
        ConnectivityManager connectivityMgr = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi != null) {
            if (wifi.isConnected()) {
                return true;
            }
        }
        if (mobile != null) {
            return mobile.isConnected();
        }
        return false;
    }

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void showLinkWebView(Context context, String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(link));
        context.startActivity(intent);
    }

    public static void shareApp(Context context) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, String.format(Locale.ENGLISH, LINK_APP_STORE, context.getPackageName()));
        context.startActivity(Intent.createChooser(intent, context.getString(com.tapi.download.video.core.R.string.share_app_title)));
    }

    public static void openPermissionSetting(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }
//    public static String formatDuration(int millisecond) {
//        Date date = new Date(millisecond);
//        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss", Locale.US);
//        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
//        return formatter.format(date);
//    }

//    public static String createDownloadDirectory() {
//        String homeDirectory = "Download Social Video/Download";
//        File file = new File(Environment.getExternalStorageDirectory(), homeDirectory);
//        if (!file.exists())
//            file.mkdirs();
//        return file.getAbsolutePath();
//    }
}
