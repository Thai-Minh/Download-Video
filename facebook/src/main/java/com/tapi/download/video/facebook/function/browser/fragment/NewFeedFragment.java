package com.tapi.download.video.facebook.function.browser.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.IWebFragment;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.core.view.RoundedHorizontalProgressBar;
import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.core.OnFaceBookListener;
import com.tapi.download.video.facebook.function.browser.BrowserManager;
import com.tapi.download.video.facebook.function.utils.FaceBookWebView;

public class NewFeedFragment extends BaseFragment implements OnFaceBookListener, View.OnClickListener, IWebFragment {
    private static final String TAG = "NewFeedFragment";
    private Context mContext;
    private RoundedHorizontalProgressBar smoothSeekBar;
    private FaceBookWebView faceBookWebView;
    private RelativeLayout rlLoadingNewFeed;

    private OnWebFragmentListener onWebFragmentListener;
    private BroadcastReceiver broadcastReceiver;

    private String mPrivateLink;

    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            smoothSeekBar.setVisibility(View.GONE);
        }
    };


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.newfeed_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        listenerPrivateLink();
    }

    private void initView(View view) {
        rlLoadingNewFeed = view.findViewById(R.id.fragment_newfeed_loading_rl);
        faceBookWebView = view.findViewById(R.id.fragment_newfeed_webview);
        smoothSeekBar = view.findViewById(R.id.fragment_newfeed_seekbar);
        faceBookWebView.setListener(this);

        if (com.tapi.download.video.facebook.utils.Utils.checkCookiesFb()) {
            rlLoadingNewFeed.setVisibility(View.VISIBLE);
        }
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
                            faceBookWebView.loadVideoPrivate(mPrivateLink);
                            break;
                        case Utils.NO_INTERNET:
                            faceBookWebView.reloadWeb();
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onLoadingCatchUrl() {
        //edit
        if (onWebFragmentListener != null) {
            onWebFragmentListener.onDelayBottomSheet();
        }
    }

    @Override
    public void onWebLoading(int progress) {
        smoothSeekBar.animateProgress(500, smoothSeekBar.getProgress(), progress);
        if (progress != 100)
            smoothSeekBar.setVisibility(View.VISIBLE);
        else {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 500);
        }
    }

    @Override
    public void onVideoLoaded(Video video) {
        if (onWebFragmentListener != null) {
            onWebFragmentListener.onListDownloadChanged(video);
        }
    }

    @Override
    public void onPageFinish(String html) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceBookWebView.clearHistory();
                rlLoadingNewFeed.setVisibility(View.GONE);
                BrowserManager.getInstance().sendPageFinish(html);
                if (mPrivateLink != null && !mPrivateLink.isEmpty())
                    faceBookWebView.loadVideoPrivate(mPrivateLink);
            }
        });

    }


    @Override
    public void onLogoutAccount() {
        BrowserManager.getInstance().sendLogoutPage();
    }

    @Override
    public void onLoadingWeb() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                faceBookWebView.clearHistory();
                rlLoadingNewFeed.setVisibility(View.VISIBLE);
            }
        });

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
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onBackPressed() {
        if (faceBookWebView.canGoBack()) {
            smoothSeekBar.setProgress(0);
            faceBookWebView.goBack();
            return true;
        }
        return false;
    }
}
