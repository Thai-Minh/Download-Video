package com.tapi.download.video.instagram.function.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.tapi.download.video.core.utils.Utils;

public class DownLoadProgressbar extends View {
    private static final String TAG = "DownLoadProgressbar";

    private static final int BACKGROUND_COLOR = 0xFFF1F1F1;

    private static final int START_PROGRESS_COLOR = 0xFFB98DCD;
    private static final int END_PROGRESS_COLOR = 0xFF00EADF;

    private static final int LINE_WIDTH = 4; //dp
    private static final int PROGRESS_PADDING = 2; //dp
    private static final int LINE_MOVE_RIGHT = 7; //dp
    private static final int LINE_SPACE = 9; //dp

    private Context mContext;

    private int progress = 0;
    private int progressAfter;

    private RectF bgRect;
    private RectF progressRect;
    private float lineMoveRight;

    private Paint bgPaint;
    private Paint progressPaint;
    private Paint linePaint;

    private LinearGradient progressGradient;

    private float startLineX;
    private float currStartLineX;
    private float lineSpace;

    private int height;

    private ValueAnimator animator;

    public DownLoadProgressbar(Context context) {
        super(context);
        init(context);
    }

    public DownLoadProgressbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DownLoadProgressbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        lineMoveRight = com.tapi.download.video.core.utils.Utils.convertDpToPixel(LINE_MOVE_RIGHT, context);
        lineSpace = com.tapi.download.video.core.utils.Utils.convertDpToPixel(LINE_SPACE, context);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(BACKGROUND_COLOR);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(BACKGROUND_COLOR);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(com.tapi.download.video.core.utils.Utils.convertDpToPixel(LINE_WIDTH, context));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            height = h;

            bgRect = new RectF(0, 0, w, h);

            float progressPadding = com.tapi.download.video.core.utils.Utils.convertDpToPixel(PROGRESS_PADDING, mContext);
            progressRect = new RectF(progressPadding, progressPadding, w - progressPadding, h - progressPadding);
            progressGradient = new LinearGradient(progressRect.left, progressRect.top, progressRect.right, progressRect.top, START_PROGRESS_COLOR, END_PROGRESS_COLOR, Shader.TileMode.CLAMP);
            progressPaint.setShader(progressGradient);

            currStartLineX = startLineX = Utils.convertDpToPixel(0, mContext);
            startAnimator();
        }
    }

    public void setProgress(int progress) {
        this.progress = progress;
        //invalidate();
    }

    private void startAnimator() {
        if (animator != null)
            animator.cancel();

        animator = ValueAnimator.ofFloat(0f, lineSpace);
        animator.setDuration(500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animator.getAnimatedValue();
                currStartLineX = startLineX + value;
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                currStartLineX = startLineX;
                invalidate();
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bgRect != null && progressRect != null) {
            drawBackground(canvas);
            drawProgress(canvas);
            drawLine(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawRoundRect(bgRect, bgRect.height() / 2, bgRect.height() / 2, bgPaint);
    }

    private void drawProgress(Canvas canvas) {
        float progressLength = (progress / 100f) * progressRect.width();
        canvas.save();
        canvas.clipRect(progressRect.left, progressRect.top, progressLength, progressRect.bottom);
        canvas.drawRoundRect(progressRect, progressRect.height() / 2, progressRect.height() / 2, progressPaint);
        canvas.restore();
    }

    private void drawLine(Canvas canvas) {
        float progressLength = (progress / 100f) * progressRect.width();
        canvas.save();
        canvas.clipRect(progressRect.left, progressRect.top, progressLength, progressRect.bottom);
        int step = 0;
        float startX, endX;
        do {
            startX = step * lineSpace + currStartLineX;
            endX = startX + lineMoveRight;
            canvas.drawLine(startX, height, endX, 0, linePaint);
            step++;
        } while (endX < bgRect.width());


        canvas.restore();
    }
}

