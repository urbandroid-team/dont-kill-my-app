package android.support.v4.view.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

class AccessibilityNodeInfoCompatApi22 {
    AccessibilityNodeInfoCompatApi22() {
    }

    public static Object getTraversalBefore(Object info) {
        return ((AccessibilityNodeInfo) info).getTraversalBefore();
    }

    public static void setTraversalBefore(Object info, View view) {
        ((AccessibilityNodeInfo) info).setTraversalBefore(view);
    }

    public static void setTraversalBefore(Object info, View root, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).setTraversalBefore(root, virtualDescendantId);
    }

    public static Object getTraversalAfter(Object info) {
        return ((AccessibilityNodeInfo) info).getTraversalAfter();
    }

    public static void setTraversalAfter(Object info, View view) {
        ((AccessibilityNodeInfo) info).setTraversalAfter(view);
    }

    public static void setTraversalAfter(Object info, View root, int virtualDescendantId) {
        ((AccessibilityNodeInfo) info).setTraversalAfter(root, virtualDescendantId);
    }
}
