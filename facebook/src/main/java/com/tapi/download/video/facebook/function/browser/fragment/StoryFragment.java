package com.tapi.download.video.facebook.function.browser.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.adapter.IStroriesListener;
import com.tapi.download.video.facebook.adapter.SpacesItemDecoration;
import com.tapi.download.video.facebook.adapter.Stories;
import com.tapi.download.video.facebook.adapter.StoriesAdapter;
import com.tapi.download.video.facebook.core.OnStoriesListener;
import com.tapi.download.video.facebook.core.StoriesFaceBookController;
import com.tapi.download.video.facebook.function.browser.BrowserManager;
import com.tapi.download.video.facebook.function.browser.OnBrowserPageFinishListener;
import com.tapi.download.video.facebook.function.preview.ui.PreviewActivity;
import com.tapi.download.video.facebook.utils.Utils;

import java.util.ArrayList;

public class StoryFragment extends BaseFragment implements IStroriesListener, OnBrowserPageFinishListener,
        OnStoriesListener {
    public static final int REQUEST_PREVIEW = 222;
    public static final String KEY_STORIES_PREVIEW = "stories";
    private static final String TAG = "StoryFragment";
    private Context mContext;
    private LinearLayout llLoading, llEmpty;
    private RecyclerView recyclerView;
    private RelativeLayout rlLoadMore;

    private StoriesAdapter storiesAdapter;
    private ArrayList<Stories> listStory = new ArrayList<>();
    private String html;
    private boolean isScroll, isLoading;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BrowserManager.getInstance().setPageFinishListeners(this);
        StoriesFaceBookController.getInstance().setListener(this);
        return inflater.inflate(R.layout.story_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initRecyclerView();
        if (html != null && !html.isEmpty())
            StoriesFaceBookController.getInstance().loadDataFirstTime(html);
    }

    private void initRecyclerView() {
        storiesAdapter = new StoriesAdapter(getContext(), listStory);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3, LinearLayoutManager.VERTICAL, false);
        recyclerView.addItemDecoration(new SpacesItemDecoration(Utils.convertDpToPixel(14, mContext)));
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(storiesAdapter);
        storiesAdapter.setListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE
                        && !isScroll && isLoading && !listStory.isEmpty()) {
                    Stories strories = listStory.get(listStory.size() - 1);
                    StoriesFaceBookController.getInstance().getDataLoadMore(strories.getEndCursor(), strories.getTraySessionId());
                    showLoadMoreProgress(true);
                    isScroll = true;
                }
            }
        });
    }

    private void showLoadMoreProgress(boolean isShow) {
        rlLoadMore.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.fragment_story_recycler);
        rlLoadMore = view.findViewById(R.id.fragment_story_loadmore_rl);
        llLoading = view.findViewById(R.id.fragment_story_loading_ll);
        llEmpty = view.findViewById(R.id.fragment_story_empty_ll);
    }

    @Override
    public void onClickItem(int pos) {
        StoriesFaceBookController.getInstance().setPosStorySelect(pos);
        Intent intent = new Intent(mContext, PreviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivityForResult(intent, REQUEST_PREVIEW);
    }

    @Override
    public void onBrowserPageFinish(String html) {
        this.html = html;
        if (isAdded()) {
            listStory.clear();
            StoriesFaceBookController.getInstance().loadDataFirstTime(html);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLogoutPage() {
        listStory.clear();
        storiesAdapter.addData(listStory);
        StoriesFaceBookController.getInstance().clearAllListData();
        llLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadSuccess(ArrayList<Stories> strories) {
        if (isAdded()) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (strories != null && !strories.isEmpty()) {
                            llLoading.setVisibility(View.GONE);
                            llEmpty.setVisibility(View.GONE);
                            storiesAdapter.addData(strories);
                        } else {
                            if (storiesAdapter.getItemCount() == 0)
                                llEmpty.setVisibility(View.VISIBLE);
                        }
                        showLoadMoreProgress(false);
                        isScroll = false;
                        isLoading = true;
                    }
                });
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

}
