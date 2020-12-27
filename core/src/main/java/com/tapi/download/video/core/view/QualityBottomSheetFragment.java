package com.tapi.download.video.core.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.R;
import com.tapi.download.video.core.Video;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.download.video.core.view.adapter.ItemLinkListAdapter;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;

import java.util.ArrayList;
import java.util.Locale;

public class QualityBottomSheetFragment extends BottomSheetDialogFragment implements ItemLinkListAdapter.OnCallBack, View.OnClickListener {

    private static final String TAG = "QualityBottomSheet";

    private ArrayList<DownloadLink> linkList;
    private ItemLinkListAdapter adapter;
    private Task task;
    private ArrayList<Task> mListTask;

    private RecyclerView rvLinkList;
    private ImageView imgThumbnail, imgActionDownload, imgCancel;
    private TextView txtTitle, txtDuration, txtProgress, txtFileData, txtSizeData;
    private DownLoadProgressbar downLoadProgressbar;
    private LinearLayout viewGroupProgressLl;
    private LinearLayout viewGroupFinishLl;
    private RelativeLayout viewGroupDownloadedRl;
    private Video video;

    private Context mContext;
    private static boolean isShowing;

    private BroadcastReceiver broadcastReceiver;

    public QualityBottomSheetFragment(Video videos, boolean isShowing, Task task) {
        this.video = videos;
        QualityBottomSheetFragment.isShowing = isShowing;
        this.task = task;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.quality_bottom_sheet, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

        linkList = video.getLinks();
        listenerActionDownload();

        if (task != null) {
            Log.e(TAG, "onViewCreated: 1");
            setTaskChange(task);
        } else {
            Log.e(TAG, "onViewCreated: 2");
            rvLinkList.setVisibility(View.VISIBLE);
            viewGroupDownloadedRl.setVisibility(View.GONE);
        }

        initRecycleView();

        //setView
        txtTitle.setText(video.getTitle());
        if (video.getDuration() < 60000) {
            txtDuration.setText(video.getDuration() / 1000 + "s");
        } else {
            txtDuration.setText(Utils.getDurationString(video.getDuration()));
        }
        Utils.loadThumbnail(mContext, video.getThumbnail(), imgThumbnail);
    }

    @Override
    public void onItemClicked(int position) {
        Utils.sendActionStartDownload(mContext, video, position, Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_START);

        viewGroupProgressLl.setVisibility(View.VISIBLE);
        imgCancel.setVisibility(View.VISIBLE);
        rvLinkList.setVisibility(View.GONE);
        imgActionDownload.setImageResource(R.drawable.core_bottom_sheet_pause_white_imv);
    }

    private void initView(View view) {
        rvLinkList = view.findViewById(R.id.bottom_sheet_video_list_rv);
        imgThumbnail = view.findViewById(R.id.bottom_sheet_thumbnail_imv);
        txtTitle = view.findViewById(R.id.bottom_sheet_title_txt);
        txtDuration = view.findViewById(R.id.bottom_sheet_duration_txt);
        txtProgress = view.findViewById(R.id.bottom_sheet_progress_txt);
        txtFileData = view.findViewById(R.id.bottom_sheet_file_data_txt);
        txtSizeData = view.findViewById(R.id.bottom_sheet_size_data_txt);
        imgActionDownload = view.findViewById(R.id.bottom_sheet_action_download_img);
        imgCancel = view.findViewById(R.id.bottom_sheet_cancel_imv);
        viewGroupFinishLl = view.findViewById(R.id.bottom_sheet_viewgroup_finish_ll);
        viewGroupProgressLl = view.findViewById(R.id.bottom_sheet_viewgroup_progress_ll);
        viewGroupDownloadedRl = view.findViewById(R.id.bottom_sheet_downloaded_ll);
        downLoadProgressbar = view.findViewById(R.id.bottom_sheet_progress_pb);

        imgCancel.setOnClickListener(this);
        imgActionDownload.setOnClickListener(this);
    }

    private void initRecycleView() {
        adapter = new ItemLinkListAdapter(mContext, linkList, this);
        rvLinkList.setHasFixedSize(true);
        rvLinkList.setLayoutManager(new GridLayoutManager(mContext, 1, GridLayoutManager.VERTICAL, false));
        rvLinkList.setAdapter(adapter);

        rvLinkList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {

                }
            }
        });
    }

    public static void showBottomSheet(FragmentManager manager, Video video, Task task) {
        if (!QualityBottomSheetFragment.isShowing) {
            QualityBottomSheetFragment qualityBottomSheetFragment = new QualityBottomSheetFragment(video, true, task);
            qualityBottomSheetFragment.show(manager, qualityBottomSheetFragment.getTag());
        }
    }

    public static void showBottomSheet(FragmentManager manager, Video video) {
        if (!QualityBottomSheetFragment.isShowing) {
            QualityBottomSheetFragment qualityBottomSheetFragment = new QualityBottomSheetFragment(video, true, null);
            qualityBottomSheetFragment.show(manager, qualityBottomSheetFragment.getTag());
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.bottom_sheet_action_download_img) {
            Utils.sendTaskLocalBroadcast(mContext, Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_PAUSE);
        } else if (id == R.id.bottom_sheet_cancel_imv) {
            Utils.sendTaskLocalBroadcast(mContext, Utils.BOTTOM_SHEET_ACTION_DOWNLOAD_CANCEL);
            dismiss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(broadcastReceiver);
    }

    private void listenerActionDownload() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Utils.CALLBACK_ACTION_DOWNLOAD_STATE);
        intentFilter.addAction(Utils.CALLBACK_ACTION_DOWNLOAD_END);
        intentFilter.addAction(Utils.CALLBACK_ACTION_DOWNLOAD_PAUSE);
        intentFilter.addAction(Utils.CALLBACK_ACTION_DOWNLOAD_DOWNLOADING);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                int progress;

                if (action != null && !action.isEmpty()) {
                    switch (action) {
                        case Utils.CALLBACK_ACTION_DOWNLOAD_STATE:
                            Log.e(TAG, "onReceive: CALLBACK_ACTION_DOWNLOAD_STATE " );
                            task = (Task) intent.getSerializableExtra(Utils.BOTTOM_SHEET_PERCENT);

                            checkTaskState(task);

                            progress = task.percentCommon > -1 ? task.percentCommon : task.percent;
                            if (task.state == TaskStates.END) {
                                downLoadProgressbar.setProgress(100);
                                viewGroupProgressLl.setVisibility(View.GONE);
                                viewGroupFinishLl.setVisibility(View.VISIBLE);
                                imgCancel.setVisibility(View.INVISIBLE);
                            } else {
                                txtProgress.setText(String.format(Locale.ENGLISH, "%d%% ", progress));
                                downLoadProgressbar.setProgress(progress);
                            }
                            break;
                        case Utils.CALLBACK_ACTION_DOWNLOAD_PAUSE:
                            Log.e(TAG, "onReceive: CALLBACK_ACTION_DOWNLOAD_PAUSE " );
                            imgActionDownload.setImageResource(R.drawable.core_bottom_sheet_download_while_imv);
                            task = (Task) intent.getSerializableExtra(Utils.BOTTOM_SHEET_PERCENT);

                            checkTaskState(task);

                            progress = task.percentCommon > -1 ? task.percentCommon : task.percent;
                            txtProgress.setText(String.format(Locale.ENGLISH, "%d%% ", progress));
                            downLoadProgressbar.setProgress(progress);
                            break;
                        case Utils.CALLBACK_ACTION_DOWNLOAD_DOWNLOADING:
                            Log.e(TAG, "onReceive: CALLBACK_ACTION_DOWNLOAD_DOWNLOADING " );
                            imgActionDownload.setImageResource(R.drawable.core_bottom_sheet_pause_white_imv);
                            task = (Task) intent.getSerializableExtra(Utils.BOTTOM_SHEET_PERCENT);

                            checkTaskState(task);

                            progress = task.percentCommon > -1 ? task.percentCommon : task.percent;
                            txtProgress.setText(String.format(Locale.ENGLISH, "%d%% ", progress));
                            downLoadProgressbar.setProgress(progress);
                            break;
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, intentFilter);
    }

    public void setTaskChange(Task taskChange) {
        this.task = taskChange;
        checkTaskState(taskChange);
        int progress = taskChange.percentCommon > -1 ? taskChange.percentCommon : taskChange.percent;
        if (taskChange.state == TaskStates.END) {
            downLoadProgressbar.setProgress(100);
            viewGroupProgressLl.setVisibility(View.GONE);
            viewGroupFinishLl.setVisibility(View.VISIBLE);
            imgCancel.setVisibility(View.INVISIBLE);
        } else {
            txtProgress.setText(String.format(Locale.ENGLISH, "%d%% ", progress));
            downLoadProgressbar.setProgress(progress);
            if (taskChange != null) {
                senBroadcastResume(mContext, taskChange);
            }
        }
        int downloadState = taskChange.state;
        if (downloadState == TaskStates.DOWNLOADING) {
            imgActionDownload.setVisibility(View.VISIBLE);
            imgActionDownload.setImageResource(R.drawable.core_bottom_sheet_pause_white_imv);
        } else if (downloadState == TaskStates.PAUSED) {
            imgActionDownload.setVisibility(View.VISIBLE);
            imgActionDownload.setImageResource(R.drawable.core_bottom_sheet_download_while_imv);
        }

        if (downloadState == TaskStates.END) {
            viewGroupDownloadedRl.setVisibility(View.VISIBLE);
            changeStateView(StateDownLoadInFo.DOWNLOAD_END);
            viewGroupFinishLl.setVisibility(View.GONE);
            viewGroupProgressLl.setVisibility(View.GONE);
            txtFileData.setText(task.getFullName());
            float v = (float) (task.size * 0.000001);
            txtSizeData.setText(String.format(Locale.ENGLISH, mContext.getString(R.string.item_bottom_sheet_size), v));
        } else {
            viewGroupProgressLl.setVisibility(View.VISIBLE);
            viewGroupDownloadedRl.setVisibility(View.GONE);
            viewGroupFinishLl.setVisibility(View.GONE);
        }
    }

    private void checkTaskState(Task task) {
        if (task.state != TaskStates.END) {
            changeStateView(StateDownLoadInFo.DOWNLOAD);
        }
    }

    private void changeStateView(StateDownLoadInFo state) {
        switch (state) {
            case NORMAL:
                rvLinkList.setVisibility(View.VISIBLE);
                imgCancel.setVisibility(View.INVISIBLE);
                viewGroupDownloadedRl.setVisibility(View.GONE);
                viewGroupFinishLl.setVisibility(View.GONE);
                viewGroupProgressLl.setVisibility(View.GONE);
                break;
            case DOWNLOAD:
                viewGroupProgressLl.setVisibility(View.VISIBLE);
                imgCancel.setVisibility(View.VISIBLE);
                viewGroupFinishLl.setVisibility(View.GONE);
                viewGroupDownloadedRl.setVisibility(View.GONE);
                rvLinkList.setVisibility(View.GONE);
                break;
            case DOWNLOAD_END:
                viewGroupFinishLl.setVisibility(View.VISIBLE);
                viewGroupDownloadedRl.setVisibility(View.VISIBLE);
                viewGroupProgressLl.setVisibility(View.GONE);
                imgCancel.setVisibility(View.INVISIBLE);
                rvLinkList.setVisibility(View.GONE);
                break;
        }
    }

    public enum StateDownLoadInFo {
        NORMAL, DOWNLOAD, DOWNLOAD_END
    }

    private void senBroadcastResume(Context context, Task task) {
        Intent intent = new Intent(Utils.ACTION_RESUME_DOWNLOAD);
        intent.putExtra(Utils.INTENT_ACTION_RESUME_DOWNLOAD, task);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        QualityBottomSheetFragment.isShowing = false;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        QualityBottomSheetFragment.isShowing = false;
    }
}
