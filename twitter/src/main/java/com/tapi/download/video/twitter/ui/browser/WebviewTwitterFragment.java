package com.tapi.download.video.twitter.ui.browser;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.IWebFragment;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.twitter.listener.TwitterListener;
import com.tapi.download.video.twitter.R;
import com.tapi.download.video.twitter.service.ClipBoardTwitterService;
import com.tapi.download.video.twitter.utils.ConstantTwitter;
import com.tapi.download.video.twitter.webview.TwitterWebview;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.util.List;

public class WebviewTwitterFragment extends BaseFragment implements View.OnClickListener, TwitterListener, IWebFragment, OnCatchVideoListener {

    private static String TAG = WebviewTwitterFragment.class.getSimpleName();

    private TwitterWebview twitterWebView;
    private RelativeLayout rlDownload, rlLoadingBottomSheet;

    private OnWebFragmentListener onWebFragmentListener;

    private Video video;
    private boolean isSwap;
    private BroadcastReceiver broadcastReceiver;
    private String mPrivateLink;
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_twitter_browser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        // clipboard listener
        Intent svc = new Intent(getActivity(), ClipBoardTwitterService.class);
        getActivity().startService(svc);

        setTwitterConfig();
        listenerPrivateLink();
    }

    @Override
    public void onResume() {
        super.onResume();
        initReceiver();
    }

    @Override
    public void onPause() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
        super.onPause();
    }

    private void initView(View view) {
        twitterWebView = view.findViewById(R.id.fragment_newfeed);
        twitterWebView.setListener(this);
        rlDownload = view.findViewById(R.id.fragment_newfeed_download_rl);
        rlDownload.setOnClickListener(this);
        rlLoadingBottomSheet = view.findViewById(R.id.fragment_newfeed_loading_bottom_sheet_rl);
        rlDownload.setAlpha(0.6f);

    }

    private void listenerPrivateLink() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.ACTION_PRIVATE_LINK);
        intentFilter.addAction(Utils.NO_INTERNET);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case Utils.ACTION_PRIVATE_LINK:
                            mPrivateLink = intent.getStringExtra(Utils.PRIVATE_LINK);
                            twitterWebView.loadVideoPrivate(mPrivateLink);
                            break;
                        case Utils.NO_INTERNET:
                            twitterWebView.reloadWeb();
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, intentFilter);
    }

    public void setTwitterConfig() {
        TwitterConfig config = new TwitterConfig.Builder(getContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(ConstantTwitter.TWITTER_KEY, ConstantTwitter.TWITTER_SECRET))
                .debug(true)
                .build();
        Twitter.initialize(config);
    }

    @Override
    public void onLoadingUrl() {

    }

    @Override
    public void onVideoLoaded(Video video) {
        this.video = video;
    }

    @Override
    public void onPageFinish(String html) {
        if (mPrivateLink != null && !mPrivateLink.isEmpty())
            twitterWebView.loadVideoPrivate(mPrivateLink);
    }

    @Override
    public void onLogoutAccount() {
        BrowserManager.getInstance().sendLogoutPage();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fragment_newfeed_download_rl && onWebFragmentListener != null && video != null) {
            onWebFragmentListener.onListDownloadChanged(video);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void setWebFragmentListener(OnWebFragmentListener onWebFragmentListener) {
        this.onWebFragmentListener = onWebFragmentListener;
    }

    @Override
    public void onStartCatch() {

    }

    @Override
    public void onCatchedLink(Video video) {
        for (DownloadLink video1 : video.getLinks()) {
            Log.e(TAG, "onVideoLoaded: " + video1.getLink());
        }
    }

    @Override
    public void onPrivateLink(String link) {

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ConstantTwitter.VIDEO_LOADED)) {
                video = (Video) intent.getSerializableExtra(ConstantTwitter.VIDEO);
                onWebFragmentListener.onListDownloadChanged(video);

                Log.e(TAG, "onReceive: " + video.getThumbnail());
                rlDownload.setAlpha(1.0f);

                rlLoadingBottomSheet.setVisibility(View.GONE);

            }
            else if (intent.getAction().equals(ConstantTwitter.VIDEO_HANDLE)) {

                rlLoadingBottomSheet.setVisibility(View.VISIBLE);
            }
        }
    };

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConstantTwitter.VIDEO_LOADED);
        filter.addAction(ConstantTwitter.VIDEO_HANDLE);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public boolean onBackPressed() {
        return onBackPress();
    }

    private boolean onBackPress() {
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments.size() != 0) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseFragment) {
                    if (((BaseFragment) fragment).onBackPressed())
                        return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
    }

}
