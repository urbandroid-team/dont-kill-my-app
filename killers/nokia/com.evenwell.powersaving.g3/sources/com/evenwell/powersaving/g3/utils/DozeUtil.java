package com.evenwell.powersaving.g3.utils;

import android.content.Context;
import android.provider.Settings.Global;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class DozeUtil {
    private static final boolean DBG = true;
    public static final String DEVICE_IDLE_CONSTANTS = "device_idle_constants";
    private static String TAG = TAG.PSLOG;

    public static boolean setDozeStateTimeout(Context context) {
        boolean bIsSuccess = false;
        String dozeTimeoutConfig = context.getString(C0321R.string.dozeTimeout);
        try {
            Log.i(TAG, "setDozeStateTimeout = " + dozeTimeoutConfig);
            bIsSuccess = Global.putString(context.getContentResolver(), DEVICE_IDLE_CONSTANTS, dozeTimeoutConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bIsSuccess;
    }
}
