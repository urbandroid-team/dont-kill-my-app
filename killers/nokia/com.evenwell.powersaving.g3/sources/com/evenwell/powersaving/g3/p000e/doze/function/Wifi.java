package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.util.Log;
import com.evenwell.powersaving.g3.lpm.LpmUtils;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.Wifi */
public class Wifi extends Function {
    public Wifi(Context context) {
        super(context, new WifiHotSpot(context));
    }

    public boolean get() {
        return LpmUtils.GetWifiEnable(this.mContext);
    }

    public void set(boolean value) {
        LpmUtils.SetWifiEnable(this.mContext, LpmUtils.BooleanToString_NoKeep(value));
    }

    public boolean forceIgnore() {
        if (!super.forceIgnore()) {
            return false;
        }
        Log.i("Function", "do not change wifi state.");
        return true;
    }
}
