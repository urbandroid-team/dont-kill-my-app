package com.evenwell.powersaving.g3.utils;

import android.content.Context;
import android.graphics.Point;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.evenwell.powersaving.g3.C0321R;

public class DisplayResolutionUtil {
    public static final String SCREEN_RESOLUTION_VALUE_LARGE = "100";
    public static final String SCREEN_RESOLUTION_VALUE_SMALL = "75";
    private static int mDefaultHeight = -1;
    private static int mDefaultWidth = -1;

    public static String getDefaultString(Context mContext) {
        String mDefaultString = "";
        int mHeight = mDefaultHeight;
        int mWidth = mDefaultWidth;
        if (mWidth == 720 && mHeight == 1080) {
            return mContext.getResources().getString(C0321R.string.screen_resolution_hd);
        }
        if (mWidth == 1080 && mHeight == 1920) {
            return mContext.getResources().getString(C0321R.string.screen_resolution_fhd);
        }
        if (mWidth == 1440 && mHeight == 1560) {
            return mContext.getResources().getString(C0321R.string.screen_resolution_qhd);
        }
        if (mWidth >= 720 && mWidth < 1080) {
            mDefaultString = mContext.getResources().getString(C0321R.string.screen_resolution_hdplus);
        } else if (mWidth >= 1080 && mWidth < 1440) {
            mDefaultString = mContext.getResources().getString(C0321R.string.screen_resolution_fhdplus);
        } else if (mWidth >= 1440 && mWidth < 2160) {
            mDefaultString = mContext.getResources().getString(C0321R.string.screen_resolution_qhdplus);
        } else if (mWidth >= 2160) {
            mDefaultString = mContext.getResources().getString(C0321R.string.screen_resolution_4k);
        }
        return mDefaultString;
    }

    public static String getSmallString(Context mContext) {
        String mSmallString = "";
        int mHeight = (mDefaultHeight * 75) / 100;
        int mWidth = (mDefaultWidth * 75) / 100;
        if (mWidth == 720 && mHeight == 1080) {
            return mContext.getResources().getString(C0321R.string.screen_resolution_hd);
        }
        if (mWidth == 1080 && mHeight == 1920) {
            return mContext.getResources().getString(C0321R.string.screen_resolution_fhd);
        }
        if (mWidth == 1440 && mHeight == 1560) {
            return mContext.getResources().getString(C0321R.string.screen_resolution_qhd);
        }
        if (mWidth >= 720 && mWidth < 1080) {
            mSmallString = mContext.getResources().getString(C0321R.string.screen_resolution_hdplus);
        } else if (mWidth >= 1080 && mWidth < 1440) {
            mSmallString = mContext.getResources().getString(C0321R.string.screen_resolution_fhdplus);
        } else if (mWidth >= 1440 && mWidth < 2160) {
            mSmallString = mContext.getResources().getString(C0321R.string.screen_resolution_qhdplus);
        } else if (mWidth >= 2160) {
            mSmallString = mContext.getResources().getString(C0321R.string.screen_resolution_4k);
        }
        return mSmallString;
    }

    public static String getCurrentSizeRatio(Context mContext) {
        int ratio;
        DisplayMetrics metrics = new DisplayMetrics();
        Point defaultSize = new Point();
        Point currentSize = new Point();
        try {
            WindowManagerGlobal.getWindowManagerService().getInitialDisplaySize(0, defaultSize);
        } catch (RemoteException e) {
        }
        if (defaultSize.x > defaultSize.y) {
            mDefaultHeight = defaultSize.x;
            mDefaultWidth = defaultSize.y;
        } else {
            mDefaultHeight = defaultSize.y;
            mDefaultWidth = defaultSize.x;
        }
        mContext.getDisplay().getRealSize(currentSize);
        switch (((WindowManager) mContext.getSystemService("window")).getDefaultDisplay().getRotation()) {
            case 1:
            case 3:
                ratio = (currentSize.x * 100) / defaultSize.y;
                break;
            default:
                ratio = (currentSize.x * 100) / defaultSize.x;
                break;
        }
        return Integer.toString(ratio);
    }
}
