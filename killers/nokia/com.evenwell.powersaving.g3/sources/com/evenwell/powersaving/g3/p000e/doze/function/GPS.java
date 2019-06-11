package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import com.evenwell.powersaving.g3.lpm.LpmUtils;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.GPS */
public class GPS extends Function {
    public GPS(Context context) {
        super(context);
    }

    public boolean get() {
        return LpmUtils.GetGPSEnable(this.mContext);
    }

    public void set(boolean value) {
        LpmUtils.SetGpsEnable(this.mContext, LpmUtils.BooleanToString_NoKeep(value));
    }
}
