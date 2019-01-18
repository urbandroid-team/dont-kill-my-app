package android.support.v4.hardware.display;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

final class DisplayManagerJellybeanMr1 {
    DisplayManagerJellybeanMr1() {
    }

    public static Object getDisplayManager(Context context) {
        return context.getSystemService("display");
    }

    public static Display getDisplay(Object displayManagerObj, int displayId) {
        return ((DisplayManager) displayManagerObj).getDisplay(displayId);
    }

    public static Display[] getDisplays(Object displayManagerObj) {
        return ((DisplayManager) displayManagerObj).getDisplays();
    }

    public static Display[] getDisplays(Object displayManagerObj, String category) {
        return ((DisplayManager) displayManagerObj).getDisplays(category);
    }
}
