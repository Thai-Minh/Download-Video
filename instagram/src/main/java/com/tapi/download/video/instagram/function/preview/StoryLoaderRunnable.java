package com.tapi.download.video.instagram.function.preview;

import com.tapi.download.video.instagram.core.LoadDataStoriesDetail;
import com.tapi.download.video.instagram.core.StoriesInstaController;

public class StoryLoaderRunnable implements Runnable {
    private final String reelsID;
    private final LoadDataStoriesDetail.OnLoadDataStoryDetailListener mListener;

    public StoryLoaderRunnable(String reelsID, LoadDataStoriesDetail.OnLoadDataStoryDetailListener listener) {
        this.reelsID = reelsID;
        mListener = listener;
    }

    @Override
    public void run() {
        StoriesInstaController.getInstance().loadDataStoryDetail(reelsID, mListener);
    }
}
