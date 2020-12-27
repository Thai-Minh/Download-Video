package com.tapi.download.video.twitter.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.RequiresApi;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.task.GetSizeLink;
import com.tapi.download.video.twitter.R;
import com.tapi.download.video.twitter.utils.ConstantTwitter;
import com.tapi.download.video.twitter.utils.Utils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;

public class ClipBoardTwitterService extends Service {

    public static final String TAG = ClipBoardTwitterService.class.getSimpleName();

    private int sizeLink = 0;

    private ClipboardManager mClipboardManager;
    private ProgressDialog progressDialog;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {

        private static final long THRESHOLD_MS = 1500;
        private long lastChangedTime = 0;
        private String lastString = "";

        @Override
        public void onPrimaryClipChanged() {
            ClipData clip = mClipboardManager.getPrimaryClip();
            String paste = clip.getItemAt(0).getText().toString();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (System.currentTimeMillis() - lastChangedTime < THRESHOLD_MS && Objects.equals(lastString, paste)) {
                    return;
                }

                lastChangedTime = System.currentTimeMillis();
                lastString = paste;

                if (lastString.matches("https://twitter.com/(.*)")) {

                    Long id = getTweetId(lastString);

                    Log.e(TAG, String.valueOf(id));

                    Utils.initializeSSLContext(getApplicationContext());

                    callApi(id);
                }
            }
        }
    };

    private void callApi(long id) {
        final ArrayList<DownloadLink> arrayListLink = new ArrayList<>();
        final Video[] video = {null};

        //start handle
        sendBroadcastHandleVideo();

        try {
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            StatusesService statusesService = twitterApiClient.getStatusesService();
            Call<Tweet> tweetCall = statusesService.show(id, null, null, null);
            tweetCall.enqueue(new Callback<Tweet>() {
                @Override
                public void success(Result<Tweet> result) {

                    //Check if media is present
                    if (result.data.extendedEntities == null || result.data.entities.media == null || result.data.entities.media.isEmpty()) {
                        if (result.data.entities.urls.get(0).expandedUrl.matches("https://twitter.com/(.*)")) {
                            String url = result.data.entities.urls.get(0).expandedUrl;
                            long expandedId = getTweetId(url);
                            callApi(expandedId);
                        }
                    } else if (result.data.extendedEntities != null && !result.data.extendedEntities.media.isEmpty()) {
                        if (!(result.data.extendedEntities.media.get(0).type).equals("video") &&
                                !(result.data.extendedEntities.media.get(0).type).equals("animated_gif")) {

                        } else {
                            String id = "", title = "", userName = "", imgUser = "";
                            if (!result.data.text.isEmpty()) {
                                title = result.data.text;
                                if (!title.matches("[a-zA-Z0-9.:?/|]*")) {
                                    if (title.length() > 220) {
                                        title = title.substring(0, 220);
                                    }
                                    title = title.replaceAll("[:#%?/|]*", "");
                                    if (title.contains("\n")) {
                                        title = title.replace("\n", "");
                                    }
                                }
                            }

                            id = result.data.idStr;
                            userName = result.data.user.name;
                            imgUser = result.data.user.profileImageUrl;
                            String thumbnail = result.data.extendedEntities.media.get(0).mediaUrl;
                            long duration = result.data.extendedEntities.media.get(0).videoInfo.durationMillis;

                            for (int i = 0; i < result.data.extendedEntities.media.size(); i++) {
                                for (int j = 0; j < result.data.extendedEntities.media.get(i).videoInfo.variants.size(); j++) {
                                    final String url = result.data.extendedEntities.media.get(i).videoInfo.variants.get(j).url;

                                    if (url.contains(".mp4")) {
                                        String resolution = url.substring(url.indexOf("vid/"));
                                        resolution = resolution.substring(4, resolution.indexOf("x"));

                                        try {
                                            sizeLink = new GetSizeLink().execute(url).get();
                                        } catch (ExecutionException e) {
                                            e.printStackTrace();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        arrayListLink.add(new DownloadLink(url, sizeLink, Integer.parseInt(resolution)));

                                    }
                                }
                            }

                            if (arrayListLink.size() > 1)
                                Utils.sortDownloadList(arrayListLink);

                            video[0] = new Video(id, imgUser, userName, title, thumbnail, (int) duration, arrayListLink);

                            sendBroadcastVideoLoaded(video[0]);

                            Log.e(TAG, "sendBroadcast success: " + title);

                        }
                    }
                }

                @Override
                public void failure(TwitterException exception) {
                    Log.e("Connect", exception.toString());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onPrimaryClipChanged: " + e.toString());
        }
    }

    private void sendBroadcastVideoLoaded(Video video) {
        Intent svcIntent = new Intent();
        svcIntent.setAction(ConstantTwitter.VIDEO_LOADED);
        svcIntent.putExtra(ConstantTwitter.VIDEO, video);
        sendBroadcast(svcIntent);

    }

    private void sendBroadcastHandleVideo() {
        Intent svcIntent = new Intent();
        svcIntent.setAction(ConstantTwitter.VIDEO_HANDLE);
        sendBroadcast(svcIntent);

    }

    private Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);
        } catch (Exception e) {
            Log.e(TAG, "getTweetId: " + e.getLocalizedMessage());
            return null;
        }
    }
}
