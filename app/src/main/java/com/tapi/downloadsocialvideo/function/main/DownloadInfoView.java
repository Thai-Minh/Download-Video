package com.tapi.downloadsocialvideo.function.main;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.view.DownLoadProgressbar;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.function.main.adapter.DownloadInfoAdapter;
import com.tapi.downloadsocialvideo.function.main.adapter.OnDownloadInfoListener;
import com.tapi.downloadsocialvideo.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class DownloadInfoView extends RelativeLayout implements View.OnClickListener, OnDownloadInfoListener {
    private static final String TAG = "DownloadInfoView";
    private Context mContext;
    private RelativeLayout rlShare, rlItem;
    private LinearLayout llGroupProgress, llGroupDetail, llItemDownload;
    private ImageView ivThumbnail, ivClose, ivShare, ivDelete, ivPause;
    private TextView tvTitle, tvDuration, tvFile, tvSize, tvProgress;
    private DownLoadProgressbar progressbar;

    private RecyclerView recyclerView;
    private DownloadInfoAdapter infoAdapter;
    private Video video;

    private Task task;

    private IDownloadInfo iDownloadInfo;

    public DownloadInfoView(Context context) {
        this(context, null);
    }

    public DownloadInfoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownloadInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public void setVideo(Video video) {
        this.video = video;
        this.task = null;
        if (llGroupProgress == null) {
            initView(mContext);
        }
        changeStateView(StateDownLoadInFo.NORMAL);
        initRecyclerView();
        initData();
    }

    private void initData() {
        Utils.loadThumbnail(mContext, video.getThumbnail(), ivThumbnail);
        tvTitle.setText(Utils.getVideoTitle(video));
        tvDuration.setText(convertTime(video.getDuration()));
    }

    private String convertTime(int time) {
        return Utils.formatDuration(time / 1000);
    }

    private void initRecyclerView() {
        infoAdapter = new DownloadInfoAdapter(mContext, video, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(infoAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void initView(Context context) {
        inflate(context, R.layout.download_info, this);
        recyclerView = findViewById(R.id.download_info_recyclerview);
        rlItem = findViewById(R.id.download_info_item_rl);
        llGroupProgress = findViewById(R.id.item_download_group_progress_ll);
        ivThumbnail = findViewById(R.id.item_download_thumbnail_iv);
        tvTitle = findViewById(R.id.item_download_title_tv);
        tvDuration = findViewById(R.id.item_download_duration_tv);
        ivClose = findViewById(R.id.item_download_cancel_iv);
        ivShare = findViewById(R.id.item_download_share_iv);
        ivDelete = findViewById(R.id.item_download_trash_iv);
        llGroupDetail = findViewById(R.id.download_info_detail_ll);
        tvFile = findViewById(R.id.download_info_file_data_tv);
        tvSize = findViewById(R.id.download_info_size_data_tv);
        tvProgress = findViewById(R.id.item_download_progress_tv);
        progressbar = findViewById(R.id.item_download_progress);
        ivPause = findViewById(R.id.item_download_action_iv);
        rlShare = findViewById(R.id.item_download_group_share_rl);
        llItemDownload = findViewById(R.id.download_info_include_layout);
        llItemDownload.setBackground(null);

        rlItem.setOnClickListener(this);
        ivPause.setOnClickListener(this);
        ivClose.setOnClickListener(this);
        ivShare.setOnClickListener(this);
        ivDelete.setOnClickListener(this);
    }

    private void changeStateView(StateDownLoadInFo state) {
        switch (state) {
            case NORMAL:
                setProgressTv(0);
                ivClose.setVisibility(View.VISIBLE);
                ivPause.setImageResource(R.drawable.pause_downloading_ic);
                progressbar.setProgress(0);
                recyclerView.setVisibility(View.VISIBLE);
                llGroupProgress.setVisibility(GONE);
                llGroupDetail.setVisibility(View.GONE);
                rlShare.setVisibility(View.GONE);
                break;
            case DOWNLOAD:
                rlShare.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                llGroupProgress.setVisibility(VISIBLE);
                llGroupDetail.setVisibility(View.GONE);
                break;
            case DOWNLOAD_END:
                rlShare.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                llGroupProgress.setVisibility(GONE);
                llGroupDetail.setVisibility(View.VISIBLE);
                ivClose.setVisibility(GONE);
                break;
        }
    }

    public void setTaskChange(Task taskChange) {
        this.task = taskChange;
        checkTaskState(taskChange);
        int progress = taskChange.percentCommon > -1 ? taskChange.percentCommon : taskChange.percent;
        if (taskChange.state == TaskStates.END) {
            progressbar.setProgress(100);
        } else {
            setProgressTv(progress);
            progressbar.setProgress(progress);
        }
        int downloadState = taskChange.state;
        if (downloadState == TaskStates.DOWNLOADING) {
            ivPause.setVisibility(View.VISIBLE);
            ivPause.setImageResource(R.drawable.pause_downloading_ic);
        } else if (downloadState == TaskStates.PAUSED) {
            ivPause.setVisibility(View.VISIBLE);
            ivPause.setImageResource(R.drawable.resume_download_ic);
        } else
            ivPause.setVisibility(View.INVISIBLE);

        if (downloadState == TaskStates.END) {
            rlShare.setVisibility(View.VISIBLE);
            changeStateView(StateDownLoadInFo.DOWNLOAD_END);
            tvFile.setText(taskChange.getFullName());
            float v = (float) (taskChange.size * 0.000001);
            tvSize.setText(String.format(Locale.ENGLISH, mContext.getString(R.string.item_download_info_size), v));

        } else {
            rlShare.setVisibility(View.GONE);
            llGroupProgress.setVisibility(View.VISIBLE);
        }
    }

    private void setProgressTv(int progress) {
        tvProgress.setText(String.format(Locale.ENGLISH, "%d%% ", progress));
    }

    private void checkTaskState(Task task) {
        if (task.state != TaskStates.END) {
            changeStateView(StateDownLoadInFo.DOWNLOAD);
        }
    }

    @Override
    public void onClick(View v) {
        int state = TaskStates.INIT;
        long id = -1;
        if (task != null) {
            id = task.id;
            state = task.state;
        }
        switch (v.getId()) {
            case R.id.item_download_cancel_iv:
                if (task != null) {
                    if (iDownloadInfo != null) {
                        if (state == TaskStates.PAUSED)
                            iDownloadInfo.onDownloadClose(id, true);
                        else
                            iDownloadInfo.onDownloadClose(id, false);
                    }
                } else {
                    if (iDownloadInfo != null)
                        iDownloadInfo.onDownloadClose(-1, true);
                }
                break;
            case R.id.item_download_action_iv:
                if (iDownloadInfo != null) {
                    if (state == TaskStates.PAUSED)
                        iDownloadInfo.onDownloadResume(id);
                    else if (state == TaskStates.DOWNLOADING)
                        iDownloadInfo.onDownloadPause(id);
                }
                break;
            case R.id.item_download_trash_iv:
                if (iDownloadInfo != null) {
                    if (state == TaskStates.END) {
                        iDownloadInfo.onDownloadDelete(id);
                    }
                }
                break;
            case R.id.item_download_share_iv:
                if (iDownloadInfo != null) {
                    if (state == TaskStates.END) {
                        String filePath = task.save_address.concat(File.separator).concat(task.getFullName());
                        iDownloadInfo.onDownloadShare(filePath);
                    }
                }
                break;
            case R.id.download_info_item_rl:
                if (task != null) {
                    if (iDownloadInfo != null) {
                        if (state == TaskStates.END) {
                            String filePath = task.save_address.concat(File.separator).concat(task.getFullName());
                            iDownloadInfo.onWatchVideo(filePath);
                        }
                    }
                } else {
                    ArrayList<DownloadLink> links = video.getLinks();
                    if (iDownloadInfo != null)
                        iDownloadInfo.onWatchVideo(links.get(0).getLink());
                }
                break;
        }
    }

    @Override
    public void onDownloadVideo(DownloadLink downloadLink) {
        changeStateView(StateDownLoadInFo.DOWNLOAD);
        if (iDownloadInfo != null)
            iDownloadInfo.onDownLoadVideo(downloadLink);
    }

    @Override
    public void onWatchVideo() {
        ArrayList<DownloadLink> links = video.getLinks();
        if (iDownloadInfo != null)
            iDownloadInfo.onWatchVideo(links.get(0).getLink());
    }

    public void setiDownloadInfo(IDownloadInfo iDownloadInfo) {
        this.iDownloadInfo = iDownloadInfo;
    }

    public enum StateDownLoadInFo {
        NORMAL, DOWNLOAD, DOWNLOAD_END
    }

    public interface IDownloadInfo {
        void onDownloadClose(long downloadID,boolean isPause);

        void onDownloadPause(long downloadID);

        void onDownloadResume(long downloadID);

        void onDownloadDelete(long downloadID);

        void onDownLoadVideo(DownloadLink downloadLink);

        void onDownloadShare(String path);

        void onWatchVideo(String url);
    }
}
