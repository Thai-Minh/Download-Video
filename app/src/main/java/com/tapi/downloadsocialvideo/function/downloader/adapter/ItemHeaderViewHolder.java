package com.tapi.downloadsocialvideo.function.downloader.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.downloadsocialvideo.R;

public class ItemHeaderViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private TextView tvHeader;

    public ItemHeaderViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.mContext = context;
        tvHeader = itemView.findViewById(R.id.item_download_header_tv);
    }

    public void bindView(ItemHeader.StateHeader stateHeader) {
        tvHeader.setText(mContext.getString(stateHeader == ItemHeader.StateHeader.STATE_DOWNLOAD_END ?
                R.string.item_download_header_end : R.string.item_download_header_downloading));
    }
}
