package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.provider.Settings.Secure;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.Glance */
public class Glance extends Function {
    private static final String DOZE_ENABLED = "doze_enabled";

    public Glance(Context context) {
        super(context);
    }

    public boolean get() {
        return Secure.getInt(this.mContext.getContentResolver(), "doze_enabled", 0) == 1;
    }

    public void set(boolean value) {
        Secure.putInt(this.mContext.getContentResolver(), "doze_enabled", value ? 1 : 0);
    }
}
