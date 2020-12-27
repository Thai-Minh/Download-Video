package com.tapi.download.video.facebook.adapter;

import android.os.Parcel;
import android.os.Parcelable;

public class Stories implements Parcelable {
    public static final Creator<Stories> CREATOR = new Creator<Stories>() {
        @Override
        public Stories createFromParcel(Parcel in) {
            return new Stories(in);
        }

        @Override
        public Stories[] newArray(int size) {
            return new Stories[size];
        }
    };
    private String imageStory;
    private String title;
    private String imageProfile;
    private String dataBucketId;
    private String threadId;
    private String endCursor;
    private String traySessionId;
    private int size;
    private String lastTime;
    private String[] listThreadId;
    private String type;
    private String imageStoryHd;
    private String videoStory;
    private int mVideoSize;
    private int duration;

    public Stories(String imageStory, String title, String imageProfile, String dataBucketId, String threadId, String endCursor, String traySessionId) {
        this.imageStory = imageStory;
        this.title = title;
        this.imageProfile = imageProfile;
        this.dataBucketId = dataBucketId;
        this.threadId = threadId;
        this.endCursor = endCursor;
        this.traySessionId = traySessionId;
    }

    public String getVideoStory() {
        return videoStory;
    }

    public void setVideoStory(String videoStory) {
        this.videoStory = videoStory;
    }

    public Stories() {
    }

    public int getmVideoSize() {
        return mVideoSize;
    }

    public void setmVideoSize(int mVideoSize) {
        this.mVideoSize = mVideoSize;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    protected Stories(Parcel in) {
        imageStory = in.readString();
        title = in.readString();
        imageProfile = in.readString();
        dataBucketId = in.readString();
        threadId = in.readString();
        endCursor = in.readString();
        traySessionId = in.readString();
        size = in.readInt();
        mVideoSize = in.readInt();
        duration = in.readInt();
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getImageStory() {
        return imageStory;
    }

    public void setImageStory(String imageStory) {
        this.imageStory = imageStory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(String imageProfile) {
        this.imageProfile = imageProfile;
    }

    public String getDataBucketId() {
        return dataBucketId;
    }

    public void setDataBucketId(String dataBucketId) {
        this.dataBucketId = dataBucketId;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getEndCursor() {
        return endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    public String getTraySessionId() {
        return traySessionId;
    }

    public void setTraySessionId(String traySessionId) {
        this.traySessionId = traySessionId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String[] getListThreadId() {
        return listThreadId;
    }

    public void setListThreadId(String[] listThreadId) {
        this.listThreadId = listThreadId;
    }

    public String getImageStoryHd() {
        return imageStoryHd;
    }

    public void setImageStoryHd(String imageStoryHd) {
        this.imageStoryHd = imageStoryHd;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageStory);
        dest.writeString(title);
        dest.writeString(imageProfile);
        dest.writeString(dataBucketId);
        dest.writeString(threadId);
        dest.writeString(endCursor);
        dest.writeString(traySessionId);
        dest.writeInt(size);
        dest.writeString(lastTime);
        dest.writeStringArray(listThreadId);
        dest.writeString(type);
        dest.writeString(imageStoryHd);
        dest.writeString(videoStory);
        dest.writeInt(mVideoSize);
        dest.writeInt(duration);
    }
}
