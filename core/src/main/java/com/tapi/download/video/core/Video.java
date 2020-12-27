package com.tapi.download.video.core;

import java.io.Serializable;
import java.util.ArrayList;

public class Video implements Serializable {
    private String imgUser;
    private String name;
    private String title;
    private String thumbnail;
    private int duration; // millisecond
    private ArrayList<DownloadLink> links;
    private String idVideo;

    public Video(String id, String imgUser, String name, String title, String thumbnail, int duration, ArrayList<DownloadLink> links) {
        this.idVideo = id;
        this.imgUser = imgUser;
        this.name = name;
        this.title = title;
        this.thumbnail = thumbnail;
        this.duration = duration;
        this.links = links;
    }

    public Video(String imgUser, String name, String title, String thumbnail, int duration, ArrayList<DownloadLink> links) {
        this.imgUser = imgUser;
        this.name = name;
        this.title = title;
        this.thumbnail = thumbnail;
        this.duration = duration;
        this.links = links;
    }

    public Video(String title, String thumbnail, ArrayList<DownloadLink> links) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.links = links;
    }

    public Video() {
    }

    public String getIdVideo() {
        return idVideo;
    }

    public void setIdVideo(String idVideo) {
        this.idVideo = idVideo;
    }

    public String getImgUser() {
        return imgUser;
    }

    public void setImgUser(String imgUser) {
        this.imgUser = imgUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<DownloadLink> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<DownloadLink> links) {
        this.links = links;
    }
}