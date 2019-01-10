package com.evenwell.powersaving.g3.lpm;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import com.evenwell.powersaving.g3.LpmDcUtils;
import com.evenwell.powersaving.g3.PowerSavingItem;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.glance.GlanceUtil;
import com.evenwell.powersaving.g3.p000e.doze.WifiTethering;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.Function;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMPARM;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.LPMSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.PERMISSION.TYPE;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.FUNCTIONDB;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.ProjectInfo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LpmUtils {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static boolean isLPMBDApply = false;

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING,
        WIFI_AP_STATE_DISABLED,
        WIFI_AP_STATE_ENABLING,
        WIFI_AP_STATE_ENABLED,
        WIFI_AP_STATE_FAILED
    }

    public static PowerSavingItem GetValueFromDB(Context mContext) {
        PowerSavingItem powerSavingItem = new PowerSavingItem();
        powerSavingItem.mBegin = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.BEGIN);
        powerSavingItem.mWifi = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.WIFI);
        powerSavingItem.mGps = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.GPS);
        powerSavingItem.mBT = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.BT);
        powerSavingItem.mMobileData = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.MOBILE_DATA);
        powerSavingItem.m3DSound = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.D3_SOUND);
        powerSavingItem.mScreenTimeout = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.SCREEN_TIMEOUT);
        powerSavingItem.mLPMAnimation = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.ANIMATION);
        powerSavingItem.mLPMVibrate = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.VIBRATION);
        powerSavingItem.mLPMBD = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.BACKGROUND_DATA);
        powerSavingItem.mWifiHotspot = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.WIFI_HOTSPOT);
        powerSavingItem.mGlance = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.GLANCE);
        powerSavingItem.mAutoSync = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.AUTOSYNC);
        powerSavingItem.mMonochromacy = PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.MONOCHROMACY);
        Log.i(TAG, "LpmUtils:  GetValueFromDB() mBegin = " + powerSavingItem.mBegin + ", mWifi  = " + powerSavingItem.mWifi + ", mGps = " + powerSavingItem.mGps + ", mBT  = " + powerSavingItem.mBT + ", mLPMAnimation = " + powerSavingItem.mLPMAnimation + ", mMobileData  = " + powerSavingItem.mMobileData + ", mLPMVibrate = " + powerSavingItem.mLPMVibrate + ", mLPMBD = " + powerSavingItem.mLPMBD + ", m3DSound  = " + powerSavingItem.m3DSound + ", mScreenTimeout = " + powerSavingItem.mScreenTimeout + ", mWifiHotspot = " + powerSavingItem.mWifiHotspot + ", mGlance = " + powerSavingItem.mGlance + ", mAutoSync = " + powerSavingItem.mAutoSync + ", mMonochromacy = " + powerSavingItem.mMonochromacy);
        if (powerSavingItem.mBegin == null || powerSavingItem.mWifi == null || powerSavingItem.mGps == null || powerSavingItem.mBT == null || powerSavingItem.mLPMAnimation == null || powerSavingItem.mMobileData == null || powerSavingItem.mLPMVibrate == null || powerSavingItem.mScreenTimeout == null || powerSavingItem.m3DSound == null || powerSavingItem.mLPMBD == null || powerSavingItem.mWifiHotspot == null || powerSavingItem.mGlance == null || powerSavingItem.mAutoSync == null || powerSavingItem.mMonochromacy == null) {
            return new PowerSavingItem();
        }
        return powerSavingItem;
    }

    public static PowerSavingItem GetValueFromBackupFile(Context mContext) {
        PowerSavingItem powerSavingItem = new PowerSavingItem();
        SharedPreferences prefStatus = mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_LPM_BACKUP_FILE, 0);
        powerSavingItem.mWifi = prefStatus.getString(LPMSPREF.WIFI, null);
        powerSavingItem.mGps = prefStatus.getString(LPMSPREF.GPS, null);
        powerSavingItem.mBT = prefStatus.getString(LPMSPREF.BT, null);
        powerSavingItem.mMobileData = prefStatus.getString(LPMSPREF.MOBILE_DATA, null);
        powerSavingItem.m3DSound = prefStatus.getString(LPMSPREF.D3_SOUND, null);
        powerSavingItem.mScreenTimeout = prefStatus.getString(LPMSPREF.SCREEN_TIMEOUT, null);
        powerSavingItem.mLPMAnimation = prefStatus.getString(LPMSPREF.ANIMATION, null);
        powerSavingItem.mLPMVibrate = prefStatus.getString(LPMSPREF.VIBRATION, null);
        powerSavingItem.mLPMBD = prefStatus.getString(LPMSPREF.BACKGROUND_DATA, null);
        powerSavingItem.mWifiHotspot = prefStatus.getString(LPMSPREF.WIFI_HOTSPOT, null);
        powerSavingItem.mGlance = prefStatus.getString(LPMSPREF.GLANCE, null);
        powerSavingItem.mAutoSync = prefStatus.getString(LPMSPREF.AUTOSYNC, null);
        powerSavingItem.mMonochromacy = prefStatus.getString(LPMSPREF.MONOCHROMACY, null);
        Log.i(TAG, "LpmUtils:  GetValueFromBackupFile() mWifi  = " + powerSavingItem.mWifi + ", mGps = " + powerSavingItem.mGps + ", mBT  = " + powerSavingItem.mBT + ", mLPMAnimation = " + powerSavingItem.mLPMAnimation + ", mMobileData  = " + powerSavingItem.mMobileData + ", mLPMVibrate = " + powerSavingItem.mLPMVibrate + ", m3DSound  = " + powerSavingItem.m3DSound + ", mScreenTimeout = " + powerSavingItem.mScreenTimeout + ", mLPMBD = " + powerSavingItem.mLPMBD + ", mWifiHotspot = " + powerSavingItem.mWifiHotspot + ", mGlance = " + powerSavingItem.mGlance + ", mAutoSync = " + powerSavingItem.mAutoSync + ", mMonochromacy = " + powerSavingItem.mMonochromacy);
        if (powerSavingItem.mWifi == null || powerSavingItem.mGps == null || powerSavingItem.mBT == null || powerSavingItem.mLPMAnimation == null || powerSavingItem.mMobileData == null || powerSavingItem.mLPMVibrate == null || powerSavingItem.mScreenTimeout == null || powerSavingItem.m3DSound == null || powerSavingItem.mLPMBD == null || powerSavingItem.mWifiHotspot == null) {
            return new PowerSavingItem();
        }
        return powerSavingItem;
    }

    public static void SetValueToBackupFile(Context mContext, PowerSavingItem mPowerSavingItem) {
        Editor editor = mContext.getSharedPreferences(FILENAME.PREF_POWER_SAVING_LPM_BACKUP_FILE, 0).edit();
        editor.putString(LPMSPREF.WIFI, mPowerSavingItem.mWifi);
        editor.putString(LPMSPREF.GPS, mPowerSavingItem.mGps);
        editor.putString(LPMSPREF.BT, mPowerSavingItem.mBT);
        editor.putString(LPMSPREF.MOBILE_DATA, mPowerSavingItem.mMobileData);
        editor.putString(LPMSPREF.D3_SOUND, mPowerSavingItem.m3DSound);
        editor.putString(LPMSPREF.SCREEN_TIMEOUT, mPowerSavingItem.mScreenTimeout);
        editor.putString(LPMSPREF.ANIMATION, mPowerSavingItem.mLPMAnimation);
        editor.putString(LPMSPREF.VIBRATION, mPowerSavingItem.mLPMVibrate);
        editor.putString(LPMSPREF.BACKGROUND_DATA, mPowerSavingItem.mLPMBD);
        editor.putString(LPMSPREF.WIFI_HOTSPOT, mPowerSavingItem.mWifiHotspot);
        editor.putString(LPMSPREF.MONOCHROMACY, mPowerSavingItem.mMonochromacy);
        editor.putString(LPMSPREF.AUTOSYNC, mPowerSavingItem.mAutoSync);
        editor.putString(LPMSPREF.GLANCE, mPowerSavingItem.mGlance);
        editor.commit();
        Log.i(TAG, "LpmUtils:  SetValueToBackupFile() mPowerSavingItem.mWifi = " + mPowerSavingItem.mWifi + ", mPowerSavingItem.mGps = " + mPowerSavingItem.mGps + ", mPowerSavingItem.mBT = " + mPowerSavingItem.mBT + ", mPowerSavingItem.mLPMAnimation = " + mPowerSavingItem.mLPMAnimation + ", mPowerSavingItem.mMobileData = " + mPowerSavingItem.mMobileData + ", mPowerSavingItem.mLPMVibrate = " + mPowerSavingItem.mLPMVibrate + ", mPowerSavingItem.m3DSound = " + mPowerSavingItem.m3DSound + ", mPowerSavingItem.mScreenTimeout = " + mPowerSavingItem.mScreenTimeout + ", mPowerSavingItem.mLPMBD = " + mPowerSavingItem.mLPMBD + ", mPowerSavingItem.mWifiHotspot = " + mPowerSavingItem.mWifiHotspot + ", mPowerSavingItem.mGlance = " + mPowerSavingItem.mGlance + ", mPowerSavingItem.mAutoSync = " + mPowerSavingItem.mAutoSync + ", mPowerSavingItem.mMonochromacy = " + mPowerSavingItem.mMonochromacy);
    }

    public static String BooleanToString_NoKeep(boolean value) {
        return value ? SWITCHER.ON : SWITCHER.OFF;
    }

    public static void SetSettingsToPhoneForApply(Context mmContext, final boolean mbackup) {
        final Context mContext = mmContext;
        executorService.execute(new Runnable() {
            public void run() {
                LpmUtils.SendIntentNotifyIsStillSetting(mContext, true);
                Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForApply() ApplySettingThread Start**********");
                PowerSavingItem mPowerSavingItem = new PowerSavingItem();
                mPowerSavingItem = LpmUtils.GetValueFromDB(mContext);
                if (mbackup) {
                    Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForApply() ApplySettingThread [do backup]**********");
                    LpmUtils.GetSettingsFromPhone(mContext, true);
                }
                Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForApply() ApplySettingThread [do apply]********");
                LpmUtils.SendIntentToFrameworkForLPM(mContext, true, mPowerSavingItem);
                LpmUtils.SetScreenTimeout(mContext, mPowerSavingItem.mScreenTimeout);
                LpmUtils.setAutoSyncEnabled(mContext, mPowerSavingItem.mAutoSync);
                GlanceUtil.setGlanceModeEnable(mContext, mPowerSavingItem.mGlance);
                Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForApply() ApplySettingThread [do get current setting]********");
                LpmDcUtils.SetLpmOrDcApplyStatus(mContext, 1, true);
                LowPowerMode.mAlreadyApplied = true;
                Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForApply() ApplySettingThread End***********");
                LowPowerMode.NotifyApplyRestoreFinish(0);
            }
        });
    }

    public static void SetSettingsToPhoneForRestore(Context mContext) {
        Log.i(TAG, "[LpmUtils] SetSettingsToPhoneForRestore()");
        if (VERSION.SDK_INT <= 22) {
            RestoreSettingsToPhone(mContext);
        } else if (PowerSavingUtils.checkPermission(mContext, TYPE.WRITE_SETTINGS)) {
            Log.i(TAG, "[LpmUtils] SetSettingsToPhoneForRestore() granted permission");
            PowerSavingUtils.CancelNotification(mContext, 2002);
            RestoreSettingsToPhone(mContext);
        } else {
            PowerSavingUtils.ShowPermissionNotification(mContext, TYPE.WRITE_SETTINGS, Function.LPM, 12);
        }
    }

    public static void RestoreSettingsToPhone(Context mmContext) {
        final Context mContext = mmContext;
        executorService.execute(new Runnable() {
            public void run() {
                LpmUtils.SendIntentNotifyIsStillSetting(mContext, true);
                Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForRestore() RestoreSettingThread Start*******");
                PowerSavingItem mPowerSavingBackupItem = new PowerSavingItem();
                mPowerSavingBackupItem = LpmUtils.GetValueFromBackupFile(mContext);
                if (mPowerSavingBackupItem != null) {
                    LpmUtils.SendIntentToFrameworkForLPM(mContext, false, mPowerSavingBackupItem);
                    LpmDcUtils.WiFiAndMobileDataRestore(mContext, 1);
                    LpmUtils.SetScreenTimeout(mContext, mPowerSavingBackupItem.mScreenTimeout);
                    LpmUtils.setAutoSyncEnabled(mContext, mPowerSavingBackupItem.mAutoSync);
                    GlanceUtil.setGlanceModeEnable(mContext, mPowerSavingBackupItem.mGlance);
                }
                LpmDcUtils.SetLpmOrDcApplyStatus(mContext, 1, false);
                LowPowerMode.mAlreadyApplied = false;
                Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForRestore() ApplySettingThread [ get current setting]*******");
                Log.i(LpmUtils.TAG, "[LowPowerMode] SetSettingsToPhoneForRestore() RestoreSettingThread End******");
                LowPowerMode.NotifyApplyRestoreFinish(1);
            }
        });
    }

    public static void GetSettingsFromPhone(Context mContext, boolean mbackup) {
        boolean mGps = GetGPSEnable(mContext);
        boolean mMobileDataEnable = GetMobileDataEnable(mContext);
        boolean m3DSound = Get3DSoundEnable(mContext);
        String mScreenTimeout = GetScreenTimeout(mContext);
        boolean mWifi = PowerSavingUtils.GetWiFiEnableByDB(mContext);
        boolean mBT = GetBTEnable(mContext);
        boolean mLPMBD = GetBackgroundDataEnable(mContext);
        boolean mWifiHotspot = GetWifiAPEnabled(mContext);
        boolean isAutoSyncEnable = getAutoSyncEnabled(mContext);
        boolean isMonoChromancyEnable = getSimulateColorSpaceMode(mContext);
        boolean isGlanceEnable = GlanceUtil.getGlanceModeEnable(mContext);
        if (mbackup) {
            PowerSavingItem mPSDBItem = GetValueFromDB(mContext);
            PowerSavingItem mPowerSavingItem = new PowerSavingItem();
            if (mPSDBItem.mWifi.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mWifi = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mWifi = BooleanToString_NoKeep(mWifi);
            }
            if (mPSDBItem.mGps.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mGps = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mGps = BooleanToString_NoKeep(mGps);
            }
            if (mPSDBItem.mBT.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mBT = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mBT = BooleanToString_NoKeep(mBT);
            }
            if (mPSDBItem.mMobileData.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mMobileData = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mMobileData = BooleanToString_NoKeep(mMobileDataEnable);
            }
            if (mPSDBItem.m3DSound.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.m3DSound = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.m3DSound = BooleanToString_NoKeep(m3DSound);
            }
            if (mPSDBItem.mLPMAnimation.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mLPMAnimation = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mLPMAnimation = BooleanToString_NoKeep(true);
            }
            if (mPSDBItem.mLPMVibrate.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mLPMVibrate = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mLPMVibrate = BooleanToString_NoKeep(true);
            }
            if (mPSDBItem.mLPMBD.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mLPMBD = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mLPMBD = BooleanToString_NoKeep(mLPMBD);
            }
            if (mPSDBItem.mScreenTimeout.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mScreenTimeout = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mScreenTimeout = mScreenTimeout;
            }
            if (mPSDBItem.mWifiHotspot.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mWifiHotspot = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mWifiHotspot = BooleanToString_NoKeep(mWifiHotspot);
            }
            if (mPSDBItem.mAutoSync.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mAutoSync = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mAutoSync = BooleanToString_NoKeep(isAutoSyncEnable);
            }
            if (mPSDBItem.mMonochromacy.equals(SWITCHER.OFF)) {
                mPowerSavingItem.mMonochromacy = SWITCHER.OFF;
            } else {
                mPowerSavingItem.mMonochromacy = BooleanToString_NoKeep(isMonoChromancyEnable);
            }
            if (mPSDBItem.mGlance.equals(SWITCHER.KEEP)) {
                mPowerSavingItem.mGlance = SWITCHER.KEEP;
            } else {
                mPowerSavingItem.mGlance = BooleanToString_NoKeep(isGlanceEnable);
            }
            SetValueToBackupFile(mContext, mPowerSavingItem);
        }
        Log.i(TAG, "LowPowerMode:  GetSettingsFromPhone() mbackup = " + mbackup + " mWifi = " + mWifi + " mGps = " + mGps + " mBT = " + mBT + " mMobileDataEnable = " + mMobileDataEnable + " mLPMBD = " + mLPMBD + " m3DSound = " + m3DSound + " mScreenTimeout = " + mScreenTimeout + " mWifiHotspot = " + mWifiHotspot + " isMonoChromancyEnable = " + isMonoChromancyEnable + " isAutoSyncEnable = " + isAutoSyncEnable);
    }

    public static void SendIntentNotifyIsStillSetting(Context mContext, boolean mStatus) {
        LowPowerMode.mHasApplySettingThread = mStatus;
        Intent NoticeIntent = new Intent(ACTION.ACTION_LPM_STILL_SETTING);
        NoticeIntent.putExtra(EXTRA.LPM_STILL_SETTING, mStatus);
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
                        Log.i(TAG, "[LpmUtils] setWifiApEnabled() failed.");
                        e.printStackTrace();
                    }
                }
                mWifiManager.setWifiEnabled(desiredState);
            }
        }
    }

    public static void SetBTEnable(Context mContext, String value) {
        boolean mBT = false;
        if (value != null && !value.equals(SWITCHER.KEEP)) {
            if (value.equals(SWITCHER.ON)) {
                mBT = true;
            }
            if (value.equals(SWITCHER.OFF)) {
                mBT = false;
            }
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean desiredState = mBT;
            if (desiredState && isRadioAllowed(mContext, SSPARM.WIFI)) {
                if (mBluetoothAdapter == null) {
                    if (desiredState) {
                        mBluetoothAdapter.enable();
                    } else {
                        mBluetoothAdapter.disable();
                    }
                }
            } else if (mBluetoothAdapter == null) {
            } else {
                if (desiredState) {
                    try {
                        mBluetoothAdapter.enable();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i(TAG, "[LpmUtils] SetBTEnable() - xxCN PermissionControl lead exception when BT dialog choose off or timeout.");
                        return;
                    }
                }
                mBluetoothAdapter.disable();
            }
        }
    }

    public static void SetGpsEnable(Context mContext, String value) {
        boolean mGps = false;
        if (value != null && !value.equals(SWITCHER.KEEP)) {
            int mode;
            if (value.equals(SWITCHER.ON)) {
                mGps = true;
            }
            if (value.equals(SWITCHER.OFF)) {
                mGps = false;
            }
            if (mGps) {
                int restoreMode = PowerSavingUtils.GetPreferencesStatusInt(mContext, PARM.KEY_PS_RESTORE_GPS_MODE);
                if (restoreMode != -1) {
                    mode = restoreMode;
                } else {
                    mode = 3;
                }
            } else {
                mode = 0;
            }
            Secure.putInt(mContext.getContentResolver(), "location_mode", mode);
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

    public static void Set3DSoundEnable(Context mContext, String value) {
        if (ProjectInfo.IsSupportLPA(mContext) && value != null && !value.equals(SWITCHER.KEEP)) {
            Log.i(TAG, "[LowPowerMode] Set3DSoundEnable value: " + value);
        }
    }

    public static void SetScreenTimeout(Context mContext, String value) {
        if (value != null && !value.equals(SWITCHER.KEEP)) {
            System.putInt(mContext.getContentResolver(), "screen_off_timeout", Integer.parseInt(value));
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

    public static boolean GetWifiEnable(Context mContext) {
        return ((WifiManager) mContext.getSystemService(SSPARM.WIFI)).isWifiEnabled();
    }

    public static boolean GetBTEnable(Context mContext) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            return false;
        }
        int status = mBluetoothAdapter.getState();
        if (status == 12) {
            return true;
        }
        if (status == 11) {
            return false;
        }
        if (status == 10) {
            return false;
        }
        if (status == 13) {
            return false;
        }
        return false;
    }

    public static boolean GetGPSEnable(Context mContext) {
        int mode = Secure.getInt(mContext.getContentResolver(), "location_mode", 0);
        PowerSavingUtils.SetPreferencesStatus(mContext, PARM.KEY_PS_RESTORE_GPS_MODE, mode);
        if (mode == 0) {
            return false;
        }
        return true;
    }

    public static boolean GetMobileDataEnable(Context mContext) {
        return PowerSavingUtils.GetMobileDataEnable(mContext);
    }

    public static boolean GetBackgroundDataEnable(Context mContext) {
        int BackgroundData = -1;
        if (PowerSavingUtils.IsUseNewMethodToStoreSettings(mContext)) {
            String value = PowerSavingUtils.getStringItemFromDB(mContext, FUNCTIONDB.ADD_BACKGROUND_DATA);
            if (value != null) {
                BackgroundData = Integer.parseInt(value);
            }
        } else {
            BackgroundData = System.getInt(mContext.getContentResolver(), FUNCTIONDB.ADD_BACKGROUND_DATA, -1);
        }
        if (BackgroundData == 0) {
            return false;
        }
        if (BackgroundData == 1) {
            return true;
        }
        return true;
    }

    public static boolean Get3DSoundEnable(Context mContext) {
        return !ProjectInfo.IsSupportLPA(mContext) ? false : false;
    }

    public static String GetScreenTimeout(Context mContext) {
        return String.valueOf(System.getLong(mContext.getContentResolver(), "screen_off_timeout", 30000));
    }

    public static String GetBacklight(Context mContext) {
        int brightnessMode = 0;
        float brightness = (float) System.getInt(mContext.getContentResolver(), "screen_brightness", 100);
        try {
            brightnessMode = System.getInt(mContext.getContentResolver(), "screen_brightness_mode");
        } catch (SettingNotFoundException e) {
        }
        if (brightnessMode == 1) {
            return LPMPARM.AUTOBRIGHTNESS;
        }
        if (brightnessMode == 0) {
            return Integer.toString((int) brightness);
        }
        return LPMPARM.AUTOBRIGHTNESS;
    }

    public static void SendIntentToFrameworkForLPM(Context mContext, boolean enabled, PowerSavingItem mPowerSavingItem) {
        Intent NoticeIntent = new Intent(ACTION.ACTION_NOW_IN_LPM);
        NoticeIntent.putExtra(EXTRA.IN_LPM, enabled);
        Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() enabled =" + enabled);
        if (mPowerSavingItem.mLPMAnimation.equals(SWITCHER.ON)) {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMAnimation = ON");
            NoticeIntent.putExtra(EXTRA.IN_LPM_ANIMATION, 1);
        } else if (mPowerSavingItem.mLPMAnimation.equals(SWITCHER.OFF)) {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMAnimation = OFF");
            NoticeIntent.putExtra(EXTRA.IN_LPM_ANIMATION, 0);
        } else {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMAnimation = KEEP");
        }
        if (mPowerSavingItem.mLPMVibrate.equals(SWITCHER.ON)) {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMVibrate = ON");
            NoticeIntent.putExtra(EXTRA.IN_LPM_VIBRATE, 1);
        } else if (mPowerSavingItem.mLPMVibrate.equals(SWITCHER.OFF)) {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMVibrate = OFF");
            NoticeIntent.putExtra(EXTRA.IN_LPM_VIBRATE, 0);
        } else {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMVibrate = KEEP");
        }
        if (mPowerSavingItem.mLPMBD.equals(SWITCHER.ON)) {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMBD = ON");
            NoticeIntent.putExtra(EXTRA.IN_LPM_BD, 1);
            SetLPMBDApply(true);
        } else if (mPowerSavingItem.mLPMBD.equals(SWITCHER.OFF)) {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMBD = OFF");
            NoticeIntent.putExtra(EXTRA.IN_LPM_BD, 0);
            SetLPMBDApply(true);
        } else {
            Log.i(TAG, "[LpmUtils] SendIntentToFrameworkForLPM() mLPMBD = KEEP");
            SetLPMBDApply(false);
        }
        mContext.sendBroadcast(NoticeIntent);
    }

    public static void SetWifiHotspotEnable(Context mContext, String value) {
        boolean mLPMHotspot = false;
        if (value != null && !value.equals(SWITCHER.KEEP)) {
            if (value.equals(SWITCHER.ON)) {
                mLPMHotspot = true;
            }
            if (value.equals(SWITCHER.OFF)) {
                mLPMHotspot = false;
            }
            Log.i(TAG, "[LpmUtils] setSoftapEnabled() enabled:" + mLPMHotspot);
            ContentResolver cr = mContext.getContentResolver();
            WifiManager mWifiManager = (WifiManager) mContext.getSystemService(SSPARM.WIFI);
            try {
                new WifiTethering(mContext).setTethering(mLPMHotspot);
            } catch (Exception e) {
                Log.i(TAG, "[LpmUtils] setWifiApEnabled() failed.");
                e.printStackTrace();
            }
            Log.i(TAG, "[LpmUtils] setWifiApEnabled retVal:" + false);
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

    public static boolean GetWifiAPEnabled(Context mContext) {
        return getWifiAPState(mContext) == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    public static boolean GetLPMBDApply() {
        return isLPMBDApply;
    }

    public static void SetLPMBDApply(boolean isApply) {
        isLPMBDApply = isApply;
    }

    public static void setAutoSyncEnabled(Context context, String value) {
        boolean enable = false;
        if (!value.equals(SWITCHER.KEEP)) {
            if (value.equals(SWITCHER.ON)) {
                enable = true;
            }
            if (value.equals(SWITCHER.OFF)) {
                enable = false;
            }
            context.getContentResolver();
            ContentResolver.setMasterSyncAutomatically(enable);
        }
    }

    public static boolean getAutoSyncEnabled(Context context) {
        context.getContentResolver();
        return ContentResolver.getMasterSyncAutomatically();
    }

    public static void setMonoChromacyEnabled(Context context, String value) {
        boolean enable = false;
        Log.i(TAG, "setMonoChromacyEnabled : " + value);
        if (!value.equals(SWITCHER.KEEP)) {
            if (value.equals(SWITCHER.ON)) {
                enable = true;
            }
            if (value.equals(SWITCHER.OFF)) {
                enable = false;
            }
            ContentResolver cr = context.getContentResolver();
            int Mode = -1;
            if (enable) {
                Mode = 0;
            }
            if (Mode < 0) {
                Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 0);
                return;
            }
            Secure.putInt(cr, "accessibility_display_daltonizer_enabled", 1);
            Secure.putInt(cr, "accessibility_display_daltonizer", Mode);
        }
    }

    public static boolean getSimulateColorSpaceMode(Context context) {
        ContentResolver cr = context.getContentResolver();
        int Mode = -1;
        if (Secure.getInt(cr, "accessibility_display_daltonizer_enabled", 0) == 1) {
            Mode = Secure.getInt(cr, "accessibility_display_daltonizer", -1);
        }
        if (Mode == 0) {
            return true;
        }
        return false;
    }
}
