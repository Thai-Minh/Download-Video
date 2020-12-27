package com.tapi.downloadsocialvideo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.activities.MainActivity;

public class DownloadServiceNotification {
    private final static String CHANNEL_ID = "Download Social Video";
    private final static String CHANNEL_NAME = "Download Social Video";
    private static final int REQUEST_CODE_ACTIVITY = 222;

    public static Notification createNotification(Context context, int numberDownload) {
        createNotificationChannel(context);
        String title = context.getResources().getQuantityString(R.plurals.number_download, numberDownload, numberDownload);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // <= android 7.1
                .setOnlyAlertOnce(true)
                .setContentIntent(getContentIntent(context))
                .setShowWhen(false)
                .setSmallIcon(R.drawable.notification_small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.download_icon));
        return builder.build();
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setSound(null, null);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    private static PendingIntent getContentIntent(Context context) {
        Intent intentActivity = new Intent(context, MainActivity.class);
        intentActivity.setAction(Intent.ACTION_MAIN);
        intentActivity.addCategory(Intent.CATEGORY_LAUNCHER);
        return PendingIntent.getActivity(context, REQUEST_CODE_ACTIVITY, intentActivity, 0);
    }
}
