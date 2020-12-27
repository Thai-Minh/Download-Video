package com.tapi.downloadsocialvideo.function.downloader.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.core.content.ContextCompat;

import com.tapi.download.video.core.Video;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloadsocialvideo.service.DownloadService;

public class DownloaderUtils {
    public static void startService(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }

    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, DownloadService.class);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static String getFileExtension(String downloadUrl) {
        return MimeTypeMap.getFileExtensionFromUrl(downloadUrl);
    }

    public static String getStateString(int state) {
        switch (state) {
            case TaskStates.INIT:
                return "INIT";
            case TaskStates.READY:
                return "READY";
            case TaskStates.DOWNLOADING:
                return "DOWNLOADING";
            case TaskStates.PAUSED:
                return "PAUSED";
            case TaskStates.DOWNLOAD_FINISHED:
                return "DOWNLOAD_FINISHED";
            case TaskStates.MERGE:
                return "MERGE";
            default:
                return "END";
        }
    }

    public static void addFileToMediaStore(Context mContext,String title,String videoPath){
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.TITLE, title);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/*");
        values.put(MediaStore.Video.Media.DATA, videoPath);
        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
    }
}
