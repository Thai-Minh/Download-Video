package com.tapi.download.video.core.listener;

import com.tapi.download.video.core.Video;

public interface OnWebFragmentListener {
    void onDelayBottomSheet();

    void onListDownloadChanged(Video video);
}
