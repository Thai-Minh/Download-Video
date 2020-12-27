package com.tapi.downloadsocialvideo.function.main;

import android.content.Context;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.tapi.download.video.core.listener.ICatch;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.dailymotion.DailyCatchVideo;
import com.tapi.download.video.dailymotion.ui.browser.WebviewDailyFragment;
import com.tapi.download.video.dailymotion.ui.home.DailyTutorial;
import com.tapi.download.video.facebook.core.FacebookCatchVideo;
import com.tapi.download.video.facebook.function.browser.fragment.FaceBookBrowserFragment;
import com.tapi.download.video.facebook.function.home.FaceBookTutorial;
import com.tapi.download.video.instagram.core.InstagramCatchVideo;
import com.tapi.download.video.instagram.function.browser.InstaBrowserFragment;
import com.tapi.download.video.instagram.function.home.InstaTutorial;
import com.tapi.download.video.twitter.task.TwitterCatchVideo;
import com.tapi.download.video.twitter.ui.browser.WebviewTwitterFragment;
import com.tapi.download.video.twitter.ui.home.TwitterTutorial;
import com.tapi.downloadsocialvideo.R;

public class SocialManager implements ISocialNetworkController {
    private final static StateApp mState = StateApp.TWITTER;

    public static SocialManager instance;

    private OnCatchVideoListener listener;

    private ICatch iCatch;

    private FaceBookBrowserFragment fbWebFragment;
    private WebviewTwitterFragment webviewTwitterFragment;
    private InstaBrowserFragment instaBrowserFragment;
    private WebviewDailyFragment webviewDailyFragment;

    public SocialManager() {
        switch (mState) {
            case FACE_BOOK:
                fbWebFragment = new FaceBookBrowserFragment();
                iCatch = FacebookCatchVideo.getInstance();
                break;
            case INSTAGRAM:
                instaBrowserFragment = new InstaBrowserFragment();
                iCatch = InstagramCatchVideo.getInstance();
                break;
            case TWITTER:
                webviewTwitterFragment = new WebviewTwitterFragment();
                iCatch = TwitterCatchVideo.getInstance();
                break;
            case DAILYMOTION:
                webviewDailyFragment = new WebviewDailyFragment();
                iCatch = DailyCatchVideo.getInstance();
                break;
        }
    }

    public static SocialManager getInstance() {
        if (instance == null)
            instance = new SocialManager();
        return instance;
    }

    public StateApp getmState() {
        return mState;
    }

    @Override
    public void catchLink(String link) {
        if (iCatch != null)
            iCatch.getVideoDownloadLink(link, listener);
    }

    @Override
    public RelativeLayout getTutorial(Context context) {
        switch (mState) {
            case FACE_BOOK:
                return new FaceBookTutorial(context);
            case INSTAGRAM:
                return new InstaTutorial(context);
            case TWITTER:
                return new TwitterTutorial(context);
            case DAILYMOTION:
                return new DailyTutorial(context);
        }
        return null;
    }

    @Override
    public Fragment getWebFragment() {
        switch (mState) {
            case FACE_BOOK:
                return fbWebFragment;
            case INSTAGRAM:
                return instaBrowserFragment;
            case TWITTER:
                return webviewTwitterFragment;
            case DAILYMOTION:
                return webviewDailyFragment;
        }
        return null;
    }

    @Override
    public void catchOnWebFragment(String link) {
    }

    @Override
    public String getTitleApplication(Context context) {
        switch (mState) {
            case FACE_BOOK:
                return context.getString(R.string.main_title_facebook);
            case INSTAGRAM:
                return context.getString(R.string.main_title_insta);
            case DAILYMOTION:
                return context.getString(R.string.main_title_daily);
            case TWITTER:
                return context.getString(R.string.main_title_twiter);
        }
        return null;
    }

    @Override
    public void setCatchLinkListener(OnCatchVideoListener listener) {
        this.listener = listener;
    }

    public void releaseInstance() {
        webviewDailyFragment = null;
        instaBrowserFragment = null;
        fbWebFragment = null;
        webviewTwitterFragment = null;
        instance = null;
    }

    public String getNameApp(Context context) {
        switch (mState) {
            case DAILYMOTION:
                return context.getString(R.string.welcome_dailymotion_app);
            case TWITTER:
                return context.getString(R.string.welcome_twitter_app);
            case INSTAGRAM:
                return context.getString(R.string.welcome_instagram_app);
            case FACE_BOOK:
                return context.getString(R.string.welcome_facebook_app);
        }
        return null;
    }

    public enum StateApp {
        FACE_BOOK, INSTAGRAM, DAILYMOTION, TWITTER
    }

    public boolean checkFormLink(String link) {
        switch (mState) {
            case DAILYMOTION:
                return link.contains("https://www.dailymotion.com");
            case TWITTER:
//                return link.contains("https://twitter.com");
                return link.contains("https://twitter.com") || link.contains("https://youtu.be/") || link.contains("https://www.youtube.com/watch?v=");
            case INSTAGRAM:
                return link.contains("https://www.instagram.com");
            case FACE_BOOK:
                return link.contains("https://www.facebook.com") || link.contains("https://m.facebook.com");
        }
        return true;
    }

    public String parseVideoId(String link) {
        switch (mState) {
            case DAILYMOTION:
                return Utils.parseVideoIdDaily(link);
            case TWITTER:
                return null;
            case INSTAGRAM:
                return null;
            case FACE_BOOK:
                return Utils.getVideoId(link);
        }
        return null;
    }

}
