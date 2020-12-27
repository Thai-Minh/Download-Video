package com.tapi.download.video.instagram.function.browser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.listener.IWebFragment;
import com.tapi.download.video.core.listener.OnWebFragmentListener;
import com.tapi.download.video.instagram.core.OnStoriesListener;
import com.tapi.download.video.instagram.R;
import com.tapi.download.video.instagram.function.browser.adapter.StoryAdapter;
import com.tapi.download.video.core.model.StoriesInsta;
import com.tapi.download.video.instagram.core.StoriesInstaController;
import com.tapi.download.video.instagram.function.preview.ui.PreviewStoriesActivity;

import java.util.ArrayList;
import java.util.List;

public class StoriesFragment extends BaseFragment implements StoryAdapter.OnCallBack, OnBrowserPageFinishListener, IWebFragment, OnStoriesListener {

    private static String TAG = StoriesFragment.class.getSimpleName();

    public static final int REQUEST_PREVIEW = 333;

    private Context mContext;
    private LinearLayout llLoading, llEmpty;
    private RecyclerView recycleViewStories;
    private String html;
    private StoryAdapter storyAdapter;
    private ArrayList<StoriesInsta> arrStories = new ArrayList<>();
    private boolean isScroll, isLoading;
    private OnWebFragmentListener onWebFragmentListener;

    private String base_url = "https://www.instagram.com/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BrowserManager.getInstance().setPageFinishListeners(this);
        StoriesInstaController.getInstance().setListener(this);
        return inflater.inflate(R.layout.fragment_insta_stories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUI(view);
        initRecycleView();

        if (html != null && !html.isEmpty()) {
            StoriesInstaController.getInstance().loadDataFirstTime(html);
            Log.e(TAG, "onViewCreated: " + html);
        }

    }

    private void setUI(View view) {
        recycleViewStories = view.findViewById(R.id.fragment_stories_recycler);
        llLoading = view.findViewById(R.id.fragment_stories_loading_ll);
        llEmpty = view.findViewById(R.id.fragment_stories_empty_ll);
    }

    private void initRecycleView() {
        int[] widthHeight = getScreenSize();
        int width = (widthHeight[0] - dpToPx(16)) / 2;
        storyAdapter = new StoryAdapter(this, getContext(), arrStories, width);
        recycleViewStories.setHasFixedSize(true);
        recycleViewStories.setLayoutManager(new GridLayoutManager(getContext(), 3, GridLayoutManager.VERTICAL, false));
        recycleViewStories.setAdapter(storyAdapter);

        recycleViewStories.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE && !isScroll && isLoading) {

                    isScroll = true;
                }
            }
        });
    }

    private int[] getScreenSize() {
        int[] sizes = {0, 0};
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        sizes[0] = displayMetrics.widthPixels;
        sizes[1] = displayMetrics.heightPixels;
        return sizes;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getActivity().getResources().getDisplayMetrics());
    }

    @Override
    public void onItemClicked(int position) {

        StoriesInstaController.getInstance().setPositionStorySelect(position);
        Intent intent = new Intent(mContext, PreviewStoriesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, REQUEST_PREVIEW);
    }

    @Override
    public void setWebFragmentListener(OnWebFragmentListener onWebFragmentListener) {
        this.onWebFragmentListener = onWebFragmentListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onBrowserPageFinish(String html) {
        this.html = html;
        if (isAdded()) {
            StoriesInstaController.getInstance().loadDataFirstTime(html);
        }
    }

    @Override
    public void onLogoutPage() {
        arrStories.clear();
        storyAdapter.addData(arrStories);
    }

    @Override
    public void onLoadSuccess(final ArrayList<StoriesInsta> listStories) {
        if (isAdded()) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (listStories != null && !listStories.isEmpty()) {
                            llLoading.setVisibility(View.GONE);
                            storyAdapter.addData(listStories);
                            llEmpty.setVisibility(View.GONE);
                            recycleViewStories.setVisibility(View.VISIBLE);
                            Log.e(TAG, "onLoadSuccess: 1");
                        } else {
                            llEmpty.setVisibility(View.VISIBLE);
                            recycleViewStories.setVisibility(View.GONE);
                            Log.e(TAG, "onLoadSuccess: 2");
                        }

                        isScroll = false;
                        isLoading = true;
                    }
                });
            }
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
