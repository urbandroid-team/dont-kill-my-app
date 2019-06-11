package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.provider.Settings.Global;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.WifiScan */
public class WifiScan extends Function {
    public WifiScan(Context context) {
        super(context);
    }

    public boolean get() {
        return Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", 0) == 1;
    }

    public void set(boolean value) {
        Global.putInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", value ? 1 : 0);
    }
}
