package com.tapi.downloadsocialvideo.function.main.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.Video;
import com.tapi.downloadsocialvideo.R;

public class DownloadInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private Video video;
    private OnDownloadInfoListener listener;

    public DownloadInfoAdapter(Context mContext, Video video, OnDownloadInfoListener listener) {
        this.mContext = mContext;
        this.video = video;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DownloadInfoViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_download_info, parent, false), mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DownloadInfoViewHolder infoViewHolder = (DownloadInfoViewHolder) holder;
        boolean isEnd = position == getItemCount() - 1;
        infoViewHolder.bindView(isEnd ? null : video.getLinks().get(position), isEnd, listener);
    }

    @Override
    public int getItemCount() {
        return video == null || video.getLinks().isEmpty() ? 0 : video.getLinks().size() + 1;
    }
}
