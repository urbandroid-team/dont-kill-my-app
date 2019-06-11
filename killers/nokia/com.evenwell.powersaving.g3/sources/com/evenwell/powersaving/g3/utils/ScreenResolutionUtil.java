package com.evenwell.powersaving.g3.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;

public class ScreenResolutionUtil {
    private static final boolean DBG = true;
    private static final String TAG = "PowerSavingAppG3";

    public static void changeResoultionByRate(Context context, int rate) {
        int size_w;
        int size_h;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService("window");
        Point size = new Point();
        windowManager.getDefaultDisplay().getRealSize(size);
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        int density = (((int) (metrics.density * 160.0f)) * rate) / 100;
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case 1:
            case 3:
                size_w = (size.y * rate) / 100;
                size_h = (size.x * rate) / 100;
                break;
            default:
                size_w = (size.x * rate) / 100;
                size_h = (size.y * rate) / 100;
                break;
        }
        Log.i("PowerSavingAppG3", "size_w = " + size_w + ", size_h = " + size_h + ", density = " + density + ", rate = " + rate);
        try {
            IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
            wm.setForcedDisplayDensityForUser(0, density, UserHandle.myUserId());
            wm.setForcedDisplaySize(0, size_w, size_h);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void resetDisplaySize() {
        try {
            WindowManagerGlobal.getWindowManagerService().clearForcedDisplaySize(0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void resetDensity() {
        try {
            WindowManagerGlobal.getWindowManagerService().clearForcedDisplayDensityForUser(0, UserHandle.myUserId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void setDensity(int density) {
        try {
            IWindowManager wm = WindowManagerGlobal.getWindowManagerService();
            int userId = UserHandle.myUserId();
            Log.i("ScreenResolutionUtil", "density = " + density);
            wm.setForcedDisplayDensityForUser(0, density, userId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void singleByIWM(int size_w, int size_h, int density) {
        try {
            WindowManagerGlobal.getWindowManagerService().setForcedDisplaySize(0, Integer.valueOf(size_w).intValue(), (Integer.valueOf(size_h).intValue() & 65535) | (Integer.valueOf(density).intValue() << 16));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
