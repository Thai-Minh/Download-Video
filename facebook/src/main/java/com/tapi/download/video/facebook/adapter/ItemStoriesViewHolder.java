package com.tapi.download.video.facebook.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tapi.download.video.facebook.R;
import com.tapi.download.video.facebook.utils.Utils;

public class ItemStoriesViewHolder extends RecyclerView.ViewHolder {
    private Context mContext;
    private RelativeLayout relativeLayout;
    private ImageView imageStory, imageProfile;
    private TextView tvTitle;
    private int mWidthSize;
    private float mRatio;

    public ItemStoriesViewHolder(Context context, @NonNull View itemView) {
        super(itemView);
        this.mContext = context;
        relativeLayout = itemView.findViewById(R.id.item_story_rl);
        imageStory = itemView.findViewById(R.id.item_story_iv);
        imageProfile = itemView.findViewById(R.id.item_story_profile_iv);
        tvTitle = itemView.findViewById(R.id.item_story_title_tv);
        mWidthSize = (Utils.getWidthScreen(mContext) - ((Utils.convertDpToPixel(2, mContext) * 4) + Utils.convertDpToPixel(4, mContext) * 2)) / 3;
        mRatio = (float) Utils.convertDpToPixel(170, mContext) / Utils.convertDpToPixel(102, mContext);
    }

    public void bindView(Stories strories, final IStroriesListener listener, final int pos) {
        relativeLayout.getLayoutParams().width = mWidthSize;
        relativeLayout.getLayoutParams().height = (int) (mWidthSize * mRatio);
        tvTitle.setText(strories.getTitle());
        Glide.with(mContext).load(strories.getImageStory()).into(imageStory);
        Glide.with(mContext).load(strories.getImageProfile()).into(imageProfile);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onClickItem(pos);
            }
        });
    }
}
