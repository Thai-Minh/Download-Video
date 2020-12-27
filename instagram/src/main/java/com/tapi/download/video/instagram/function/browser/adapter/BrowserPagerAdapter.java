package com.tapi.download.video.instagram.function.browser.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tapi.download.video.instagram.R;
import com.tapi.download.video.instagram.function.browser.NewFeedFragment;
import com.tapi.download.video.instagram.function.browser.StoriesFragment;

public class BrowserPagerAdapter extends FragmentStatePagerAdapter {

    private static final int PAGER_COUNT = 2;
    private Context mContext;

    public BrowserPagerAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            default:
                fragment = new NewFeedFragment();
                break;
            case 1:
                fragment = new StoriesFragment();
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return PAGER_COUNT;
    }

    public CharSequence getPageTitle(int pos) {
        String title;
        switch (pos) {
            default:
                title = mContext.getResources().getString(R.string.insta_browser_new_feed);
                break;
            case 1:
                title = mContext.getResources().getString(R.string.insta_browser_story);
                break;
        }
        return title;
    }
}
