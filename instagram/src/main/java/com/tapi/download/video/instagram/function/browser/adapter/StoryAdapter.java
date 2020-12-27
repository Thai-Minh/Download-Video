package com.tapi.download.video.instagram.function.browser.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.tapi.download.video.instagram.R;
import com.tapi.download.video.core.model.StoriesInsta;

import java.util.ArrayList;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<StoriesInsta> listStories;
    private int width, height;
    private OnCallBack mListener;

    public StoryAdapter(OnCallBack mListener, Context context, ArrayList<StoriesInsta> listStories, int width) {
        this.mListener = mListener;
        this.context = context;
        this.listStories = listStories;
        this.width = width;
    }

    public void addData(ArrayList<StoriesInsta> stories) {
        listStories.clear();
        listStories.addAll(stories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View viewCustom = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stories, parent, false);
        return new ViewHolder(viewCustom);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.ViewHolder holder, final int position) {
        StoriesInsta storiesInsta = listStories.get(position);

        if (storiesInsta.getThumbnailStory() != null) {
            holder.bindView(width);
            RequestOptions requestOptions = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // because file name is always same
                    .skipMemoryCache(true)
                    .centerInside();

            Glide.with(context)
                    .load(storiesInsta.getThumbnailStory())
                    .apply(requestOptions)
                    .override(width, height)
                    .centerCrop()
                    .into(holder.imgThumnailStory);

            holder.txtTitle.setText(storiesInsta.getUserName());

            Glide.with(context)
                    .load(storiesInsta.getImageUser())
                    .apply(requestOptions)
                    .override(width, height)
                    .centerCrop()
                    .into(holder.imageUser);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listStories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumnailStory, imageUser;
        TextView txtTitle;
        RelativeLayout rl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rl = itemView.findViewById(R.id.rl);
            imgThumnailStory = itemView.findViewById(R.id.imgThumnailStory);
            imageUser = itemView.findViewById(R.id.imageUser);
            txtTitle = itemView.findViewById(R.id.txtUserName);
        }

        public void bindView(int width) {
            rl.getLayoutParams().height = width;
        }
    }

    public interface OnCallBack {
        void onItemClicked(int position);
    }

}
