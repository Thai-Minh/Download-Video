package com.tapi.download.video.instagram.core;

import android.os.AsyncTask;
import android.util.Log;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.listener.ICatch;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.core.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class InstagramCatchVideo implements ICatch {

    private static final String TAG = "InstagramCatchVideo";

    private static InstagramCatchVideo instance;

    public static InstagramCatchVideo getInstance() {
        if (instance == null)
            instance = new InstagramCatchVideo();
        return instance;
    }

    @Override
    public void getVideoDownloadLink(String viewLink, OnCatchVideoListener onCatchVideoListener) {
        AppPreferences.INSTANCE.putString("URL", viewLink);
        new getLinkAsyntask(onCatchVideoListener).execute(viewLink);
        // a
    }

    private static Video getVideo(Document document) {
        Elements strs = document.body().select("script");
        ArrayList<DownloadLink> listLinks = new ArrayList<>();

        String id = "", url = "", title = "", image = "", userName = "", imageUser = "";
        double duration = 0.0d;
        int sizeLink = 0;

        title = document.title();
        if (!title.matches("[a-zA-Z0-9.:?/|]*")) {
            if (title.length() > 220) {
                title = title.substring(0, 220);
            }
            title = title.replaceAll("[:#%?/|]*", "");
            if (title.contains("\n")) {
                title = title.replace("\n", "");
            }
        }

        for (Element element : strs) {
            String elString = element.toString();

            if (elString.contains("video_url")) {
                elString = elString.substring(elString.indexOf("{\"graphql\""), elString.lastIndexOf("[]}}}}") + 6);
                try {
                    JSONObject jsonObject = new JSONObject(elString);
                    JSONObject objGraphql = jsonObject.getJSONObject("graphql");
                    JSONObject objShortCode = objGraphql.getJSONObject("shortcode_media");

                    url = objShortCode.getString("video_url");
                    if (url.contains("\\")) {
                        url = url.replace("\\", "");
                    }

                    id = objShortCode.getString("id");

                    image = objShortCode.getString("display_url");
                    if (image.contains("\\")) {
                        image = image.replace("\\", "");
                    }

                    duration = objShortCode.getDouble("video_duration");

                    JSONObject objOwner = objShortCode.getJSONObject("owner");
                    userName = objOwner.getString("username");
                    imageUser = objOwner.getString("profile_pic_url");
                    if (imageUser.contains("\\")) {
                        imageUser = imageUser.replace("\\", "");
                    }

                    sizeLink = Utils.getVideoSize(url);

                    listLinks.add(new DownloadLink(url, sizeLink, Utils.VIDEO_HD));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (listLinks.isEmpty())
            return null;
        return new Video(id, imageUser, userName, title, image, (int) Math.round(duration) * 1000, listLinks);
    }

    private static Video getVideoWithIndex(Document document, String index) {
        Elements strs = document.body().select("script");
        ArrayList<DownloadLink> listLinks = new ArrayList<>();

        String id = "", url = "", title = "", thumbnail = "", userName = "", imageUser = "";
        int duration = 0;
        int sizeLink = 0;

        title = document.title();
        if (!title.matches("[a-zA-Z0-9.:?]*")) {
            if (title.length() > 220) {
                title = title.substring(0, 220);
            }
            title = title.replaceAll("[:#%?]*", "");
            if (title.contains("\n")) {
                title = title.replace("\n", "");
            }
        }

        for (Element element : strs) {
            String elString = element.toString();

            if (elString.contains("edge_sidecar_to_children")) {
                elString = elString.substring(elString.indexOf("{\"graphql\""), elString.lastIndexOf("[]}}}}") + 6);
                try {
                    JSONObject jsonObject = new JSONObject(elString);
                    JSONObject objGraphql = jsonObject.getJSONObject("graphql");
                    JSONObject objShortCode = objGraphql.getJSONObject("shortcode_media");
                    JSONObject objChildren = objShortCode.getJSONObject("edge_sidecar_to_children");
                    JSONArray objEdges = objChildren.getJSONArray("edges");

                    JSONObject obInto = objEdges.getJSONObject(Integer.parseInt(index));
                    JSONObject obNode = obInto.getJSONObject("node");

                    id = obNode.getString("id");

                    thumbnail = obNode.getString("display_url");
                    if (thumbnail.contains("amp;")) {
                        thumbnail = thumbnail.replace("amp;", "");
                    }

                    boolean isVideo = obNode.getBoolean("is_video");
                    if (isVideo) {
                        url = obNode.getString("video_url");
                        if (url.contains("\\")) {
                            url = url.replace("\\", "");
                        }

                        // return s
                        duration = com.tapi.download.video.instagram.utils.Utils.getDuration(url) / 1000;

                        Log.e(TAG, "getVideoWithIndex: " + duration);
                    }

                    JSONObject objOwner = objShortCode.getJSONObject("owner");
                    userName = objOwner.getString("username");
                    imageUser = objOwner.getString("profile_pic_url");
                    if (imageUser.contains("\\")) {
                        imageUser = imageUser.replace("\\", "");
                    }

                    sizeLink = Utils.getVideoSize(url);

                    listLinks.add(new DownloadLink(url, sizeLink, Utils.VIDEO_HD));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (listLinks.isEmpty())
            return null;
        return new Video(id, imageUser, userName, title, thumbnail, Math.round(duration) * 1000, listLinks);
    }

    private static class getLinkAsyntask extends AsyncTask<String, Void, Video> {
        private OnCatchVideoListener listener;
        private String link;

        public getLinkAsyntask(OnCatchVideoListener listener) {
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

            String id;
            try {
                link = strings[0];
                id = link.substring(link.lastIndexOf("/") + 1);
                if (id.contains("?")) {
                    id = "";
                }
                link = link.substring(0, link.lastIndexOf("/") + 1);

                Log.e(TAG, "doInBackground: id: " + id);
                Log.e(TAG, "doInBackground: link: " + link);

                Document document = Jsoup.connect(link)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36")
                        .header("Cookie", AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, ""))
                        .get();

                if (id.isEmpty() || id == null) {
                    return getVideo(document);
                } else {
                    return getVideoWithIndex(document, id);
                }
            } catch (IOException e) {
                return null;
            }

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
