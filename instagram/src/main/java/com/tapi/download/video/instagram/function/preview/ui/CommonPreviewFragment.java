package com.tapi.download.video.instagram.function.preview.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.model.StoriesInsta;
import com.tapi.download.video.core.task.GetSizeLink;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.core.view.PlayControllerView;
import com.tapi.download.video.instagram.R;
import com.tapi.download.video.instagram.core.LoadDataStoriesDetail;
import com.tapi.download.video.instagram.function.preview.OnSwipeTouchListener;
import com.tapi.download.video.instagram.function.preview.listener.ICommonFragment;
import com.tapi.download.video.instagram.function.preview.listener.OnCommonFragmentListener;
import com.tapi.download.video.instagram.function.util.InstaStoriesHeaderView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CommonPreviewFragment extends BaseFragment implements LoadDataStoriesDetail.OnLoadDataStoryDetailListener,
        View.OnClickListener, RequestListener<Drawable>, ICommonFragment {

    private static final String COMMON_PREVIEW_DATA = "common_data";
    private static final String COMMON_PREVIEW_POSITION = "common_position";

    private Context mContext;
    private RelativeLayout rlLoading, rlLoadingView;
    private PreviewStoriesActivity previewActivity;
    private PlayControllerView playControllerView;
    private InstaStoriesHeaderView instaStoriesHeaderView;
    private ImageView imgStories, imgProfile;
    private RelativeLayout rlTitle;
    private TextView txtLastTime, txtName;
    private StoriesInsta stories;
    private String url;
    private int listItem;
    private OnCommonFragmentListener commonFragmentListener;
    private int posItem = 0;
    private StateLoading stateLoading = StateLoading.NONE;
    private boolean isFirstCreate, isLoading, menuVisible;

    private Video video;
    private ArrayList<DownloadLink> arrDownloadLink;

    public static CommonPreviewFragment newInstance(StoriesInsta stories) {
        Bundle args = new Bundle();
        args.putSerializable(COMMON_PREVIEW_DATA, stories);
        CommonPreviewFragment fragment = new CommonPreviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            stories = (StoriesInsta) arguments.getSerializable(COMMON_PREVIEW_DATA);
        }
        return inflater.inflate(R.layout.fragment_insta_common_preview, container, false);
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
        previewActivity = (PreviewStoriesActivity) getActivity();
        if (stateLoading == StateLoading.READY) {
            loadDataStories(stories.getReelsID(), 0);
            stateLoading = StateLoading.LOADED;
        }
    }

    @Override
    public void onLoadSuccess(final StoriesInsta stories) {
        if (isAdded()) {
            final FragmentActivity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (stories == null) {
                            activity.finish();
                        } else {
                            listItem = stories.getSize();
                            setDataStories(stories);
                            loadImageStories(stories);
                            checkState(stories.getType());
                            hideLoadingView();
                            if (!isLoading) {
                                posItem = 0;
                                instaStoriesHeaderView.setTabSelect(posItem);
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
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
        this.commonFragmentListener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (stateLoading == StateLoading.READY) {
            loadDataStories(stories.getReelsID(), 0);
            stateLoading = StateLoading.LOADED;
        }
        if (stories != null && stories.getType() != null)
            upDateButtonDownload(checkTypeVideoStories(stories.getType()));
        playControllerView.playPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        playControllerView.pausePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        playControllerView.releaseExoPlayer();
        if (previewActivity != null) {
            previewActivity.stopBackgroundThread();
        }
    }

    private void loadDataStories(String reelsID, int posItem) {
        if (stories != null && previewActivity != null) {
            previewActivity.loadDataStoriesDetail(reelsID + " + position " + posItem, this);
        }
    }

    private void initView(View view) {
        rlLoading = view.findViewById(R.id.common_fragment_demo_loading_rl);
        imgStories = view.findViewById(R.id.common_fragment_demo_image_iv);
        playControllerView = view.findViewById(R.id.fragment_common_playcontroler);
        instaStoriesHeaderView = view.findViewById(R.id.common_fragment_demo_header_stories);
        txtName = view.findViewById(R.id.common_fragment_demo_name_tv);
        txtLastTime = view.findViewById(R.id.common_fragment_demo_time_tv);
        imgProfile = view.findViewById(R.id.common_fragment_demo_profile_iv);
        rlLoadingView = view.findViewById(R.id.common_fragment_demo_loading_view_rl);
        rlTitle = view.findViewById(R.id.common_fragment_demo_group_title_rl);

        playControllerView.setStories(true);
    }

    private void checkState(String type) {
        boolean photo = checkTypeVideoStories(type);
        imgStories.setVisibility(photo ? View.GONE : View.VISIBLE);
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
        return !type.equalsIgnoreCase("photo");
    }

    private void setDataStories(StoriesInsta stories) {
        this.stories.setSize(stories.getSize());
        this.stories.setType(stories.getType());
        this.stories.setLastTime(stories.getLastTime());
        this.stories.setThumbnailStory(stories.getThumbnailStory());
        this.stories.setLinkVideoStory(stories.getLinkVideoStory());
        this.stories.setDuration(stories.getDuration());
        this.stories.setId(stories.getId());
        if (stories.getLinkVideoStory() != null && !stories.getLinkVideoStory().isEmpty()) {
            ArrayList<DownloadLink> listLink = stories.getLinkVideoStory();
            if (listLink.size() == 1) {
                this.url = listLink.get(0).getLink();
            } else {
                this.url = listLink.get(listLink.size() - 1).getLink();
            }
        }
    }

    private void loadImageStories(final StoriesInsta stories) {
        Utils.loadThumbnail(mContext, stories.getThumbnailStory(), imgStories, this);
        Utils.loadThumbnail(mContext, stories.getImageUser(), imgProfile);
        instaStoriesHeaderView.setmSize(stories.getSize());

        txtName.setText(stories.getUserName());

        String time = stories.getLastTime();
        if (time != null) {
            long lastTime = Long.parseLong(time);
            lastTime = (long) Math.floor(lastTime / 3600000);
            if (lastTime < 1) {
                long realtime = Long.parseLong(time);
                realtime = (long) Math.floor(realtime / 60000);
                txtLastTime.setText(realtime + " m");
            } else {
                txtLastTime.setText(lastTime + " h");
            }
        }

    }

    public Video getVideo() {
        arrDownloadLink = new ArrayList<>();
        video = new Video();

        if (stories != null) {
            if (stories.getLinkVideoStory() != null) {
                ArrayList<DownloadLink> listLink = stories.getLinkVideoStory();
                if (listLink.size() == 1) {
                    arrDownloadLink.add(new DownloadLink(listLink.get(0).getLink(), listLink.get(0).getSize(), Utils.VIDEO_SD));
                } else {
                    arrDownloadLink.add(new DownloadLink(listLink.get(0).getLink(), listLink.get(0).getSize(), Utils.VIDEO_SD));
                    arrDownloadLink.add(new DownloadLink(listLink.get(1).getLink(), listLink.get(1).getSize(), Utils.VIDEO_HD));
                }
            }

            video.setIdVideo(stories.getId());
            video.setDuration(stories.getDuration() * 1000); //millisecond
            video.setTitle(stories.getUserName());
            video.setThumbnail(stories.getThumbnailStory());
            video.setLinks(arrDownloadLink);
        }

        return video;
    }

    private void upDateButtonDownload(boolean photo) {
        if (commonFragmentListener != null) {
            commonFragmentListener.onHideDownLoadBt(photo);
        }
    }

    private void checkPosThreadNext() {
        if (posItem != listItem - 1) {
            if (!isLoading) {
                posItem++;
                changeData(posItem);
                isLoading = true;
            }
        } else {
            if (commonFragmentListener != null)
                commonFragmentListener.onNextStories();
        }
    }

    private void checkPosThreadPre() {
        if (posItem != 0) {
            if (!isLoading) {
                posItem--;
                changeData(posItem);
                isLoading = true;
            }
        } else {
            if (commonFragmentListener != null)
                commonFragmentListener.onPreviousStories();
        }
    }

    private void changeData(int posItem) {
        if (posItem >= 0 && posItem < listItem) {
            instaStoriesHeaderView.setTabSelect(posItem);
            showLoadingView();
            loadDataStories(stories.getReelsID(), posItem);
        }
    }

    private void showLoadingView() {
        Utils.animationChangeAlpha(rlLoadingView, 0f, 1f, false);
    }

    private void hideLoadingView() {
        Utils.animationChangeAlpha(rlLoadingView, 1f, 0f, false);
    }

    public void receiveEventTouch(OnSwipeTouchListener.StateEvent event) {
        switch (event) {
            case CLICK_UP:
                Utils.animationChangeAlpha(rlTitle, 0, 1, true);
                playControllerView.showHideGroupSeekbar(true);
                if (checkTypeVideoStories(stories.getType())) {
                    playControllerView.playPlayer();
                    if (commonFragmentListener != null)
                        commonFragmentListener.onHideDownLoadBt(true);
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
                if (checkTypeVideoStories(stories.getType())) {
                    playControllerView.pausePlayer();
                    if (commonFragmentListener != null)
                        commonFragmentListener.onHideDownLoadBt(false);
                }
                break;
        }
    }

    enum StateLoading {NONE, READY, LOADED}

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
