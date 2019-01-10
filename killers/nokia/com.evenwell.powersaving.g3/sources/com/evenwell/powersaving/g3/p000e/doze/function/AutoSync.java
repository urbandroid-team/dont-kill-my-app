package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.ContentResolver;
import android.content.Context;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.AutoSync */
public class AutoSync extends Function {
    public AutoSync(Context context) {
        super(context);
    }

    public boolean get() {
        this.mContext.getContentResolver();
        return ContentResolver.getMasterSyncAutomatically();
    }

    public void set(boolean value) {
        this.mContext.getContentResolver();
        ContentResolver.setMasterSyncAutomatically(value);
    }
}
