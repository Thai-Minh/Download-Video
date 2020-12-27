package com.tapi.download.video.core.task;

import android.os.AsyncTask;

import com.tapi.download.video.core.utils.Utils;

public class GetSizeLink extends AsyncTask<String, Void, Integer> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... strings) {
        return Utils.getVideoSize(strings[0]);
    }

    @Override
    protected void onPostExecute(Integer size) {
        super.onPostExecute(size);
    }
}
