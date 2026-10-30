package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.Calendar;
import java.util.Locale;

/**
 * Custom View showing clock seconds that roll like a mechanical dial cylinder / odometer.
 * The digits roll vertically and fade smoothly with no background boxes or dark edges.
 */
public class RollingDialSecondsView extends View {

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float density;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int prevSecond = -1;
    private int currentSecond = 0;
    private float rollProgress = 1.0f; // 0.0 -> 1.0
    private ValueAnimator rollAnimator;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateSeconds();
            long now = System.currentTimeMillis();
            long delay = 1000 - (now % 1000);
            handler.postDelayed(this, Math.max(10, delay));
        }
    };

    public RollingDialSecondsView(Context context) {
        this(context, null);
    }

    public RollingDialSecondsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;

        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(26f * density);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setLetterSpacing(-0.02f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        prevSecond = -1;
        updateSeconds();
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(ticker);
        if (rollAnimator != null) rollAnimator.cancel();
    }

    private void updateSeconds() {
        Calendar cal = Calendar.getInstance();
        int sec = cal.get(Calendar.SECOND);

        if (sec != currentSecond) {
            prevSecond = currentSecond;
            currentSecond = sec;

            if (rollAnimator != null && rollAnimator.isRunning()) {
                rollAnimator.cancel();
            }

            rollAnimator = ValueAnimator.ofFloat(0f, 1f);
            rollAnimator.setDuration(320);
            rollAnimator.setInterpolator(new DecelerateInterpolator(2.0f));
            rollAnimator.addUpdateListener(anim -> {
                rollProgress = (float) anim.getAnimatedValue();
                invalidate();
            });
            rollAnimator.start();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = Math.round(42f * density);
        int h = Math.round(48f * density);
        setMeasuredDimension(resolveSize(w, widthMeasureSpec), resolveSize(h, heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w / 2f;
        float cy = h / 2f;
        float itemHeight = 30f * density;

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textCenterOffset = -(fm.ascent + fm.descent) / 2f;

        canvas.save();
        canvas.clipRect(0, 0, w, h);

        String curStr = String.format(Locale.US, "%02d", currentSecond);
        String prevStr = String.format(Locale.US, "%02d", prevSecond >= 0 ? prevSecond : (currentSecond + 59) % 60);

        // Previous second rolling out upward
        if (rollProgress < 1.0f) {
            float prevY = cy - rollProgress * itemHeight;
            float prevAlpha = Math.max(0f, 1.0f - rollProgress * 1.2f);
            float prevScale = 1.0f - rollProgress * 0.25f;

            textPaint.setColor(Color.argb(Math.round(prevAlpha * 255), 255, 255, 255));
            canvas.save();
            canvas.scale(prevScale, prevScale, cx, prevY);
            canvas.drawText(prevStr, cx, prevY + textCenterOffset, textPaint);
            canvas.restore();
        }

        // Current second rolling in from bottom
        float curY = cy + (1.0f - rollProgress) * itemHeight;
        float curAlpha = Math.min(1.0f, 0.2f + rollProgress * 0.8f);
        float curScale = 0.75f + rollProgress * 0.25f;

        textPaint.setColor(Color.argb(Math.round(curAlpha * 255), 255, 255, 255));
        canvas.save();
        canvas.scale(curScale, curScale, cx, curY);
        canvas.drawText(curStr, cx, curY + textCenterOffset, textPaint);
        canvas.restore();

        canvas.restore();
    }
}
