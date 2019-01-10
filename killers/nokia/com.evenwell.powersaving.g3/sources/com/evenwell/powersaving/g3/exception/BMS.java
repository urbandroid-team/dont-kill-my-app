package com.evenwell.powersaving.g3.exception;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;

public class BMS {
    public static String FALSE = "false";
    public static String KEY_ORIGINAL_BMS_SETTINGS = "original_bms_settings";
    private static final String TAG = "BMS";
    public static String TRUE = "true";
    private static BMS mBMS;
    private Context mContext;
    private boolean mDefaultValue;

    private BMS(Context context) {
        this.mContext = context;
        init();
    }

    public static BMS getInstance(Context context) {
        if (mBMS == null) {
            mBMS = new BMS(context);
        }
        return mBMS;
    }

    public void init() {
        this.mDefaultValue = this.mContext.getResources().getBoolean(C0321R.bool.powersaving_restore_default_value);
        Log.i(TAG, "mDefaultValue = " + this.mDefaultValue);
        if (TextUtils.isEmpty(PowerSavingUtils.getSettingsProvider(this.mContext, KEY_ORIGINAL_BMS_SETTINGS))) {
            setBMSValue(this.mDefaultValue);
        }
    }

    public void setBMSValue(boolean bmsValue) {
        PowerSavingUtils.setSettingsProvider(this.mContext, KEY_ORIGINAL_BMS_SETTINGS, bmsValue ? TRUE : FALSE);
        Log.i(TAG, "bmsValue = " + bmsValue);
    }

    public boolean getBMSValue() {
        String value = PowerSavingUtils.getSettingsProvider(this.mContext, KEY_ORIGINAL_BMS_SETTINGS);
        if (!TextUtils.isEmpty(value)) {
            return value.equals(TRUE);
        } else {
            init();
            return this.mDefaultValue;
        }
    }
}
