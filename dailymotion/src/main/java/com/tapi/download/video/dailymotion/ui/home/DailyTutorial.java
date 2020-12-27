package com.tapi.download.video.dailymotion.ui.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.tapi.download.video.dailymotion.R;
import com.tapi.download.video.dailymotion.util.Utils;

public class DailyTutorial extends RelativeLayout implements View.OnClickListener {
    public static final int COLLAPSE = 0;
    public static final int EXPAND = 1;
    private Context mContext;
    private LinearLayout llGroupView;
    private ImageView imgState;
    private TextView txtOpenDaily;
    private int state = COLLAPSE;


    public DailyTutorial(Context context) {
        this(context, null);
    }

    public DailyTutorial(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DailyTutorial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.daily_tutorial, this);
        llGroupView = findViewById(R.id.daily_tutorial_group_ll);
        imgState = findViewById(R.id.daily_tutorial_state_img);
        imgState.setOnClickListener(this);
        txtOpenDaily = findViewById(R.id.daily_tutorial_open_daily_txt);
        txtOpenDaily.setOnClickListener(this);
        findViewById(R.id.daily_tutorial_group_all_rl).setOnClickListener(this);
    }


    public void expandTutorial() {
        int matchParentMeasureSpec = MeasureSpec.makeMeasureSpec(((View) llGroupView.getParent()).getWidth(), MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        llGroupView.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = llGroupView.getMeasuredHeight();

        llGroupView.getLayoutParams().height = 1;
        llGroupView.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                llGroupView.getLayoutParams().height = interpolatedTime == 1
                        ? LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                llGroupView.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imgState.setImageResource(R.drawable.daily_tutorial_close_iv);
                txtOpenDaily.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        a.setDuration((int) (targetHeight / llGroupView.getContext().getResources().getDisplayMetrics().density));
        llGroupView.startAnimation(a);
    }

    public void collapseTutorial() {
        final int initialHeight = llGroupView.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    llGroupView.setVisibility(View.GONE);
                } else {
                    llGroupView.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    llGroupView.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imgState.setImageResource(R.drawable.daily_tutorial_info_iv);
                txtOpenDaily.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        a.setDuration((int) (initialHeight / llGroupView.getContext().getResources().getDisplayMetrics().density));
        llGroupView.startAnimation(a);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.daily_tutorial_open_daily_txt) {
            Utils.openDailyApp(mContext);
            return;
        }
        if (id == R.id.daily_tutorial_group_all_rl || id == R.id.daily_tutorial_state_img) {
            if (state == COLLAPSE) {
                expandTutorial();
                state = EXPAND;
            } else {
                collapseTutorial();
                state = COLLAPSE;
            }
        }
    }
}
