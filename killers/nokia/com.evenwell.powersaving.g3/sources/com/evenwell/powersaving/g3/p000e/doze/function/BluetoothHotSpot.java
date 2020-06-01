package com.evenwell.powersaving.g3.p000e.doze.function;

import android.content.Context;
import android.util.Log;
import com.evenwell.powersaving.g3.p000e.doze.BluetoothTetheringService;
import com.evenwell.powersaving.g3.p000e.doze.TetheringProxy;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.BluetoothHotSpot */
public class BluetoothHotSpot extends Function {
    private TetheringProxy mBluetoothTetheringProxy;

    public BluetoothHotSpot(Context context, Function function) {
        super(context, function);
        this.mBluetoothTetheringProxy = new TetheringProxy(context, BluetoothTetheringService.class);
    }

    public BluetoothHotSpot(Context context) {
        this(context, null);
    }

    public boolean get() {
        return this.mBluetoothTetheringProxy.isTetheringOn();
    }

    public void set(boolean value) {
        this.mBluetoothTetheringProxy.setTethering(value);
    }

    public void release() {
        super.release();
        this.mBluetoothTetheringProxy.release();
    }

    public boolean forceIgnore() {
        if (super.forceIgnore()) {
            Log.i("Function", "do not change BluetoothHotSpot state.");
            return true;
        } else if (hasUserRestriction("no_config_tethering")) {
            Log.i("Function", "hasUserRestriction UserManager.DISALLOW_CONFIG_TETHERING, do not change BluetoothHotSpot state.");
            return true;
        } else {
            Log.i("Function", "Bluetooth Tethering = " + this.mBluetoothTetheringProxy.isTetheringOn() + ", TetheringSize() = " + this.mBluetoothTetheringProxy.TetheringSize());
            if (!this.mBluetoothTetheringProxy.isTetheringOn() || this.mBluetoothTetheringProxy.TetheringSize() <= 0) {
                return false;
            }
            Log.i("Function", "do not change BluetoothHotSpot state.");
            return true;
        }
    }
}
