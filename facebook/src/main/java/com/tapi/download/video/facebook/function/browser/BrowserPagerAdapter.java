package com.tapi.download.video.facebook.function.browser;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.function.browser.fragment.NewFeedFragment;
import com.tapi.download.video.facebook.function.browser.fragment.StoryFragment;

public class BrowserPagerAdapter extends FragmentStatePagerAdapter {
    private static final int PAGE_COUNT = 2;
    private Context mContext;

    public BrowserPagerAdapter(@NonNull FragmentManager fm, Context context) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = context;
    }


    @Override
    public Fragment getItem(int pos) {
        Fragment fragment;
        switch (pos) {
            default:
                fragment = new NewFeedFragment();
                break;
            case 1:
                fragment = new StoryFragment();
                break;

        }
        return fragment;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    public CharSequence getPageTitle(int pos) {
        String title;
        switch (pos) {
            default:
                title = mContext.getResources().getString(R.string.facebook_browser_new_feed);
                break;
            case 1:
                title = mContext.getResources().getString(R.string.facebook_browser_story);
                break;
        }
        return title;
    }
}
