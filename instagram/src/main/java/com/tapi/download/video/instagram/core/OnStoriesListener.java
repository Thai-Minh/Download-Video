package com.tapi.download.video.instagram.core;

import com.tapi.download.video.core.model.StoriesInsta;

import java.util.ArrayList;

public interface OnStoriesListener {
    void onLoadSuccess(ArrayList<StoriesInsta> strories);
}
