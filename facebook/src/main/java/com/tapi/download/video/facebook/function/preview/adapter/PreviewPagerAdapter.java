package com.tapi.download.video.facebook.function.preview.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tapi.download.video.facebook.adapter.Stories;
import com.tapi.download.video.facebook.function.preview.ui.CommonPreviewFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class PreviewPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<Stories> stories;
    private HashMap<Integer, CommonPreviewFragment> mListFragment = new HashMap<>();

    public PreviewPagerAdapter(@NonNull FragmentManager fm, ArrayList<Stories> stories) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.stories = stories;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        CommonPreviewFragment commonPreviewFragment1 = CommonPreviewFragment.newInstance(stories.get(position));
        mListFragment.put(position, commonPreviewFragment1);
        return commonPreviewFragment1;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
        mListFragment.remove(position);
    }

    public CommonPreviewFragment getFragment(int key) {
        return mListFragment.get(key);
    }

    @Override
    public int getCount() {
        return stories.size();
    }

    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
