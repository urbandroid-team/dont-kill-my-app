package com.evenwell.powersaving.g3.p000e.doze.function;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import java.util.Set;

/* renamed from: com.evenwell.powersaving.g3.e.doze.function.Bluetooth */
public class Bluetooth extends Function {
    public Bluetooth(Context context) {
        super(context, new BluetoothHotSpot(context));
    }

    public boolean get() {
        return LpmUtils.GetBTEnable(this.mContext);
    }

    public void set(boolean value) {
        LpmUtils.SetBTEnable(this.mContext, LpmUtils.BooleanToString_NoKeep(value));
    }

    public boolean forceIgnore() {
        if (super.forceIgnore()) {
            Log.i("Function", "do not change bluetooth state");
            return true;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices == null || pairedDevices.size() <= 0) {
                if (pairedDevices == null) {
                    Log.i("Function", "bluetooth paired device = null");
                } else {
                    Log.i("Function", "bluetooth paired device = " + pairedDevices.size());
                }
                return false;
            }
            Log.i("Function", "bluetooth paired device = " + pairedDevices.size());
            Log.i("Function", "do not change bluetooth state");
            return true;
        }
        Log.i("Function", "Bluetooth is not supported on this hardware platform");
        return false;
    }
}
