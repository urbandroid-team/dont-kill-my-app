package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.provider.Settings.Secure;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.DoubleTap */
public class DoubleTap extends Function {
    public DoubleTap(Context context) {
        super(context);
    }

    public boolean get() {
        return Secure.getInt(this.mContext.getContentResolver(), "double_tap_to_wake", 0) == 1;
    }

    public void set(boolean value) {
        Secure.putInt(this.mContext.getContentResolver(), "double_tap_to_wake", value ? 1 : 0);
    }
}
