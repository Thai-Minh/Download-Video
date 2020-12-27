package com.tapi.downloadsocialvideo.function.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tapi.download.video.core.BaseFragment;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.listener.OnCatchVideoListener;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.activities.MainActivity;
import com.tapi.downloadsocialvideo.function.downloader.OnDownloadListener;
import com.tapi.downloadsocialvideo.function.downloader.ui.DialogConfirmDelete;
import com.tapi.downloadsocialvideo.util.Utils;

import java.util.ArrayList;


public class HomeFragment extends BaseFragment implements View.OnClickListener, OnDownloadListener,
        OnCatchVideoListener, DownloadInfoView.IDownloadInfo, DialogConfirmDelete.OnDialogConFirm,
        MainActivity.OnSendIntentListener {
    private static final String TAG = "HomeFragment";
    private RelativeLayout rlTutorial, rlLoading;
    private LinearLayout llGroupScroll;
    private DownloadInfoView downloadInfoView;
    private MainActivity mainActivity;
    private Context mContext;
    private EditText edtPaste;
    private TextView tvDownload;

    private Video video;

    private boolean isTaskChange, isVideoDownloaded;
    private String mLinkBefore;
    private int mIdDownload = -1;
    private String mIdServer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.facebook_home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setOnSendIntentListener(this);
            Intent intent = mainActivity.getIntentSend();
            if (intent != null) {
                detectIntent(intent);
            } else {
                detectIntent(mainActivity.getIntent());
            }
            mainActivity.setListener(this);
        }
    }

    private void initView(View view) {
        edtPaste = view.findViewById(R.id.fragment_home_paste_link_edt);
        downloadInfoView = view.findViewById(R.id.fragment_home_download_info);
        llGroupScroll = view.findViewById(R.id.fragment_home_group_scroll_ll);
        rlLoading = view.findViewById(R.id.fragment_home_loading_rl);

        view.findViewById(R.id.fragment_home_paste_link_tv).setOnClickListener(this);
        tvDownload = view.findViewById(R.id.fragment_home_download_link_tv);
        tvDownload.setOnClickListener(this);

        downloadInfoView.setiDownloadInfo(this);
        rlTutorial = SocialManager.getInstance().getTutorial(mContext);
        llGroupScroll.addView(rlTutorial);
        showHideViewTutorial(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_home_paste_link_tv:
                String stringClipBoard = Utils.getStringClipBoard(mContext);
                Log.e(TAG, "onClick: ");
                if (stringClipBoard != null) {
                    pasteLinkDownload(stringClipBoard);
                }
                break;
            case R.id.fragment_home_download_link_tv:
                actionDownload();
                break;
        }
    }

    private void pasteLinkDownload(String text) {
        edtPaste.setText(text);
        actionDownload();
    }

    private void changeViewBt(boolean isEnable) {
        tvDownload.setClickable(isEnable);
        tvDownload.setAlpha(!isEnable ? 0.7f : 1f);
    }

    private void showHideViewTutorial(boolean isShow) {
        rlTutorial.setVisibility(isShow ? View.VISIBLE : View.GONE);
        downloadInfoView.setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    public void setVideoFragment(Video video) {
        if (!Utils.checkVideoLive(video)) {
            showHideViewTutorial(false);
            downloadInfoView.setVideo(video);
        } else {
            showHideViewTutorial(true);
            Toast.makeText(mainActivity, getString(R.string.item_download_info_live_video), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDownloadTaskListChange(ArrayList<Task> downloadTasks) {
        Log.e(TAG, "onDownloadTaskListChange: ");
        isTaskChange = true;
        if (isVideoDownloaded) {
            actionDownload();
            isVideoDownloaded = false;
        }
        if (mIdDownload != -1) {
            showHideViewTutorial(!checkTaskRemove(downloadTasks));
            downloadInfoView.requestLayout();
        }
        setTaskInfoView(downloadTasks);
    }

    private void setTaskInfoView(ArrayList<Task> downloadTasks) {
        for (Task task : downloadTasks) {
            if (task.idServer.equalsIgnoreCase(mIdServer)) {
                downloadInfoView.setTaskChange(task);
                return;
            }
        }
    }

    private boolean checkTaskRemove(ArrayList<Task> downloadTasks) {
        for (Task task : downloadTasks) {
            if (task.idServer.equalsIgnoreCase(mIdServer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDownloadStateChange(Task downloadTask) {
        updateDownloadItemChange(downloadTask);
    }

    @Override
    public void onDownloadProgressChange(Task downloadTask) {
        updateDownloadItemChange(downloadTask);
    }

    private void updateDownloadItemChange(Task downloadTask) {
        if (downloadTask.idServer.equalsIgnoreCase(mIdServer)) {
            downloadInfoView.setTaskChange(downloadTask);
        }
    }

    @Override
    public void onStartCatch() {
        rlTutorial.setVisibility(View.GONE);
        rlLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCatchedLink(Video video) {
        mIdDownload = -1;
        this.video = video;

        rlTutorial.setVisibility(View.GONE);
        rlLoading.setVisibility(View.GONE);
        changeViewBt(true);

        if (SocialManager.getInstance().getmState() == SocialManager.StateApp.TWITTER
                || SocialManager.getInstance().getmState() == SocialManager.StateApp.INSTAGRAM) {
            if (checkVideoDownloaded(video.getIdVideo())) {
                return;
            }
        }
        setVideoFragment(this.video);

    }

    @Override
    public void onPrivateLink(String link) {
        video = null;
        showHideViewTutorial(true);
        rlTutorial.setVisibility(View.VISIBLE);
        rlLoading.setVisibility(View.GONE);
        changeViewBt(true);
    }

    @Override
    public void onDownloadClose(long downloadID, boolean isPause) {
        Log.e("phi.hd", "onDownloadClose: " + downloadID + ", " +isPause);
        showHideViewTutorial(true);
        if (mainActivity != null && downloadID != -1) {
            if (!isPause)
                mainActivity.cancelDownload(downloadID);
            else mainActivity.deleteDownload(downloadID);
        }
    }

    @Override
    public void onDownloadPause(long downloadID) {
        if (mainActivity != null)
            mainActivity.pauseDownload(downloadID);
    }

    @Override
    public void onDownloadResume(long downloadID) {
        if (mainActivity != null)
            mainActivity.resumeDownload(downloadID);
    }

    @Override
    public void onDownloadDelete(long downloadID) {
        DialogConfirmDelete.showDialogConfirm(mContext, this, downloadID);
    }

    @Override
    public void onDownLoadVideo(DownloadLink downloadLink) {
        if (mainActivity != null) {
            mIdServer = video.getIdVideo();
            mIdDownload = mainActivity.startDownload(video, downloadLink);
        }
    }

    @Override
    public void onDownloadShare(String path) {
        Utils.shareVideo(mContext, path);
    }

    @Override
    public void onWatchVideo(String url) {
        Utils.startPlayVideoActivity(mContext, null, url);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainActivity != null)
            mainActivity.setListener(null);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onConfirmOk(long mDownloadId) {
        if (mainActivity != null) {
            mainActivity.deleteDownload(mDownloadId);
            showHideViewTutorial(true);
        }
    }

    public void detectIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSharedText(intent);
            }
        }
    }

    private void handleSharedText(Intent intent) {
        rlLoading.setVisibility(View.VISIBLE);
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            try {
                if (sharedText.split("\\ ").length > 1) {
                    hideViewHomeFrag();
                    pasteLinkDownload(sharedText.split("\\ ")[4]);
                } else {
                    hideViewHomeFrag();
                    String text = sharedText.split("\\ ")[0];
                    pasteLinkDownload(SocialManager.getInstance().getmState() == SocialManager.StateApp.FACE_BOOK ? text.substring(0, sharedText.lastIndexOf("?sfnsn")) : text);
                }
            } catch (Exception e) {
                Log.e(TAG, "handleSharedText: " + e);
            }
        }
    }

    private void hideViewHomeFrag() {
        downloadInfoView.setVisibility(View.GONE);
        rlTutorial.setVisibility(View.GONE);
    }

    private void actionDownload() {
        String videoID = SocialManager.getInstance().parseVideoId(edtPaste.getText().toString().trim());
        if (videoID != null && checkVideoDownloaded(videoID)) {
            return;
        }
        mIdServer = videoID;
        hideViewHomeFrag();
        changeViewBt(false);
        String link = edtPaste.getText().toString().trim();
        if (Utils.isInternetConnected(mContext)) {
            if (SocialManager.getInstance().checkFormLink(link)) {
                if (link.isEmpty()) {
                    showHideViewTutorial(true);
                    changeViewBt(true);
                    Toast.makeText(mContext, R.string.fb_home_no_empty_link, Toast.LENGTH_SHORT).show();
                } else {
                    if (!link.equals(mLinkBefore) || video == null) {
                        this.mLinkBefore = link;
                        SocialManager.getInstance().catchLink(link);
                    } else {
                        rlLoading.setVisibility(View.GONE);
                        changeViewBt(true);
                        setVideoFragment(video);
                    }
                }
            } else {
                showHideViewTutorial(true);
                changeViewBt(true);
                Toast.makeText(mContext, R.string.fb_home_correct_link, Toast.LENGTH_SHORT).show();
            }
        } else {
            showHideViewTutorial(true);
            changeViewBt(true);
            Toast.makeText(mContext, R.string.fb_home_check_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkVideoDownloaded(String videoId) {
        if (mainActivity != null) {
            if (!isTaskChange) {
                isVideoDownloaded = true;
                return true;
            }
            ArrayList<Task> tasks = mainActivity.getmListTask();
            for (Task task : tasks) {
                if (task.idServer.equalsIgnoreCase(videoId)) {
                    mIdServer = videoId;
                    mIdDownload = task.id;
                    rlLoading.setVisibility(View.GONE);
                    showHideViewTutorial(false);
                    downloadInfoView.setVideo(new Video(null, null, task.name, task.thumbnailUrl, (int) task.duration, null));
                    downloadInfoView.setTaskChange(task);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onSendIntent(Intent intent) {
        detectIntent(intent);
    }
}
