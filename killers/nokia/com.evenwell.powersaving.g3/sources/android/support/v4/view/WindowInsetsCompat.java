package android.support.v4.view;

import android.graphics.Rect;

public class WindowInsetsCompat {
    WindowInsetsCompat() {
    }

    public int getSystemWindowInsetLeft() {
        return 0;
    }

    public int getSystemWindowInsetTop() {
        return 0;
    }

    public int getSystemWindowInsetRight() {
        return 0;
    }

    public int getSystemWindowInsetBottom() {
        return 0;
    }

    public boolean hasSystemWindowInsets() {
        return false;
    }

    public boolean hasInsets() {
        return false;
    }

    public boolean isConsumed() {
        return false;
    }

    public boolean isRound() {
        return false;
    }

    public WindowInsetsCompat consumeSystemWindowInsets() {
        return this;
    }

    public WindowInsetsCompat replaceSystemWindowInsets(int left, int top, int right, int bottom) {
        return this;
    }

    public WindowInsetsCompat replaceSystemWindowInsets(Rect systemWindowInsets) {
        return this;
    }

    public int getStableInsetTop() {
        return 0;
    }

    public int getStableInsetLeft() {
        return 0;
    }

    public int getStableInsetRight() {
        return 0;
    }

    public int getStableInsetBottom() {
        return 0;
    }

    public boolean hasStableInsets() {
        return false;
    }

    public WindowInsetsCompat consumeStableInsets() {
        return this;
    }
}
