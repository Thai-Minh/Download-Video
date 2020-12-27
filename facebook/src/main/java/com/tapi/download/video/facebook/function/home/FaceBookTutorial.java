package com.tapi.download.video.facebook.function.home;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.tapi.download.video.facebook.R;

public class FaceBookTutorial extends RelativeLayout implements View.OnClickListener {
    public static final int COLLAPSE = 0;
    public static final int EXPAND = 1;
    private Context mContext;
    private LinearLayout llGroupView;
    private ImageView ivState;
    private TextView tvOpenFaceBook;
    private int state = COLLAPSE;


    public FaceBookTutorial(Context context) {
        this(context, null);
    }

    public FaceBookTutorial(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceBookTutorial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {
        inflate(mContext, R.layout.facebook_tutorial, this);
        llGroupView = findViewById(R.id.faceBook_tutorial_group_ll);
        ivState = findViewById(R.id.faceBook_tutorial_state_iv);
        tvOpenFaceBook = findViewById(R.id.face_book_tutorial_open_fb_tv);
        tvOpenFaceBook.setOnClickListener(this);
        ivState.setOnClickListener(this);
        findViewById(R.id.faceBook_tutorial_group_all_rl).setOnClickListener(this);
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
                ivState.setImageResource(R.drawable.fb_tutorial_close_iv);
                tvOpenFaceBook.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        a.setDuration(250);
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
                ivState.setImageResource(R.drawable.fb_tutorial_info_iv);
                tvOpenFaceBook.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        a.setDuration(250);
        llGroupView.startAnimation(a);
    }

    public static void newFacebookIntent(Context context) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.fb_tutorial_app_not_installed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.face_book_tutorial_open_fb_tv) {
            newFacebookIntent(mContext);
            return;
        }
        if (id == R.id.faceBook_tutorial_group_all_rl || id == R.id.faceBook_tutorial_state_iv) {
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
