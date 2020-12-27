package com.tapi.download.video.twitter.ui.home;

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

import com.tapi.download.video.twitter.R;
import com.tapi.download.video.twitter.utils.Utils;

public class TwitterTutorial extends RelativeLayout implements View.OnClickListener {
    public static final int COLLAPSE = 0;
    public static final int EXPAND = 1;
    private Context mContext;
    private LinearLayout llGroupView;
    private ImageView imgState;
    private TextView txtOpenTwitter;
    private int state = COLLAPSE;

    public TwitterTutorial(Context context) {
        this(context, null);
    }

    public TwitterTutorial(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwitterTutorial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.twitter_tutorial, this);
        llGroupView = findViewById(R.id.twitter_tutorial_group_ll);
        imgState = findViewById(R.id.twitter_tutorial_state_img);
        imgState.setOnClickListener(this);
        txtOpenTwitter = findViewById(R.id.twitter_tutorial_open_tw_txt);
        txtOpenTwitter.setOnClickListener(this);
        findViewById(R.id.twitter_tutorial_group_all_rl).setOnClickListener(this);
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
                imgState.setImageResource(R.drawable.twitter_tutorial_close_iv);
                txtOpenTwitter.setVisibility(VISIBLE);
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
                imgState.setImageResource(R.drawable.twitter_tutorial_info_iv);
                txtOpenTwitter.setVisibility(GONE);
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
        if (id == R.id.twitter_tutorial_open_tw_txt) {
            Utils.openTwitterApp(mContext);
            return;
        }
        if (id == R.id.twitter_tutorial_group_all_rl || id == R.id.twitter_tutorial_state_img) {
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
