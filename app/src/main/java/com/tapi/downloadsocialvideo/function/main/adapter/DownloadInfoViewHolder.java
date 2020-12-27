package com.tapi.downloadsocialvideo.function.main.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.utils.Utils;
import com.tapi.downloadsocialvideo.R;

import java.util.Locale;

public class DownloadInfoViewHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "DownloadInfoViewHolder";

    private TextView tvQuality, tvSize;
    private RelativeLayout rlItem;
    private Context mContext;
    private ImageView ivIcon;

    public DownloadInfoViewHolder(@NonNull View itemView, Context context) {
        super(itemView);
        this.mContext = context;
        tvQuality = itemView.findViewById(R.id.item_download_info_quality_tv);
        tvSize = itemView.findViewById(R.id.item_download_info_size_tv);
        rlItem = itemView.findViewById(R.id.item_download_info_group_rl);
        ivIcon = itemView.findViewById(R.id.item_download_info_iv);
    }

    public void bindView(DownloadLink downloadLink, boolean isEnd, OnDownloadInfoListener listener) {
        tvSize.setVisibility(isEnd ? View.GONE : View.VISIBLE);
        ivIcon.setImageResource(isEnd ? R.drawable.item_download_info_image_ic : R.drawable.item_download_info_ic);

        if (!isEnd) {
            int resolution = downloadLink.getResolution();
            double size = (double) downloadLink.getSize() / (1024 * 1024);

            if (size > 1024) {
                size = size / 1024;
                tvSize.setText(String.format(Locale.ENGLISH, "%.2f GB", size));
            } else if (size < 1) {
                size = size * 1024;
                tvSize.setText(String.format(Locale.ENGLISH, "%.2f KB", size));
            } else {
                tvSize.setText(String.format(Locale.ENGLISH, "%.2f MB", size));
            }

            tvQuality.setText(String.format(Locale.ENGLISH, mContext.getString(R.string.item_download_info_quality),
                    resolution < 0 ? (resolution == Utils.VIDEO_SD ? "SD" : "HD") : resolution + "P"));

        } else {
            tvQuality.setText(mContext.getString(R.string.item_download_watch));
        }

        rlItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    if (!isEnd)
                        listener.onDownloadVideo(downloadLink);
                    else listener.onWatchVideo();
                }
            }
        });
    }
}
