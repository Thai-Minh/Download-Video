package com.tapi.download.video.facebook.function.browser.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.listener.IWebFragment;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.core.CustomViewPager;
import com.tapi.download.video.facebook.function.browser.BrowserPagerAdapter;

import java.util.List;

public class FaceBookBrowserFragment extends BaseFragment implements IWebFragment, OnWebFragmentListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {
    private static final String TAG = "FaceBookBrowserFragment";
    private Context mContext;
    private TabLayout mTabLayout;
    private CustomViewPager mViewPager;
    private RelativeLayout rlDownload, rlLoadingBottomSheet;

    private Video video;

    private BrowserPagerAdapter browserPagerAdapter;
    private BroadcastReceiver broadcastReceiver;

    private OnWebFragmentListener onWebFragmentListener;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_browser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initViewPagerTab();
        listenerPrivateLink();
    }

    private void listenerPrivateLink() {
        IntentFilter intentFilter = new IntentFilter(Utils.ACTION_PRIVATE_LINK);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case Utils.ACTION_PRIVATE_LINK:
                            mViewPager.setCurrentItem(0);
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initView(View view) {
        mTabLayout = view.findViewById(R.id.fragment_browser_tablayout);
        mViewPager = view.findViewById(R.id.fragment_browser_viewpager);
        rlDownload = view.findViewById(R.id.fragment_browser_download_rl);
        rlLoadingBottomSheet = view.findViewById(R.id.fragment_browser_loading_bottom_sheet_rl);

        rlDownload.setAlpha(0.7f);
        rlDownload.setClickable(false);
        rlDownload.setOnClickListener(this);

        AppPreferences.INSTANCE.setListenerChange(this);
        checkShowTablayout();

    }

    private void checkShowTablayout() {
        boolean b = com.tapi.download.video.facebook.utils.Utils.checkCookiesFb();
        mTabLayout.setVisibility(b ? View.VISIBLE : View.GONE);
        mViewPager.setPagingEnabled(b);
    }

    private void initViewPagerTab() {
        browserPagerAdapter = new BrowserPagerAdapter(getChildFragmentManager(), getActivity());
        mViewPager.setAdapter(browserPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                rlDownload.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void setWebFragmentListener(OnWebFragmentListener onWebFragmentListener) {
        this.onWebFragmentListener = onWebFragmentListener;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        if (childFragment instanceof IWebFragment) {
            ((IWebFragment) childFragment).setWebFragmentListener(this);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDelayBottomSheet() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                rlLoadingBottomSheet.setVisibility(View.VISIBLE);
                Log.e(TAG, "onDelayBottomSheet: " );
            }
        });
    }

    @Override
    public void onListDownloadChanged(Video video) {
        this.video = video;
        if (onWebFragmentListener != null)
            onWebFragmentListener.onListDownloadChanged(video);
        rlDownload.setAlpha(1f);
        rlLoadingBottomSheet.setVisibility(View.GONE);
        rlDownload.setClickable(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesContains.COOKIE)) {
            checkShowTablayout();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fragment_browser_download_rl) {
            if (onWebFragmentListener != null && video != null)
                onWebFragmentListener.onListDownloadChanged(video);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0);
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

}
