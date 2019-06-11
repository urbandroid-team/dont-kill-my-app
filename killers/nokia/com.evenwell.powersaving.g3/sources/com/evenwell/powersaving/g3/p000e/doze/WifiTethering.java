package com.evenwell.powersaving.g3.p000e.doze;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Log;
import java.lang.ref.WeakReference;

/* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTethering */
public class WifiTethering {
    private static final String TAG = "WifiTethering";
    private ConnectivityManager mConnectivityManager;
    private Handler mHandler = new Handler();
    private OnStartTetheringCallback mStartTetheringCallback;

    /* renamed from: com.evenwell.powersaving.g3.e.doze.WifiTethering$OnStartTetheringCallback */
    private static final class OnStartTetheringCallback extends android.net.ConnectivityManager.OnStartTetheringCallback {
        final WeakReference<WifiTethering> mTetherSettings;

        OnStartTetheringCallback(WifiTethering settings) {
            this.mTetherSettings = new WeakReference(settings);
        }

        public void onTetheringStarted() {
            Log.i(WifiTethering.TAG, "onTetheringStarted");
        }

        public void onTetheringFailed() {
            Log.i(WifiTethering.TAG, "onTetheringFailed");
        }
    }

    public WifiTethering(Context context) {
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mStartTetheringCallback = new OnStartTetheringCallback(this);
    }

    public void setTethering(boolean enable) {
        if (enable) {
            this.mConnectivityManager.startTethering(0, true, this.mStartTetheringCallback, this.mHandler);
        } else {
            this.mConnectivityManager.stopTethering(0);
        }
    }
}
