package com.tapi.download.video.dailymotion;

import com.tapi.download.video.core.listener.ICatch;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.dailymotion.util.Utils;

public class DailyCatchVideo implements ICatch {
    private static DailyCatchVideo instance;

    public static DailyCatchVideo getInstance() {
        if (instance == null)
            instance = new DailyCatchVideo();
        return instance;
    }

    @Override
    public void getVideoDownloadLink(String viewLink, OnCatchVideoListener onCatchVideoListener) {
        GetVideoDownloadTask getVideoDownloadTask = new GetVideoDownloadTask(onCatchVideoListener);
        Utils.executeAsyncTask(getVideoDownloadTask, viewLink);
    }
}
