package android.support.v4.view;

import android.graphics.Rect;
import android.view.WindowInsets;

class WindowInsetsCompatApi21 extends WindowInsetsCompat {
    private final WindowInsets mSource;

    WindowInsetsCompatApi21(WindowInsets source) {
        this.mSource = source;
    }

    public int getSystemWindowInsetLeft() {
        return this.mSource.getSystemWindowInsetLeft();
    }

    public int getSystemWindowInsetTop() {
        return this.mSource.getSystemWindowInsetTop();
    }

    public int getSystemWindowInsetRight() {
        return this.mSource.getSystemWindowInsetRight();
    }

    public int getSystemWindowInsetBottom() {
        return this.mSource.getSystemWindowInsetBottom();
    }

    public boolean hasSystemWindowInsets() {
        return this.mSource.hasSystemWindowInsets();
    }

    public boolean hasInsets() {
        return this.mSource.hasInsets();
    }

    public boolean isConsumed() {
        return this.mSource.isConsumed();
    }

    public boolean isRound() {
        return this.mSource.isRound();
    }

    public WindowInsetsCompat consumeSystemWindowInsets() {
        return new WindowInsetsCompatApi21(this.mSource.consumeSystemWindowInsets());
    }

    public WindowInsetsCompat replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        return new WindowInsetsCompatApi21(this.mSource.replaceSystemWindowInsets(left, top, right, bottom));
    }

    public WindowInsetsCompat replaceSystemWindowInsets(Rect systemWindowInsets) {
        return new WindowInsetsCompatApi21(this.mSource.replaceSystemWindowInsets(systemWindowInsets));
    }

    public int getStableInsetTop() {
        return this.mSource.getStableInsetTop();
    }

    public int getStableInsetLeft() {
        return this.mSource.getStableInsetLeft();
    }

    public int getStableInsetRight() {
        return this.mSource.getStableInsetRight();
    }

    public int getStableInsetBottom() {
        return this.mSource.getStableInsetBottom();
    }

    public boolean hasStableInsets() {
        return this.mSource.hasStableInsets();
    }

    public WindowInsetsCompat consumeStableInsets() {
        return new WindowInsetsCompatApi21(this.mSource.consumeStableInsets());
    }

    WindowInsets unwrap() {
        return this.mSource;
    }
}
