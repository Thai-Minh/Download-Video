package com.tapi.downloadsocialvideo.function.main;

import android.content.Context;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.core.listener.OnTutorialListener;

public interface ISocialNetworkController {
    void catchLink(String link);

    RelativeLayout getTutorial(Context context);

    Fragment getWebFragment();

    void catchOnWebFragment(String link);

    void setCatchLinkListener(OnCatchVideoListener listener);

    String getTitleApplication(Context context);
}
