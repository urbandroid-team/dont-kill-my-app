package com.evenwell.powersaving.g3.p000e.doze;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/* renamed from: com.evenwell.powersaving.g3.e.doze.BluetoothTetheringService */
public class BluetoothTetheringService extends TetheringService {
    private static final String TAG = "BluetoothTetheringService";
    private final IBinder mBinder = new LocalBinder();
    private AtomicReference<BluetoothPan> mBluetoothPan = new AtomicReference();
    private ConnectivityManager mCm;
    private Handler mHandler = new Handler();
    private ServiceListener mProfileServiceListener = new C03451();
    private OnStartTetheringCallback mStartTetheringCallback;

    /* renamed from: com.evenwell.powersaving.g3.e.doze.BluetoothTetheringService$1 */
    class C03451 implements ServiceListener {
        C03451() {
        }

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            BluetoothTetheringService.this.mBluetoothPan.set((BluetoothPan) proxy);
        }

        public void onServiceDisconnected(int profile) {
            BluetoothTetheringService.this.mBluetoothPan.set(null);
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.BluetoothTetheringService$LocalBinder */
    private class LocalBinder extends TetheringBinder {
        private LocalBinder() {
        }

        public BluetoothTetheringService getService() {
            return BluetoothTetheringService.this;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.BluetoothTetheringService$OnStartTetheringCallback */
    private static final class OnStartTetheringCallback extends android.net.ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<BluetoothTetheringService> mTetherSettings;

        OnStartTetheringCallback(BluetoothTetheringService settings) {
            this.mTetherSettings = new WeakReference(settings);
        }

        public void onTetheringStarted() {
            updateState();
        }

        public void onTetheringFailed() {
            updateState();
        }

        private void updateState() {
            BluetoothTetheringService settings = (BluetoothTetheringService) this.mTetherSettings.get();
            if (settings != null) {
                settings.TetheringSize();
                settings.isTetheringOn();
            }
        }
    }

    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return this.mBinder;
    }

    public void onCreate() {
        Log.i(TAG, "onCreate()");
        this.mCm = (ConnectivityManager) getSystemService("connectivity");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            adapter.getProfileProxy(getApplicationContext(), this.mProfileServiceListener, 5);
        }
        this.mStartTetheringCallback = new OnStartTetheringCallback(this);
    }

    public void setTethering(boolean enable) {
        if (enable) {
            this.mCm.startTethering(2, true, this.mStartTetheringCallback, this.mHandler);
        } else {
            this.mCm.stopTethering(2);
        }
    }

    public boolean isTetheringOn() {
        return isTetheringOn(this.mCm.getTetherableIfaces(), this.mCm.getTetheredIfaces(), this.mCm.getTetheringErroredIfaces());
    }

    public int TetheringSize() {
        return TetheringSize(this.mCm.getTetherableIfaces(), this.mCm.getTetheredIfaces(), this.mCm.getTetheringErroredIfaces());
    }

    private boolean isTetheringOn(String[] available, String[] tethered, String[] errored) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return false;
        }
        int btState = adapter.getState();
        if (btState == 13) {
            Log.i(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
            return false;
        } else if (btState == 11) {
            Log.i(TAG, "BluetoothAdapter.STATE_TURNING_ON");
            return true;
        } else {
            BluetoothPan bluetoothPan = (BluetoothPan) this.mBluetoothPan.get();
            if (btState == 12 && bluetoothPan != null && bluetoothPan.isTetheringOn()) {
                Log.i(TAG, "bluetoothPan.isTetheringOn() = " + bluetoothPan.isTetheringOn());
                return true;
            }
            Log.i(TAG, "bluetoothTethered null");
            return false;
        }
    }

    private int TetheringSize(String[] available, String[] tethered, String[] errored) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return 0;
        }
        int btState = adapter.getState();
        if (btState == 13) {
            Log.i(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
            return 0;
        } else if (btState == 11) {
            Log.i(TAG, "BluetoothAdapter.STATE_TURNING_ON");
            return 0;
        } else {
            BluetoothPan bluetoothPan = (BluetoothPan) this.mBluetoothPan.get();
            if (btState == 12 && bluetoothPan != null && bluetoothPan.isTetheringOn()) {
                int bluetoothTethered = bluetoothPan.getConnectedDevices().size();
                Log.i(TAG, "bluetoothTethered size = " + bluetoothTethered);
                return bluetoothTethered;
            }
            Log.i(TAG, "bluetoothTethered null");
            return 0;
        }
    }
}
