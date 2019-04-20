package android.support.v4.view.accessibility;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityWindowInfo;

class AccessibilityWindowInfoCompatApi21 {
    AccessibilityWindowInfoCompatApi21() {
    }

    public static Object obtain() {
        return AccessibilityWindowInfo.obtain();
    }

    public static Object obtain(Object info) {
        return AccessibilityWindowInfo.obtain((AccessibilityWindowInfo) info);
    }

    public static int getType(Object info) {
        return ((AccessibilityWindowInfo) info).getType();
    }

    public static int getLayer(Object info) {
        return ((AccessibilityWindowInfo) info).getLayer();
    }

    public static Object getRoot(Object info) {
        return ((AccessibilityWindowInfo) info).getRoot();
    }

    public static Object getParent(Object info) {
        return ((AccessibilityWindowInfo) info).getParent();
    }

    public static int getId(Object info) {
        return ((AccessibilityWindowInfo) info).getId();
    }

    public static void getBoundsInScreen(Object info, Rect outBounds) {
        ((AccessibilityWindowInfo) info).getBoundsInScreen(outBounds);
    }

    public static boolean isActive(Object info) {
        return ((AccessibilityWindowInfo) info).isActive();
    }

    public static boolean isFocused(Object info) {
        return ((AccessibilityWindowInfo) info).isFocused();
    }

    public static boolean isAccessibilityFocused(Object info) {
        return ((AccessibilityWindowInfo) info).isAccessibilityFocused();
    }

    public static int getChildCount(Object info) {
        return ((AccessibilityWindowInfo) info).getChildCount();
    }

    public static Object getChild(Object info, int index) {
        return ((AccessibilityWindowInfo) info).getChild(index);
    }

    public static void recycle(Object info) {
        ((AccessibilityWindowInfo) info).recycle();
    }
}
