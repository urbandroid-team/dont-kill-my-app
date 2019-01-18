package android.support.v4.view.accessibility;

import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

class AccessibilityNodeInfoCompatJellybeanMr2 {
    AccessibilityNodeInfoCompatJellybeanMr2() {
    }

    public static void setViewIdResourceName(Object info, String viewId) {
        ((AccessibilityNodeInfo) info).setViewIdResourceName(viewId);
    }

    public static String getViewIdResourceName(Object info) {
        return ((AccessibilityNodeInfo) info).getViewIdResourceName();
    }

    public static List<Object> findAccessibilityNodeInfosByViewId(Object info, String viewId) {
        return ((AccessibilityNodeInfo) info).findAccessibilityNodeInfosByViewId(viewId);
    }

    public static void setTextSelection(Object info, int start, int end) {
        ((AccessibilityNodeInfo) info).setTextSelection(start, end);
    }

    public static int getTextSelectionStart(Object info) {
        return ((AccessibilityNodeInfo) info).getTextSelectionStart();
    }

    public static int getTextSelectionEnd(Object info) {
        return ((AccessibilityNodeInfo) info).getTextSelectionEnd();
    }

    public static boolean isEditable(Object info) {
        return ((AccessibilityNodeInfo) info).isEditable();
    }

    public static void setEditable(Object info, boolean editable) {
        ((AccessibilityNodeInfo) info).setEditable(editable);
    }

    public static boolean refresh(Object info) {
        return ((AccessibilityNodeInfo) info).refresh();
    }
}
