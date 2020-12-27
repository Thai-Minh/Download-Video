package com.tapi.download.video.instagram.function.browser;

import android.content.Context;
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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.config.AppPreferences;
import com.tapi.download.video.core.config.PreferencesContains;
import com.tapi.download.video.core.listener.IWebFragment;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.instagram.R;
import com.tapi.download.video.instagram.core.CustomViewPager;
import com.tapi.download.video.instagram.function.browser.adapter.BrowserPagerAdapter;

import java.util.List;

public class InstaBrowserFragment extends BaseFragment implements IWebFragment, OnWebFragmentListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private static final String TAG = "InstaBrowserFragment";

    private BrowserPagerAdapter browserPagerAdapter;
    private TabLayout mTabLayout;
    private CustomViewPager mViewPager;
    private RelativeLayout rlDownload, rlLoadingBottomSheet;

    private Video video;
    private OnWebFragmentListener onWebFragmentListener;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_insta_browser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initViewPagerTab();
    }

    private void initView(View view) {
        mTabLayout = view.findViewById(R.id.fragment_browser_insta_tablayout);
        mViewPager = view.findViewById(R.id.fragment_browser_insta_viewpager);
        rlDownload = view.findViewById(R.id.fragment_browser_insta_download_rl);
        rlLoadingBottomSheet = view.findViewById(R.id.fragment_browser_insta_loading_bottom_sheet_rl);

        rlDownload.setAlpha(0.7f);
        rlDownload.setClickable(false);
        rlDownload.setOnClickListener(this);

        AppPreferences.INSTANCE.setListenerChange(this);
        checkShowTablayout(PreferencesContains.COOKIE);
    }

    private void checkShowTablayout(String key) {
        String cookies = AppPreferences.INSTANCE.getString(key, "");
        boolean b = cookies != null && !cookies.isEmpty() && cookies.contains("ds_user_id");
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
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesContains.COOKIE)) {
            checkShowTablayout(key);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fragment_browser_insta_download_rl) {
            if (onWebFragmentListener != null && video != null)
                onWebFragmentListener.onListDownloadChanged(video);
        }
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
}
