package com.tapi.download.video.facebook.function.preview.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.view.PlayControllerView;
import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.adapter.Stories;
import com.tapi.download.video.facebook.core.StoriesFaceBookController;
import com.tapi.download.video.facebook.core.StoriesFunction;
import com.tapi.download.video.facebook.function.preview.OnSwipeTouchListener;
import com.tapi.download.video.facebook.function.preview.listener.ICommonFragment;
import com.tapi.download.video.facebook.function.preview.listener.OnCommonFragmentListener;
import com.tapi.download.video.facebook.function.utils.FBStoriesHeaderView;
import com.tapi.download.video.facebook.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class CommonPreviewFragment extends BaseFragment implements StoriesFunction.OnLoadDataStoryDetailListener,
        View.OnClickListener, ICommonFragment, RequestListener<Drawable> {
    public static final String COMMON_DATA = "data";
    private static final String TAG = "CommonPreviewFragment1";
    private PreviewActivity previewActivity;
    private Context mContext;
    private RelativeLayout rlLoading, rlLoadingView;
    private FBStoriesHeaderView fbStoriesHeaderView;
    private PlayControllerView playControllerView;
    private ImageView ivStories, ivProfile;
    private RelativeLayout rlTitle;
    private Stories mStories;
    private TextView tvLastTime, tvName;
    private OnCommonFragmentListener listener;
    private String[] listThreadId;
    private String url, type;
    private int posThread = 0;
    private StateLoading stateLoading = StateLoading.NONE;
    private boolean isFirstCreate, isLoading, menuVisible;

    public static CommonPreviewFragment newInstance(Stories stories) {
        Bundle args = new Bundle();
        args.putParcelable(COMMON_DATA, stories);
        CommonPreviewFragment fragment = new CommonPreviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mStories = arguments.getParcelable(COMMON_DATA);
            StoriesFaceBookController instance = StoriesFaceBookController.getInstance();
            if (instance.getPosItemInList(mStories) == instance.getSizeListStories() - 1) {
                ArrayList<Stories> stories = instance.getmListStories();
                Stories stories1 = stories.get(stories.size() - 1);
                instance.getDataLoadMore(stories1.getEndCursor(), stories1.getTraySessionId());
            }
        }
        return inflater.inflate(R.layout.common_fragment, container, false);
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);
        this.menuVisible = menuVisible;
        if (menuVisible && stateLoading == StateLoading.NONE) {
            stateLoading = StateLoading.READY;
        } else {
            if (previewActivity != null && stateLoading == StateLoading.LOADED) {
                previewActivity.stopBackgroundThread();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        previewActivity = (PreviewActivity) getActivity();
        if (stateLoading == StateLoading.READY) {
            loadDataStories(mStories.getThreadId(), mStories.getDataBucketId());
            stateLoading = StateLoading.LOADED;
        }
    }

    private void loadDataStories(String threadId, String dataBucketId) {
        if (mStories != null && previewActivity != null) {
            previewActivity.loadDataStoriesDetail(String.format(Locale.ENGLISH, Utils.BASE_STORIES_VIEW,
                    dataBucketId, threadId), dataBucketId, this);
        }
    }

    private void initView(View view) {
        rlLoading = view.findViewById(R.id.common_fragment_demo_loading_rl);
        ivStories = view.findViewById(R.id.common_fragment_demo_image_iv);
        playControllerView = view.findViewById(R.id.common_fragment_demo_controller);
        fbStoriesHeaderView = view.findViewById(R.id.common_fragment_demo_header_stories);
        tvName = view.findViewById(R.id.common_fragment_demo_name_tv);
        tvLastTime = view.findViewById(R.id.common_fragment_demo_time_tv);
        ivProfile = view.findViewById(R.id.common_fragment_demo_profile_iv);
        rlLoadingView = view.findViewById(R.id.common_fragment_demo_loading_view_rl);
        rlTitle = view.findViewById(R.id.common_fragment_demo_group_title_rl);

        playControllerView.setStories(true);
    }

    @Override
    public void onLoadSuccess(Stories stories1, String dataBucketId) {
        if (isAdded() && menuVisible) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stories1 == null) {
                            activity.finish();
                        } else {
                            listThreadId = stories1.getListThreadId();
                            setDataStories(stories1);
                            loadImageStories(stories1);
                            checkState(stories1.getType());
                            hideLoadingView();
                            if (!isLoading) {
                                posThread = getFindThreadSelect();
                                fbStoriesHeaderView.setTabSelect(posThread);
                            }
                            isLoading = false;
                        }
                    }
                });
            }
        } else {
            stateLoading = StateLoading.NONE;
        }
    }

    private int getFindThreadSelect() {
        String threadId = mStories.getThreadId();
        for (int i = 0; i < listThreadId.length; i++) {
            if (listThreadId[i].equals(threadId)) {
                return i;
            }
        }
        return -1;
    }

    private void checkState(String type) {
        boolean photo = checkTypeVideoStories(type);
        ivStories.setVisibility(photo ? View.GONE : View.VISIBLE);
        playControllerView.setVisibility(photo ? View.VISIBLE : View.GONE);
        upDateButtonDownload(photo);
        if (photo) {
            playControllerView.setUrl(url);
            playControllerView.setStories(true);
        } else {
            playControllerView.releaseExoPlayer();
        }
        if (!isFirstCreate) {
            isFirstCreate = true;
            Utils.animationChangeAlpha(rlLoading, 1f, 0f, false);
        }
    }

    private boolean checkTypeVideoStories(String type) {
        return type != null && !type.equalsIgnoreCase("photo");
    }

    private void setDataStories(Stories stories) {
        mStories.setListThreadId(stories.getListThreadId());
        mStories.setSize(stories.getSize());
        mStories.setType(stories.getType());
        mStories.setLastTime(stories.getLastTime());
        mStories.setImageStoryHd(stories.getImageStoryHd());
        mStories.setVideoStory(stories.getVideoStory());
        mStories.setmVideoSize(stories.getmVideoSize());
        mStories.setDuration(stories.getDuration());
        url = stories.getVideoStory();
        type = stories.getType();
    }

    private void loadImageStories(Stories stories) {
        Utils.loadThumbnail(mContext, stories.getImageStoryHd(), ivStories, this);
        Utils.loadThumbnail(mContext, mStories.getImageProfile(), ivProfile);
        fbStoriesHeaderView.setmSize(stories.getListThreadId().length);
        tvName.setText(mStories.getTitle());
        tvLastTime.setText(stories.getLastTime());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public void receiveEventTouch(OnSwipeTouchListener.StateEvent event) {
        switch (event) {
            case CLICK_UP:
                Utils.animationChangeAlpha(rlTitle, 0, 1, true);
                playControllerView.showHideGroupSeekbar(true);
                if (checkTypeVideoStories(type)) {
                    playControllerView.playPlayer();
                    if (listener != null)
                        listener.onHideAnimDownLoadBt(true);
                }
                break;
            case CLICK_LEFT:
                checkPosThreadPre();
                break;
            case CLICK_RIGHT:
                checkPosThreadNext();
                break;
            case LONG_CLICK:
                Utils.animationChangeAlpha(rlTitle, 1, 0, true);
                playControllerView.showHideGroupSeekbar(false);
                if (checkTypeVideoStories(type)) {
                    playControllerView.pausePlayer();
                    if (listener != null)
                        listener.onHideAnimDownLoadBt(false);
                }
                break;
        }
    }

    public String getUrlVideo() {
        return url;
    }

    public Video getVideoStories() {
        ArrayList<DownloadLink> links = new ArrayList<>();
        links.add(new DownloadLink(url, mStories.getmVideoSize(), com.tapi.download.video.core.utils.Utils.VIDEO_HD));
        Video video = new Video(mStories.getThreadId(), mStories.getImageProfile(), null, mStories.getTitle(), mStories.getImageStoryHd(), mStories.getDuration(), links);
        video.setIdVideo(mStories.getThreadId());
        return video;
    }

    private void checkPosThreadPre() {
        if (posThread != 0) {
            if (!isLoading) {
                posThread--;
                changeData(posThread);
                isLoading = true;
            }
        } else {
            if (listener != null)
                listener.onPreviousStories();
        }
    }

    private void changeData(int posThread) {
        if (posThread >= 0 && posThread < listThreadId.length) {
            fbStoriesHeaderView.setTabSelect(posThread);
            showLoadingView();
            loadDataStories(listThreadId[posThread], mStories.getDataBucketId());
            Log.e(TAG, "changeData: ");
        }
    }

    private void showLoadingView() {
        Utils.animationChangeAlpha(rlLoadingView, 0f, 1f, false);
    }

    private void hideLoadingView() {
        Utils.animationChangeAlpha(rlLoadingView, 1f, 0f, false);
    }

    private void checkPosThreadNext() {
        if (listThreadId!=null){
            if (posThread != listThreadId.length - 1) {
                if (!isLoading) {
                    posThread++;
                    changeData(posThread);
                    isLoading = true;
                }
            } else {
                if (listener != null)
                    listener.onNextStories();
            }
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        return false;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        return false;
    }

    @Override
    public void setCommonFragmentListener(OnCommonFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPause() {
        super.onPause();
        playControllerView.pausePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stateLoading == StateLoading.READY) {
            loadDataStories(mStories.getThreadId(), mStories.getDataBucketId());
            stateLoading = StateLoading.LOADED;
        }
        if (mStories != null && type != null)
            upDateButtonDownload(checkTypeVideoStories(type));
        playControllerView.playPlayer();
    }

    private void upDateButtonDownload(boolean photo) {
        if (listener != null && menuVisible) {
            listener.onHideAnimDownLoadBt(photo);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playControllerView.releaseExoPlayer();
        if (previewActivity != null) {
            previewActivity.stopBackgroundThread();
        }
    }

    enum StateLoading {NONE, READY, LOADED}
}
