package android.support.v4.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

final class SwipeProgressBar {
    private static final int ANIMATION_DURATION_MS = 2000;
    private static final int COLOR1 = -1291845632;
    private static final int COLOR2 = Integer.MIN_VALUE;
    private static final int COLOR3 = 1291845632;
    private static final int COLOR4 = 436207616;
    private static final int FINISH_ANIMATION_DURATION_MS = 1000;
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();
    private Rect mBounds = new Rect();
    private final RectF mClipRect = new RectF();
    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mColor4;
    private long mFinishTime;
    private final Paint mPaint = new Paint();
    private View mParent;
    private boolean mRunning;
    private long mStartTime;
    private float mTriggerPercentage;

    public SwipeProgressBar(View parent) {
        this.mParent = parent;
        this.mColor1 = COLOR1;
        this.mColor2 = Integer.MIN_VALUE;
        this.mColor3 = COLOR3;
        this.mColor4 = COLOR4;
    }

    void setColorScheme(int color1, int color2, int color3, int color4) {
        this.mColor1 = color1;
        this.mColor2 = color2;
        this.mColor3 = color3;
        this.mColor4 = color4;
    }

    void setTriggerPercentage(float triggerPercentage) {
        this.mTriggerPercentage = triggerPercentage;
        this.mStartTime = 0;
        ViewCompat.postInvalidateOnAnimation(this.mParent, this.mBounds.left, this.mBounds.top, this.mBounds.right, this.mBounds.bottom);
    }

    void start() {
        if (!this.mRunning) {
            this.mTriggerPercentage = 0.0f;
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis();
            this.mRunning = true;
            this.mParent.postInvalidate();
        }
    }

    void stop() {
        if (this.mRunning) {
            this.mTriggerPercentage = 0.0f;
            this.mFinishTime = AnimationUtils.currentAnimationTimeMillis();
            this.mRunning = false;
            this.mParent.postInvalidate();
        }
    }

    boolean isRunning() {
        return this.mRunning || this.mFinishTime > 0;
    }

    void draw(Canvas canvas) {
        int width = this.mBounds.width();
        int height = this.mBounds.height();
        int cx = width / 2;
        int cy = height / 2;
        boolean drawTriggerWhileFinishing = false;
        int restoreCount = canvas.save();
        canvas.clipRect(this.mBounds);
        if (this.mRunning || this.mFinishTime > 0) {
            Canvas canvas2;
            long now = AnimationUtils.currentAnimationTimeMillis();
            long iterations = (now - this.mStartTime) / 2000;
            float rawProgress = ((float) ((now - this.mStartTime) % 2000)) / 20.0f;
            if (!this.mRunning) {
                if (now - this.mFinishTime >= 1000) {
                    this.mFinishTime = 0;
                    return;
                }
                float clearRadius = ((float) (width / 2)) * INTERPOLATOR.getInterpolation((((float) ((now - this.mFinishTime) % 1000)) / 10.0f) / 100.0f);
                this.mClipRect.set(((float) cx) - clearRadius, 0.0f, ((float) cx) + clearRadius, (float) height);
                canvas.saveLayerAlpha(this.mClipRect, 0, 0);
                drawTriggerWhileFinishing = true;
            }
            if (iterations == 0) {
                canvas.drawColor(this.mColor1);
            } else if (rawProgress >= 0.0f && rawProgress < 25.0f) {
                canvas.drawColor(this.mColor4);
            } else if (rawProgress >= 25.0f && rawProgress < 50.0f) {
                canvas.drawColor(this.mColor1);
            } else if (rawProgress < 50.0f || rawProgress >= 75.0f) {
                canvas.drawColor(this.mColor3);
            } else {
                canvas.drawColor(this.mColor2);
            }
            if (rawProgress >= 0.0f && rawProgress <= 25.0f) {
                canvas2 = canvas;
                drawCircle(canvas2, (float) cx, (float) cy, this.mColor1, ((25.0f + rawProgress) * 2.0f) / 100.0f);
            }
            if (rawProgress >= 0.0f && rawProgress <= 50.0f) {
                canvas2 = canvas;
                drawCircle(canvas2, (float) cx, (float) cy, this.mColor2, (2.0f * rawProgress) / 100.0f);
            }
            if (rawProgress >= 25.0f && rawProgress <= 75.0f) {
                canvas2 = canvas;
                drawCircle(canvas2, (float) cx, (float) cy, this.mColor3, ((rawProgress - 25.0f) * 2.0f) / 100.0f);
            }
            if (rawProgress >= 50.0f && rawProgress <= 100.0f) {
                canvas2 = canvas;
                drawCircle(canvas2, (float) cx, (float) cy, this.mColor4, ((rawProgress - 50.0f) * 2.0f) / 100.0f);
            }
            if (rawProgress >= 75.0f && rawProgress <= 100.0f) {
                canvas2 = canvas;
                drawCircle(canvas2, (float) cx, (float) cy, this.mColor1, ((rawProgress - 75.0f) * 2.0f) / 100.0f);
            }
            if (this.mTriggerPercentage > 0.0f && drawTriggerWhileFinishing) {
                canvas.restoreToCount(restoreCount);
                restoreCount = canvas.save();
                canvas.clipRect(this.mBounds);
                drawTrigger(canvas, cx, cy);
            }
            ViewCompat.postInvalidateOnAnimation(this.mParent, this.mBounds.left, this.mBounds.top, this.mBounds.right, this.mBounds.bottom);
        } else if (this.mTriggerPercentage > 0.0f && ((double) this.mTriggerPercentage) <= 1.0d) {
            drawTrigger(canvas, cx, cy);
        }
        canvas.restoreToCount(restoreCount);
    }

    private void drawTrigger(Canvas canvas, int cx, int cy) {
        this.mPaint.setColor(this.mColor1);
        canvas.drawCircle((float) cx, (float) cy, ((float) cx) * this.mTriggerPercentage, this.mPaint);
    }

    private void drawCircle(Canvas canvas, float cx, float cy, int color, float pct) {
        this.mPaint.setColor(color);
        canvas.save();
        canvas.translate(cx, cy);
        float radiusScale = INTERPOLATOR.getInterpolation(pct);
        canvas.scale(radiusScale, radiusScale);
        canvas.drawCircle(0.0f, 0.0f, cx, this.mPaint);
        canvas.restore();
    }

    void setBounds(int left, int top, int right, int bottom) {
        this.mBounds.left = left;
        this.mBounds.top = top;
        this.mBounds.right = right;
        this.mBounds.bottom = bottom;
    }
}
