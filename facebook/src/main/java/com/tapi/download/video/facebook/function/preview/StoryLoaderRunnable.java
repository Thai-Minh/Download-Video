package com.tapi.download.video.facebook.function.preview;

import com.tapi.download.video.facebook.core.StoriesFaceBookController;
import com.tapi.download.video.facebook.core.StoriesFunction;

public class StoryLoaderRunnable implements Runnable {
    private final String mLink;
    private final String mDataBucketId;
    private final StoriesFunction.OnLoadDataStoryDetailListener mListener;

    public StoryLoaderRunnable(String link, String dataBucketId, StoriesFunction.OnLoadDataStoryDetailListener listener) {
        mLink = link;
        mDataBucketId = dataBucketId;
        mListener = listener;
    }

    @Override
    public void run() {
        StoriesFaceBookController.getInstance().loadDataStoryDetail(mLink, mDataBucketId, mListener);
    }
}
