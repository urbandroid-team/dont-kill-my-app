package android.support.v4.widget;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import com.fihtdc.backuptool.FileOperator;
import java.util.Arrays;

public class ViewDragHelper {
    private static final int BASE_SETTLE_DURATION = 256;
    public static final int DIRECTION_ALL = 3;
    public static final int DIRECTION_HORIZONTAL = 1;
    public static final int DIRECTION_VERTICAL = 2;
    public static final int EDGE_ALL = 15;
    public static final int EDGE_BOTTOM = 8;
    public static final int EDGE_LEFT = 1;
    public static final int EDGE_RIGHT = 2;
    private static final int EDGE_SIZE = 20;
    public static final int EDGE_TOP = 4;
    public static final int INVALID_POINTER = -1;
    private static final int MAX_SETTLE_DURATION = 600;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_SETTLING = 2;
    private static final String TAG = "ViewDragHelper";
    private static final Interpolator sInterpolator = new C03021();
    private int mActivePointerId = -1;
    private final Callback mCallback;
    private View mCapturedView;
    private int mDragState;
    private int[] mEdgeDragsInProgress;
    private int[] mEdgeDragsLocked;
    private int mEdgeSize;
    private int[] mInitialEdgesTouched;
    private float[] mInitialMotionX;
    private float[] mInitialMotionY;
    private float[] mLastMotionX;
    private float[] mLastMotionY;
    private float mMaxVelocity;
    private float mMinVelocity;
    private final ViewGroup mParentView;
    private int mPointersDown;
    private boolean mReleaseInProgress;
    private ScrollerCompat mScroller;
    private final Runnable mSetIdleRunnable = new C03032();
    private int mTouchSlop;
    private int mTrackingEdges;
    private VelocityTracker mVelocityTracker;

    public static abstract class Callback {
        public abstract boolean tryCaptureView(View view, int i);

        public void onViewDragStateChanged(int state) {
        }

        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
        }

        public void onViewCaptured(View capturedChild, int activePointerId) {
        }

        public void onViewReleased(View releasedChild, float xvel, float yvel) {
        }

        public void onEdgeTouched(int edgeFlags, int pointerId) {
        }

        public boolean onEdgeLock(int edgeFlags) {
            return false;
        }

        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
        }

        public int getOrderedChildIndex(int index) {
            return index;
        }

        public int getViewHorizontalDragRange(View child) {
            return 0;
        }

        public int getViewVerticalDragRange(View child) {
            return 0;
        }

        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return 0;
        }

        public int clampViewPositionVertical(View child, int top, int dy) {
            return 0;
        }
    }

    /* renamed from: android.support.v4.widget.ViewDragHelper$1 */
    static class C03021 implements Interpolator {
        C03021() {
        }

        public float getInterpolation(float t) {
            t -= 1.0f;
            return ((((t * t) * t) * t) * t) + 1.0f;
        }
    }

    /* renamed from: android.support.v4.widget.ViewDragHelper$2 */
    class C03032 implements Runnable {
        C03032() {
        }

        public void run() {
            ViewDragHelper.this.setDragState(0);
        }
    }

    public static ViewDragHelper create(ViewGroup forParent, Callback cb) {
        return new ViewDragHelper(forParent.getContext(), forParent, cb);
    }

    public static ViewDragHelper create(ViewGroup forParent, float sensitivity, Callback cb) {
        ViewDragHelper helper = create(forParent, cb);
        helper.mTouchSlop = (int) (((float) helper.mTouchSlop) * (1.0f / sensitivity));
        return helper;
    }

    private ViewDragHelper(Context context, ViewGroup forParent, Callback cb) {
        if (forParent == null) {
            throw new IllegalArgumentException("Parent view may not be null");
        } else if (cb == null) {
            throw new IllegalArgumentException("Callback may not be null");
        } else {
            this.mParentView = forParent;
            this.mCallback = cb;
            ViewConfiguration vc = ViewConfiguration.get(context);
            this.mEdgeSize = (int) ((20.0f * context.getResources().getDisplayMetrics().density) + 0.5f);
            this.mTouchSlop = vc.getScaledTouchSlop();
            this.mMaxVelocity = (float) vc.getScaledMaximumFlingVelocity();
            this.mMinVelocity = (float) vc.getScaledMinimumFlingVelocity();
            this.mScroller = ScrollerCompat.create(context, sInterpolator);
        }
    }

    public void setMinVelocity(float minVel) {
        this.mMinVelocity = minVel;
    }

    public float getMinVelocity() {
        return this.mMinVelocity;
    }

    public int getViewDragState() {
        return this.mDragState;
    }

    public void setEdgeTrackingEnabled(int edgeFlags) {
        this.mTrackingEdges = edgeFlags;
    }

    public int getEdgeSize() {
        return this.mEdgeSize;
    }

    public void captureChildView(View childView, int activePointerId) {
        if (childView.getParent() != this.mParentView) {
            throw new IllegalArgumentException("captureChildView: parameter must be a descendant of the ViewDragHelper's tracked parent view (" + this.mParentView + ")");
        }
        this.mCapturedView = childView;
        this.mActivePointerId = activePointerId;
        this.mCallback.onViewCaptured(childView, activePointerId);
        setDragState(1);
    }

    public View getCapturedView() {
        return this.mCapturedView;
    }

    public int getActivePointerId() {
        return this.mActivePointerId;
    }

    public int getTouchSlop() {
        return this.mTouchSlop;
    }

    public void cancel() {
        this.mActivePointerId = -1;
        clearMotionHistory();
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    public void abort() {
        cancel();
        if (this.mDragState == 2) {
            int oldX = this.mScroller.getCurrX();
            int oldY = this.mScroller.getCurrY();
            this.mScroller.abortAnimation();
            int newX = this.mScroller.getCurrX();
            int newY = this.mScroller.getCurrY();
            this.mCallback.onViewPositionChanged(this.mCapturedView, newX, newY, newX - oldX, newY - oldY);
        }
        setDragState(0);
    }

    public boolean smoothSlideViewTo(View child, int finalLeft, int finalTop) {
        this.mCapturedView = child;
        this.mActivePointerId = -1;
        boolean continueSliding = forceSettleCapturedViewAt(finalLeft, finalTop, 0, 0);
        if (!(continueSliding || this.mDragState != 0 || this.mCapturedView == null)) {
            this.mCapturedView = null;
        }
        return continueSliding;
    }

    public boolean settleCapturedViewAt(int finalLeft, int finalTop) {
        if (this.mReleaseInProgress) {
            return forceSettleCapturedViewAt(finalLeft, finalTop, (int) VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), (int) VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId));
        }
        throw new IllegalStateException("Cannot settleCapturedViewAt outside of a call to Callback#onViewReleased");
    }

    private boolean forceSettleCapturedViewAt(int finalLeft, int finalTop, int xvel, int yvel) {
        int startLeft = this.mCapturedView.getLeft();
        int startTop = this.mCapturedView.getTop();
        int dx = finalLeft - startLeft;
        int dy = finalTop - startTop;
        if (dx == 0 && dy == 0) {
            this.mScroller.abortAnimation();
            setDragState(0);
            return false;
        }
        this.mScroller.startScroll(startLeft, startTop, dx, dy, computeSettleDuration(this.mCapturedView, dx, dy, xvel, yvel));
        setDragState(2);
        return true;
    }

    private int computeSettleDuration(View child, int dx, int dy, int xvel, int yvel) {
        xvel = clampMag(xvel, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        yvel = clampMag(yvel, (int) this.mMinVelocity, (int) this.mMaxVelocity);
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int absXVel = Math.abs(xvel);
        int absYVel = Math.abs(yvel);
        int addedVel = absXVel + absYVel;
        int addedDistance = absDx + absDy;
        return (int) ((((float) computeAxisDuration(dx, xvel, this.mCallback.getViewHorizontalDragRange(child))) * (xvel != 0 ? ((float) absXVel) / ((float) addedVel) : ((float) absDx) / ((float) addedDistance))) + (((float) computeAxisDuration(dy, yvel, this.mCallback.getViewVerticalDragRange(child))) * (yvel != 0 ? ((float) absYVel) / ((float) addedVel) : ((float) absDy) / ((float) addedDistance))));
    }

    private int computeAxisDuration(int delta, int velocity, int motionRange) {
        if (delta == 0) {
            return 0;
        }
        int duration;
        int width = this.mParentView.getWidth();
        int halfWidth = width / 2;
        float distance = ((float) halfWidth) + (((float) halfWidth) * distanceInfluenceForSnapDuration(Math.min(1.0f, ((float) Math.abs(delta)) / ((float) width))));
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = Math.round(1000.0f * Math.abs(distance / ((float) velocity))) * 4;
        } else {
            duration = (int) (((((float) Math.abs(delta)) / ((float) motionRange)) + 1.0f) * 256.0f);
        }
        return Math.min(duration, MAX_SETTLE_DURATION);
    }

    private int clampMag(int value, int absMin, int absMax) {
        int absValue = Math.abs(value);
        if (absValue < absMin) {
            return 0;
        }
        if (absValue <= absMax) {
            return value;
        }
        if (value <= 0) {
            return -absMax;
        }
        return absMax;
    }

    private float clampMag(float value, float absMin, float absMax) {
        float absValue = Math.abs(value);
        if (absValue < absMin) {
            return 0.0f;
        }
        if (absValue <= absMax) {
            return value;
        }
        if (value <= 0.0f) {
            return -absMax;
        }
        return absMax;
    }

    private float distanceInfluenceForSnapDuration(float f) {
        return (float) Math.sin((double) ((float) (((double) (f - 0.5f)) * 0.4712389167638204d)));
    }

    public void flingCapturedView(int minLeft, int minTop, int maxLeft, int maxTop) {
        if (this.mReleaseInProgress) {
            this.mScroller.fling(this.mCapturedView.getLeft(), this.mCapturedView.getTop(), (int) VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), (int) VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId), minLeft, maxLeft, minTop, maxTop);
            setDragState(2);
            return;
        }
        throw new IllegalStateException("Cannot flingCapturedView outside of a call to Callback#onViewReleased");
    }

    public boolean continueSettling(boolean deferCallbacks) {
        if (this.mDragState == 2) {
            boolean keepGoing = this.mScroller.computeScrollOffset();
            int x = this.mScroller.getCurrX();
            int y = this.mScroller.getCurrY();
            int dx = x - this.mCapturedView.getLeft();
            int dy = y - this.mCapturedView.getTop();
            if (dx != 0) {
                ViewCompat.offsetLeftAndRight(this.mCapturedView, dx);
            }
            if (dy != 0) {
                ViewCompat.offsetTopAndBottom(this.mCapturedView, dy);
            }
            if (!(dx == 0 && dy == 0)) {
                this.mCallback.onViewPositionChanged(this.mCapturedView, x, y, dx, dy);
            }
            if (keepGoing && x == this.mScroller.getFinalX() && y == this.mScroller.getFinalY()) {
                this.mScroller.abortAnimation();
                keepGoing = false;
            }
            if (!keepGoing) {
                if (deferCallbacks) {
                    this.mParentView.post(this.mSetIdleRunnable);
                } else {
                    setDragState(0);
                }
            }
        }
        return this.mDragState == 2;
    }

    private void dispatchViewReleased(float xvel, float yvel) {
        this.mReleaseInProgress = true;
        this.mCallback.onViewReleased(this.mCapturedView, xvel, yvel);
        this.mReleaseInProgress = false;
        if (this.mDragState == 1) {
            setDragState(0);
        }
    }

    private void clearMotionHistory() {
        if (this.mInitialMotionX != null) {
            Arrays.fill(this.mInitialMotionX, 0.0f);
            Arrays.fill(this.mInitialMotionY, 0.0f);
            Arrays.fill(this.mLastMotionX, 0.0f);
            Arrays.fill(this.mLastMotionY, 0.0f);
            Arrays.fill(this.mInitialEdgesTouched, 0);
            Arrays.fill(this.mEdgeDragsInProgress, 0);
            Arrays.fill(this.mEdgeDragsLocked, 0);
            this.mPointersDown = 0;
        }
    }

    private void clearMotionHistory(int pointerId) {
        if (this.mInitialMotionX != null) {
            this.mInitialMotionX[pointerId] = 0.0f;
            this.mInitialMotionY[pointerId] = 0.0f;
            this.mLastMotionX[pointerId] = 0.0f;
            this.mLastMotionY[pointerId] = 0.0f;
            this.mInitialEdgesTouched[pointerId] = 0;
            this.mEdgeDragsInProgress[pointerId] = 0;
            this.mEdgeDragsLocked[pointerId] = 0;
            this.mPointersDown &= (1 << pointerId) ^ -1;
        }
    }

    private void ensureMotionHistorySizeForId(int pointerId) {
        if (this.mInitialMotionX == null || this.mInitialMotionX.length <= pointerId) {
            float[] imx = new float[(pointerId + 1)];
            float[] imy = new float[(pointerId + 1)];
            float[] lmx = new float[(pointerId + 1)];
            float[] lmy = new float[(pointerId + 1)];
            int[] iit = new int[(pointerId + 1)];
            int[] edip = new int[(pointerId + 1)];
            int[] edl = new int[(pointerId + 1)];
            if (this.mInitialMotionX != null) {
                System.arraycopy(this.mInitialMotionX, 0, imx, 0, this.mInitialMotionX.length);
                System.arraycopy(this.mInitialMotionY, 0, imy, 0, this.mInitialMotionY.length);
                System.arraycopy(this.mLastMotionX, 0, lmx, 0, this.mLastMotionX.length);
                System.arraycopy(this.mLastMotionY, 0, lmy, 0, this.mLastMotionY.length);
                System.arraycopy(this.mInitialEdgesTouched, 0, iit, 0, this.mInitialEdgesTouched.length);
                System.arraycopy(this.mEdgeDragsInProgress, 0, edip, 0, this.mEdgeDragsInProgress.length);
                System.arraycopy(this.mEdgeDragsLocked, 0, edl, 0, this.mEdgeDragsLocked.length);
            }
            this.mInitialMotionX = imx;
            this.mInitialMotionY = imy;
            this.mLastMotionX = lmx;
            this.mLastMotionY = lmy;
            this.mInitialEdgesTouched = iit;
            this.mEdgeDragsInProgress = edip;
            this.mEdgeDragsLocked = edl;
        }
    }

    private void saveInitialMotion(float x, float y, int pointerId) {
        ensureMotionHistorySizeForId(pointerId);
        float[] fArr = this.mInitialMotionX;
        this.mLastMotionX[pointerId] = x;
        fArr[pointerId] = x;
        fArr = this.mInitialMotionY;
        this.mLastMotionY[pointerId] = y;
        fArr[pointerId] = y;
        this.mInitialEdgesTouched[pointerId] = getEdgesTouched((int) x, (int) y);
        this.mPointersDown |= 1 << pointerId;
    }

    private void saveLastMotion(MotionEvent ev) {
        int pointerCount = MotionEventCompat.getPointerCount(ev);
        for (int i = 0; i < pointerCount; i++) {
            int pointerId = MotionEventCompat.getPointerId(ev, i);
            float x = MotionEventCompat.getX(ev, i);
            float y = MotionEventCompat.getY(ev, i);
            this.mLastMotionX[pointerId] = x;
            this.mLastMotionY[pointerId] = y;
        }
    }

    public boolean isPointerDown(int pointerId) {
        return (this.mPointersDown & (1 << pointerId)) != 0;
    }

    void setDragState(int state) {
        this.mParentView.removeCallbacks(this.mSetIdleRunnable);
        if (this.mDragState != state) {
            this.mDragState = state;
            this.mCallback.onViewDragStateChanged(state);
            if (this.mDragState == 0) {
                this.mCapturedView = null;
            }
        }
    }

    boolean tryCaptureViewForDrag(View toCapture, int pointerId) {
        if (toCapture == this.mCapturedView && this.mActivePointerId == pointerId) {
            return true;
        }
        if (toCapture == null || !this.mCallback.tryCaptureView(toCapture, pointerId)) {
            return false;
        }
        this.mActivePointerId = pointerId;
        captureChildView(toCapture, pointerId);
        return true;
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int dy, int x, int y) {
        if (v instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) v;
            int scrollX = v.getScrollX();
            int scrollY = v.getScrollY();
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() && y + scrollY >= child.getTop() && y + scrollY < child.getBottom()) {
                    if (canScroll(child, true, dx, dy, (x + scrollX) - child.getLeft(), (y + scrollY) - child.getTop())) {
                        return true;
                    }
                }
            }
        }
        return checkV && (ViewCompat.canScrollHorizontally(v, -dx) || ViewCompat.canScrollVertically(v, -dy));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean shouldInterceptTouchEvent(android.view.MotionEvent r27) {
        /*
        r26 = this;
        r4 = android.support.v4.view.MotionEventCompat.getActionMasked(r27);
        r5 = android.support.v4.view.MotionEventCompat.getActionIndex(r27);
        if (r4 != 0) goto L_0x000d;
    L_0x000a:
        r26.cancel();
    L_0x000d:
        r0 = r26;
        r0 = r0.mVelocityTracker;
        r24 = r0;
        if (r24 != 0) goto L_0x001f;
    L_0x0015:
        r24 = android.view.VelocityTracker.obtain();
        r0 = r24;
        r1 = r26;
        r1.mVelocityTracker = r0;
    L_0x001f:
        r0 = r26;
        r0 = r0.mVelocityTracker;
        r24 = r0;
        r0 = r24;
        r1 = r27;
        r0.addMovement(r1);
        switch(r4) {
            case 0: goto L_0x0040;
            case 1: goto L_0x0255;
            case 2: goto L_0x0148;
            case 3: goto L_0x0255;
            case 4: goto L_0x002f;
            case 5: goto L_0x00bf;
            case 6: goto L_0x0246;
            default: goto L_0x002f;
        };
    L_0x002f:
        r0 = r26;
        r0 = r0.mDragState;
        r24 = r0;
        r25 = 1;
        r0 = r24;
        r1 = r25;
        if (r0 != r1) goto L_0x025a;
    L_0x003d:
        r24 = 1;
    L_0x003f:
        return r24;
    L_0x0040:
        r22 = r27.getX();
        r23 = r27.getY();
        r24 = 0;
        r0 = r27;
        r1 = r24;
        r17 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r1);
        r0 = r26;
        r1 = r22;
        r2 = r23;
        r3 = r17;
        r0.saveInitialMotion(r1, r2, r3);
        r0 = r22;
        r0 = (int) r0;
        r24 = r0;
        r0 = r23;
        r0 = (int) r0;
        r25 = r0;
        r0 = r26;
        r1 = r24;
        r2 = r25;
        r20 = r0.findTopChildUnder(r1, r2);
        r0 = r26;
        r0 = r0.mCapturedView;
        r24 = r0;
        r0 = r20;
        r1 = r24;
        if (r0 != r1) goto L_0x0094;
    L_0x007d:
        r0 = r26;
        r0 = r0.mDragState;
        r24 = r0;
        r25 = 2;
        r0 = r24;
        r1 = r25;
        if (r0 != r1) goto L_0x0094;
    L_0x008b:
        r0 = r26;
        r1 = r20;
        r2 = r17;
        r0.tryCaptureViewForDrag(r1, r2);
    L_0x0094:
        r0 = r26;
        r0 = r0.mInitialEdgesTouched;
        r24 = r0;
        r8 = r24[r17];
        r0 = r26;
        r0 = r0.mTrackingEdges;
        r24 = r0;
        r24 = r24 & r8;
        if (r24 == 0) goto L_0x002f;
    L_0x00a6:
        r0 = r26;
        r0 = r0.mCallback;
        r24 = r0;
        r0 = r26;
        r0 = r0.mTrackingEdges;
        r25 = r0;
        r25 = r25 & r8;
        r0 = r24;
        r1 = r25;
        r2 = r17;
        r0.onEdgeTouched(r1, r2);
        goto L_0x002f;
    L_0x00bf:
        r0 = r27;
        r17 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r5);
        r0 = r27;
        r22 = android.support.v4.view.MotionEventCompat.getX(r0, r5);
        r0 = r27;
        r23 = android.support.v4.view.MotionEventCompat.getY(r0, r5);
        r0 = r26;
        r1 = r22;
        r2 = r23;
        r3 = r17;
        r0.saveInitialMotion(r1, r2, r3);
        r0 = r26;
        r0 = r0.mDragState;
        r24 = r0;
        if (r24 != 0) goto L_0x010f;
    L_0x00e4:
        r0 = r26;
        r0 = r0.mInitialEdgesTouched;
        r24 = r0;
        r8 = r24[r17];
        r0 = r26;
        r0 = r0.mTrackingEdges;
        r24 = r0;
        r24 = r24 & r8;
        if (r24 == 0) goto L_0x002f;
    L_0x00f6:
        r0 = r26;
        r0 = r0.mCallback;
        r24 = r0;
        r0 = r26;
        r0 = r0.mTrackingEdges;
        r25 = r0;
        r25 = r25 & r8;
        r0 = r24;
        r1 = r25;
        r2 = r17;
        r0.onEdgeTouched(r1, r2);
        goto L_0x002f;
    L_0x010f:
        r0 = r26;
        r0 = r0.mDragState;
        r24 = r0;
        r25 = 2;
        r0 = r24;
        r1 = r25;
        if (r0 != r1) goto L_0x002f;
    L_0x011d:
        r0 = r22;
        r0 = (int) r0;
        r24 = r0;
        r0 = r23;
        r0 = (int) r0;
        r25 = r0;
        r0 = r26;
        r1 = r24;
        r2 = r25;
        r20 = r0.findTopChildUnder(r1, r2);
        r0 = r26;
        r0 = r0.mCapturedView;
        r24 = r0;
        r0 = r20;
        r1 = r24;
        if (r0 != r1) goto L_0x002f;
    L_0x013d:
        r0 = r26;
        r1 = r20;
        r2 = r17;
        r0.tryCaptureViewForDrag(r1, r2);
        goto L_0x002f;
    L_0x0148:
        r0 = r26;
        r0 = r0.mInitialMotionX;
        r24 = r0;
        if (r24 == 0) goto L_0x002f;
    L_0x0150:
        r0 = r26;
        r0 = r0.mInitialMotionY;
        r24 = r0;
        if (r24 == 0) goto L_0x002f;
    L_0x0158:
        r16 = android.support.v4.view.MotionEventCompat.getPointerCount(r27);
        r10 = 0;
    L_0x015d:
        r0 = r16;
        if (r10 >= r0) goto L_0x021b;
    L_0x0161:
        r0 = r27;
        r17 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r10);
        r0 = r26;
        r1 = r17;
        r24 = r0.isValidPointerForActionMove(r1);
        if (r24 != 0) goto L_0x0174;
    L_0x0171:
        r10 = r10 + 1;
        goto L_0x015d;
    L_0x0174:
        r0 = r27;
        r22 = android.support.v4.view.MotionEventCompat.getX(r0, r10);
        r0 = r27;
        r23 = android.support.v4.view.MotionEventCompat.getY(r0, r10);
        r0 = r26;
        r0 = r0.mInitialMotionX;
        r24 = r0;
        r24 = r24[r17];
        r6 = r22 - r24;
        r0 = r26;
        r0 = r0.mInitialMotionY;
        r24 = r0;
        r24 = r24[r17];
        r7 = r23 - r24;
        r0 = r22;
        r0 = (int) r0;
        r24 = r0;
        r0 = r23;
        r0 = (int) r0;
        r25 = r0;
        r0 = r26;
        r1 = r24;
        r2 = r25;
        r20 = r0.findTopChildUnder(r1, r2);
        if (r20 == 0) goto L_0x0220;
    L_0x01aa:
        r0 = r26;
        r1 = r20;
        r24 = r0.checkTouchSlop(r1, r6, r7);
        if (r24 == 0) goto L_0x0220;
    L_0x01b4:
        r15 = 1;
    L_0x01b5:
        if (r15 == 0) goto L_0x0222;
    L_0x01b7:
        r13 = r20.getLeft();
        r0 = (int) r6;
        r24 = r0;
        r18 = r13 + r24;
        r0 = r26;
        r0 = r0.mCallback;
        r24 = r0;
        r0 = (int) r6;
        r25 = r0;
        r0 = r24;
        r1 = r20;
        r2 = r18;
        r3 = r25;
        r11 = r0.clampViewPositionHorizontal(r1, r2, r3);
        r14 = r20.getTop();
        r0 = (int) r7;
        r24 = r0;
        r19 = r14 + r24;
        r0 = r26;
        r0 = r0.mCallback;
        r24 = r0;
        r0 = (int) r7;
        r25 = r0;
        r0 = r24;
        r1 = r20;
        r2 = r19;
        r3 = r25;
        r12 = r0.clampViewPositionVertical(r1, r2, r3);
        r0 = r26;
        r0 = r0.mCallback;
        r24 = r0;
        r0 = r24;
        r1 = r20;
        r9 = r0.getViewHorizontalDragRange(r1);
        r0 = r26;
        r0 = r0.mCallback;
        r24 = r0;
        r0 = r24;
        r1 = r20;
        r21 = r0.getViewVerticalDragRange(r1);
        if (r9 == 0) goto L_0x0215;
    L_0x0211:
        if (r9 <= 0) goto L_0x0222;
    L_0x0213:
        if (r11 != r13) goto L_0x0222;
    L_0x0215:
        if (r21 == 0) goto L_0x021b;
    L_0x0217:
        if (r21 <= 0) goto L_0x0222;
    L_0x0219:
        if (r12 != r14) goto L_0x0222;
    L_0x021b:
        r26.saveLastMotion(r27);
        goto L_0x002f;
    L_0x0220:
        r15 = 0;
        goto L_0x01b5;
    L_0x0222:
        r0 = r26;
        r1 = r17;
        r0.reportNewEdgeDrags(r6, r7, r1);
        r0 = r26;
        r0 = r0.mDragState;
        r24 = r0;
        r25 = 1;
        r0 = r24;
        r1 = r25;
        if (r0 == r1) goto L_0x021b;
    L_0x0237:
        if (r15 == 0) goto L_0x0171;
    L_0x0239:
        r0 = r26;
        r1 = r20;
        r2 = r17;
        r24 = r0.tryCaptureViewForDrag(r1, r2);
        if (r24 == 0) goto L_0x0171;
    L_0x0245:
        goto L_0x021b;
    L_0x0246:
        r0 = r27;
        r17 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r5);
        r0 = r26;
        r1 = r17;
        r0.clearMotionHistory(r1);
        goto L_0x002f;
    L_0x0255:
        r26.cancel();
        goto L_0x002f;
    L_0x025a:
        r24 = 0;
        goto L_0x003f;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.widget.ViewDragHelper.shouldInterceptTouchEvent(android.view.MotionEvent):boolean");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void processTouchEvent(android.view.MotionEvent r22) {
        /*
        r21 = this;
        r3 = android.support.v4.view.MotionEventCompat.getActionMasked(r22);
        r4 = android.support.v4.view.MotionEventCompat.getActionIndex(r22);
        if (r3 != 0) goto L_0x000d;
    L_0x000a:
        r21.cancel();
    L_0x000d:
        r0 = r21;
        r0 = r0.mVelocityTracker;
        r19 = r0;
        if (r19 != 0) goto L_0x001f;
    L_0x0015:
        r19 = android.view.VelocityTracker.obtain();
        r0 = r19;
        r1 = r21;
        r1.mVelocityTracker = r0;
    L_0x001f:
        r0 = r21;
        r0 = r0.mVelocityTracker;
        r19 = r0;
        r0 = r19;
        r1 = r22;
        r0.addMovement(r1);
        switch(r3) {
            case 0: goto L_0x0030;
            case 1: goto L_0x02a0;
            case 2: goto L_0x011a;
            case 3: goto L_0x02b6;
            case 4: goto L_0x002f;
            case 5: goto L_0x008e;
            case 6: goto L_0x0217;
            default: goto L_0x002f;
        };
    L_0x002f:
        return;
    L_0x0030:
        r17 = r22.getX();
        r18 = r22.getY();
        r19 = 0;
        r0 = r22;
        r1 = r19;
        r15 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r1);
        r0 = r17;
        r0 = (int) r0;
        r19 = r0;
        r0 = r18;
        r0 = (int) r0;
        r20 = r0;
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r16 = r0.findTopChildUnder(r1, r2);
        r0 = r21;
        r1 = r17;
        r2 = r18;
        r0.saveInitialMotion(r1, r2, r15);
        r0 = r21;
        r1 = r16;
        r0.tryCaptureViewForDrag(r1, r15);
        r0 = r21;
        r0 = r0.mInitialEdgesTouched;
        r19 = r0;
        r7 = r19[r15];
        r0 = r21;
        r0 = r0.mTrackingEdges;
        r19 = r0;
        r19 = r19 & r7;
        if (r19 == 0) goto L_0x002f;
    L_0x0078:
        r0 = r21;
        r0 = r0.mCallback;
        r19 = r0;
        r0 = r21;
        r0 = r0.mTrackingEdges;
        r20 = r0;
        r20 = r20 & r7;
        r0 = r19;
        r1 = r20;
        r0.onEdgeTouched(r1, r15);
        goto L_0x002f;
    L_0x008e:
        r0 = r22;
        r15 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r4);
        r0 = r22;
        r17 = android.support.v4.view.MotionEventCompat.getX(r0, r4);
        r0 = r22;
        r18 = android.support.v4.view.MotionEventCompat.getY(r0, r4);
        r0 = r21;
        r1 = r17;
        r2 = r18;
        r0.saveInitialMotion(r1, r2, r15);
        r0 = r21;
        r0 = r0.mDragState;
        r19 = r0;
        if (r19 != 0) goto L_0x00f5;
    L_0x00b1:
        r0 = r17;
        r0 = (int) r0;
        r19 = r0;
        r0 = r18;
        r0 = (int) r0;
        r20 = r0;
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r16 = r0.findTopChildUnder(r1, r2);
        r0 = r21;
        r1 = r16;
        r0.tryCaptureViewForDrag(r1, r15);
        r0 = r21;
        r0 = r0.mInitialEdgesTouched;
        r19 = r0;
        r7 = r19[r15];
        r0 = r21;
        r0 = r0.mTrackingEdges;
        r19 = r0;
        r19 = r19 & r7;
        if (r19 == 0) goto L_0x002f;
    L_0x00de:
        r0 = r21;
        r0 = r0.mCallback;
        r19 = r0;
        r0 = r21;
        r0 = r0.mTrackingEdges;
        r20 = r0;
        r20 = r20 & r7;
        r0 = r19;
        r1 = r20;
        r0.onEdgeTouched(r1, r15);
        goto L_0x002f;
    L_0x00f5:
        r0 = r17;
        r0 = (int) r0;
        r19 = r0;
        r0 = r18;
        r0 = (int) r0;
        r20 = r0;
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r19 = r0.isCapturedViewUnder(r1, r2);
        if (r19 == 0) goto L_0x002f;
    L_0x010b:
        r0 = r21;
        r0 = r0.mCapturedView;
        r19 = r0;
        r0 = r21;
        r1 = r19;
        r0.tryCaptureViewForDrag(r1, r15);
        goto L_0x002f;
    L_0x011a:
        r0 = r21;
        r0 = r0.mDragState;
        r19 = r0;
        r20 = 1;
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x019e;
    L_0x0128:
        r0 = r21;
        r0 = r0.mActivePointerId;
        r19 = r0;
        r0 = r21;
        r1 = r19;
        r19 = r0.isValidPointerForActionMove(r1);
        if (r19 == 0) goto L_0x002f;
    L_0x0138:
        r0 = r21;
        r0 = r0.mActivePointerId;
        r19 = r0;
        r0 = r22;
        r1 = r19;
        r12 = android.support.v4.view.MotionEventCompat.findPointerIndex(r0, r1);
        r0 = r22;
        r17 = android.support.v4.view.MotionEventCompat.getX(r0, r12);
        r0 = r22;
        r18 = android.support.v4.view.MotionEventCompat.getY(r0, r12);
        r0 = r21;
        r0 = r0.mLastMotionX;
        r19 = r0;
        r0 = r21;
        r0 = r0.mActivePointerId;
        r20 = r0;
        r19 = r19[r20];
        r19 = r17 - r19;
        r0 = r19;
        r10 = (int) r0;
        r0 = r21;
        r0 = r0.mLastMotionY;
        r19 = r0;
        r0 = r21;
        r0 = r0.mActivePointerId;
        r20 = r0;
        r19 = r19[r20];
        r19 = r18 - r19;
        r0 = r19;
        r11 = (int) r0;
        r0 = r21;
        r0 = r0.mCapturedView;
        r19 = r0;
        r19 = r19.getLeft();
        r19 = r19 + r10;
        r0 = r21;
        r0 = r0.mCapturedView;
        r20 = r0;
        r20 = r20.getTop();
        r20 = r20 + r11;
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r0.dragTo(r1, r2, r10, r11);
        r21.saveLastMotion(r22);
        goto L_0x002f;
    L_0x019e:
        r14 = android.support.v4.view.MotionEventCompat.getPointerCount(r22);
        r8 = 0;
    L_0x01a3:
        if (r8 >= r14) goto L_0x01e9;
    L_0x01a5:
        r0 = r22;
        r15 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r8);
        r0 = r21;
        r19 = r0.isValidPointerForActionMove(r15);
        if (r19 != 0) goto L_0x01b6;
    L_0x01b3:
        r8 = r8 + 1;
        goto L_0x01a3;
    L_0x01b6:
        r0 = r22;
        r17 = android.support.v4.view.MotionEventCompat.getX(r0, r8);
        r0 = r22;
        r18 = android.support.v4.view.MotionEventCompat.getY(r0, r8);
        r0 = r21;
        r0 = r0.mInitialMotionX;
        r19 = r0;
        r19 = r19[r15];
        r5 = r17 - r19;
        r0 = r21;
        r0 = r0.mInitialMotionY;
        r19 = r0;
        r19 = r19[r15];
        r6 = r18 - r19;
        r0 = r21;
        r0.reportNewEdgeDrags(r5, r6, r15);
        r0 = r21;
        r0 = r0.mDragState;
        r19 = r0;
        r20 = 1;
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x01ee;
    L_0x01e9:
        r21.saveLastMotion(r22);
        goto L_0x002f;
    L_0x01ee:
        r0 = r17;
        r0 = (int) r0;
        r19 = r0;
        r0 = r18;
        r0 = (int) r0;
        r20 = r0;
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r16 = r0.findTopChildUnder(r1, r2);
        r0 = r21;
        r1 = r16;
        r19 = r0.checkTouchSlop(r1, r5, r6);
        if (r19 == 0) goto L_0x01b3;
    L_0x020c:
        r0 = r21;
        r1 = r16;
        r19 = r0.tryCaptureViewForDrag(r1, r15);
        if (r19 == 0) goto L_0x01b3;
    L_0x0216:
        goto L_0x01e9;
    L_0x0217:
        r0 = r22;
        r15 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r4);
        r0 = r21;
        r0 = r0.mDragState;
        r19 = r0;
        r20 = 1;
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x0299;
    L_0x022b:
        r0 = r21;
        r0 = r0.mActivePointerId;
        r19 = r0;
        r0 = r19;
        if (r15 != r0) goto L_0x0299;
    L_0x0235:
        r13 = -1;
        r14 = android.support.v4.view.MotionEventCompat.getPointerCount(r22);
        r8 = 0;
    L_0x023b:
        if (r8 >= r14) goto L_0x0290;
    L_0x023d:
        r0 = r22;
        r9 = android.support.v4.view.MotionEventCompat.getPointerId(r0, r8);
        r0 = r21;
        r0 = r0.mActivePointerId;
        r19 = r0;
        r0 = r19;
        if (r9 != r0) goto L_0x0250;
    L_0x024d:
        r8 = r8 + 1;
        goto L_0x023b;
    L_0x0250:
        r0 = r22;
        r17 = android.support.v4.view.MotionEventCompat.getX(r0, r8);
        r0 = r22;
        r18 = android.support.v4.view.MotionEventCompat.getY(r0, r8);
        r0 = r17;
        r0 = (int) r0;
        r19 = r0;
        r0 = r18;
        r0 = (int) r0;
        r20 = r0;
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r19 = r0.findTopChildUnder(r1, r2);
        r0 = r21;
        r0 = r0.mCapturedView;
        r20 = r0;
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x024d;
    L_0x027c:
        r0 = r21;
        r0 = r0.mCapturedView;
        r19 = r0;
        r0 = r21;
        r1 = r19;
        r19 = r0.tryCaptureViewForDrag(r1, r9);
        if (r19 == 0) goto L_0x024d;
    L_0x028c:
        r0 = r21;
        r13 = r0.mActivePointerId;
    L_0x0290:
        r19 = -1;
        r0 = r19;
        if (r13 != r0) goto L_0x0299;
    L_0x0296:
        r21.releaseViewForPointerUp();
    L_0x0299:
        r0 = r21;
        r0.clearMotionHistory(r15);
        goto L_0x002f;
    L_0x02a0:
        r0 = r21;
        r0 = r0.mDragState;
        r19 = r0;
        r20 = 1;
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x02b1;
    L_0x02ae:
        r21.releaseViewForPointerUp();
    L_0x02b1:
        r21.cancel();
        goto L_0x002f;
    L_0x02b6:
        r0 = r21;
        r0 = r0.mDragState;
        r19 = r0;
        r20 = 1;
        r0 = r19;
        r1 = r20;
        if (r0 != r1) goto L_0x02d1;
    L_0x02c4:
        r19 = 0;
        r20 = 0;
        r0 = r21;
        r1 = r19;
        r2 = r20;
        r0.dispatchViewReleased(r1, r2);
    L_0x02d1:
        r21.cancel();
        goto L_0x002f;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.widget.ViewDragHelper.processTouchEvent(android.view.MotionEvent):void");
    }

    private void reportNewEdgeDrags(float dx, float dy, int pointerId) {
        int dragsStarted = 0;
        if (checkNewEdgeDrag(dx, dy, pointerId, 1)) {
            dragsStarted = 0 | 1;
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, 4)) {
            dragsStarted |= 4;
        }
        if (checkNewEdgeDrag(dx, dy, pointerId, 2)) {
            dragsStarted |= 2;
        }
        if (checkNewEdgeDrag(dy, dx, pointerId, 8)) {
            dragsStarted |= 8;
        }
        if (dragsStarted != 0) {
            int[] iArr = this.mEdgeDragsInProgress;
            iArr[pointerId] = iArr[pointerId] | dragsStarted;
            this.mCallback.onEdgeDragStarted(dragsStarted, pointerId);
        }
    }

    private boolean checkNewEdgeDrag(float delta, float odelta, int pointerId, int edge) {
        float absDelta = Math.abs(delta);
        float absODelta = Math.abs(odelta);
        if ((this.mInitialEdgesTouched[pointerId] & edge) != edge || (this.mTrackingEdges & edge) == 0 || (this.mEdgeDragsLocked[pointerId] & edge) == edge || (this.mEdgeDragsInProgress[pointerId] & edge) == edge) {
            return false;
        }
        if (absDelta <= ((float) this.mTouchSlop) && absODelta <= ((float) this.mTouchSlop)) {
            return false;
        }
        if (absDelta < 0.5f * absODelta && this.mCallback.onEdgeLock(edge)) {
            int[] iArr = this.mEdgeDragsLocked;
            iArr[pointerId] = iArr[pointerId] | edge;
            return false;
        } else if ((this.mEdgeDragsInProgress[pointerId] & edge) != 0 || absDelta <= ((float) this.mTouchSlop)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean checkTouchSlop(View child, float dx, float dy) {
        if (child == null) {
            return false;
        }
        boolean checkHorizontal;
        boolean checkVertical;
        if (this.mCallback.getViewHorizontalDragRange(child) > 0) {
            checkHorizontal = true;
        } else {
            checkHorizontal = false;
        }
        if (this.mCallback.getViewVerticalDragRange(child) > 0) {
            checkVertical = true;
        } else {
            checkVertical = false;
        }
        if (checkHorizontal && checkVertical) {
            if ((dx * dx) + (dy * dy) <= ((float) (this.mTouchSlop * this.mTouchSlop))) {
                return false;
            }
            return true;
        } else if (checkHorizontal) {
            if (Math.abs(dx) <= ((float) this.mTouchSlop)) {
                return false;
            }
            return true;
        } else if (!checkVertical) {
            return false;
        } else {
            if (Math.abs(dy) <= ((float) this.mTouchSlop)) {
                return false;
            }
            return true;
        }
    }

    public boolean checkTouchSlop(int directions) {
        int count = this.mInitialMotionX.length;
        for (int i = 0; i < count; i++) {
            if (checkTouchSlop(directions, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTouchSlop(int directions, int pointerId) {
        if (!isPointerDown(pointerId)) {
            return false;
        }
        boolean checkHorizontal;
        boolean checkVertical;
        if ((directions & 1) == 1) {
            checkHorizontal = true;
        } else {
            checkHorizontal = false;
        }
        if ((directions & 2) == 2) {
            checkVertical = true;
        } else {
            checkVertical = false;
        }
        float dx = this.mLastMotionX[pointerId] - this.mInitialMotionX[pointerId];
        float dy = this.mLastMotionY[pointerId] - this.mInitialMotionY[pointerId];
        if (checkHorizontal && checkVertical) {
            if ((dx * dx) + (dy * dy) <= ((float) (this.mTouchSlop * this.mTouchSlop))) {
                return false;
            }
            return true;
        } else if (checkHorizontal) {
            if (Math.abs(dx) <= ((float) this.mTouchSlop)) {
                return false;
            }
            return true;
        } else if (!checkVertical) {
            return false;
        } else {
            if (Math.abs(dy) <= ((float) this.mTouchSlop)) {
                return false;
            }
            return true;
        }
    }

    public boolean isEdgeTouched(int edges) {
        int count = this.mInitialEdgesTouched.length;
        for (int i = 0; i < count; i++) {
            if (isEdgeTouched(edges, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEdgeTouched(int edges, int pointerId) {
        return isPointerDown(pointerId) && (this.mInitialEdgesTouched[pointerId] & edges) != 0;
    }

    private void releaseViewForPointerUp() {
        this.mVelocityTracker.computeCurrentVelocity(FileOperator.MAX_DIR_LENGTH, this.mMaxVelocity);
        dispatchViewReleased(clampMag(VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity), clampMag(VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mActivePointerId), this.mMinVelocity, this.mMaxVelocity));
    }

    private void dragTo(int left, int top, int dx, int dy) {
        int clampedX = left;
        int clampedY = top;
        int oldLeft = this.mCapturedView.getLeft();
        int oldTop = this.mCapturedView.getTop();
        if (dx != 0) {
            clampedX = this.mCallback.clampViewPositionHorizontal(this.mCapturedView, left, dx);
            ViewCompat.offsetLeftAndRight(this.mCapturedView, clampedX - oldLeft);
        }
        if (dy != 0) {
            clampedY = this.mCallback.clampViewPositionVertical(this.mCapturedView, top, dy);
            ViewCompat.offsetTopAndBottom(this.mCapturedView, clampedY - oldTop);
        }
        if (dx != 0 || dy != 0) {
            this.mCallback.onViewPositionChanged(this.mCapturedView, clampedX, clampedY, clampedX - oldLeft, clampedY - oldTop);
        }
    }

    public boolean isCapturedViewUnder(int x, int y) {
        return isViewUnder(this.mCapturedView, x, y);
    }

    public boolean isViewUnder(View view, int x, int y) {
        if (view != null && x >= view.getLeft() && x < view.getRight() && y >= view.getTop() && y < view.getBottom()) {
            return true;
        }
        return false;
    }

    public View findTopChildUnder(int x, int y) {
        for (int i = this.mParentView.getChildCount() - 1; i >= 0; i--) {
            View child = this.mParentView.getChildAt(this.mCallback.getOrderedChildIndex(i));
            if (x >= child.getLeft() && x < child.getRight() && y >= child.getTop() && y < child.getBottom()) {
                return child;
            }
        }
        return null;
    }

    private int getEdgesTouched(int x, int y) {
        int result = 0;
        if (x < this.mParentView.getLeft() + this.mEdgeSize) {
            result = 0 | 1;
        }
        if (y < this.mParentView.getTop() + this.mEdgeSize) {
            result |= 4;
        }
        if (x > this.mParentView.getRight() - this.mEdgeSize) {
            result |= 2;
        }
        if (y > this.mParentView.getBottom() - this.mEdgeSize) {
            return result | 8;
        }
        return result;
    }

    private boolean isValidPointerForActionMove(int pointerId) {
        if (isPointerDown(pointerId)) {
            return true;
        }
        Log.e(TAG, "Ignoring pointerId=" + pointerId + " because ACTION_DOWN was not received " + "for this pointer before ACTION_MOVE. It likely happened because " + " ViewDragHelper did not receive all the events in the event stream.");
        return false;
    }
}
