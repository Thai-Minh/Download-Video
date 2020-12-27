package com.tapi.download.video.instagram.function.preview.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.tapi.download.video.core.BaseActivity;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.model.StoriesInsta;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.core.view.QualityBottomSheetFragment;
import com.tapi.download.video.instagram.R;
import com.tapi.download.video.instagram.core.LoadDataStoriesDetail;
import com.tapi.download.video.instagram.core.OnStoriesListener;
import com.tapi.download.video.instagram.core.StoriesInstaController;
import com.tapi.download.video.instagram.function.preview.OnSwipeTouchListener;
import com.tapi.download.video.instagram.function.preview.StoriesViewPager;
import com.tapi.download.video.instagram.function.preview.StoryLoaderRunnable;
import com.tapi.download.video.instagram.function.preview.adapter.PreviewPagerAdapter;
import com.tapi.download.video.instagram.function.preview.listener.ICommonFragment;
import com.tapi.download.video.instagram.function.preview.listener.OnCommonFragmentListener;
import com.tapi.downloader.database.elements.Task;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PreviewStoriesActivity extends BaseActivity implements OnStoriesListener, LoadDataStoriesDetail.OnLoadDataStoryDetailListener, View.OnClickListener,
        OnSwipeTouchListener.OnViewTouchListener, OnCommonFragmentListener {

    private static final String TAG = PreviewStoriesActivity.class.getSimpleName();
    private int[] BIND_CLICK = {R.id.preview_activity_play_controller_download_rl, R.id.activity_preview_close_imv};

    protected Handler mBackgroundHandler;
    private StoriesViewPager mViewPager;
    private PreviewPagerAdapter pagerAdapter;
    private RelativeLayout rlLoading, rlDownload;

    private StoryLoaderRunnable mLastStoryLoader = null;
    private Handler mMainThread = new Handler(Looper.getMainLooper());
    private StoriesInsta stories;
    private Video video;
    private ArrayList<StoriesInsta> mListStories = new ArrayList<>();

    private BroadcastReceiver broadcastReceiver;
    private ArrayList<Task> mListTask;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_insta_preview;
    }

    @Override
    protected void findViewById() {
        mViewPager = findViewById(R.id.activity_preview_viewpager);
        rlLoading = findViewById(R.id.activity_preview_viewpager_loading_rl);
        findViewById(R.id.activity_preview_close_imv);
        rlDownload = findViewById(R.id.preview_activity_play_controller_download_rl);
    }

    @Override
    protected void onCreateInit(@Nullable Bundle savedInstanceState) {

        super.onCreateInit(savedInstanceState);

        bindClicks(this, BIND_CLICK);
        StoriesInstaController.getInstance().setListener(this);

        getData();
        listenerCheckId();
        initViewPager();

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (stories != null) {
            StoriesInstaController.getInstance().loadDataStoryDetail(stories.getReelsID(), this);
        }

    }

    @Override
    public void onLoadSuccess(final StoriesInsta stories) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (stories == null) {
                    finish();
                } else {
                    getData();
                    setDataStories(stories);
                    initViewPager();
                    Utils.animationChangeAlpha(rlLoading, 1f, 0f, false);
                }
            }
        });
    }

    private void initViewPager() {
        pagerAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), mListStories);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                onHideDownLoadBt(false);
                StoriesInstaController.getInstance().setPositionStorySelect(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setCurrentItem(StoriesInstaController.getInstance().getPosStorySelect());
        mViewPager.setListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.activity_preview_close_imv) {
            finish();
        } else if (id == R.id.preview_activity_play_controller_download_rl) {
            CommonPreviewFragment instanceCommonFrag = getInstanceCommonFrag();
            if (instanceCommonFrag != null) {
                video = instanceCommonFrag.getVideo();

                sendActionShowBottomSheet(this);

            }
        }
    }

    @Override
    public void onClickState(OnSwipeTouchListener.StateEvent event) {
        if (event == OnSwipeTouchListener.StateEvent.SWIPE_DOWN) {
            Log.e(TAG, "onClickState: SWIPE_DOWN");
            finish();
        } else {
            CommonPreviewFragment instanceCommonFrag = getInstanceCommonFrag();
            if (instanceCommonFrag != null) {
                instanceCommonFrag.receiveEventTouch(event);
            }
        }
    }

    @Override
    public void onLoadSuccess(final ArrayList<StoriesInsta> listStories) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listStories != null && !listStories.isEmpty()) {
                    mListStories.clear();
                    mListStories.addAll(listStories);
                    if (pagerAdapter != null) {
                        pagerAdapter.notifyDataSetChanged();
                    }
                }
                if (listStories == null) {
                    Toast.makeText(PreviewStoriesActivity.this, "No Stories", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onHideDownLoadBt(final boolean isShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rlDownload.setVisibility(!isShow ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onNextStories() {
        if (mViewPager.getCurrentItem() < pagerAdapter.getCount() - 1) {
            onHideDownLoadBt(false);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onPreviousStories() {
        if (mViewPager.getCurrentItem() <= pagerAdapter.getCount() - 1) {
            onHideDownLoadBt(false);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ICommonFragment) {
            ((ICommonFragment) fragment).setCommonFragmentListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.onDestroyViewpager();
        stopBackgroundThread();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(broadcastReceiver);
        Log.e(TAG, "onDestroy: ");
    }

    private void getData() {
        stories = StoriesInstaController.getInstance().getStoriesSelected();
        mListStories.clear();
        mListStories.addAll(StoriesInstaController.getInstance().getmListStories());
    }

    private void setDataStories(StoriesInsta stories) {
        stories.setSize(stories.getSize());
        stories.setLastTime(stories.getLastTime());
        stories.setType(stories.getType());
        stories.setThumbnailStory(stories.getThumbnailStory());
        stories.setLinkVideoStory(stories.getLinkVideoStory());
    }

    public CommonPreviewFragment getInstanceCommonFrag() {
        int index = mViewPager.getCurrentItem();
        PreviewPagerAdapter adapter = ((PreviewPagerAdapter) mViewPager.getAdapter());
        if (adapter != null)
            return adapter.getFragment(index);
        return null;
    }

    public void loadDataStoriesDetail(String reelsID, LoadDataStoriesDetail.OnLoadDataStoryDetailListener listener) {
        mLastStoryLoader = new StoryLoaderRunnable(reelsID, listener);
        getBackgroundHandler().post(mLastStoryLoader);
    }

    protected Handler getBackgroundHandler() {
        if (mBackgroundHandler == null) {
            HandlerThread thread = new HandlerThread(getClass().getSimpleName() + " background thread");
            thread.start();
            mBackgroundHandler = new Handler(thread.getLooper());
        }
        return mBackgroundHandler;
    }

    protected void stopBackgroundThread() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            if (mBackgroundHandler != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mBackgroundHandler.getLooper().quitSafely();
                } else {
                    mBackgroundHandler.getLooper().quit();
                }
                mBackgroundHandler = null;
            }
        } else {
            mMainThread.post(new Runnable() {
                @Override
                public void run() {
                    stopBackgroundThread();
                }
            });
        }
    }

    private void listenerCheckId() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.ACTION_CHECK_ID);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case Utils.ACTION_CHECK_ID:
                            mListTask = (ArrayList<Task>) intent.getSerializableExtra(Utils.INTENT_ACTION_CHECK_ID);
                            if (!checkVideoDownloaded(video)) {
                                QualityBottomSheetFragment.showBottomSheet(getSupportFragmentManager(), video);
                            }
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private boolean checkVideoDownloaded(Video video) {
        if (mListTask != null) {
            for (Task task : mListTask) {
                if (task.idServer.equalsIgnoreCase(video.getIdVideo())) {
                    QualityBottomSheetFragment.showBottomSheet(getSupportFragmentManager(), video, task);
                    return true;
                }
            }
        }
        return false;
    }

    private void sendActionShowBottomSheet(Context context) {
        Intent intent = new Intent(Utils.ACTION_SHOW_BOTTOM_SHEET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
