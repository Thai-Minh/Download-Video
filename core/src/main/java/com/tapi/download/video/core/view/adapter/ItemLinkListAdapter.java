package com.tapi.download.video.core.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapi.download.video.core.DownloadLink;
import com.tapi.download.video.core.R;

import java.util.ArrayList;
import java.util.Locale;

public class ItemLinkListAdapter extends RecyclerView.Adapter<ItemLinkListAdapter.ViewHoler> {

    private static final String TAG = "ItemLinkListAdapter";

    private Context mContext;
    private ArrayList<DownloadLink> linkList;
    private OnCallBack mListener;

    public ItemLinkListAdapter(Context mContext, ArrayList<DownloadLink> linkList, OnCallBack mListener) {
        this.mContext = mContext;
        this.linkList = linkList;
        this.mListener = mListener;
    }

    public void addData(ArrayList<DownloadLink> links) {
        linkList.clear();
        linkList.addAll(links);
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCustom = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_link_list_bottom_sheet, parent, false);
        return new ViewHoler(viewCustom);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoler holder, final int position) {

        DownloadLink link = linkList.get(position);

        int resolution = link.getResolution();

        if (resolution == -1) {
            holder.txtLinkInfo.setText(mContext.getResources().getString(R.string.item_link_quality_list) + " " + "HD");
        } else if (resolution == -2) {
            holder.txtLinkInfo.setText(mContext.getResources().getString(R.string.item_link_quality_list) + " " + "SD");
        } else {
            holder.txtLinkInfo.setText(mContext.getResources().getString(R.string.item_link_quality_list) + " " + resolution + "P");
        }

        double size = (double) link.getSize() / 1048576;

        if (size > 1024) {
            size = size / 1024;
            holder.txtLinkSize.setText(String.format(Locale.ENGLISH, "%.2f GB", size));
        } else if (size < 1) {
            size = size * 1024;
            holder.txtLinkSize.setText(String.format(Locale.ENGLISH, "%.2f KB", size));
        } else {
            holder.txtLinkSize.setText(String.format(Locale.ENGLISH, "%.2f MB", size));
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
        return linkList.size();
    }

    public class ViewHoler extends RecyclerView.ViewHolder {

        ImageView imgDownload;
        TextView txtLinkInfo, txtLinkSize;

        public ViewHoler(@NonNull View itemView) {
            super(itemView);
            imgDownload = itemView.findViewById(R.id.item_link_list_download_img);
            txtLinkInfo = itemView.findViewById(R.id.item_link_list_quality_txt);
            txtLinkSize = itemView.findViewById(R.id.item_link_list_size_txt);
        }
    }

    public interface OnCallBack {
        void onItemClicked(int position);
    }
}
