package com.tapi.download.video.facebook.core;

import com.tapi.download.video.facebook.adapter.Stories;

import java.util.ArrayList;

public interface OnStoriesListener {
    void onLoadSuccess(ArrayList<Stories> strories);
}
