package com.tapi.download.video.instagram.function.preview;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.tapi.download.video.instagram.utils.Utils;

public class OnSwipeTouchListener {
    private static final String TAG = "OnSwipeTouchListener";
    private static OnSwipeTouchListener instance;
    private int widthXScreen;
    private GestureDetector gestureDetector;
    private OnViewTouchListener listener;
    private boolean isLongPress;
    private boolean isDoubleClick;

    public OnSwipeTouchListener(Context c, OnViewTouchListener listener) {
        this.listener = listener;
        GestureListener gestureListener = new GestureListener();
        gestureDetector = new GestureDetector(c, gestureListener);
        widthXScreen = Utils.getWidthScreen(c);
    }

    public static OnSwipeTouchListener getInstance(Context context, OnViewTouchListener listener) {
        if (instance == null) {
            instance = new OnSwipeTouchListener(context, listener);
        }
        return instance;
    }

    public boolean onTouch(final MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            if (isLongPress && listener != null) {
                isDoubleClick = true;
                listener.onClickState(StateEvent.CLICK_UP);
                isLongPress = false;
                return true;
            }
        }
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public void removeInstance() {
        instance = null;
    }


    public enum StateEvent {
        CLICK_LEFT, CLICK_RIGHT, CLICK_UP, SWIPE_DOWN, LONG_CLICK
    }

    public interface OnViewTouchListener {
        void onClickState(StateEvent event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            isDoubleClick = false;
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (e.getX() > widthXScreen >> 1) {
                if (listener != null) {
                    isDoubleClick = true;
                    listener.onClickState(StateEvent.CLICK_RIGHT);
                }
            } else {
                if (listener != null) {
                    isDoubleClick = true;
                    listener.onClickState(StateEvent.CLICK_LEFT);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (listener != null && !isDoubleClick) {
                listener.onClickState(StateEvent.LONG_CLICK);
                isLongPress = true;
            }
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (!(Math.abs(diffX) > Math.abs(diffY))) {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            if (listener != null) {
                                isDoubleClick = true;
                                listener.onClickState(StateEvent.SWIPE_DOWN);
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }
}
