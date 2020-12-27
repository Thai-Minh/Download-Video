package com.tapi.download.video.core.listener;

import com.tapi.download.video.core.Video;

public interface OnCatchVideoListener {
    void onStartCatch();

    void onCatchedLink(Video video);

    void onPrivateLink(String link);
}
