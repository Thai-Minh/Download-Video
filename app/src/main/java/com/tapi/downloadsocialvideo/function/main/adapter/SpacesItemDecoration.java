package com.tapi.downloadsocialvideo.function.main.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.utils.Utils;
import com.tapi.downloadsocialvideo.function.downloader.adapter.DownloadTaskAdapter;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private Context mContext;
    private int space;

    public SpacesItemDecoration(int space, Context context) {
        this.space = space;
        this.mContext = context;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (parent.getAdapter() != null) {
            int viewType = parent.getAdapter().getItemViewType(position);
            if (viewType != DownloadTaskAdapter.HEADER_TYPE) {
                outRect.bottom = space;
            } else {
                if (position == 0) {
                    outRect.bottom = Utils.convertDpToPixel(5f, mContext);
                    outRect.top = Utils.convertDpToPixel(5f, mContext);
                } else {
                    outRect.bottom = Utils.convertDpToPixel(5f, mContext);
                }
            }
        }

    }
}
