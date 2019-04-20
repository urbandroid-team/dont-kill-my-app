package com.evenwell.powersaving.g3.ss;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.p000e.doze.WifiTethering;
import com.evenwell.powersaving.g3.utils.PSConst.Function;
import com.evenwell.powersaving.g3.utils.PSConst.PERMISSION.TYPE;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.SSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class SsUtils {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING,
        WIFI_AP_STATE_DISABLED,
        WIFI_AP_STATE_ENABLING,
        WIFI_AP_STATE_ENABLED,
        WIFI_AP_STATE_FAILED
    }

    public static String BooleanToString(boolean value) {
        if (value) {
            return SWITCHER.OFF;
        }
        return SWITCHER.KEEP;
    }

    public static boolean StringToBoolean(String value) {
        if (value != null && value.equals(SWITCHER.OFF)) {
            return true;
        }
        return false;
    }

    public static void SetWifiEnable(Context mContext, String value) {
        boolean mWifi = false;
        if (value != null && !value.equals(SWITCHER.KEEP)) {
            if (value.equals(SWITCHER.ON)) {
                mWifi = true;
            }
            if (value.equals(SWITCHER.OFF)) {
                mWifi = false;
            }
            WifiManager mWifiManager = (WifiManager) mContext.getSystemService(SSPARM.WIFI);
            boolean desiredState = mWifi;
            int wifiApState;
            if (desiredState && isRadioAllowed(mContext, SSPARM.WIFI)) {
                if (mWifiManager != null) {
                    wifiApState = mWifiManager.getWifiApState();
                    new WifiTethering(mContext).setTethering(false);
                    mWifiManager.setWifiEnabled(desiredState);
                }
            } else if (mWifiManager != null) {
                wifiApState = mWifiManager.getWifiApState();
                if (desiredState && (wifiApState == 12 || wifiApState == 13)) {
                    try {
                        new WifiTethering(mContext).setTethering(false);
                    } catch (Exception e) {
                        Log.i(TAG, "[SsUtils] setWifiApEnabled() failed.");
                        e.printStackTrace();
                    }
                }
                mWifiManager.setWifiEnabled(desiredState);
            }
        }
    }

    public static boolean isRadioAllowed(Context mContext, String type) {
        if (!isAirplaneModeOn(mContext)) {
            return true;
        }
        String toggleable = System.getString(mContext.getContentResolver(), "airplane_mode_toggleable_radios");
        if (toggleable == null || !toggleable.contains(type)) {
            return false;
        }
        return true;
    }

    public static boolean isAirplaneModeOn(Context mContext) {
        return System.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public static boolean haveInternet(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService("connectivity");
        if (cm != null && cm.getNetworkInfo(1) != null) {
            return cm.getNetworkInfo(1).isConnectedOrConnecting();
        }
        Log.i(TAG, "haveInternet: getNetWorkInfo is null");
        return false;
    }

    public static void setSoftapEnabled(Context mContext, boolean enable) {
        Log.i(TAG, "[SsUtils] setSoftapEnabled() enabled:" + enable);
        if (VERSION.SDK_INT <= 22) {
            setWifiApEnabled(mContext, enable);
        } else if (PowerSavingUtils.checkPermission(mContext, TYPE.WRITE_SETTINGS)) {
            setWifiApEnabled(mContext, enable);
        } else {
            PowerSavingUtils.ShowPermissionNotification(mContext, TYPE.WRITE_SETTINGS, Function.SS, 11);
        }
    }

    public static void setWifiApEnabled(Context mContext, boolean enable) {
        Log.i(TAG, "[SsUtils] setWifiApEnabled() enabled:" + enable);
        ContentResolver cr = mContext.getContentResolver();
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(SSPARM.WIFI);
        int wifiState = mWifiManager.getWifiState();
        Log.i(TAG, "wifiState:" + wifiState);
        if (enable && (wifiState == 2 || wifiState == 3)) {
            Log.i(TAG, "wifiState:" + wifiState);
            mWifiManager.setWifiEnabled(false);
            Global.putInt(cr, "wifi_saved_state", 1);
        }
        try {
            new WifiTethering(mContext).setTethering(enable);
        } catch (Exception e) {
            Log.i(TAG, "[SsUtils] setWifiApEnabled() failed.");
            e.printStackTrace();
        }
        Log.i(TAG, "setWifiApEnabled retVal:" + false);
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Global.getInt(cr, "wifi_saved_state");
            } catch (SettingNotFoundException e2) {
            }
            if (wifiSavedState == 1) {
                mWifiManager.setWifiEnabled(true);
                Global.putInt(cr, "wifi_saved_state", 0);
            }
        }
    }

    public static WIFI_AP_STATE getWifiAPState(Context mContext) {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(SSPARM.WIFI);
        try {
            int tmp = ((Integer) mWifiManager.getClass().getMethod("getWifiApState", new Class[0]).invoke(mWifiManager, new Object[0])).intValue();
            if (tmp > 10) {
                tmp -= 10;
            }
            return ((WIFI_AP_STATE[]) WIFI_AP_STATE.class.getEnumConstants())[tmp];
        } catch (Exception e) {
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    public static boolean isWifiAPEnabled(Context mContext) {
        return getWifiAPState(mContext) == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    public static void checkWififirst(Context ctx) {
        SmartSwitch.stopWifiCountdownthread();
        boolean mWifi_option = StringToBoolean(PowerSavingUtils.getStringItemFromDB(ctx, SSDB.WIFI));
        Log.i(TAG, "mWifi_option: " + mWifi_option);
        if (mWifi_option) {
            boolean mWifi = PowerSavingUtils.GetWiFiEnableByDB(ctx);
            Log.i(TAG, "mWifi: " + mWifi);
            if (mWifi) {
                boolean mConnected = haveInternet(ctx);
                Log.i(TAG, "mConnected: " + mConnected);
                if (!mConnected) {
                    SmartSwitch.startWifiCountdownthread(ctx);
                }
            }
        }
    }

    public static void checkHotspotfirst(Context ctx) {
        SmartSwitch.stopHotspotthread();
        boolean mhotspot_option = StringToBoolean(PowerSavingUtils.getStringItemFromDB(ctx, SSDB.HOTSPOT));
        Log.i(TAG, "mhotspot_option: " + mhotspot_option);
        if (mhotspot_option && isWifiAPEnabled(ctx) && !PowerSavingUtils.getHotspotstate(ctx)) {
            SmartSwitch.startHotspotthread(ctx);
        }
    }
}
