package com.evenwell.powersaving.g3.ss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.os.Handler;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.PSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.SSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.iFunctionMode;
import com.fihtdc.backuptool.FileOperator;

public class SmartSwitch implements iFunctionMode {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private static Runnable countdownthread_hotspot = null;
    private static Runnable countdownthread_wifi = null;
    private static Handler handler = null;
    private static boolean isHotspotThreadRunning = false;
    private static boolean isWifiThreadRunning = false;
    private static long mCurrentTime = 0;
    private static long mStartTime = 0;
    private static int mTimeOut = 0;
    private Context mContext;
    private final BroadcastReceiver mSmartSwitchReceiver = new C04151();

    /* renamed from: com.evenwell.powersaving.g3.ss.SmartSwitch$1 */
    class C04151 extends BroadcastReceiver {
        C04151() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    Log.i(SmartSwitch.TAG, "Reveice NETWORK_STATE_CHANGED_ACTION");
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info.getDetailedState() == DetailedState.DISCONNECTED) {
                        Log.i(SmartSwitch.TAG, "wifi DISCONNECTED");
                        if (PowerSavingUtils.GetWiFiEnableByDB(context)) {
                            SmartSwitch.startWifiCountdownthread(context);
                        } else {
                            SmartSwitch.stopWifiCountdownthread();
                        }
                    } else if (info.getDetailedState() == DetailedState.CONNECTED) {
                        Log.i(SmartSwitch.TAG, "wifi CONNECTED");
                        SmartSwitch.stopWifiCountdownthread();
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    switch (intent.getIntExtra("wifi_state", 0)) {
                        case 1:
                            Log.i(SmartSwitch.TAG, "WIFI_STATE_DISABLED");
                            SmartSwitch.stopWifiCountdownthread();
                            return;
                        case 3:
                            Log.i(SmartSwitch.TAG, "WIFI_STATE_ENABLED");
                            SmartSwitch.startWifiCountdownthread(context);
                            return;
                        default:
                            return;
                    }
                } else if (action.equals("android.intent.action.SCREEN_ON")) {
                    Log.i(SmartSwitch.TAG, "[SmartSwitch] Receive ACTION_SCREEN_ON");
                    if (SsUtils.StringToBoolean(PowerSavingUtils.getStringItemFromDB(context, PSDB.DATA_CONNECTION))) {
                        SsUtils.checkWififirst(context);
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    Log.i(SmartSwitch.TAG, "[SmartSwitch] Receive ACTION_SCREEN_OFF");
                    if (SsUtils.StringToBoolean(PowerSavingUtils.getStringItemFromDB(context, PSDB.DATA_CONNECTION))) {
                        SmartSwitch.stopWifiCountdownthread();
                    }
                } else if (action.equals(ACTION.ACTION_CHECK_WIFI)) {
                    SsUtils.checkWififirst(context);
                } else if (action.equals(ACTION.ACTION_CHECK_HOTSPOT)) {
                    SsUtils.checkHotspotfirst(context);
                } else if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    if (!PowerSavingUtils.getHotspotstate(context)) {
                        int intextra = intent.getIntExtra("wifi_state", 14);
                        Log.i(SmartSwitch.TAG, "AP_state_change--intextra: " + intextra);
                        if (intextra == 13) {
                            SmartSwitch.startHotspotthread(context);
                        } else if (intextra == 11) {
                            SmartSwitch.stopHotspotthread();
                        }
                    }
                } else if (action.equals(ACTION.ACTION_HOTSPOT_STATUS)) {
                    int mHotspotStatus = intent.getIntExtra("counter", -1);
                    Log.i(SmartSwitch.TAG, "Hotspot--mHotspotStatus: " + mHotspotStatus);
                    if (mHotspotStatus == 0) {
                        SmartSwitch.startHotspotthread(context);
                    } else if (mHotspotStatus >= 1) {
                        SmartSwitch.stopHotspotthread();
                    } else {
                        Log.i(SmartSwitch.TAG, "[SmartSwitch] ACTION_HOTSPOT_STATUS get error !!");
                    }
                }
            }
        }
    }

    public SmartSwitch(Context context, int reason) {
        Log.i(TAG, "[SmartSwitch] init");
        this.mContext = context;
        if (handler == null) {
            handler = new Handler();
        }
    }

    public void registerReceiver(final Context ctx) {
        Log.i(TAG, "[SmartSwitch] registerReceiver()");
        if (countdownthread_wifi == null) {
            countdownthread_wifi = new Runnable() {
                public void run() {
                    Log.i(SmartSwitch.TAG, "countdownthread_wifi");
                    long mCurrentTime = System.currentTimeMillis();
                    if (mCurrentTime - SmartSwitch.mStartTime >= ((long) SmartSwitch.mTimeOut)) {
                        Log.i(SmartSwitch.TAG, "Turn off wifi>>Time(ms): " + (mCurrentTime - SmartSwitch.mStartTime));
                        SsUtils.SetWifiEnable(ctx, SWITCHER.OFF);
                        return;
                    }
                    SmartSwitch.handler.postDelayed(SmartSwitch.countdownthread_wifi, 5000);
                }
            };
        }
        if (countdownthread_hotspot == null) {
            countdownthread_hotspot = new Runnable() {
                public void run() {
                    Log.i(SmartSwitch.TAG, "countdownthread_hotspot");
                    long mCurrentTime = System.currentTimeMillis();
                    if (mCurrentTime - SmartSwitch.mStartTime >= ((long) SmartSwitch.mTimeOut)) {
                        Log.i(SmartSwitch.TAG, "Turn off wifi hotspot >>Time(ms): " + (mCurrentTime - SmartSwitch.mStartTime));
                        SsUtils.setSoftapEnabled(ctx, false);
                        return;
                    }
                    SmartSwitch.handler.postDelayed(SmartSwitch.countdownthread_hotspot, 5000);
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION.ACTION_CHECK_WIFI);
        filter.addAction(ACTION.ACTION_CHECK_HOTSPOT);
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        filter.addAction(ACTION.ACTION_HOTSPOT_STATUS);
        ctx.registerReceiver(this.mSmartSwitchReceiver, filter);
        if (!PowerSavingUtils.isShowSSHotSpot(ctx)) {
            PowerSavingUtils.setStringItemToDB(ctx, SSDB.HOTSPOT, SWITCHER.KEEP);
        }
        SsUtils.checkWififirst(ctx);
        SsUtils.checkHotspotfirst(ctx);
    }

    public void unregisterReceiver(Context ctx) {
        Log.i(TAG, "[SmartSwitch] unregisterReceiver");
        try {
            if (this.mSmartSwitchReceiver != null) {
                ctx.unregisterReceiver(this.mSmartSwitchReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopWifiCountdownthread();
        stopHotspotthread();
    }

    private static int getTimeOutValue(Context ctx, String key) {
        int timeout = (Integer.parseInt(PowerSavingUtils.getStringItemFromDB(ctx, key)) * 60) * FileOperator.MAX_DIR_LENGTH;
        Log.i(TAG, "timeout: " + timeout);
        return timeout;
    }

    public static void startWifiCountdownthread(Context ctx) {
        Log.i(TAG, "[SmartSwitch] startWifiCountdownthread");
        if (!isWifiThreadRunning && SsUtils.StringToBoolean(PowerSavingUtils.getStringItemFromDB(ctx, SSDB.WIFI))) {
            mStartTime = System.currentTimeMillis();
            mTimeOut = getTimeOutValue(ctx, SSDB.WIFI_TIMEOUT);
            handler.postDelayed(countdownthread_wifi, 5000);
            isWifiThreadRunning = true;
        }
    }

    public static void stopWifiCountdownthread() {
        if (isWifiThreadRunning) {
            Log.i(TAG, "[SmartSwitch] stopWifiCountdownthread");
            isWifiThreadRunning = false;
            handler.removeCallbacks(countdownthread_wifi);
        }
    }

    public static void startHotspotthread(Context ctx) {
        Log.i(TAG, "[SmartSwitch] startHotspotthread");
        if (!isHotspotThreadRunning && SsUtils.StringToBoolean(PowerSavingUtils.getStringItemFromDB(ctx, SSDB.HOTSPOT))) {
            mStartTime = System.currentTimeMillis();
            mTimeOut = getTimeOutValue(ctx, SSDB.HOTSPOT_TIMEOUT);
            handler.postDelayed(countdownthread_hotspot, 5000);
            isHotspotThreadRunning = true;
        }
    }

    public static void stopHotspotthread() {
        if (isHotspotThreadRunning) {
            Log.i(TAG, "[SmartSwitch] stopHotspotthread");
            isHotspotThreadRunning = false;
            handler.removeCallbacks(countdownthread_hotspot);
        }
    }
}
