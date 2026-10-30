package com.gathin.tvhome;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.Random;

/**
 * High-performance hardware-accelerated interactive Sharingan wallpaper view.
 * Layer Architecture:
 *  1. Base Background: Full 1080p/4K cinematic artwork with eye sockets prepared.
 *  2. Pure Irises: Pure red Sharingan irises with zero skin, gliding smoothly with D-pad.
 *  3. Static Face Overlay: Keeps eyelids, skin, lashes, and hair strands permanently fixed on top.
 *  4. Anatomical Blinking: Natural organic eyelid closing and opening.
 *  5. Crimson Spotlight: Soft radial red glow tracking the focused tile.
 *  6. Cinematic Launch: 360ms zoom and red chakra pulse when opening apps.
 */
public class SharinganView extends View {

    private static final float DESIGN_W = 1920f;
    private static final float DESIGN_H = 1080f;

    // Resting positions in 1920x1080 space
    private static final float LEFT_IRIS_X = 660f;
    private static final float LEFT_IRIS_Y = 441f;
    private static final float LEFT_IRIS_W = 48f;
    private static final float LEFT_IRIS_H = 29f;
    private static final float LEFT_EYE_CX = 684f;
    private static final float LEFT_EYE_CY = 455f;

    private static final float RIGHT_IRIS_X = 946f;
    private static final float RIGHT_IRIS_Y = 374f;
    private static final float RIGHT_IRIS_W = 98f;
    private static final float RIGHT_IRIS_H = 56f;
    private static final float RIGHT_EYE_CX = 995f;
    private static final float RIGHT_EYE_CY = 402f;

    // Overlay containing stationary eyelids, skin, and hair strands
    private static final float OVERLAY_X = 580f;
    private static final float OVERLAY_Y = 340f;
    private static final float OVERLAY_W = 510f;
    private static final float OVERLAY_H = 160f;

    // Natural, subtle anatomical pupil travel bounds (keeps iris safely inside socket)
    private static final float MAX_SHIFT_LX = 5.0f;
    private static final float MAX_SHIFT_LY = 3.5f;
    private static final float MAX_SHIFT_RX = 6.0f;
    private static final float MAX_SHIFT_RY = 4.0f;

    // Bitmaps
    private Bitmap baseBitmap;
    private Bitmap fullBitmap;
    private Bitmap leftPupilBitmap;
    private Bitmap rightPupilBitmap;
    private Bitmap overlayBitmap;

    // Eye tracking state
    private float targetShiftLX = 0f;
    private float targetShiftLY = 0f;
    private float currentShiftLX = 0f;
    private float currentShiftLY = 0f;

    private float targetShiftRX = 0f;
    private float targetShiftRY = 0f;
    private float currentShiftRX = 0f;
    private float currentShiftRY = 0f;

    private ValueAnimator trackingAnimator;

    // Spotlight state
    private float targetSpotlightX = DESIGN_W / 2f;
    private float targetSpotlightY = 700f;
    private float currentSpotlightX = DESIGN_W / 2f;
    private float currentSpotlightY = 700f;
    private float spotlightIntensity = 0.75f;

    // Blinking state
    private float blinkProgress = 0f; // 0 = open, 1 = closed
    private ValueAnimator blinkAnimator;
    private final Handler blinkHandler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private boolean isBlinkingActive = false;

    // Launch zoom & chakra flash
    private float zoomScale = 1.0f;
    private float zoomPivotX = DESIGN_W / 2f;
    private float zoomPivotY = DESIGN_H / 2f;
    private float flashAlpha = 0f;
    private float pupilScale = 1.0f;
    private ValueAnimator launchAnimator;

    // Eyelid paths
    private final Path leftLidPath = new Path();
    private final Path rightLidPath = new Path();

    // Paints
    private final Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint pupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint spotlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyelidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyelidCreasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint flashPaint = new Paint();

    // Rects
    private final Rect srcRectBase = new Rect();
    private final RectF dstRectBase = new RectF();
    private final Rect srcRectPupilL = new Rect();
    private final RectF dstRectPupilL = new RectF();
    private final Rect srcRectPupilR = new Rect();
    private final RectF dstRectPupilR = new RectF();
    private final Rect srcRectOverlay = new Rect();
    private final RectF dstRectOverlay = new RectF();

    // Settings
    private boolean eyeTrackingEnabled = true;
    private boolean blinkingEnabled = true;
    private boolean spotlightEnabled = true;
    private boolean zoomLaunchEnabled = true;
    private float sensitivityFactor = 1.0f;

    public SharinganView(Context context) {
        super(context);
        init(context);
    }

    public SharinganView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SharinganView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_HARDWARE, null);

        eyelidPaint.setStyle(Paint.Style.FILL);
        eyelidPaint.setColor(Color.rgb(22, 14, 18)); // Natural eyelid tone

        eyelidCreasePaint.setStyle(Paint.Style.STROKE);
        eyelidCreasePaint.setColor(Color.rgb(5, 2, 3));
        eyelidCreasePaint.setStrokeWidth(2.0f);

        loadBitmaps(context);
        loadSettings(context);
    }

    private void loadBitmaps(Context context) {
        try {
            BitmapFactory.Options bgOpts = new BitmapFactory.Options();
            bgOpts.inPreferredConfig = Bitmap.Config.RGB_565;
            baseBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sharingan_base, bgOpts);
            fullBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sharingan_full, bgOpts);

            BitmapFactory.Options alphaOpts = new BitmapFactory.Options();
            alphaOpts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            leftPupilBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sharingan_left_pupil, alphaOpts);
            rightPupilBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sharingan_right_pupil, alphaOpts);
            overlayBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sharingan_overlay, alphaOpts);

            if (baseBitmap != null) {
                srcRectBase.set(0, 0, baseBitmap.getWidth(), baseBitmap.getHeight());
            }
            if (leftPupilBitmap != null) {
                srcRectPupilL.set(0, 0, leftPupilBitmap.getWidth(), leftPupilBitmap.getHeight());
            }
            if (rightPupilBitmap != null) {
                srcRectPupilR.set(0, 0, rightPupilBitmap.getWidth(), rightPupilBitmap.getHeight());
            }
            if (overlayBitmap != null) {
                srcRectOverlay.set(0, 0, overlayBitmap.getWidth(), overlayBitmap.getHeight());
                dstRectOverlay.set(OVERLAY_X, OVERLAY_Y, OVERLAY_X + OVERLAY_W, OVERLAY_Y + OVERLAY_H);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void loadSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE);
        eyeTrackingEnabled = prefs.getBoolean("sharingan_eye_tracking", true);
        blinkingEnabled = prefs.getBoolean("sharingan_blinking", true);
        spotlightEnabled = prefs.getBoolean("sharingan_spotlight", true);
        zoomLaunchEnabled = prefs.getBoolean("sharingan_zoom_launch", true);

        int sensLevel = prefs.getInt("sharingan_sensitivity", 2);
        sensitivityFactor = sensLevel == 3 ? 1.30f : (sensLevel == 1 ? 0.65f : 1.0f);

        int spotLevel = prefs.getInt("sharingan_spotlight_intensity", 2);
        spotlightIntensity = spotLevel == 3 ? 0.95f : (spotLevel == 1 ? 0.45f : 0.75f);

        if (!eyeTrackingEnabled) {
            targetShiftLX = targetShiftLY = currentShiftLX = currentShiftLY = 0f;
            targetShiftRX = targetShiftRY = currentShiftRX = currentShiftRY = 0f;
        }

        if (blinkingEnabled && isAttachedToWindow()) {
            scheduleNextBlink();
        } else {
            stopBlinking();
        }
        invalidate();
    }

    public void setFocusTarget(float screenX, float screenY) {
        if (!eyeTrackingEnabled && !spotlightEnabled) return;

        float viewW = getWidth();
        float viewH = getHeight();
        if (viewW <= 0 || viewH <= 0) return;

        float designTargetX = screenX * (DESIGN_W / viewW);
        float designTargetY = screenY * (DESIGN_H / viewH);

        targetSpotlightX = designTargetX;
        targetSpotlightY = designTargetY;

        // Vector for Left Eye
        float vlx = designTargetX - LEFT_EYE_CX;
        float vly = designTargetY - LEFT_EYE_CY;
        float distL = (float) Math.sqrt(vlx * vlx + vly * vly);
        if (distL > 0) {
            float magL = Math.min(1.0f, distL / 700f);
            targetShiftLX = (vlx / distL) * MAX_SHIFT_LX * sensitivityFactor * magL;
            targetShiftLY = (vly / distL) * MAX_SHIFT_LY * sensitivityFactor * magL;
        }

        // Vector for Right Eye
        float vrx = designTargetX - RIGHT_EYE_CX;
        float vry = designTargetY - RIGHT_EYE_CY;
        float distR = (float) Math.sqrt(vrx * vrx + vry * vry);
        if (distR > 0) {
            float magR = Math.min(1.0f, distR / 700f);
            targetShiftRX = (vrx / distR) * MAX_SHIFT_RX * sensitivityFactor * magR;
            targetShiftRY = (vry / distR) * MAX_SHIFT_RY * sensitivityFactor * magR;
        }

        animateGazeAndSpotlight();
    }

    private void animateGazeAndSpotlight() {
        if (trackingAnimator != null) trackingAnimator.cancel();

        final float startLX = currentShiftLX, startLY = currentShiftLY;
        final float endLX = targetShiftLX, endLY = targetShiftLY;
        final float startRX = currentShiftRX, startRY = currentShiftRY;
        final float endRX = targetShiftRX, endRY = targetShiftRY;

        final float startSpotX = currentSpotlightX, startSpotY = currentSpotlightY;
        final float endSpotX = targetSpotlightX, endSpotY = targetSpotlightY;

        trackingAnimator = ValueAnimator.ofFloat(0f, 1f);
        trackingAnimator.setDuration(220);
        trackingAnimator.setInterpolator(new DecelerateInterpolator(1.6f));
        trackingAnimator.addUpdateListener(anim -> {
            float frac = (Float) anim.getAnimatedValue();
            currentShiftLX = startLX + (endLX - startLX) * frac;
            currentShiftLY = startLY + (endLY - startLY) * frac;

            currentShiftRX = startRX + (endRX - startRX) * frac;
            currentShiftRY = startRY + (endRY - startRY) * frac;

            currentSpotlightX = startSpotX + (endSpotX - startSpotX) * frac;
            currentSpotlightY = startSpotY + (endSpotY - startSpotY) * frac;

            invalidate();
        });
        trackingAnimator.start();
    }

    // ---- Blinking Engine --------------------------------------------------

    private final Runnable blinkTask = new Runnable() {
        @Override
        public void run() {
            if (!blinkingEnabled || !isBlinkingActive) return;

            triggerBlink(() -> {
                if (random.nextInt(5) == 0) {
                    blinkHandler.postDelayed(() -> triggerBlink(SharinganView.this::scheduleNextBlink), 120);
                } else {
                    scheduleNextBlink();
                }
            });
        }
    };

    private void triggerBlink(Runnable onComplete) {
        if (blinkAnimator != null) blinkAnimator.cancel();

        blinkAnimator = ValueAnimator.ofFloat(0f, 1f, 0f);
        blinkAnimator.setDuration(260);
        blinkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        blinkAnimator.addUpdateListener(anim -> {
            blinkProgress = (Float) anim.getAnimatedValue();
            invalidate();
        });
        blinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                blinkProgress = 0f;
                invalidate();
                if (onComplete != null) onComplete.run();
            }
        });
        blinkAnimator.start();
    }

    private void scheduleNextBlink() {
        blinkHandler.removeCallbacks(blinkTask);
        if (!blinkingEnabled || !isBlinkingActive) return;
        long delay = 3200 + random.nextInt(4300);
        blinkHandler.postDelayed(blinkTask, delay);
    }

    public void startBlinking() {
        isBlinkingActive = true;
        scheduleNextBlink();
    }

    public void stopBlinking() {
        isBlinkingActive = false;
        blinkHandler.removeCallbacks(blinkTask);
        if (blinkAnimator != null) blinkAnimator.cancel();
        blinkProgress = 0f;
        invalidate();
    }

    // ---- Cinematic Launch -------------------------------------------------

    public void performLaunchAnimation(float focusScreenX, float focusScreenY, Runnable onLaunch) {
        if (!zoomLaunchEnabled) {
            if (onLaunch != null) onLaunch.run();
            return;
        }

        setFocusTarget(focusScreenX, focusScreenY);

        float viewW = getWidth();
        float viewH = getHeight();
        if (viewW > 0 && viewH > 0) {
            zoomPivotX = focusScreenX * (DESIGN_W / viewW);
            zoomPivotY = focusScreenY * (DESIGN_H / viewH);
        }

        if (launchAnimator != null) launchAnimator.cancel();

        launchAnimator = ValueAnimator.ofFloat(0f, 1f);
        launchAnimator.setDuration(360);
        launchAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        launchAnimator.addUpdateListener(anim -> {
            float frac = (Float) anim.getAnimatedValue();
            zoomScale = 1.0f + 0.16f * frac;
            pupilScale = 1.0f + 0.08f * frac;
            if (frac < 0.4f) {
                flashAlpha = (frac / 0.4f) * 0.45f;
            } else {
                flashAlpha = (1f - (frac - 0.4f) / 0.6f) * 0.45f;
            }
            invalidate();
        });
        launchAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onLaunch != null) onLaunch.run();
            }
        });
        launchAnimator.start();
    }

    public void resetLaunchState() {
        if (launchAnimator != null) launchAnimator.cancel();

        if (zoomScale != 1.0f || flashAlpha > 0f || pupilScale != 1.0f) {
            final float startZoom = zoomScale;
            final float startPupil = pupilScale;
            final float startFlash = flashAlpha;

            ValueAnimator restoreAnim = ValueAnimator.ofFloat(0f, 1f);
            restoreAnim.setDuration(280);
            restoreAnim.setInterpolator(new DecelerateInterpolator(1.8f));
            restoreAnim.addUpdateListener(anim -> {
                float frac = (Float) anim.getAnimatedValue();
                zoomScale = startZoom + (1.0f - startZoom) * frac;
                pupilScale = startPupil + (1.0f - startPupil) * frac;
                flashAlpha = startFlash * (1.0f - frac);
                invalidate();
            });
            restoreAnim.start();
        }
        startBlinking();
    }

    // ---- Drawing ----------------------------------------------------------

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startBlinking();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopBlinking();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.save();

        float scaleX = (float) w / DESIGN_W;
        float scaleY = (float) h / DESIGN_H;
        canvas.scale(scaleX, scaleY);

        if (zoomScale != 1.0f) {
            canvas.scale(zoomScale, zoomScale, zoomPivotX, zoomPivotY);
        }

        // 1. Draw base wallpaper (full face with eyeball sclera in sockets)
        dstRectBase.set(0, 0, DESIGN_W, DESIGN_H);
        Bitmap bg = (eyeTrackingEnabled && baseBitmap != null) ? baseBitmap : (fullBitmap != null ? fullBitmap : baseBitmap);
        if (bg != null) {
            canvas.drawBitmap(bg, srcRectBase, dstRectBase, bitmapPaint);
        } else {
            canvas.drawColor(Color.BLACK);
        }

        // 2. Draw pure red Sharingan irises (NO skin, NO eyelids)
        if (eyeTrackingEnabled && leftPupilBitmap != null && rightPupilBitmap != null) {
            // Draw Left Iris
            float lw = LEFT_IRIS_W * pupilScale;
            float lh = LEFT_IRIS_H * pupilScale;
            float lx = LEFT_IRIS_X + currentShiftLX - (lw - LEFT_IRIS_W) / 2f;
            float ly = LEFT_IRIS_Y + currentShiftLY - (lh - LEFT_IRIS_H) / 2f;
            dstRectPupilL.set(lx, ly, lx + lw, ly + lh);
            canvas.drawBitmap(leftPupilBitmap, srcRectPupilL, dstRectPupilL, pupilPaint);

            // Draw Right Iris
            float rw = RIGHT_IRIS_W * pupilScale;
            float rh = RIGHT_IRIS_H * pupilScale;
            float rx = RIGHT_IRIS_X + currentShiftRX - (rw - RIGHT_IRIS_W) / 2f;
            float ry = RIGHT_IRIS_Y + currentShiftRY - (rh - RIGHT_IRIS_H) / 2f;
            dstRectPupilR.set(rx, ry, rx + rw, ry + rh);
            canvas.drawBitmap(rightPupilBitmap, srcRectPupilR, dstRectPupilR, pupilPaint);
        }

        // 3. Draw static face overlay (eyelids, eyelashes, skin, and hair strands stay fixed ON TOP!)
        if (eyeTrackingEnabled && overlayBitmap != null) {
            canvas.drawBitmap(overlayBitmap, srcRectOverlay, dstRectOverlay, bitmapPaint);
        }

        // 4. Draw blinking eyelids if blinking
        if (blinkProgress > 0.01f) {
            drawBlinkEyelids(canvas);
        }

        // 5. Draw crimson spotlight
        if (spotlightEnabled) {
            drawSpotlight(canvas);
        }

        // 6. Draw red chakra energy flash during launch
        if (flashAlpha > 0.005f) {
            flashPaint.setColor(Color.argb((int) (flashAlpha * 255), 255, 20, 35));
            canvas.drawRect(0, 0, DESIGN_W, DESIGN_H, flashPaint);
        }

        canvas.restore();
    }

    private void drawBlinkEyelids(Canvas canvas) {
        // Left eye blink
        drawEyeBlink(canvas, LEFT_EYE_CX, LEFT_EYE_CY, 24f, 15f, leftLidPath);
        // Right eye blink
        drawEyeBlink(canvas, RIGHT_EYE_CX, RIGHT_EYE_CY, 48f, 28f, rightLidPath);
    }

    private void drawEyeBlink(Canvas canvas, float cx, float cy, float rx, float ry, Path lidPath) {
        lidPath.reset();

        float topY = cy - ry - 6f;
        float bottomY = cy + ry + 6f;
        float closeY = cy + (ry * 0.12f * blinkProgress);

        float upperCurrentY = topY + (closeY - topY) * blinkProgress;
        float lowerCurrentY = bottomY - (bottomY - closeY) * 0.25f * blinkProgress;

        // Upper lid
        lidPath.moveTo(cx - rx - 6f, topY);
        lidPath.lineTo(cx + rx + 6f, topY);
        lidPath.lineTo(cx + rx + 6f, upperCurrentY);
        lidPath.quadTo(cx, upperCurrentY + (ry * 0.3f * (1f - blinkProgress)), cx - rx - 6f, upperCurrentY);
        lidPath.close();
        canvas.drawPath(lidPath, eyelidPaint);

        // Lower lid
        if (blinkProgress > 0.25f) {
            lidPath.reset();
            lidPath.moveTo(cx - rx - 6f, bottomY);
            lidPath.lineTo(cx + rx + 6f, bottomY);
            lidPath.lineTo(cx + rx + 6f, lowerCurrentY);
            lidPath.quadTo(cx, lowerCurrentY - (ry * 0.2f), cx - rx - 6f, lowerCurrentY);
            lidPath.close();
            canvas.drawPath(lidPath, eyelidPaint);
        }

        if (blinkProgress > 0.85f) {
            canvas.drawLine(cx - rx + 4f, closeY, cx + rx - 4f, closeY, eyelidCreasePaint);
        }
    }

    private void drawSpotlight(Canvas canvas) {
        int alphaInt = (int) (spotlightIntensity * 120);
        RadialGradient grad = new RadialGradient(
                currentSpotlightX, currentSpotlightY, 260f,
                new int[]{
                        Color.argb(alphaInt, 220, 20, 35),
                        Color.argb(alphaInt / 2, 130, 5, 15),
                        Color.argb(0, 0, 0, 0)
                },
                new float[]{0.0f, 0.55f, 1.0f},
                Shader.TileMode.CLAMP
        );
        spotlightPaint.setShader(grad);
        canvas.drawCircle(currentSpotlightX, currentSpotlightY, 260f, spotlightPaint);
    }
}
