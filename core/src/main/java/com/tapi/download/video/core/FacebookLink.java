package com.tapi.download.video.core;

public class FacebookLink extends DownloadLink {
    private String audioLink;

    public FacebookLink(String link, int resolution) {
        super(link, resolution);
    }

    public FacebookLink(String link, int size, int resolution) {
        super(link, size, resolution);
    }

    public String getAudioLink() {
        return audioLink;
    }

    public void setAudioLink(String audioLink) {
        this.audioLink = audioLink;
    }
}
