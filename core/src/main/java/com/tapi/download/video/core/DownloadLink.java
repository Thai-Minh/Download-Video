package com.tapi.download.video.core;

import java.io.Serializable;

public class DownloadLink implements Serializable {
    private String link;
    private String audioLink;
    private int size;
    private int resolution;

    public DownloadLink(String link, int resolution) {
        this.link = link;
        this.resolution = resolution;
    }

    public DownloadLink(String link, int size, int resolution) {
        this.link = link;
        this.size = size;
        this.resolution = resolution;
    }

    public DownloadLink() {
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getResolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }
}
