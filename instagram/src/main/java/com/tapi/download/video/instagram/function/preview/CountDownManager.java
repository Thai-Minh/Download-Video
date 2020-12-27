package com.tapi.download.video.instagram.function.preview;

import android.os.CountDownTimer;

public class CountDownManager {
    private static CountDownManager instance;
    private CountDownTimer countDownTimer;
    private OnTimerListener mListener;

    public CountDownManager() {
    }

    public static CountDownManager getInstance() {
        if (instance == null) {
            instance = new CountDownManager();
        }
        return instance;
    }

    public void setmListener(OnTimerListener listener) {
        this.mListener = listener;
    }

    public void removeListener() {
        mListener = null;
    }

    public void startTimer(long time) {
        if (countDownTimer == null) {
            createTimmer(time);
        } else {
            countDownTimer.cancel();
            createTimmer(time);
        }

    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void createTimmer(long time) {
        countDownTimer = new CountDownTimer(time, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (mListener != null) {
                    mListener.onFinish();
                }
            }
        }.start();
    }

    public interface OnTimerListener {
        void onFinish();
    }
}
