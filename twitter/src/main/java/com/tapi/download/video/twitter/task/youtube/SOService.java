package com.tapi.download.video.twitter.task.youtube;

import com.tapi.download.video.core.Video;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface SOService {
    @GET("youtube?url=")
    Call<String> getAnswers();

    @GET
    Call<String> getAnswers(@Url String url);
}
