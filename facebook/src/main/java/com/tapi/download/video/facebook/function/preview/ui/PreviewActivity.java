package com.tapi.download.video.facebook.function.preview.ui;

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
import com.tapi.download.video.core.view.QualityBottomSheetFragment;
import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.adapter.Stories;
import com.tapi.download.video.facebook.core.OnStoriesListener;
import com.tapi.download.video.facebook.core.StoriesFaceBookController;
import com.tapi.download.video.facebook.core.StoriesFunction;
import com.tapi.download.video.facebook.function.preview.OnSwipeTouchListener;
import com.tapi.download.video.facebook.function.preview.StoriesViewPager;
import com.tapi.download.video.facebook.function.preview.StoryLoaderRunnable;
import com.tapi.download.video.facebook.function.preview.adapter.PreviewPagerAdapter;
import com.tapi.download.video.facebook.function.preview.listener.ICommonFragment;
import com.tapi.download.video.facebook.function.preview.listener.OnCommonFragmentListener;
import com.tapi.download.video.facebook.utils.Utils;
import com.tapi.downloader.database.elements.Task;

import java.util.ArrayList;

public class PreviewActivity extends BaseActivity implements OnStoriesListener, StoriesFunction.OnLoadDataStoryDetailListener,
        View.OnClickListener, OnSwipeTouchListener.OnViewTouchListener, OnCommonFragmentListener {
    private static final String TAG = "PreviewActivity1";
    protected Handler mBackgroundHandler;
    private int[] BIND_CLICK = {R.id.preview_activity_close_iv, R.id.preview_activity_play_controller_download_rl};
    private StoriesViewPager mViewPager;
    private RelativeLayout rlLoading, rlDownLoad;
    private PreviewPagerAdapter pagerAdapter;
    private Stories mStories;
    private ArrayList<Stories> mListStories = new ArrayList<>();
    private Handler mMainThread = new Handler(Looper.getMainLooper());
    private StoryLoaderRunnable mLastStoryLoader = null;

    private Video videoStories;

    private BroadcastReceiver broadcastReceiver;
    private ArrayList<Task> mListTask;


    @Override
    protected int getLayoutResId() {
        return R.layout.preview_activity_1;
    }

    @Override
    protected void findViewById() {
        mViewPager = findViewById(R.id.preview_activity_viewpager);
        rlLoading = findViewById(R.id.preview_activity_viewpager_loading_rl);
        rlDownLoad = findViewById(R.id.preview_activity_play_controller_download_rl);
    }

    @Override
    protected void onCreateInit(@Nullable Bundle savedInstanceState) {
        super.onCreateInit(savedInstanceState);
        bindClicks(this, BIND_CLICK);

        StoriesFaceBookController.getInstance().setListener(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getData();
        listenerCheckId();
        initViewPager();
    }

    private void getData() {
        mStories = StoriesFaceBookController.getInstance().getStoriesSelected();
        mListStories.clear();
        mListStories.addAll(StoriesFaceBookController.getInstance().getmListStories());
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
                onHideAnimDownLoadBt(false);
                StoriesFaceBookController.getInstance().setPosStorySelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(StoriesFaceBookController.getInstance().getPosStorySelect());
        mViewPager.setListener(this);
    }


    @Override
    public void onLoadSuccess(ArrayList<Stories> strories) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (strories != null && !strories.isEmpty()) {
                    mListStories.clear();
                    mListStories.addAll(strories);
                    if (pagerAdapter != null) {
                        pagerAdapter.notifyDataSetChanged();
                    }
                }
                if (strories == null) {
                    Toast.makeText(PreviewActivity.this, getString(R.string.facebook_no_stories), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loadDataStoriesDetail(String link, String dataBucketId, StoriesFunction.OnLoadDataStoryDetailListener listener) {
        mLastStoryLoader = new StoryLoaderRunnable(link, dataBucketId, listener);
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

    @Override
    public void onLoadSuccess(Stories stories, String dataBucketId) {
        if (StoriesFaceBookController.getInstance().getStoriesSelected().getDataBucketId().equals(dataBucketId)) {
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
    }

    private void setDataStories(Stories stories) {
        mStories.setListThreadId(stories.getListThreadId());
        mStories.setSize(stories.getSize());
        mStories.setType(stories.getType());
        mStories.setLastTime(stories.getLastTime());
        mStories.setImageStoryHd(stories.getImageStoryHd());
        mStories.setVideoStory(stories.getVideoStory());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.onDestroyViewpager();
        stopBackgroundThread();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Log.e(TAG, "onDestroy: ");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.preview_activity_close_iv) {
            finish();
        } else if (id == R.id.preview_activity_play_controller_download_rl) {
            CommonPreviewFragment instanceCommonFrag = getInstanceCommonFrag();
            if (instanceCommonFrag != null) {
                videoStories = instanceCommonFrag.getVideoStories();
                Log.e(TAG, "onClick: " + videoStories.getLinks().get(0).getLink());

                sendActionShowBottomSheet(this);
            }
        }
    }

    @Override
    public void onClickState(OnSwipeTouchListener.StateEvent event) {
        if (event == OnSwipeTouchListener.StateEvent.SWIPE_DOWN) {
            finish();
        } else {
            CommonPreviewFragment instanceCommonFrag = getInstanceCommonFrag();
            if (instanceCommonFrag != null) {
                instanceCommonFrag.receiveEventTouch(event);
            }
        }
    }


    public CommonPreviewFragment getInstanceCommonFrag() {
        int index = mViewPager.getCurrentItem();
        PreviewPagerAdapter adapter = ((PreviewPagerAdapter) mViewPager.getAdapter());
        if (adapter != null)
            return adapter.getFragment(index);
        return null;
    }

    @Override
    public void onNextStories() {
        if (mViewPager.getCurrentItem() < pagerAdapter.getCount() - 1) {
            onHideAnimDownLoadBt(false);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onPreviousStories() {
        if (mViewPager.getCurrentItem() > 0 && mViewPager.getCurrentItem() <= pagerAdapter.getCount() - 1) {
            onHideAnimDownLoadBt(false);
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onHideAnimDownLoadBt(boolean isShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rlDownLoad.setVisibility(isShow ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ICommonFragment) {
            ((ICommonFragment) fragment).setCommonFragmentListener(this);
        }
    }

    private void listenerCheckId() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.tapi.download.video.core.utils.Utils.ACTION_CHECK_ID);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case com.tapi.download.video.core.utils.Utils.ACTION_CHECK_ID:
                            mListTask = (ArrayList<Task>) intent.getSerializableExtra(com.tapi.download.video.core.utils.Utils.INTENT_ACTION_CHECK_ID);
                            if (!checkVideoDownloaded(videoStories)) {
                                Log.e(TAG, "listenerCheckId: ");
                                QualityBottomSheetFragment.showBottomSheet(getSupportFragmentManager(), videoStories);
                            }
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
    }

    private boolean checkVideoDownloaded(Video videoStories) {
        if (mListTask != null) {
            for (Task task : mListTask) {
                if (task.idServer.equalsIgnoreCase(videoStories.getIdVideo())) {
                    QualityBottomSheetFragment.showBottomSheet(getSupportFragmentManager(), videoStories, task);
                    return true;
                }
            }
        }
        return false;
    }

    private void sendActionShowBottomSheet(Context context) {
        Intent intent = new Intent(com.tapi.download.video.core.utils.Utils.ACTION_SHOW_BOTTOM_SHEET);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
