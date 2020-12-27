package com.tapi.downloadsocialvideo.function.downloader.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.view.DownLoadProgressbar;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.util.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class ItemDownloadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private Context mContext;
    private ImageView ivThumbnail, ivCancel, ivPause, ivShare, ivRemove;
    private TextView tvTitle, tvProgress, tvDuration;
    private DownLoadProgressbar progressbar;
    private LinearLayout llGroupProgress;
    private RelativeLayout rlShare;
    private DownloadTaskAdapter.OnDownloadItemListener listener;
    private ArrayList<Object> list;

    public ItemDownloadViewHolder(@NonNull View itemView, Context context,
                                  DownloadTaskAdapter.OnDownloadItemListener listener, ArrayList<Object> list) {
        super(itemView);
        this.list = list;
        this.mContext = context;
        this.listener = listener;
        rlShare = itemView.findViewById(R.id.item_download_group_share_rl);
        llGroupProgress = itemView.findViewById(R.id.item_download_group_progress_ll);
        ivThumbnail = itemView.findViewById(R.id.item_download_thumbnail_iv);
        tvTitle = itemView.findViewById(R.id.item_download_title_tv);
        tvProgress = itemView.findViewById(R.id.item_download_progress_tv);
        tvDuration = itemView.findViewById(R.id.item_download_duration_tv);
        progressbar = itemView.findViewById(R.id.item_download_progress);
        ivCancel = itemView.findViewById(R.id.item_download_cancel_iv);
        ivPause = itemView.findViewById(R.id.item_download_action_iv);
        ivShare = itemView.findViewById(R.id.item_download_share_iv);
        ivRemove = itemView.findViewById(R.id.item_download_trash_iv);
        ivCancel.setOnClickListener(this);
        ivPause.setOnClickListener(this);
        ivShare.setOnClickListener(this);
        ivRemove.setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    public void bind(Task downloadTask) {
        Utils.loadThumbnail(mContext, downloadTask.thumbnailUrl, ivThumbnail);
        tvTitle.setText(downloadTask.getFullName());
        int progress = downloadTask.percentCommon > -1 ? downloadTask.percentCommon : downloadTask.percent;
        if (downloadTask.state == TaskStates.END) {
            progressbar.setProgress(100);
        } else {
            tvProgress.setText(String.format(Locale.ENGLISH, "%d%% ", progress));
            progressbar.setProgress(progress);
        }
        tvDuration.setText(String.format("%s s", Utils.formatDuration(downloadTask.duration / 1000)));
        int downloadState = downloadTask.state;
        if (downloadState == TaskStates.DOWNLOADING) {
            ivPause.setVisibility(View.VISIBLE);
            ivPause.setImageResource(R.drawable.pause_downloading_ic);
        } else if (downloadState == TaskStates.PAUSED) {
            ivPause.setVisibility(View.VISIBLE);
            ivPause.setImageResource(R.drawable.resume_download_ic);
        } else
            ivPause.setVisibility(View.INVISIBLE);

        if (downloadState == TaskStates.END) {
            ivCancel.setVisibility(View.GONE);
            rlShare.setVisibility(View.VISIBLE);
            llGroupProgress.setVisibility(View.GONE);
        } else {
            ivCancel.setVisibility(View.VISIBLE);
            rlShare.setVisibility(View.GONE);
            llGroupProgress.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View v) {
        int pos = getAdapterPosition();
        if (pos != -1) {
            Task downloadTasks = (Task) list.get(pos);
            int downloadState = downloadTasks.state;
            switch (v.getId()) {
                case R.id.item_download_cancel_iv:
                    if (listener != null) {
                        if (downloadState == TaskStates.PAUSED)
                            listener.onDownloadItemCancelClick(downloadTasks, true);
                        else
                            listener.onDownloadItemCancelClick(downloadTasks, false);
                    }
                    break;
                case R.id.item_download_action_iv:
                    if (listener != null) {
                        if (downloadState == TaskStates.PAUSED)
                            listener.onDownloadItemResumeClick(downloadTasks);
                        else if (downloadState == TaskStates.DOWNLOADING)
                            listener.onDownloadItemPauseClick(downloadTasks);
                    }
                    break;
                case R.id.item_download_trash_iv:
                    if (listener != null) {
                        if (downloadState == TaskStates.END) {
                            listener.onDownloadItemDeleteClick(downloadTasks);
                        }
                    }
                    break;
                case R.id.item_download_share_iv:
                    if (listener != null) {
                        if (downloadState == TaskStates.END) {
                            listener.onDownloadItemShareClick(downloadTasks);
                        }
                    }
                    break;
                default:
                    if (listener != null) {
                        if (downloadState == TaskStates.END) {
                            listener.onDownloadItemCompleteClick(downloadTasks);
                        }
                    }
                    break;
            }
        } else
            Log.d("onClick", "item deleted: ");
    }
}
