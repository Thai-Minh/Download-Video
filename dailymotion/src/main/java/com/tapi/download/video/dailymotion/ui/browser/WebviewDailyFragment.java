package com.tapi.download.video.dailymotion.ui.browser;

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
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.dailymotion.listener.DailyListener;
import com.tapi.download.video.dailymotion.R;
import com.tapi.download.video.dailymotion.webview.DailyWebview;

import java.util.List;

public class WebviewDailyFragment extends BaseFragment implements View.OnClickListener, DailyListener, IWebFragment, DailyWebview.OnLoadingNewFeedListener {

    private static String TAG = WebviewDailyFragment.class.getSimpleName();

    private DailyWebview dailyWebview;
    private RelativeLayout rlDownload, rlLoadingNewFeed, rlLoadingBottomSheet;
    private OnWebFragmentListener onWebFragmentListener;
    private Video video;
    private Context mContext;
    private BroadcastReceiver broadcastReceiver;

    private Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_daily_browser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        listenerInternet();

        String cookie = AppPreferences.INSTANCE.getString(PreferencesContains.COOKIE, "");
        if (cookie != null && (cookie.contains("client_token"))) {
            Log.e(TAG, "onViewCreated: ");
            rlLoadingNewFeed.setVisibility(View.VISIBLE);
        }
    }

    private void initView(View view) {
        dailyWebview = view.findViewById(R.id.fragment_newfeed);
        dailyWebview.setListener(this);
        dailyWebview.setOnLoadingNewFeedListener(this);
        rlDownload = view.findViewById(R.id.fragment_newfeed_download_rl);
        rlDownload.setOnClickListener(this);
        rlLoadingNewFeed = view.findViewById(R.id.fragment_newfeed_loading_rl);
        rlLoadingBottomSheet = view.findViewById(R.id.fragment_newfeed_loading_bottom_sheet_rl);
        rlDownload.setAlpha(0.6f);
    }

    private void listenerInternet() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.NO_INTERNET);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case Utils.NO_INTERNET:
                            dailyWebview.reloadWeb();
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onLoadingUrl() {
        rlDownload.setAlpha(0.6f);
        handler.post(new Runnable() {
            @Override
            public void run() {
                rlLoadingBottomSheet.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onVideoLoaded(final Video video) {
        this.video = video;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (video != null) {
                    onWebFragmentListener.onListDownloadChanged(video);
                    rlDownload.setAlpha(1.0f);
                    rlLoadingBottomSheet.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), "Get video failure", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onPageFinish(String html) {
        Log.e(TAG, "run: ");
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                rlLoadingNewFeed.setVisibility(View.GONE);
                dailyWebview.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fragment_newfeed_download_rl && onWebFragmentListener != null) {
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
    public boolean onBackPressed() {
        if (dailyWebview.canGoBack()) {
            dailyWebview.goBack();
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
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
        AppPreferences.INSTANCE.putString(com.tapi.download.video.dailymotion.util.Utils.URL, "");
    }

    @Override
    public void OnLoading() {
        rlLoadingNewFeed.setVisibility(View.VISIBLE);
    }
}
