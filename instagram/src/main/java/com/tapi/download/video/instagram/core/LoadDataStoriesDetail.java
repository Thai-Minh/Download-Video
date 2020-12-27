package com.tapi.download.video.instagram.core;

import android.os.AsyncTask;
import android.util.Log;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.model.StoriesInsta;
import com.tapi.download.video.core.task.GetSizeLink;
import com.tapi.download.video.core.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class LoadDataStoriesDetail {
    private static final String TAG = LoadDataStoriesDetail.class.getSimpleName();

    public interface OnLoadDataStoryDetailListener {
        void onLoadSuccess(StoriesInsta stories);
    }

    public static class loadDataStoryAsync extends AsyncTask<String, Void, StoriesInsta> {
        private OnLoadDataStoryDetailListener listener;

        private StoriesInsta loadDataDetail(JSONArray arrItems, JSONObject obOwner, int position, long postTime, long lastTime, long currentTime,
                                            String thumbnailStory, String linkVideo, double duration, ArrayList<DownloadLink> arrayListVideo) {
            arrayListVideo = new ArrayList<>();
            DownloadLink downloadLink;
            int linkSize = 0;
            StoriesInsta stories = new StoriesInsta();
            String id = "";
            try {
                JSONObject obInto = arrItems.getJSONObject(position);
                JSONArray arrDisplay = obInto.getJSONArray("display_resources");

                id = obInto.getString("id");

                postTime = obInto.getLong("taken_at_timestamp");
                if (postTime != 0) {
                    postTime = postTime * 1000;
                }
                lastTime = currentTime - postTime;

                JSONObject obRes = arrDisplay.getJSONObject(0);
                thumbnailStory = obRes.getString("src");
                if (thumbnailStory.contains("amp;")) {
                    thumbnailStory = thumbnailStory.replace("amp;", "");
                }

                boolean isVideo = obInto.getBoolean("is_video");
                if (isVideo) {
                    JSONArray arrVideoResources = obInto.getJSONArray("video_resources");
                    if (arrVideoResources.length() != 0) {
                        stories.setType("video");
                        //duration
                        duration = obInto.getDouble("video_duration"); // second
                        stories.setDuration((int) duration);

                        for (int j = 0; j < arrVideoResources.length(); j++) {
                            JSONObject obIntoVideo = arrVideoResources.getJSONObject(j);
                            linkVideo = obIntoVideo.getString("src");
                            if (linkVideo.contains("amp;")) {
                                linkVideo = linkVideo.replace("amp;", "");
                                linkSize = Utils.getVideoSize(linkVideo);
                                Log.e(TAG, "loadDataDetail: size: " + linkSize);
                            }
                            downloadLink = new DownloadLink(linkVideo, linkSize, 0);
                            arrayListVideo.add(downloadLink);
                            stories.setLinkVideoStory(arrayListVideo);

                        }
                    }
                } else {
                    stories.setType("photo");
//                    arrayListVideo.add(null);
                    stories.setLinkVideoStory(arrayListVideo);
                }

                stories.setUserName(obOwner.getString("username"));

                String imageUser = obOwner.getString("profile_pic_url");
                if (imageUser.contains("amp;")) {
                    imageUser = imageUser.replace("amp;", "");
                    stories.setImageUser(imageUser);
                }

                stories.setId(id);
                stories.setThumbnailStory(thumbnailStory);
                stories.setLastTime(String.valueOf(lastTime));
                stories.setSize(arrItems.length());

            } catch (JSONException e) {
                Log.e(TAG, "loadDataDetail: " + e.getMessage());
            }

            return stories;
        }

        public loadDataStoryAsync(OnLoadDataStoryDetailListener listener) {
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected StoriesInsta doInBackground(String... strings) {
            ArrayList<DownloadLink> arrayListVideo = new ArrayList<>();
            StoriesInsta stories = new StoriesInsta();

            String thumbnailStory = "", linkVideo = "";
            String getPosition = null;

            double duration = 0.0d;

            long postTime = 0, lastTime = 0;
            Date date = new Date();
            long currentTime = date.getTime();

            if (strings[0].contains(" + position ")) {
                getPosition = strings[0].substring(strings[0].indexOf(" + position ") + 12);
            }

            try {
                JSONObject object = new JSONObject(strings[0]);
                JSONArray arrItems = object.getJSONArray("items");
                JSONObject obOwner = object.getJSONObject("owner");

                if (getPosition != null) {
                    stories = loadDataDetail(arrItems, obOwner, Integer.parseInt(getPosition), postTime, lastTime, currentTime,
                            thumbnailStory, linkVideo, duration, arrayListVideo);
                } else {
                    for (int i = 0; i < arrItems.length(); i++) {
                        stories = loadDataDetail(arrItems, obOwner, i, postTime, lastTime, currentTime,
                                thumbnailStory, linkVideo, duration, arrayListVideo);
                    }
                }

                stories.setReelsID(strings[0]);

            } catch (JSONException e) {
                Log.e(TAG, "doInBackground: " + e.getMessage());
            }

            Log.e(TAG, "doInBackground: link size: " + stories.getLinkVideoStory().size());

            return stories;
        }

        @Override
        protected void onPostExecute(StoriesInsta stories) {
            super.onPostExecute(stories);
            if (listener != null)
                listener.onLoadSuccess(stories);
        }
    }
}
