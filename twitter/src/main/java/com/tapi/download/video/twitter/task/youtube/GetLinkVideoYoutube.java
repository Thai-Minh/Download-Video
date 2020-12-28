package com.tapi.download.video.twitter.task.youtube;

import android.os.AsyncTask;
import android.util.Log;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.twitter.utils.Utils;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Response;

public class GetLinkVideoYoutube extends AsyncTask<String, Void, Video> {
    private boolean isWaitingForGettingVideo = false;

    private OnCatchVideoListener listener;
    private static final String TAG = "MTHAI";
    private String link;
    private SOService mService;

    public GetLinkVideoYoutube(OnCatchVideoListener listener) {
        this.listener = listener;
    }

    public Video loadAnswers(String url) {
        final ArrayList<DownloadLink> arrayListLinks = new ArrayList<>();
        final Video[] video = {new Video()};

        final String[] videoId = new String[1];
        final String[] imgUser = new String[1];
        final String[] userName = new String[1];
        final String[] title = new String[1];
        final String[] thumbnail = new String[1];
        final long[] duration = new long[1];

        isWaitingForGettingVideo = true;
        mService.getAnswers(url).enqueue(new retrofit2.Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                try {
                    Log.e(TAG, "success: ");
                } finally {
                    isWaitingForGettingVideo = false;
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "failure: ");

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

        mService = Utils.getSOService();
        loadAnswers(link);

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
