package com.tapi.download.video.core.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.tapi.download.video.core.R;
import com.tapi.download.video.core.utils.Utils;

import java.util.Locale;


public class PlayControllerView extends RelativeLayout implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, View.OnTouchListener, CountDownManager.OnTimerListener {
    private static final String TAG = "PlayControllerView";
    private Context context;
    private SeekBar mSeekBar;
    private LinearLayout llGroupSeekbar;
    private TextView tvStart, tvEnd;
    private PlayerView videoView;
    private SimpleExoPlayer player;
    private int timeVideo;
    private ImageView ivPlay;
    private String url;
    private Handler handlerHiddenController = new Handler();
    private Handler handler = new Handler();
    private PlayControlState playControlState = PlayControlState.NONE;
    private float startAnimValue = 0.0f;
    private float endAnimValue = 1.0f;
    private long timeSave;
    private boolean isStop, isStories;
    private ValueAnimator playControlVisibilityAnimator;
    private ValueAnimator playControlHiddenAnimator;
    private RelativeLayout rlGroupController, rlPlayVideo, rlLoadingBuffer;
    private final ValueAnimator.AnimatorUpdateListener runningAnimVisibility = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

            float currValue = (float) animation.getAnimatedValue();
            if (currValue == endAnimValue) {
                startAnimValue = 0.0f;
                endAnimValue = 1.0f;
                playControlState = PlayControlState.VISIBLE;
            } else {
                if (playControlState == PlayControlState.RUNNING_ANIM_VISIBILITY) {
                    rlGroupController.setAlpha(currValue);//
                    rlPlayVideo.setAlpha(currValue);//
                } else {
                    if (playControlState == PlayControlState.RUNNING_ANIM_HIDDEN) {
                        startAnimValue = 0f;
                        endAnimValue = currValue;
                        // cancel this animation
                        playControlVisibilityAnimator.cancel();
                        createPlayControlHiddenAnimator(startAnimValue, endAnimValue);
                        playControlHiddenAnimator.start();
                    }
                }
            }
        }
    };
    private final ValueAnimator.AnimatorUpdateListener runningAnimHidden = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float currValue = (float) animation.getAnimatedValue();
            if (currValue == endAnimValue) {
                startAnimValue = 0.0f;
                endAnimValue = 1.0f;
                playControlState = PlayControlState.HIDDEN;
            } else {
                if (playControlState == PlayControlState.RUNNING_ANIM_HIDDEN) {
                    rlGroupController.setAlpha(endAnimValue - currValue);//
                    rlPlayVideo.setAlpha(endAnimValue - currValue);//
                } else {
                    if (playControlState == PlayControlState.RUNNING_ANIM_VISIBILITY) {
                        startAnimValue = endAnimValue - currValue;
                        endAnimValue = 0f;
                        playControlHiddenAnimator.cancel();
                        createPlayControlVisibilityAnimator(startAnimValue, endAnimValue);
                        playControlVisibilityAnimator.start();

                    }
                }
            }
        }
    };
    private Runnable runnableHiddenController = new Runnable() {
        @Override
        public void run() {
            if (playControlState == PlayControlState.NONE) {
                playControlState = PlayControlState.RUNNING_ANIM_HIDDEN;
                playControlHiddenAnimator.start();
            }
        }
    };
    private Runnable updateUiControler = new Runnable() {
        @Override
        public void run() {
            int currentPosition = (int) player.getCurrentPosition();
            setCurrenTime(currentPosition);
            if (!isStop)
                postDelayed(this, 500);
        }
    };

    public PlayControllerView(Context context) {
        this(context, null);
    }

    public PlayControllerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initView();
    }

    public void setStories(boolean stories) {
        isStories = stories;
        rlPlayVideo.setVisibility(isStories ? GONE : VISIBLE);
    }

    private final void createPlayControlVisibilityAnimator(float startValue, float endValue) {
        if (playControlVisibilityAnimator != null) {
            playControlVisibilityAnimator.removeAllUpdateListeners();
        }
        playControlVisibilityAnimator = ValueAnimator.ofFloat(startValue, endValue);
        playControlVisibilityAnimator.setDuration(500);
        playControlVisibilityAnimator.addUpdateListener(runningAnimVisibility);
        playControlVisibilityAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                rlGroupController.setVisibility(View.VISIBLE);//
                rlPlayVideo.setVisibility(View.VISIBLE);//
            }
        });

    }

    private final void createPlayControlHiddenAnimator(float startValue, float endValue) {
        if (playControlHiddenAnimator != null) {
            playControlHiddenAnimator.removeAllUpdateListeners();
        }
        playControlHiddenAnimator = ValueAnimator.ofFloat(startValue, endValue);
        playControlHiddenAnimator.setDuration(500);
        playControlHiddenAnimator.addUpdateListener(runningAnimHidden);
        playControlHiddenAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rlGroupController.setVisibility(View.GONE);//
                rlPlayVideo.setVisibility(View.GONE);//
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }
        });
    }

    private void initVideoView() {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Utils.getUserAgent());
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(url));
        player = new SimpleExoPlayer.Builder(context).build();
        videoView.setPlayer(player);
        player.prepare(videoSource);
        player.setPlayWhenReady(true);
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == ExoPlayer.STATE_READY) {
                    setMaxSeekBar((int) player.getDuration());
                    playPlayer();
                    handler.post(updateUiControler);
                    isStop = false;
                    if (!isStories)
                        handlerHiddenController.postDelayed(runnableHiddenController, 2000);
                }
                switch (playbackState) {
                    case Player.STATE_IDLE:
                        Log.e(TAG, "onPlayerStateChanged: STATE_IDLE");
                        break;
                    case Player.STATE_BUFFERING:
                        rlLoadingBuffer.setVisibility(View.VISIBLE);
                        Log.e(TAG, "onPlayerStateChanged: STATE_BUFFERING");
                        break;
                    case Player.STATE_READY:
                        rlLoadingBuffer.setVisibility(View.GONE);
                        Log.e(TAG, "onPlayerStateChanged: STATE_READY");
                        break;
                    case Player.STATE_ENDED:
                        isStop = true;
                        pausePlayer();
                        player.seekTo(0);
                        ivPlay.setImageResource(R.drawable.fragment_common_play_bt);//fragment_common_play_iv
                        setPaddingView(true);
                        break;
                }
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.e(TAG, "onPlayerError: " + error.getMessage());
            }
        });


    }

    public void setMaxSeekBar(int second) {
        mSeekBar.setMax(second);
        this.timeVideo = second;
        second = second / 1000;
        tvEnd.setText(changeTextTime(second));
        tvStart.getLayoutParams().width = (int) Utils.getWidthTextPlayController(changeTextTime(second), context) +
                Utils.convertDpToPixel(8, context);
        tvEnd.getLayoutParams().width = (int) Utils.getWidthTextPlayController(changeTextTime(second), context) +
                (int) Utils.getWidthTextPlayController("-", context) + Utils.convertDpToPixel(8, context);
    }

    public void setCurrenTime(int second) {
        mSeekBar.setProgress(second);
        changeTime(second);
    }

    private void initView() {
        CountDownManager.getInstance().setmListener(this);
        inflate(context, R.layout.play_controller_view, this);
        mSeekBar = findViewById(R.id.play_controller_seekbar);
        tvStart = findViewById(R.id.play_controller_start_time_tv);
        tvEnd = findViewById(R.id.play_controller_end_time_tv);
        ivPlay = findViewById(R.id.play_controller_play_1_iv);//
        mSeekBar.setOnSeekBarChangeListener(this);
        Utils.changeColorSeekbar(getContext(), mSeekBar);
        rlLoadingBuffer = findViewById(R.id.play_controller_loading_rl);
        rlGroupController = findViewById(R.id.play_controller_group_ll);
        rlGroupController.setOnClickListener(this);
        llGroupSeekbar = findViewById(R.id.play_controller_group_download_ll);
        videoView = findViewById(R.id.play_controller_video_view);
        rlPlayVideo = findViewById(R.id.play_controller_group_click_rl);//
        rlPlayVideo.setOnClickListener(this);//
        findViewById(R.id.play_controller_group_all_rl).setOnTouchListener(this);
        createPlayControlVisibilityAnimator(startAnimValue, endAnimValue);
        createPlayControlHiddenAnimator(startAnimValue, endAnimValue);

        rlLoadingBuffer.setVisibility(View.VISIBLE);
    }

    private void setPaddingView(boolean isPlay) {
        Utils.setPaddingLeftView(context, ivPlay, isPlay ? 5 : 0);//
    }

    public void setUrl(String url) {
        this.url = url;
        releaseExoPlayer();
        initVideoView();
    }

    @Override
    public void onClick(View v) {
        if (playControlState != PlayControlState.HIDDEN) {
            int id = v.getId();
            if (id == R.id.play_controller_group_click_rl) {
                if (player.isPlaying()) {
                    pausePlayer();
                    ivPlay.setImageResource(R.drawable.fragment_common_play_bt);
                    setPaddingView(true);
                } else {
                    playPlayer();
                    ivPlay.setImageResource(R.drawable.controller_pause_ic);
                    setPaddingView(false);
                }
            }
            CountDownManager.getInstance().setmListener(this);
            CountDownManager.getInstance().startTimer(2500);
        } else {
            startAnimHidden();
        }

    }

    public void playPlayer() {
        if (player != null)
            player.setPlayWhenReady(true);
    }

    public void stopPlayer() {
        if (player != null)
            player.stop();
    }

    public void pausePlayer() {
        if (player != null)
            player.setPlayWhenReady(false);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            player.seekTo(progress);
            changeTime(progress);
            if (!player.isPlaying()){
                playPlayer();
            }
            if (!isStories) {
                CountDownManager.getInstance().setmListener(this);
                CountDownManager.getInstance().startTimer(2500);
            }
        }
    }

    private void changeTime(int second) {
        if (second <= timeVideo) {
            tvStart.setText(changeTextTime(second));
            tvEnd.setText(String.format(Locale.ENGLISH, "-%s", changeTextTime(timeVideo - second)));
        }
    }

    private String changeTextTime(int seconds) {
        seconds = seconds / 1000;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int second = seconds % 60;
        return String.format(Locale.ENGLISH, "%s%d:%02d", hours == 0 ? "" : hours + ":",
                minutes, second);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (!isStories) {
            Log.e(TAG, "onStopTrackingTouch: " );
            CountDownManager.getInstance().setmListener(this);
            CountDownManager.getInstance().startTimer(2500);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!isStories) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    long l = System.currentTimeMillis();
                    if (l - timeSave >= 500) {
                        timeSave = l;
                        switch (playControlState) {
                            case NONE:
                                if (handlerHiddenController != null && runnableHiddenController != null) {
                                    handlerHiddenController.removeCallbacks(runnableHiddenController);
                                    handlerHiddenController.post(runnableHiddenController);
                                }
                                break;
                            case VISIBLE:
                                playControlState = PlayControlState.RUNNING_ANIM_HIDDEN;
                                playControlVisibilityAnimator.cancel();
                                createPlayControlHiddenAnimator(startAnimValue, endAnimValue);
                                playControlHiddenAnimator.start();
                                CountDownManager.getInstance().stopTimer();
                                break;
                            case HIDDEN:
                                startAnimHidden();
                                break;
                            case RUNNING_ANIM_HIDDEN:
                                playControlState = PlayControlState.RUNNING_ANIM_VISIBILITY;
                                break;
                            case RUNNING_ANIM_VISIBILITY:
                                playControlState = PlayControlState.RUNNING_ANIM_HIDDEN;
                                break;
                        }
                    }

                    break;
            }
        }
        return true;
    }

    private void startAnimHidden() {
        playControlState = PlayControlState.RUNNING_ANIM_VISIBILITY;
        playControlHiddenAnimator.cancel();
        createPlayControlVisibilityAnimator(startAnimValue, endAnimValue);
        playControlVisibilityAnimator.start();
        CountDownManager.getInstance().setmListener(this);
        CountDownManager.getInstance().startTimer(2500);
    }

    public void releaseExoPlayer() {
        isStories = false;
        CountDownManager.getInstance().removeListener();
        CountDownManager.getInstance().stopTimer();
        if (player != null) {
            player.stop();
            player.release();
        }
        if (handler != null && updateUiControler != null) {
            handler.removeCallbacks(updateUiControler);
        }
    }

    @Override
    public void onFinish() {
        Log.e(TAG, "onFinish: ");
        playControlState = PlayControlState.RUNNING_ANIM_HIDDEN;
        playControlVisibilityAnimator.cancel();
        createPlayControlHiddenAnimator(startAnimValue, endAnimValue);
        playControlHiddenAnimator.start();
    }

    enum PlayControlState {
        NONE,
        RUNNING_ANIM_VISIBILITY,
        VISIBLE,
        RUNNING_ANIM_HIDDEN,
        HIDDEN
    }
    public void showHideGroupSeekbar(boolean isShow){
        if (isShow){
            Utils.animationChangeAlpha(llGroupSeekbar, 0, 1, true);
        }else {
            Utils.animationChangeAlpha(llGroupSeekbar, 1, 0, true);
        }
    }
}
