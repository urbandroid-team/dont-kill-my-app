package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import com.evenwell.powersaving.g3.PowerSavingUtils;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.Data */
public class Data extends Function {
    public Data(Context context) {
        super(context);
    }

    public boolean get() {
        return PowerSavingUtils.GetMobileDataEnable(this.mContext);
    }

    public void set(boolean value) {
        PowerSavingUtils.SetMobileDataEnable(this.mContext, value);
    }
}
