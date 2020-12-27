package com.tapi.download.video.instagram.function.preview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.tapi.download.video.instagram.utils.Utils;

public class StoriesViewPager extends ViewPager {
    private static final String TAG = "StoriesViewPager";
    private Context mContext;
    private OnSwipeTouchListener.OnViewTouchListener listener;
    int heightScreen,heightSeekbar;

    public StoriesViewPager(@NonNull Context context) {
        this(context, null);
    }

    public StoriesViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        heightScreen = Utils.getHeightScreen(mContext, false);
        heightSeekbar = heightScreen- Utils.convertDpToPixel(42f,mContext);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (checkTouchEvent(ev)){
            return false;
        }
        OnSwipeTouchListener.getInstance(mContext, listener).onTouch(ev);
        return super.dispatchTouchEvent(ev);
    }

    private boolean checkTouchEvent(MotionEvent ev) {
        if (ev.getY() >= heightSeekbar){
            return true;
        }else {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    public void setListener(OnSwipeTouchListener.OnViewTouchListener listener) {
        this.listener = listener;
    }

    public void onDestroyViewpager() {
        OnSwipeTouchListener.getInstance(mContext, listener).removeInstance();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
}
