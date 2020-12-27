package com.tapi.download.video.instagram.function.browser;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.listener.IWebFragment;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.instagram.R;
import com.tapi.download.video.instagram.core.InstagramListener;
import com.tapi.download.video.instagram.core.webview.InstagramWebview;
import com.tapi.download.video.instagram.core.webview.InstagramWebviewClient;

import java.util.List;

public class NewFeedFragment extends BaseFragment implements View.OnClickListener, InstagramListener, IWebFragment, InstagramWebview.OnLoadingNewFeedListener {

    private static final String TAG = "NewFeedFragment";

    private InstagramWebview instaWebView;
    private RelativeLayout rlLoadingNewFeed;
    private OnWebFragmentListener onWebFragmentListener;
    private BroadcastReceiver broadcastReceiver;
    private Context mContext;

    private Video video;
    private String mPrivateLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insta_newfeed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        listenerPrivateLink();

        // check cookie for loading
        String cookie = AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "");
        if (cookie != null && (cookie.contains("c_user") || cookie.contains("ds_user_id"))) {
            Log.e(TAG, "onViewCreated: ");
            rlLoadingNewFeed.setVisibility(View.VISIBLE);
        }
    }

    private void initView(View view) {
        rlLoadingNewFeed = view.findViewById(R.id.fragment_newfeed_loading_rl);
        instaWebView = view.findViewById(R.id.fragment_newfeed);
        instaWebView.setListener(this);
        instaWebView.setOnLoadingNewFeedListener(this);
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
                            instaWebView.loadVideoPrivate(mPrivateLink);
                            break;
                        case Utils.NO_INTERNET:
                            instaWebView.reloadWeb();
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onLoadingUrl() {
        if (onWebFragmentListener != null) {
            onWebFragmentListener.onDelayBottomSheet();
        }
    }

    @Override
    public void onVideoLoaded(final Video video) {
        this.video = video;
        if (onWebFragmentListener != null)
            onWebFragmentListener.onListDownloadChanged(video);
    }

    @Override
    public void onPageFinish(final String html) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                rlLoadingNewFeed.setVisibility(View.GONE);
                BrowserManager.getInstance().sendPageFinish(html);
            }
        });
    }

    @Override
    public void onLogoutAccount() {
        BrowserManager.getInstance().sendLogoutPage();
        if (mPrivateLink != null && !mPrivateLink.isEmpty())
            instaWebView.loadVideoPrivate(mPrivateLink);
    }

    @Override
    public void OnLoading() {
        rlLoadingNewFeed.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

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
    public boolean onBackPressed() {
        if (instaWebView.canGoBack()) {
            instaWebView.goBack();
            return true;
        }
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
    public void onPause() {
        super.onPause();
        AppPreferences.INSTANCE.putString("URL", "");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
        AppPreferences.INSTANCE.putString("URL", "");
    }
}
