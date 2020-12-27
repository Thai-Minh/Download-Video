package com.tapi.download.video.dailymotion;

import android.os.AsyncTask;
import android.util.Log;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.task.GetSizeLink;
import com.tapi.download.video.core.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

public class GetVideoDownloadTask extends AsyncTask<String, Void, Boolean> {
    private static final String TAG = "GetVideoDownloadTask";
    private static final int TIME_OUT = 15000; // millisecond

    private String link;
    private String id;
    private OnCatchVideoListener listener;
    private Video video = new Video();

    public GetVideoDownloadTask(OnCatchVideoListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (listener != null)
            listener.onStartCatch();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        link = strings[0];
        Log.e(TAG, "doInBackground: " + link);
        if (link.contains("?")) {
            id = link.substring(link.lastIndexOf("/") + 1, link.indexOf("?"));
        } else {
            id = link.substring(link.lastIndexOf("/") + 1);
        }

        String videoId = getVideoId(link);
        if (videoId.isEmpty())
            return false;

        String url = getEmbedLink(getVideoId(link));
        Log.e(TAG, "embed link: " + url);

        try {
            Document document = Jsoup.connect(url).timeout(TIME_OUT).get();
            Elements elements = document.select("script");
            Log.e(TAG, "script: " + elements.get(14).toString());
            String scripStr = elements.get(14).toString();
            String json = scripStr.substring(getJsonStartIndex(scripStr), getJsonEndIndex(scripStr)).trim();
            json = json.substring(0, json.length() - 1); // remove ';'
            String m3u8Link = getM3u8DownloadLink(json);

            String m3u8String = getM3u8String(m3u8Link);
            getVideoDownloadLinks(m3u8String);

            getVideoInfoFromJson(json);

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "doInBackground: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "doInBackground: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.e(TAG, "doInBackground: " + e.getMessage());
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
        Log.e(TAG, "onPostExecute: ");

        if (listener != null)
            if (video != null)
                listener.onCatchedLink(video);
            else listener.onPrivateLink(link);
    }

    // Get video id (ex: video/x7rw77z)
    private String getVideoId(String viewLink) throws IndexOutOfBoundsException {
        try {
            int startIndex = viewLink.indexOf("video/");
            int strLength = viewLink.length();
            Log.e("getVideoId", "getVideoId: " + viewLink.substring(startIndex, strLength));
            return viewLink.substring(startIndex, strLength);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getEmbedLink(String videoId) {
        return String.format("https://www.dailymotion.com/embed/%s", videoId);
    }

    // Get video download link (with resolution)
    private void getVideoDownloadLinks(String m3u8String) {
        ArrayList<DownloadLink> downloadLinks = new ArrayList<>();
        String[] split = m3u8String.split("#EXT-X-STREAM-INF:");
        for (int i = 1; i < split.length; i++) {
            String[] split1 = split[i].split(",");
            String resolutionStr = split1[4].split("=")[1].replace("\"", "");

            if (resolutionStr.contains("@")) {
                resolutionStr = resolutionStr.substring(0, resolutionStr.indexOf("@"));
            }

            Log.e(TAG, "getVideoDownloadLinks: resolution " + resolutionStr);
            String downloadUrl = split1[5].split("\"")[1].split("#")[0];
            int resolution = 0;
            int sizeLink = 0;
            try {
                resolution = Integer.parseInt(resolutionStr);
                sizeLink = Utils.getVideoSize(downloadUrl);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            boolean resolutionExist = checkResolutionExist(resolution, downloadLinks);
            if (!resolutionExist) {
                DownloadLink link = new DownloadLink(downloadUrl, sizeLink, resolution);
                downloadLinks.add(link);
            }
            if (downloadLinks.size() > 1)
                sortDownloadList(downloadLinks);
            video.setLinks(downloadLinks);
        }
    }

    private boolean checkResolutionExist(int resolution, ArrayList<DownloadLink> downloadLinks) {
        for (DownloadLink downloadLink : downloadLinks)
            if (downloadLink.getResolution() == resolution)
                return true;
        return false;
    }

    private void sortDownloadList(ArrayList<DownloadLink> downloadLinks) {
        Collections.sort(downloadLinks, new Comparator<DownloadLink>() {
            @Override
            public int compare(DownloadLink link1, DownloadLink link2) {
                return link2.getResolution() - link1.getResolution();
            }
        });
    }

    private String getM3u8String(String m3u8Link) throws IOException {
        String m3u8String = "";
        URL m3u8Url = new URL(m3u8Link);
        InputStreamReader inputStreamReader = new InputStreamReader(m3u8Url.openStream());
        BufferedReader in = new BufferedReader(inputStreamReader);
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            m3u8String += inputLine + "\n";

        in.close();
        Log.e("getM3u8String", "getM3u8String: " + m3u8String);
        return m3u8String;
    }

    private String getM3u8DownloadLink(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject metaJson = jsonObject.getJSONObject("metadata");
        JSONObject qualitiesJson = metaJson.getJSONObject("qualities");
        JSONObject urlJson = qualitiesJson.getJSONArray("auto").getJSONObject(0);
        Log.e("getM3u8DownloadLink", "getM3u8DownloadLink: " + urlJson.getString("url"));
        return urlJson.getString("url");
    }

    private void getVideoInfoFromJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONObject metaJson = jsonObject.getJSONObject("metadata");
        JSONObject posters = metaJson.getJSONObject("posters");
        String thumbnailUrl = getThumbnailUrlFromJsonObject(posters);
        video.setThumbnail(thumbnailUrl);
        String title = metaJson.getString("title");
        Log.e(TAG, "getVideoInfoFromJson title: " + title);

        if (!title.matches("[a-zA-Z0-9.:?/|]*")) {
            if (title.length() > 220) {
                title = title.substring(0, 220);
            }
            title = title.replaceAll("[:#%?/|]*", "");
            if (title.contains("\n")) {
                title = title.replace("\n", "");
            }
        }

        Log.e(TAG, "getVideoInfoFromJson2: " + title);

        video.setTitle(title);

        int duration = metaJson.getInt("duration") * 1000; // millisecond
        video.setDuration(duration);
        video.setIdVideo(id);
    }

    private String getThumbnailUrlFromJsonObject(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("1080") && !jsonObject.getString("1080").isEmpty())
            return jsonObject.getString("1080");
        if (jsonObject.has("720") && !jsonObject.getString("720").isEmpty())
            return jsonObject.getString("720");
        if (jsonObject.has("480") && !jsonObject.getString("480").isEmpty())
            return jsonObject.getString("480");
        if (jsonObject.has("360") && !jsonObject.getString("360").isEmpty())
            return jsonObject.getString("360");
        if (jsonObject.has("240") && !jsonObject.getString("240").isEmpty())
            return jsonObject.getString("240");
        if (jsonObject.has("180") && !jsonObject.getString("180").isEmpty())
            return jsonObject.getString("180");
        if (jsonObject.has("120") && !jsonObject.getString("120").isEmpty())
            return jsonObject.getString("120");
        if (jsonObject.has("60") && !jsonObject.getString("60").isEmpty())
            return jsonObject.getString("60");
        return "";
    }

    private int getJsonStartIndex(String scripStr) {
        return scripStr.indexOf('\"') - 1;
    }

    private int getJsonEndIndex(String scripStr) {
        return scripStr.indexOf("window.playerV5");
    }
}
