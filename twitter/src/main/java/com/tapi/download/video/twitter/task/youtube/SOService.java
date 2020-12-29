package com.tapi.download.video.twitter.task.youtube;

import com.tapi.download.video.core.Video;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface SOService {
    @GET("/instagram?")
    Call<String> getVideoInstagramInfo(@Query("url") String tags);

    @GET("/youtube?")
    Call<String> getVideoYoutubeInfo(@Query("url") String tags);
}
