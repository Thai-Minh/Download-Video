package com.tapi.download.video.twitter.task;

import android.os.AsyncTask;
import android.util.Log;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.ICatch;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.twitter.utils.Utils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.ArrayList;

import retrofit2.Call;

public class TwitterCatchVideo implements ICatch {

    private static final String TAG = "TwitterCatchVideo";
    private static TwitterCatchVideo instance;
    private boolean isWaitingForGettingVideo = false;

    public static TwitterCatchVideo getInstance() {
        if (instance == null)
            instance = new TwitterCatchVideo();
        return instance;
    }

    @Override
    public void getVideoDownloadLink(String viewLink, OnCatchVideoListener onCatchVideoListener) {
        if (viewLink.contains("https://twitter.com")) {
            new getLinkVideoTwitter(onCatchVideoListener).execute(viewLink);
        } else if (viewLink.contains("https://youtu.be/") ||viewLink.contains("https://www.youtube.com/watch?v=")) {
            new getLinkVideoYoutube(onCatchVideoListener).execute(viewLink);
        }
    }

    private Video getVideo(final long id) {

        final ArrayList<DownloadLink> arrayListLinks = new ArrayList<>();
        final Video[] video = {new Video()};

        final String[] videoId = new String[1];
        final String[] imgUser = new String[1];
        final String[] userName = new String[1];
        final String[] title = new String[1];
        final String[] thumbnail = new String[1];
        final long[] duration = new long[1];

        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<Tweet> tweetCall = statusesService.show(id, null, null, null);
        isWaitingForGettingVideo = true;
        tweetCall.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                try {
                    //Check if media is present
                    if (result.data.extendedEntities == null || result.data.entities.media == null || result.data.entities.media.isEmpty()) {
                        if (result.data.entities.urls.get(0).expandedUrl.matches("https://twitter.com/(.*)")) {
                            String url = result.data.entities.urls.get(0).expandedUrl;
                            final long expandedId = getTweetId(url);

                            video[0].setImgUser(String.valueOf(expandedId));

                        }
                    } else if (result.data.extendedEntities != null && !result.data.extendedEntities.media.isEmpty()) {
                        if (!(result.data.extendedEntities.media.get(0).type).equals("video") &&
                                !(result.data.extendedEntities.media.get(0).type).equals("animated_gif")) {

                        } else {
                            if (!result.data.text.isEmpty()) {
                                title[0] = result.data.text;
                                Log.e(TAG, "success: " + title[0]);

                                if (!title[0].matches("[a-zA-Z0-9.:?/|]*")) {
                                    if (title[0].length() > 220) {
                                        title[0] = title[0].substring(0, 220);
                                    }
                                    title[0] = title[0].replaceAll("[:#%?/|]*", "");
                                    if (title[0].contains("\n")) {
                                        title[0] = title[0].replace("\n", "");
                                    }
                                }
                            }

                            videoId[0] = result.data.idStr;
                            userName[0] = result.data.user.name;
                            imgUser[0] = result.data.user.profileImageUrl;
                            thumbnail[0] = result.data.entities.media.get(0).mediaUrl;
                            duration[0] = result.data.extendedEntities.media.get(0).videoInfo.durationMillis;

                            for (int j = 0; j < result.data.extendedEntities.media.get(0).videoInfo.variants.size(); j++) {
                                final String url = result.data.extendedEntities.media.get(0).videoInfo.variants.get(j).url;
                                if (url.contains(".mp4")) {
                                    String resolution = url.substring(url.indexOf("vid/"));
                                    resolution = resolution.substring(4, resolution.indexOf("x"));
                                    int sizeLink = 0;
                                    arrayListLinks.add(new DownloadLink(url, sizeLink, Integer.parseInt(resolution)));
                                    Log.e(TAG, "success: link " + url);
                                    Log.e(TAG, "success: resolution " + resolution);
                                }
                            }
                            if (arrayListLinks.size() > 1)
                                Utils.sortDownloadList(arrayListLinks);

                            video[0] = new Video(videoId[0], imgUser[0], userName[0], title[0], thumbnail[0], (int) duration[0], arrayListLinks);
                        }
                    }

                } catch (Exception e) {

                } finally {
                    isWaitingForGettingVideo = false;
                }
            }

            @Override
            public void failure(TwitterException exception) {
                Log.e(TAG, "failure: " + exception.toString());
                isWaitingForGettingVideo = false;
            }
        });

        while (isWaitingForGettingVideo) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        return video[0];

    }

    private static Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);
        } catch (Exception e) {
            Log.d("TAG", "getTweetId: " + e.getLocalizedMessage());
            return null;
        }
    }

    private class getLinkVideoTwitter extends AsyncTask<String, Void, Video> {
        private OnCatchVideoListener listener;
        private String link;

        public getLinkVideoTwitter(OnCatchVideoListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listener != null)
                listener.onStartCatch();
        }

        @Override
        protected Video doInBackground(String... strings) {
            link = strings[0];
            Long id = getTweetId(link);

            Video video = getVideo(id);
            if (video.getTitle() == null && video.getLinks() == null) {
                video = getVideo(Long.parseLong(video.getImgUser()));
                for (DownloadLink downloadLink : video.getLinks()) {
                    try {
                        int videoSize = com.tapi.download.video.core.utils.Utils.getVideoSize(downloadLink.getLink());
                        downloadLink.setSize(videoSize);
                    } catch (Exception e) {
                        Log.e(TAG, "success1: " + e.toString());
                    }
                }
            } else {
                for (DownloadLink downloadLink : video.getLinks()) {
                    try {
                        int videoSize = com.tapi.download.video.core.utils.Utils.getVideoSize(downloadLink.getLink());
                        downloadLink.setSize(videoSize);
                    } catch (Exception e) {
                        Log.e(TAG, "success2: " + e.toString());
                    }
                }
            }

            Log.e(TAG, "doInBackground: " + video.getThumbnail());
            return video;
        }

        @Override
        protected void onPostExecute(Video video) {
            super.onPostExecute(video);
            if (listener != null) {
                if (video != null)
                    listener.onCatchedLink(video);
                else listener.onPrivateLink(link);
            }
        }
    }

    private class getLinkVideoYoutube extends AsyncTask<String, Void, Video> {
        private OnCatchVideoListener listener;
        private String link;

        public getLinkVideoYoutube(OnCatchVideoListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listener != null)
                listener.onStartCatch();
        }

        @Override
        protected Video doInBackground(String... strings) {
            link = strings[0];

            return new Video();
        }

        @Override
        protected void onPostExecute(Video video) {
            super.onPostExecute(video);
            if (listener != null) {
                if (video != null)
                    listener.onCatchedLink(video);
                else listener.onPrivateLink(link);
            }
        }
    }
}
