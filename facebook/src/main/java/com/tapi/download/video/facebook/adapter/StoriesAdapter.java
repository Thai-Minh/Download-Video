package com.tapi.download.video.facebook.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.facebook.R;

import java.util.ArrayList;

public class StoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<Stories> strories;
    private IStroriesListener listener;

    public StoriesAdapter(Context mContext, ArrayList<Stories> strories) {
        this.mContext = mContext;
        this.strories = strories;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new ItemStoriesViewHolder(parent.getContext(), layoutInflater.inflate(R.layout.item_stories_1, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemStoriesViewHolder viewHolder = (ItemStoriesViewHolder) holder;
        viewHolder.bindView(strories.get(position), listener, position);
    }

    public void setListener(IStroriesListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return strories.size();
    }

    public void addData(ArrayList<Stories> stories) {
        strories.clear();
        strories.addAll(stories);
        notifyDataSetChanged();
    }
}
