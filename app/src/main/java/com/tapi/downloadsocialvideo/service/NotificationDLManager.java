package com.tapi.downloadsocialvideo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.activities.MainActivity;

public class NotificationDLManager {
    private static final String TAG = "NotificationDLManager";
    private final static String CHANNEL_ID = "Download Social Video";
    private final static String CHANNEL_NAME = "Download Social Video";
    private static final int REQUEST_CODE_ACTIVITY = 111;

    private Context context;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    public NotificationDLManager(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public Notification createNotification(String title, String state) {
        createNotificationChannel();
        builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(state)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // <= android 7.1
                .setOnlyAlertOnce(true)
                .setContentIntent(getContentIntent(context))
                .setShowWhen(false)
                .setSmallIcon(R.drawable.notification_small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.download_icon));

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setSound(null, null);

            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, int notificationId) {
        Log.d(TAG, "showNotification: " + notificationId);
        if (notificationManager != null)
            notificationManager.notify(notificationId, createNotification(title, "Downloading"));
    }

    public void cancelNotification(int notificationId) {
        Log.d(TAG, "cancelNotification: " + notificationId);
        notificationManager.cancel(notificationId);
    }

    public void updateNotification(int notificationId, String title, String state) {
        Notification notification = getNotification(title, state);
        if (notificationManager != null)
            notificationManager.notify(notificationId, notification);
    }

    private Notification getNotification(String title, String state) {
        builder.setSmallIcon(R.drawable.notification_small_icon)
                .setContentTitle(title)
                .setContentText(state);
        return builder.build();
    }

    private PendingIntent getContentIntent(Context context) {
        Intent intentActivity = new Intent(context, MainActivity.class);
        intentActivity.setAction(Intent.ACTION_MAIN);
        intentActivity.addCategory(Intent.CATEGORY_LAUNCHER);
        return PendingIntent.getActivity(context, REQUEST_CODE_ACTIVITY, intentActivity, 0);
    }
}
