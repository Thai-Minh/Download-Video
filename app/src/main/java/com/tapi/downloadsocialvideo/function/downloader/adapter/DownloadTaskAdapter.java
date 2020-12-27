package com.tapi.downloadsocialvideo.function.downloader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.downloader.core.enums.TaskStates;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class DownloadTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HEADER_TYPE = 1;
    public static final int ITEM_TYPE = 2;
    private static final String TAG = "DownloadTaskAdapter";

    private List<Task> downloadTasks = new ArrayList<>();
    private ArrayList<Object> listSort;
    private Context context;
    private OnDownloadItemListener listener;

    public DownloadTaskAdapter(Context context, OnDownloadItemListener listener) {
        this.context = context;
        this.listener = listener;
        listSort = new ArrayList<>();
    }

    public void update(List<Task> tasks) {
        downloadTasks = tasks;
        sortListData();
    }

    public void notifyItemDataChange(Task task) {
        int i = listSort.indexOf(task);
        notifyItemChanged(i);
    }

    private synchronized void sortListData() {
        listSort.clear();
        if (downloadTasks != null && !downloadTasks.isEmpty()) {
            ArrayList<Task> listTaskDownloading = new ArrayList<>();
            ArrayList<Task> listTaskEnd = new ArrayList<>();
            ItemHeader itemHeader;
            for (Task task : downloadTasks) {
                if (task.state == TaskStates.END) {
                    if (task.videoTaskId == 0)
                        listTaskEnd.add(task);
                } else {
                    if (task.videoTaskId == 0)
                        listTaskDownloading.add(task);
                }
            }
            Collections.sort(listTaskDownloading, Task.SORT_ID);
            Collections.sort(listTaskEnd, Task.SORT_ID);
            if (listTaskDownloading.isEmpty()) {
                itemHeader = new ItemHeader(ItemHeader.StateHeader.STATE_DOWNLOAD_END);
                listSort.add(itemHeader);
                listSort.addAll(listTaskEnd);
            } else {
                itemHeader = new ItemHeader(ItemHeader.StateHeader.STATE_DOWNLOADING);
                listSort.add(itemHeader);
                listSort.addAll(listTaskDownloading);
                if (!listTaskEnd.isEmpty()) {
                    itemHeader = new ItemHeader(ItemHeader.StateHeader.STATE_DOWNLOAD_END);
                    listSort.add(itemHeader);
                    listSort.addAll(listTaskEnd);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (listSort.get(position) instanceof ItemHeader) {
            return HEADER_TYPE;
        }
        return ITEM_TYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER_TYPE) {
            return new ItemHeaderViewHolder(LayoutInflater.from(context).inflate(R.layout.item_download_header, parent, false), context);
        }
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_download, parent, false);
        return new ItemDownloadViewHolder(itemView, context, listener, listSort);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ItemDownloadViewHolder) {
            ItemDownloadViewHolder holder = (ItemDownloadViewHolder) viewHolder;
            Task task = (Task) listSort.get(position);
            if (task.videoTaskId != 0) { // audio task
                holder.itemView.setVisibility(View.GONE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
                holder.bind(task);
            }
        } else {
            ItemHeaderViewHolder itemHeader = (ItemHeaderViewHolder) viewHolder;
            ItemHeader header = (ItemHeader) listSort.get(position);
            itemHeader.bindView(header.getStateHeader());
        }
    }

    @Override
    public int getItemCount() {
        return listSort != null ? listSort.size() : 0;
    }

    public interface OnDownloadItemListener {
        void onDownloadItemCancelClick(Task downloadTask, boolean isPause);

        void onDownloadItemDeleteClick(Task downloadTask);

        void onDownloadItemPauseClick(Task downloadTask);

        void onDownloadItemResumeClick(Task downloadTask);

        void onDownloadItemCompleteClick(Task downloadTask);

        void onDownloadItemShareClick(Task downloadTask);

    }
}
