package com.tapi.downloadsocialvideo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.tapi.download.video.core.BaseActivity;
import com.tapi.download.video.core.view.PlayControllerView;
import com.tapi.downloader.database.elements.Task;
import com.tapi.downloadsocialvideo.R;
import com.tapi.downloadsocialvideo.util.Utils;

public class PlayVideoActivity extends BaseActivity {
    private PlayControllerView controllerView;

    private Task downloadTask;
    private String video_url;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_play_video;
    }

    @Override
    protected void findViewById() {
        controllerView = findViewById(R.id.play_video_controler);
    }


    @Override
    protected void onCreateInit(@Nullable Bundle savedInstanceState) {
        super.onCreateInit(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getDataIntent();
        initControllerView();
    }

    private void initControllerView() {
        if (video_url != null && !video_url.isEmpty()) {
            controllerView.setStories(false);
            controllerView.setUrl(video_url);
        }
    }

    private void getDataIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            downloadTask = (Task) intent.getSerializableExtra(Utils.TASK_DOWNLOAD);
            video_url = intent.getStringExtra(Utils.VIDEO_URL);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controllerView.releaseExoPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (controllerView != null)
            controllerView.playPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (controllerView != null)
            controllerView.pausePlayer();
    }
}