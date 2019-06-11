package com.evenwell.powersaving.g3.lpm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import com.evenwell.powersaving.g3.LpmDcUtils;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.provider.PowerSavingProvider;
import com.evenwell.powersaving.g3.utils.PSConst.DC.TIME.VALUE;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.FUNCTIONDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class LpmObserverUtils {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private static Context mContext;
    private static final BroadcastReceiver mHotspotStateReceiver = new C03801();
    private static ContentObserver mPSBTSettingObserver;
    private static ContentObserver mPSGPSSettingObserver;
    private static ContentObserver mPSMobileDataSettingObserver;
    private static ContentObserver mPSRestrictBDSettingObserver;
    private static ContentObserver mPSScreenTimeoutSettingObserver;
    private static ContentObserver mPSWiFiHotSpotStateObserver;
    private static ContentObserver mPSWiFiSettingObserver;

    /* renamed from: com.evenwell.powersaving.g3.lpm.LpmObserverUtils$1 */
    static class C03801 extends BroadcastReceiver {
        C03801() {
        }

        public void onReceive(Context mContext, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.i(LpmObserverUtils.TAG, "[LpmObserverUtils] mHotspotStateReceiver action: " + action);
                if (action.equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                    int state_from_intent = intent.getIntExtra("wifi_state", 0);
                    Log.i(LpmObserverUtils.TAG, "[LpmObserverUtils] hotspot state from intent: " + state_from_intent);
                    if (PowerSavingUtils.IsUseNewMethodToStoreSettings(mContext)) {
                        int mHotspot_state_in_db = -1;
                        String value = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.HOTSPOT_STATE);
                        if (value != null) {
                            mHotspot_state_in_db = Integer.parseInt(value);
                        }
                        Log.i(LpmObserverUtils.TAG, "[LpmObserverUtils] mHotspot_state_in_db: " + mHotspot_state_in_db);
                        if (state_from_intent == 13) {
                            PowerSavingUtils.setStringItemToDB(mContext, LPMDB.HOTSPOT_STATE, "1");
                            return;
                        } else if (state_from_intent == 11) {
                            PowerSavingUtils.setStringItemToDB(mContext, LPMDB.HOTSPOT_STATE, SYMBOLS.ZERO);
                            return;
                        } else {
                            return;
                        }
                    }
                    Log.i(LpmObserverUtils.TAG, "[LpmObserverUtils] mHotspot_state_in_db: " + Global.getInt(mContext.getContentResolver(), LPMDB.HOTSPOT_STATE, -1));
                    if (state_from_intent == 13) {
                        Global.putInt(mContext.getContentResolver(), LPMDB.HOTSPOT_STATE, 1);
                    } else if (state_from_intent == 11) {
                        Global.putInt(mContext.getContentResolver(), LPMDB.HOTSPOT_STATE, 0);
                    }
                }
            }
        }
    }

    public static class PSBTSettingObserver extends ContentObserver {
        public PSBTSettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (LpmObserverUtils.IsNeedSaveToBackUpProfile(LPMSPREF.BT)) {
                boolean mBT = LpmObserverUtils.GetBTEnableForSyncBackUpFile(LpmObserverUtils.mContext);
                LpmObserverUtils.UpdateValueToBackUpSharedPreferences(LpmObserverUtils.mContext, LPMSPREF.BT, LpmUtils.BooleanToString_NoKeep(mBT));
                Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mBT = " + mBT);
            }
        }
    }

    public static class PSGPSSettingObserver extends ContentObserver {
        public PSGPSSettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (LpmObserverUtils.IsNeedSaveToBackUpProfile(LPMSPREF.GPS)) {
                boolean mGps = LpmUtils.GetGPSEnable(LpmObserverUtils.mContext);
                LpmObserverUtils.UpdateValueToBackUpSharedPreferences(LpmObserverUtils.mContext, LPMSPREF.GPS, LpmUtils.BooleanToString_NoKeep(mGps));
                Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mGps = " + mGps);
            }
        }
    }

    public static class PSMobileDataSettingObserver extends ContentObserver {
        public PSMobileDataSettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (!LpmObserverUtils.IsNeedSaveToBackUpProfile(LPMSPREF.MOBILE_DATA)) {
                return;
            }
            if (LpmDcUtils.IsDCApply(LpmObserverUtils.mContext)) {
                Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mMobileData = dc apply so return mMobileData=" + LpmObserverUtils.GetMobileEnableForSyncBackUpFile(LpmObserverUtils.mContext));
                return;
            }
            boolean mMobileData = LpmObserverUtils.GetMobileEnableForSyncBackUpFile(LpmObserverUtils.mContext);
            LpmObserverUtils.UpdateValueToBackUpSharedPreferences(LpmObserverUtils.mContext, LPMSPREF.MOBILE_DATA, LpmUtils.BooleanToString_NoKeep(mMobileData));
            Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mMobileData = " + mMobileData);
        }
    }

    public static class PSRestrictBDSettingObserver extends ContentObserver {
        public PSRestrictBDSettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (LpmObserverUtils.IsNeedSaveToBackUpProfile(LPMSPREF.BACKGROUND_DATA)) {
                boolean mLPMBD = LpmUtils.GetBackgroundDataEnable(LpmObserverUtils.mContext);
                LpmObserverUtils.UpdateValueToBackUpSharedPreferences(LpmObserverUtils.mContext, LPMSPREF.BACKGROUND_DATA, LpmUtils.BooleanToString_NoKeep(mLPMBD));
                Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mLPMBD = " + mLPMBD);
            }
        }
    }

    public static class PSScreenTimeoutSettingObserver extends ContentObserver {
        public PSScreenTimeoutSettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (LpmObserverUtils.IsNeedSaveToBackUpProfile(LPMSPREF.SCREEN_TIMEOUT)) {
                String mScreenTimeout = LpmUtils.GetScreenTimeout(LpmObserverUtils.mContext);
                LpmObserverUtils.UpdateValueToBackUpSharedPreferences(LpmObserverUtils.mContext, LPMSPREF.SCREEN_TIMEOUT, mScreenTimeout);
                Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mScreenTimeout = " + mScreenTimeout);
            }
        }
    }

    public static class PSWiFiHotSpotStateObserver extends ContentObserver {
        public PSWiFiHotSpotStateObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            int mHotSpot_State = -1;
            if (PowerSavingUtils.IsUseNewMethodToStoreSettings(LpmObserverUtils.mContext)) {
                String value = PowerSavingUtils.getStringItemFromDB(LpmObserverUtils.mContext, LPMDB.HOTSPOT_STATE);
                if (value != null) {
                    mHotSpot_State = Integer.parseInt(value);
                }
            } else {
                mHotSpot_State = Global.getInt(LpmObserverUtils.mContext.getContentResolver(), LPMDB.HOTSPOT_STATE, -1);
            }
            Log.i(LpmObserverUtils.TAG, "[LpmObserverUtils] mHotSpot_State: " + mHotSpot_State);
            boolean mWifiHotSpot_State_boolean = false;
            if (mHotSpot_State == -1) {
                Log.e(LpmObserverUtils.TAG, "Error mHotSpot_State !!");
            } else if (mHotSpot_State == 0) {
                mWifiHotSpot_State_boolean = false;
            } else if (mHotSpot_State == 1) {
                mWifiHotSpot_State_boolean = true;
            }
            LpmObserverUtils.UpdateValueToBackUpSharedPreferences(LpmObserverUtils.mContext, LPMSPREF.WIFI_HOTSPOT, LpmUtils.BooleanToString_NoKeep(mWifiHotSpot_State_boolean));
            int wifiState = ((WifiManager) LpmObserverUtils.mContext.getSystemService(SSPARM.WIFI)).getWifiState();
            Log.i(LpmObserverUtils.TAG, "[LpmObserverUtils] wifiState:" + wifiState);
            if (!mWifiHotSpot_State_boolean) {
                return;
            }
            if (wifiState == 0 || wifiState != 1) {
                Global.putInt(LpmObserverUtils.mContext.getContentResolver(), "wifi_saved_state", 0);
            }
        }
    }

    public static class PSWiFiSettingObserver extends ContentObserver {
        public PSWiFiSettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
            if (!LpmObserverUtils.IsNeedSaveToBackUpProfile(LPMSPREF.WIFI)) {
                return;
            }
            if (LpmDcUtils.IsDCApply(LpmObserverUtils.mContext)) {
                Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mWifi = dc apply so return mWifi=" + LpmObserverUtils.GetWiFiEnableForSyncBackUpFile(LpmObserverUtils.mContext));
                return;
            }
            boolean mWifi = LpmObserverUtils.GetWiFiEnableForSyncBackUpFile(LpmObserverUtils.mContext);
            LpmObserverUtils.UpdateValueToBackUpSharedPreferences(LpmObserverUtils.mContext, LPMSPREF.WIFI, LpmUtils.BooleanToString_NoKeep(mWifi));
            Log.i(LpmObserverUtils.TAG, "LpmObserverUtils:mWifi = " + mWifi);
        }
    }

    public LpmObserverUtils(Context context) {
        mContext = context;
        mPSWiFiSettingObserver = new PSWiFiSettingObserver();
        mPSBTSettingObserver = new PSBTSettingObserver();
        mPSGPSSettingObserver = new PSGPSSettingObserver();
        mPSMobileDataSettingObserver = new PSMobileDataSettingObserver();
        mPSScreenTimeoutSettingObserver = new PSScreenTimeoutSettingObserver();
        mPSRestrictBDSettingObserver = new PSRestrictBDSettingObserver();
        mPSWiFiHotSpotStateObserver = new PSWiFiHotSpotStateObserver();
    }

    public static void SmartAmpSettingObserver(int mode) {
        if (IsNeedSaveToBackUpProfile(LPMSPREF.D3_SOUND)) {
            boolean mLPM3DSound = LpmUtils.Get3DSoundEnable(mContext);
            UpdateValueToBackUpSharedPreferences(mContext, LPMSPREF.D3_SOUND, LpmUtils.BooleanToString_NoKeep(mLPM3DSound));
            Log.i(TAG, "LpmObserverUtils:mLPM3DSound = " + mLPM3DSound);
        }
    }

    public static void RegisterContentObserver(Context mContext) {
        registerHotspotReceiver(mContext);
        mContext.getContentResolver().registerContentObserver(Global.getUriFor("wifi_on"), true, mPSWiFiSettingObserver);
        mContext.getContentResolver().registerContentObserver(Global.getUriFor("bluetooth_on"), true, mPSBTSettingObserver);
        mContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, mPSGPSSettingObserver);
        mContext.getContentResolver().registerContentObserver(Global.getUriFor("mobile_data"), true, mPSMobileDataSettingObserver);
        mContext.getContentResolver().registerContentObserver(System.getUriFor("screen_off_timeout"), true, mPSScreenTimeoutSettingObserver);
        if (PowerSavingUtils.IsUseNewMethodToStoreSettings(mContext)) {
            mContext.getContentResolver().registerContentObserver(PowerSavingProvider.getUriFor(FUNCTIONDB.ADD_BACKGROUND_DATA), true, mPSRestrictBDSettingObserver);
            mContext.getContentResolver().registerContentObserver(PowerSavingProvider.getUriFor(LPMDB.HOTSPOT_STATE), true, mPSWiFiHotSpotStateObserver);
            return;
        }
        mContext.getContentResolver().registerContentObserver(System.getUriFor(FUNCTIONDB.ADD_BACKGROUND_DATA), true, mPSRestrictBDSettingObserver);
        mContext.getContentResolver().registerContentObserver(Global.getUriFor(LPMDB.HOTSPOT_STATE), true, mPSWiFiHotSpotStateObserver);
    }

    public static void UnRegisterContentObserver(Context mContext) {
        unregisterHotspotReceiver(mContext);
        mContext.getContentResolver().unregisterContentObserver(mPSWiFiSettingObserver);
        mContext.getContentResolver().unregisterContentObserver(mPSBTSettingObserver);
        mContext.getContentResolver().unregisterContentObserver(mPSGPSSettingObserver);
        mContext.getContentResolver().unregisterContentObserver(mPSMobileDataSettingObserver);
        mContext.getContentResolver().unregisterContentObserver(mPSScreenTimeoutSettingObserver);
        mContext.getContentResolver().unregisterContentObserver(mPSRestrictBDSettingObserver);
        mContext.getContentResolver().unregisterContentObserver(mPSWiFiHotSpotStateObserver);
    }

    private static boolean IsNeedSaveToBackUpProfile(String Item) {
        Log.i(TAG, "LpmObserverUtils:IsNeedSaveToBackUpProfile LowPowerMode.mHasApplySettingThread=" + LowPowerMode.mHasApplySettingThread);
        if (!LowPowerMode.mHasApplySettingThread && LowPowerMode.mAlreadyApplied) {
            return true;
        }
        Log.i(TAG, "LpmObserverUtils:IsNeedSaveToBackUpProfile [Item= " + Item + "]  skip");
        return false;
    }

    public static boolean GetBTEnableForSyncBackUpFile(Context mContext) {
        return System.getInt(mContext.getContentResolver(), "bluetooth_on", 1) != 0;
    }

    public static boolean GetWiFiEnableForSyncBackUpFile(Context mContext) {
        return System.getInt(mContext.getContentResolver(), "wifi_on", 1) != 0;
    }

    public static boolean GetMobileEnableForSyncBackUpFile(Context mContext) {
        return PowerSavingUtils.GetMobileDataEnable(mContext);
    }

    public static void UpdateValueToBackUpSharedPreferences(Context mContext, String Item, String Value) {
        SharedPreferences prefStatus = mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_LPM_BACKUP_FILE, 0);
        Editor editor = prefStatus.edit();
        if (!prefStatus.getString(Item, SWITCHER.KEEP).equals(SWITCHER.KEEP)) {
            editor.putString(Item, Value);
            editor.commit();
        }
    }

    private static void registerHotspotReceiver(final Context mContext) {
        if (PowerSavingUtils.IsUseNewMethodToStoreSettings(mContext)) {
            PowerSavingUtils.setStringItemToDB(mContext, LPMDB.HOTSPOT_STATE, SYMBOLS.ZERO);
        } else {
            Global.putInt(mContext.getContentResolver(), LPMDB.HOTSPOT_STATE, 0);
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Log.i(LpmObserverUtils.TAG, "[LpmObserverUtils] registerHotspotReceiver");
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                mContext.registerReceiver(LpmObserverUtils.mHotspotStateReceiver, filter);
            }
        }, VALUE.SCREEN_OFF_WAIT_TIME);
    }

    private static void unregisterHotspotReceiver(Context mContext) {
        try {
            if (mHotspotStateReceiver != null) {
                mContext.unregisterReceiver(mHotspotStateReceiver);
            }
        } catch (Exception e) {
        }
    }
}
