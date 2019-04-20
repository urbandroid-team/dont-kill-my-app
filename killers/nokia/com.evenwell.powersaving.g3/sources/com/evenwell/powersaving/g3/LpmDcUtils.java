package com.evenwell.powersaving.g3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.util.Log;
import com.evenwell.powersaving.g3.dataconnection.DataConnectionUtils;
import com.evenwell.powersaving.g3.lpm.LowPowerMode;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.p000e.doze.WifiTethering;
import com.evenwell.powersaving.g3.utils.PSConst.DC.DCPREF;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.LPM_AND_DC_APPLY.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.LPM_AND_DC_APPLY.PREF;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class LpmDcUtils {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;

    public static boolean SetLpmOrDcApplyStatus(Context context, int Item, boolean value) {
        Editor editor = context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_LPM_AND_DC_APPLY_FILE, 0).edit();
        if (Item == 1) {
            if (!IsDCApply(context) && value) {
                editor.putString(PREF.WHO_FRIST, Integer.toString(1));
                Log.i(TAG, "[LpmDcUtils] SetLpmOrDcApplyStatus: LPM apply first");
            }
            Log.i(TAG, "[LpmDcUtils] SetLpmOrDcApplyStatus: LPM apply =" + value);
            editor.putBoolean(PREF.IS_LPM_APPLY, value);
        } else {
            if (!IsLPMApply(context) && value) {
                editor.putString(PREF.WHO_FRIST, Integer.toString(2));
                Log.i(TAG, "[LpmDcUtils] SetLpmOrDcApplyStatus: DC apply first");
            }
            Log.i(TAG, "[LpmDcUtils] SetLpmOrDcApplyStatus: DC apply =" + value);
            editor.putBoolean(PREF.IS_DC_APPLY, value);
        }
        editor.commit();
        return value;
    }

    public static boolean IsLPMApply(Context mContext) {
        return mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_LPM_AND_DC_APPLY_FILE, 0).getBoolean(PREF.IS_LPM_APPLY, false);
    }

    public static boolean IsDCApply(Context mContext) {
        return mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_LPM_AND_DC_APPLY_FILE, 0).getBoolean(PREF.IS_DC_APPLY, false);
    }

    public static int GetWhoApplyFirst(Context mContext) {
        boolean mIsLPMApply = IsLPMApply(mContext);
        boolean mIsDCApply = IsDCApply(mContext);
        int WhoFirst = 0;
        if (mIsLPMApply && mIsDCApply) {
            String Value = mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_LPM_AND_DC_APPLY_FILE, 0).getString(PREF.WHO_FRIST, null);
            WhoFirst = Value != null ? Integer.parseInt(Value) : 0;
        }
        Log.i(TAG, "[LpmDcUtils] GetWhoApplyFirst: WhoFirst = " + WhoFirst);
        return WhoFirst;
    }

    public static void NotifyLpmOrDc(Context mContext, int NotifyWho) {
        Intent NoticeIntent;
        if (NotifyWho == 1) {
            Log.i(TAG, "[LpmDcUtils] NotifyLpmOrDc: notify LPM");
            NoticeIntent = new Intent(ACTION.ACTION_DC_APPLY_END_BUT_STILL_HAS_LPM);
        } else {
            Log.i(TAG, "[LpmDcUtils] NotifyLpmOrDc: notify DC");
            NoticeIntent = new Intent(ACTION.ACTION_LPM_APPLY_END_BUT_STILL_HAS_DC);
        }
        mContext.sendBroadcast(NoticeIntent);
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
            if (desiredState && LpmUtils.isRadioAllowed(mContext, SSPARM.WIFI)) {
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
                        Log.i(TAG, "[LpmDcUtils] setWifiApEnabled() failed.");
                        e.printStackTrace();
                    }
                }
                try {
                    mWifiManager.setWifiEnabled(desiredState);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    Log.i(TAG, "[LpmDcUtils] SetWifiEnable() - xxCN PermissionControl lead exception when wifi dialog choose off or timeout.");
                }
            }
        }
    }

    public static void SetMobileDataEnable(Context mContext, String value) {
        boolean mMobileData = false;
        if (value != null && !value.equals(SWITCHER.KEEP)) {
            if (value.equals(SWITCHER.ON)) {
                mMobileData = true;
            }
            if (value.equals(SWITCHER.OFF)) {
                mMobileData = false;
            }
            PowerSavingUtils.SetMobileDataEnable(mContext, mMobileData);
        }
    }

    public static void WiFiAndMobileDataRestore(Context mContext, int WhoExit) {
        Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore()");
        int WhoFirst = GetWhoApplyFirst(mContext);
        if (WhoFirst == 2) {
            Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore()  - [DC first case] ");
            SetWifiEnable(mContext, BooleanToString(DataConnectionUtils.getFromPref(mContext, DCPREF.WIFI)));
            SetMobileDataEnable(mContext, BooleanToString(DataConnectionUtils.getFromPref(mContext, DCPREF.MOBILE)));
        } else if (WhoFirst == 1) {
            Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore()  - [lpm first case] ");
            mPowerSavingBackupItem = LpmUtils.GetValueFromBackupFile(mContext);
            if (mPowerSavingBackupItem.mWifi.equals(SWITCHER.KEEP)) {
                Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore()  - [lpm first case]-Wi-Fi [LPM(KeeP) + DC (value)]");
                SetWifiEnable(mContext, BooleanToString(DataConnectionUtils.getFromPref(mContext, DCPREF.WIFI)));
            } else {
                Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore()  - [lpm first case]-Wi-Fi [LPM(value)]");
                SetWifiEnable(mContext, mPowerSavingBackupItem.mWifi);
            }
            if (mPowerSavingBackupItem.mMobileData.equals(SWITCHER.KEEP)) {
                Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore()  - [lpm first case]-MobileData [LPM(KeeP) + DC (value)]");
                SetMobileDataEnable(mContext, BooleanToString(DataConnectionUtils.getFromPref(mContext, DCPREF.MOBILE)));
                return;
            }
            Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore()  - [lpm first case]-MobileData [LPM(value)]");
            SetMobileDataEnable(mContext, mPowerSavingBackupItem.mMobileData);
        } else if (WhoExit == 1) {
            Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore() - [general case]-LPM exit,so restore ");
            mPowerSavingBackupItem = LpmUtils.GetValueFromBackupFile(mContext);
            SetWifiEnable(mContext, mPowerSavingBackupItem.mWifi);
            SetMobileDataEnable(mContext, mPowerSavingBackupItem.mMobileData);
        } else if (WhoExit == 2) {
            Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore() - [general case]-DC exit,so restore ");
            SetWifiEnable(mContext, BooleanToString(DataConnectionUtils.getFromPref(mContext, DCPREF.WIFI)));
            SetMobileDataEnable(mContext, BooleanToString(DataConnectionUtils.getFromPref(mContext, DCPREF.MOBILE)));
        } else {
            Log.i(TAG, "[LpmDcUtils] WiFiAndMobileDataRestore() - [general case]-Reboot/Crash  ");
        }
    }

    public static void RestoreWhenReStart(Context mmContext) {
        Log.i(TAG, "[LpmDcUtils] RestoreWhenReStart()");
        final Context mContext = mmContext;
        new Thread() {
            public void run() {
                Looper.prepare();
                LpmUtils.SendIntentNotifyIsStillSetting(mContext, true);
                Looper.loop();
                LpmDcUtils.SetLpmOrDcApplyStatus(mContext, 1, false);
                LpmDcUtils.SetLpmOrDcApplyStatus(mContext, 2, false);
                Log.i(LpmDcUtils.TAG, "[LpmDcUtils] RestoreforServiceStart() RestoreSettingThread Start*******");
                PowerSavingItem mPowerSavingBackupItem = new PowerSavingItem();
                mPowerSavingBackupItem = LpmUtils.GetValueFromBackupFile(mContext);
                if (mPowerSavingBackupItem != null) {
                    LpmDcUtils.WiFiAndMobileDataRestore(mContext, 1);
                    LpmUtils.SetBTEnable(mContext, mPowerSavingBackupItem.mBT);
                    LpmUtils.SetGpsEnable(mContext, mPowerSavingBackupItem.mGps);
                    LpmUtils.Set3DSoundEnable(mContext, mPowerSavingBackupItem.m3DSound);
                    LpmUtils.SetScreenTimeout(mContext, mPowerSavingBackupItem.mScreenTimeout);
                    LpmUtils.SendIntentToFrameworkForLPM(mContext, false, mPowerSavingBackupItem);
                }
                Log.i(LpmDcUtils.TAG, "[LpmDcUtils] RestoreforServiceStart() ApplySettingThread [ get current setting]*******");
                Log.i(LpmDcUtils.TAG, "[LpmDcUtils] RestoreforServiceStart() RestoreSettingThread End******");
                LowPowerMode.NotifyApplyRestoreFinish(2);
            }
        }.start();
    }

    public static String BooleanToString(boolean value) {
        if (value) {
            return SWITCHER.ON;
        }
        return SWITCHER.OFF;
    }

    public static String GetMobileDataValueFromLPMDB(Context mContext) {
        return PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.MOBILE_DATA);
    }

    public static String GetWifiValueFromLPMDB(Context mContext) {
        return PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.WIFI);
    }
}
