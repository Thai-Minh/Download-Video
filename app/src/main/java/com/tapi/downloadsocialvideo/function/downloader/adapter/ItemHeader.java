package com.tapi.downloadsocialvideo.function.downloader.adapter;

public class ItemHeader {
    private StateHeader stateHeader;

    public ItemHeader(StateHeader stateHeader) {
        this.stateHeader = stateHeader;
    }

    public StateHeader getStateHeader() {
        return stateHeader;
    }

    public void setStateHeader(StateHeader staeteHeader) {
        this.stateHeader = staeteHeader;
    }

    public enum StateHeader {
        STATE_DOWNLOADING, STATE_DOWNLOAD_END
    }
}
