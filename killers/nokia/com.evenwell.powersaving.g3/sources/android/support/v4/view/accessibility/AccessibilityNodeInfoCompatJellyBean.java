package android.support.v4.view.accessibility;

import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

class AccessibilityNodeInfoCompatJellyBean {
    AccessibilityNodeInfoCompatJellyBean() {
    }

    public static void addChild(Object info, View child, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).addChild(child, virtualDescendantId);
    }

    public static void setSource(Object info, View root, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).setSource(root, virtualDescendantId);
    }

    public static boolean isVisibleToUser(Object info) {
        return ((AccessibilityNodeInfo) info).isVisibleToUser();
    }

    public static void setVisibleToUser(Object info, boolean visibleToUser) {
        ((AccessibilityNodeInfo) info).setVisibleToUser(visibleToUser);
    }

    public static boolean performAction(Object info, int action, Bundle arguments) {
        return ((AccessibilityNodeInfo) info).performAction(action, arguments);
    }

    public static void setMovementGranularities(Object info, int granularities) {
        ((AccessibilityNodeInfo) info).setMovementGranularities(granularities);
    }

    public static int getMovementGranularities(Object info) {
        return ((AccessibilityNodeInfo) info).getMovementGranularities();
    }

    public static Object obtain(View root, int virtualDescendantId) {
        return AccessibilityNodeInfo.obtain(root, virtualDescendantId);
    }

    public static Object findFocus(Object info, int focus) {
        return ((AccessibilityNodeInfo) info).findFocus(focus);
    }

    public static Object focusSearch(Object info, int direction) {
        return ((AccessibilityNodeInfo) info).focusSearch(direction);
    }

    public static void setParent(Object info, View root, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).setParent(root, virtualDescendantId);
    }

    public static boolean isAccessibilityFocused(Object info) {
        return ((AccessibilityNodeInfo) info).isAccessibilityFocused();
    }

    public static void setAccesibilityFocused(Object info, boolean focused) {
        ((AccessibilityNodeInfo) info).setAccessibilityFocused(focused);
    }
}
