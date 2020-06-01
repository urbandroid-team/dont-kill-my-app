package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.util.Log;
import com.evenwell.powersaving.g3.p000e.doze.TetheringProxy;
import com.evenwell.powersaving.g3.p000e.doze.WifiTetheringService;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.WifiHotSpot */
public class WifiHotSpot extends Function {
    private TetheringProxy mWifiTetheringProxy;

    public WifiHotSpot(Context context) {
        super(context);
        this.mWifiTetheringProxy = new TetheringProxy(context, WifiTetheringService.class);
    }

    public boolean get() {
        return this.mWifiTetheringProxy.isTetheringOn();
    }

    public void set(boolean value) {
        this.mWifiTetheringProxy.setTethering(value);
    }

    public void release() {
        super.release();
        this.mWifiTetheringProxy.release();
    }

    public boolean forceIgnore() {
        if (super.forceIgnore()) {
            Log.i("Function", "do not change WifiHotSpot state.");
            return true;
        } else if (hasUserRestriction("no_config_tethering")) {
            Log.i("Function", "hasUserRestriction UserManager.DISALLOW_CONFIG_TETHERING, do not change WifiHotSpot state.");
            return true;
        } else {
            Log.i("Function", "Wifi Tethering = " + this.mWifiTetheringProxy.isTetheringOn() + ", TetheringSize() = " + this.mWifiTetheringProxy.TetheringSize());
            if (!this.mWifiTetheringProxy.isTetheringOn() || this.mWifiTetheringProxy.TetheringSize() <= 0) {
                return false;
            }
            Log.i("Function", "do not change WifiHotSpot state.");
            return true;
        }
    }

    public int getTetheringSize() {
        return this.mWifiTetheringProxy.TetheringSize();
    }
}
