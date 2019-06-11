package android.support.v4.view.accessibility;

import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.view.accessibility.AccessibilityNodeInfo.CollectionInfo;
import java.util.List;

class AccessibilityNodeInfoCompatApi21 {

    static class CollectionItemInfo {
        CollectionItemInfo() {
        }

        public static boolean isSelected(Object info) {
            return ((android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo) info).isSelected();
        }
    }

    AccessibilityNodeInfoCompatApi21() {
    }

    static List<Object> getActionList(Object info) {
        return ((AccessibilityNodeInfo) info).getActionList();
    }

    static void addAction(Object info, Object action) {
        ((AccessibilityNodeInfo) info).addAction((AccessibilityAction) action);
    }

    public static boolean removeAction(Object info, Object action) {
        return ((AccessibilityNodeInfo) info).removeAction((AccessibilityAction) action);
    }

    public static Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
        return CollectionInfo.obtain(rowCount, columnCount, hierarchical, selectionMode);
    }

    public static Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
        return android.view.accessibility.AccessibilityNodeInfo.CollectionItemInfo.obtain(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
    }

    public static CharSequence getError(Object info) {
        return ((AccessibilityNodeInfo) info).getError();
    }

    public static void setError(Object info, CharSequence error) {
        ((AccessibilityNodeInfo) info).setError(error);
    }

    public static void setMaxTextLength(Object info, int max) {
        ((AccessibilityNodeInfo) info).setMaxTextLength(max);
    }

    public static int getMaxTextLength(Object info) {
        return ((AccessibilityNodeInfo) info).getMaxTextLength();
    }

    public static Object getWindow(Object info) {
        return ((AccessibilityNodeInfo) info).getWindow();
    }

    public static boolean removeChild(Object info, View child) {
        return ((AccessibilityNodeInfo) info).removeChild(child);
    }

    public static boolean removeChild(Object info, View root, int virtualDescendantId) {
        return ((AccessibilityNodeInfo) info).removeChild(root, virtualDescendantId);
    }

    static Object newAccessibilityAction(int actionId, CharSequence label) {
        return new AccessibilityAction(actionId, label);
    }

    static int getAccessibilityActionId(Object action) {
        return ((AccessibilityAction) action).getId();
    }

    static CharSequence getAccessibilityActionLabel(Object action) {
        return ((AccessibilityAction) action).getLabel();
    }
}
