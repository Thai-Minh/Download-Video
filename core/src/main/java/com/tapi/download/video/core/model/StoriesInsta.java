package com.tapi.download.video.core.model;

import com.tapi.download.video.core.DownloadLink;

import java.io.Serializable;
import java.util.ArrayList;

public class StoriesInsta implements Serializable {

    private String id;
    private String thumbnailStory;
    private String userName;
    private String imageUser;
    private int size;
    private String reelsID;
    private String lastTime;
    private String strIntoItems;
    private String type;
    private int duration;
    private ArrayList<DownloadLink> linkVideoStory;

    public StoriesInsta() {

    }

    public StoriesInsta(String thumbnailStory, String userName, String imageUser, String reelsID, String lastTime) {
        this.thumbnailStory = thumbnailStory;
        this.userName = userName;
        this.imageUser = imageUser;
        this.reelsID = reelsID;
        this.lastTime = lastTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThumbnailStory() {
        return thumbnailStory;
    }

    public void setThumbnailStory(String thumbnailStory) {
        this.thumbnailStory = thumbnailStory;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUser() {
        return imageUser;
    }

    public void setImageUser(String imageUser) {
        this.imageUser = imageUser;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getReelsID() {
        return reelsID;
    }

    public void setReelsID(String reelsID) {
        this.reelsID = reelsID;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getStrIntoItems() {
        return strIntoItems;
    }

    public void setStrIntoItems(String strIntoItems) {
        this.strIntoItems = strIntoItems;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<DownloadLink> getLinkVideoStory() {
        return linkVideoStory;
    }

    public void setLinkVideoStory(ArrayList<DownloadLink> linkVideoStory) {
        this.linkVideoStory = linkVideoStory;
    }
}
