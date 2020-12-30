package com.tapi.download.video.twitter.task.youtube;

import android.os.AsyncTask;
import android.util.Log;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.twitter.utils.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public class GetLinkVideoYoutube extends AsyncTask<String, Void, Video> {

    private OnCatchVideoListener listener;
    private static final String TAG = "MTHAI";
    private String link;
    private SOService mService;

    public GetLinkVideoYoutube(OnCatchVideoListener listener) {
        this.listener = listener;
    }

    public Video loadData(String values) {
        ArrayList<DownloadLink> arrayListLinks = new ArrayList<>();
        Video video = new Video();

        String videoId = "";
        String imgUser = "";
        String userName = "";
        String title = "";
        String thumbnail = "";
        int duration = 0;

        String baseUrl = "http://10.134.115.220:5000/";
        String key = "youtube?url=";
        try {
            URL url = new URL(baseUrl + key + values);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "text/plain");
            conn.setRequestProperty("charset", "utf-8");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Log.e(TAG, "loadAnswers: " + response.toString());

            //convert string to json
            JSONObject json = new JSONObject(String.valueOf(response));
            String link720 = json.getJSONObject("Download").getJSONObject("Video").getString("720p");
//            String link480 = json.getJSONObject("Download").getJSONObject("Video").getString("480p");
            String link360 = json.getJSONObject("Download").getJSONObject("Video").getString("360p");

            Log.e(TAG, "loadAnswers: 2" + response.toString());
            arrayListLinks.add(new DownloadLink(link720, 720));
//            arrayListLinks.add(new DownloadLink(link480, 480));
            arrayListLinks.add(new DownloadLink(link360, 360));

            videoId = json.getString("ID");
            title = json.getString("Title");
            thumbnail = json.getString("Thumbnail URL");

            video = new Video(videoId, imgUser, userName, title, thumbnail, duration, arrayListLinks);

            Log.e(TAG, "videoId: " + videoId);
            Log.e(TAG, "title: " + title);
            Log.e(TAG, "thumbnail: " + thumbnail);
            Log.e(TAG, "arrayListLinks: " + arrayListLinks.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return video;
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

        Log.e(TAG, "doInBackground: " + link);

        return loadData(link);
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
