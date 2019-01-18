package android.support.v4.view.accessibility;

import android.graphics.Rect;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

class AccessibilityNodeInfoCompatIcs {
    AccessibilityNodeInfoCompatIcs() {
    }

    public static Object obtain() {
        return AccessibilityNodeInfo.obtain();
    }

    public static Object obtain(View source) {
        return AccessibilityNodeInfo.obtain(source);
    }

    public static Object obtain(Object info) {
        return AccessibilityNodeInfo.obtain((AccessibilityNodeInfo) info);
    }

    public static void addAction(Object info, int action) {
        ((AccessibilityNodeInfo) info).addAction(action);
    }

    public static void addChild(Object info, View child) {
        ((AccessibilityNodeInfo) info).addChild(child);
    }

    public static List<Object> findAccessibilityNodeInfosByText(Object info, String text) {
        return ((AccessibilityNodeInfo) info).findAccessibilityNodeInfosByText(text);
    }

    public static int getActions(Object info) {
        return ((AccessibilityNodeInfo) info).getActions();
    }

    public static void getBoundsInParent(Object info, Rect outBounds) {
        ((AccessibilityNodeInfo) info).getBoundsInParent(outBounds);
    }

    public static void getBoundsInScreen(Object info, Rect outBounds) {
        ((AccessibilityNodeInfo) info).getBoundsInScreen(outBounds);
    }

    public static Object getChild(Object info, int index) {
        return ((AccessibilityNodeInfo) info).getChild(index);
    }

    public static int getChildCount(Object info) {
        return ((AccessibilityNodeInfo) info).getChildCount();
    }

    public static CharSequence getClassName(Object info) {
        return ((AccessibilityNodeInfo) info).getClassName();
    }

    public static CharSequence getContentDescription(Object info) {
        return ((AccessibilityNodeInfo) info).getContentDescription();
    }

    public static CharSequence getPackageName(Object info) {
        return ((AccessibilityNodeInfo) info).getPackageName();
    }

    public static Object getParent(Object info) {
        return ((AccessibilityNodeInfo) info).getParent();
    }

    public static CharSequence getText(Object info) {
        return ((AccessibilityNodeInfo) info).getText();
    }

    public static int getWindowId(Object info) {
        return ((AccessibilityNodeInfo) info).getWindowId();
    }

    public static boolean isCheckable(Object info) {
        return ((AccessibilityNodeInfo) info).isCheckable();
    }

    public static boolean isChecked(Object info) {
        return ((AccessibilityNodeInfo) info).isChecked();
    }

    public static boolean isClickable(Object info) {
        return ((AccessibilityNodeInfo) info).isClickable();
    }

    public static boolean isEnabled(Object info) {
        return ((AccessibilityNodeInfo) info).isEnabled();
    }

    public static boolean isFocusable(Object info) {
        return ((AccessibilityNodeInfo) info).isFocusable();
    }

    public static boolean isFocused(Object info) {
        return ((AccessibilityNodeInfo) info).isFocused();
    }

    public static boolean isLongClickable(Object info) {
        return ((AccessibilityNodeInfo) info).isLongClickable();
    }

    public static boolean isPassword(Object info) {
        return ((AccessibilityNodeInfo) info).isPassword();
    }

    public static boolean isScrollable(Object info) {
        return ((AccessibilityNodeInfo) info).isScrollable();
    }

    public static boolean isSelected(Object info) {
        return ((AccessibilityNodeInfo) info).isSelected();
    }

    public static boolean performAction(Object info, int action) {
        return ((AccessibilityNodeInfo) info).performAction(action);
    }

    public static void setBoundsInParent(Object info, Rect bounds) {
        ((AccessibilityNodeInfo) info).setBoundsInParent(bounds);
    }

    public static void setBoundsInScreen(Object info, Rect bounds) {
        ((AccessibilityNodeInfo) info).setBoundsInScreen(bounds);
    }

    public static void setCheckable(Object info, boolean checkable) {
        ((AccessibilityNodeInfo) info).setCheckable(checkable);
    }

    public static void setChecked(Object info, boolean checked) {
        ((AccessibilityNodeInfo) info).setChecked(checked);
    }

    public static void setClassName(Object info, CharSequence className) {
        ((AccessibilityNodeInfo) info).setClassName(className);
    }

    public static void setClickable(Object info, boolean clickable) {
        ((AccessibilityNodeInfo) info).setClickable(clickable);
    }

    public static void setContentDescription(Object info, CharSequence contentDescription) {
        ((AccessibilityNodeInfo) info).setContentDescription(contentDescription);
    }

    public static void setEnabled(Object info, boolean enabled) {
        ((AccessibilityNodeInfo) info).setEnabled(enabled);
    }

    public static void setFocusable(Object info, boolean focusable) {
        ((AccessibilityNodeInfo) info).setFocusable(focusable);
    }

    public static void setFocused(Object info, boolean focused) {
        ((AccessibilityNodeInfo) info).setFocused(focused);
    }

    public static void setLongClickable(Object info, boolean longClickable) {
        ((AccessibilityNodeInfo) info).setLongClickable(longClickable);
    }

    public static void setPackageName(Object info, CharSequence packageName) {
        ((AccessibilityNodeInfo) info).setPackageName(packageName);
    }

    public static void setParent(Object info, View parent) {
        ((AccessibilityNodeInfo) info).setParent(parent);
    }

    public static void setPassword(Object info, boolean password) {
        ((AccessibilityNodeInfo) info).setPassword(password);
    }

    public static void setScrollable(Object info, boolean scrollable) {
        ((AccessibilityNodeInfo) info).setScrollable(scrollable);
    }

    public static void setSelected(Object info, boolean selected) {
        ((AccessibilityNodeInfo) info).setSelected(selected);
    }

    public static void setSource(Object info, View source) {
        ((AccessibilityNodeInfo) info).setSource(source);
    }

    public static void setText(Object info, CharSequence text) {
        ((AccessibilityNodeInfo) info).setText(text);
    }

    public static void recycle(Object info) {
        ((AccessibilityNodeInfo) info).recycle();
    }
}
