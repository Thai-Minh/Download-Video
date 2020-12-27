package com.tapi.downloadsocialvideo.function.downloader.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.tapi.download.video.core.BaseFragment;
import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.activities.MainActivity;
import com.tapi.downloadsocialvideo.function.downloader.OnDownloadListener;
import com.tapi.downloadsocialvideo.function.downloader.adapter.DownloadTaskAdapter;
import com.tapi.downloadsocialvideo.function.main.adapter.SpacesItemDecoration;
import com.tapi.downloadsocialvideo.util.Utils;

import java.io.File;
import java.util.ArrayList;

public class DownloadFragment extends BaseFragment implements OnDownloadListener, DownloadTaskAdapter.OnDownloadItemListener, DialogConfirmDelete.OnDialogConFirm {
    private static final String TAG = "DownloadFragment";
    private Context mContext;
    private MainActivity mainActivity;
    private RecyclerView recyclerView;
    private LinearLayout llNoItemDownload;
    private LinearLayoutManager layout;
    private DownloadTaskAdapter taskAdapter;
    private ArrayList<Task> mListTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        setData();
        if (mListTask != null && !mListTask.isEmpty()) {
            onDownloadTaskListChange(mListTask);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
    }

    private void setData() {
        taskAdapter = new DownloadTaskAdapter(mContext, this);
        layout = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layout);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        SimpleItemAnimator itemAnimator = (SimpleItemAnimator) recyclerView.getItemAnimator();
        if (itemAnimator != null)
            itemAnimator.setSupportsChangeAnimations(false);
        recyclerView.addItemDecoration(new SpacesItemDecoration(com.tapi.download.video.core.utils.Utils.convertDpToPixel(10f, mContext), mContext));
        recyclerView.setAdapter(taskAdapter);
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.fragment_download_recycler);
        llNoItemDownload = view.findViewById(R.id.fragment_download_no_item_ll);
    }

    @Override
    public void onDownloadTaskListChange(ArrayList<Task> downloadTasks) {
        if (isAdded()) {
            llNoItemDownload.setVisibility(downloadTasks.isEmpty() ? View.VISIBLE : View.GONE);
            mListTask = downloadTasks;
            taskAdapter.update(downloadTasks);
        } else {
            mListTask = downloadTasks;
        }
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
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                taskAdapter.notifyItemDataChange(downloadTask);
                if (downloadTask.state == TaskStates.END) {
                    taskAdapter.update(mListTask);
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDownloadItemCancelClick(Task downloadTask, boolean isPause) {
        if (mainActivity != null) {
            if (!isPause)
                mainActivity.cancelDownload(downloadTask.id);
            else mainActivity.deleteDownload(downloadTask.id);
        }
    }

    @Override
    public void onDownloadItemDeleteClick(Task downloadTask) {
        DialogConfirmDelete.showDialogConfirm(mContext, this, downloadTask.id);
    }

    @Override
    public void onDownloadItemPauseClick(Task downloadTask) {
        if (mainActivity != null)
            mainActivity.pauseDownload(downloadTask.id);
    }

    @Override
    public void onDownloadItemResumeClick(Task downloadTask) {
        if (mainActivity != null)
            mainActivity.resumeDownload(downloadTask.id);
    }

    @Override
    public void onDownloadItemCompleteClick(Task downloadTask) {
        String filePath = downloadTask.save_address.concat(File.separator).concat(downloadTask.getFullName());
        if (!Utils.isFileDeleted(filePath)) {
            Utils.startPlayVideoActivity(mContext, downloadTask, filePath);
        } else {
            Toast.makeText(mContext, "File deleted!", Toast.LENGTH_SHORT).show();
            if (mainActivity != null)
                mainActivity.deleteDownload(downloadTask.id);
        }
    }

    @Override
    public void onDownloadItemShareClick(Task downloadTask) {
        String filePath = downloadTask.save_address.concat(File.separator).concat(downloadTask.getFullName());
        Utils.shareVideo(mContext, filePath);
    }

    @Override
    public void onConfirmOk(long mDownloadId) {
        if (mainActivity != null)
            mainActivity.deleteDownload(mDownloadId);
    }
}
