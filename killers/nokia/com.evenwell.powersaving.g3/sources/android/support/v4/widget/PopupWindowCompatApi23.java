package android.support.v4.widget;

import android.widget.PopupWindow;

class PopupWindowCompatApi23 {
    PopupWindowCompatApi23() {
    }

    static void setOverlapAnchor(PopupWindow popupWindow, boolean overlapAnchor) {
        popupWindow.setOverlapAnchor(overlapAnchor);
    }

    static boolean getOverlapAnchor(PopupWindow popupWindow) {
        return popupWindow.getOverlapAnchor();
    }

    static void setWindowLayoutType(PopupWindow popupWindow, int layoutType) {
        popupWindow.setWindowLayoutType(layoutType);
    }

    static int getWindowLayoutType(PopupWindow popupWindow) {
        return popupWindow.getWindowLayoutType();
    }
}
