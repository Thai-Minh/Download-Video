package com.tapi.downloadsocialvideo.function.main.adapter;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tapi.downloadsocialvideo.function.market.MarketAdFragment;
import com.tapi.downloadsocialvideo.function.downloader.ui.DownloadFragment;
import com.tapi.downloadsocialvideo.function.main.HomeFragment;
import com.tapi.downloadsocialvideo.function.main.SocialManager;

public class MainPageAdapter extends FragmentPagerAdapter {
    private static final int PAGE_COUNT = 4;

    public MainPageAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 1:
                return new DownloadFragment();
            case 2:
                return SocialManager.getInstance().getWebFragment();
            case 3:
                return new MarketAdFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public void restoreState(final Parcelable state, final ClassLoader loader) {
        try {
            super.restoreState(state, loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}
