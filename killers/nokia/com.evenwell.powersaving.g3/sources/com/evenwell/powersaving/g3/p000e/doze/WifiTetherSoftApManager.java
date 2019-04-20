package com.evenwell.powersaving.g3.p000e.doze;

import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.SoftApCallback;
import android.os.Handler;

/* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTetherSoftApManager */
public class WifiTetherSoftApManager {
    private Handler mHandler;
    private SoftApCallback mSoftApCallback = new C03541();
    private WifiManager mWifiManager;
    private WifiTetherSoftApCallback mWifiTetherSoftApCallback;

    /* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTetherSoftApManager$1 */
    class C03541 implements SoftApCallback {
        C03541() {
        }

        public void onStateChanged(int state, int failureReason) {
            WifiTetherSoftApManager.this.mWifiTetherSoftApCallback.onStateChanged(state, failureReason);
        }

        public void onNumClientsChanged(int numClients) {
            WifiTetherSoftApManager.this.mWifiTetherSoftApCallback.onNumClientsChanged(numClients);
        }

        public void onStaConnected(String Macaddr, int numClients) {
        }

        public void onStaDisconnected(String Macaddr, int numClients) {
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTetherSoftApManager$WifiTetherSoftApCallback */
    public interface WifiTetherSoftApCallback {
        void onNumClientsChanged(int i);

        void onStateChanged(int i, int i2);
    }

    WifiTetherSoftApManager(WifiManager wifiManager, WifiTetherSoftApCallback wifiTetherSoftApCallback) {
        this.mWifiManager = wifiManager;
        this.mWifiTetherSoftApCallback = wifiTetherSoftApCallback;
        this.mHandler = new Handler();
    }

    public void registerSoftApCallback() {
        this.mWifiManager.registerSoftApCallback(this.mSoftApCallback, this.mHandler);
    }

    public void unRegisterSoftApCallback() {
        this.mWifiManager.unregisterSoftApCallback(this.mSoftApCallback);
    }
}
