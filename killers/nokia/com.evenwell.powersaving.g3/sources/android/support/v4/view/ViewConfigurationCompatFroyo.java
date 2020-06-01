package android.support.v4.view;

import android.view.ViewConfiguration;

class ViewConfigurationCompatFroyo {
    ViewConfigurationCompatFroyo() {
    }

    public static int getScaledPagingTouchSlop(ViewConfiguration config) {
        return config.getScaledPagingTouchSlop();
    }
}
