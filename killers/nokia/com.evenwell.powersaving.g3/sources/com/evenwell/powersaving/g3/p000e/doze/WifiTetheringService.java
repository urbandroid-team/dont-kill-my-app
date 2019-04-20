package com.evenwell.powersaving.g3.p000e.doze;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import com.evenwell.powersaving.g3.p000e.doze.WifiTetherSoftApManager.WifiTetherSoftApCallback;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;
import com.evenwell.powersaving.g3.utils.WifiManagerUtils;

/* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTetheringService */
public class WifiTetheringService extends TetheringService {
    private static final String TAG = "WifiTetheringService";
    private final IBinder mBinder = new LocalBinder();
    private int mNumClients;
    private WifiManager mWifiManager;
    private WifiTetherSoftApManager mWifiTetherSoftApManager;

    /* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTetheringService$1 */
    class C03551 implements WifiTetherSoftApCallback {
        private int mSoftApState;

        C03551() {
        }

        public void onStateChanged(int state, int failureReason) {
            this.mSoftApState = state;
        }

        public void onNumClientsChanged(int numClients) {
            if (this.mSoftApState == 13) {
                WifiTetheringService.this.mNumClients = numClients;
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTetheringService$LocalBinder */
    private class LocalBinder extends TetheringBinder {
        private LocalBinder() {
        }

        public WifiTetheringService getService() {
            return WifiTetheringService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind()");
        return this.mBinder;
    }

    public void onCreate() {
        Log.i(TAG, "onCreate()");
        this.mWifiManager = (WifiManager) getSystemService(SSPARM.WIFI);
        if (!WifiManagerUtils.has_getWifiApNumClients_API()) {
            initWifiTetherSoftApManager();
            if (this.mWifiTetherSoftApManager != null) {
                this.mWifiTetherSoftApManager.registerSoftApCallback();
            }
        }
    }

    private void initWifiTetherSoftApManager() {
        this.mWifiTetherSoftApManager = new WifiTetherSoftApManager(this.mWifiManager, new C03551());
    }

    public void setTethering(boolean enable) {
        new WifiTethering(this).setTethering(enable);
    }

    public boolean isTetheringOn() {
        if (this.mWifiManager.getWifiApState() == 13 || this.mWifiManager.getWifiApState() == 12) {
            return true;
        }
        return false;
    }

    public int TetheringSize() {
        if (WifiManagerUtils.has_getWifiApNumClients_API()) {
            this.mNumClients = WifiManagerUtils.getWifiApNumClients(this);
            if (this.mNumClients == -1) {
                this.mNumClients = 1;
            }
        }
        return this.mNumClients;
    }

    public void onDestroy() {
        if (!WifiManagerUtils.has_getWifiApNumClients_API() && this.mWifiTetherSoftApManager != null) {
            this.mWifiTetherSoftApManager.unRegisterSoftApCallback();
        }
    }
}
