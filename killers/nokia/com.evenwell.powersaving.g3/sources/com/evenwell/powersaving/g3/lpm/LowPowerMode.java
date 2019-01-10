package com.evenwell.powersaving.g3.lpm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.evenwell.powersaving.g3.LpmDcUtils;
import com.evenwell.powersaving.g3.PowerSavingItem;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.Function;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.LPM_AND_DC_APPLY.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION;
import com.evenwell.powersaving.g3.utils.PSConst.PERMISSION.TYPE;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.iFunctionMode;

public class LowPowerMode implements iFunctionMode {
    public static int BATTERY_INTENT_MIN_INTERVAL = 6000;
    private static final boolean DBG = true;
    private static Long OldReceiveTime = Long.valueOf(0);
    private static String TAG = TAG.PSLOG;
    public static boolean mAlreadyApplied = false;
    public static boolean mAutoSyncingWhenAppling = false;
    private static Context mContext;
    private static Handler mHandler = new C03792();
    public static boolean mHasApplySettingThread = false;
    private static int mNowBatteryLevel = 100;
    public static LpmObserverUtils mObserver;
    public static int mThreshold = 30;
    private final BroadcastReceiver mLowPowerModeReceiver = new C03781();

    /* renamed from: com.evenwell.powersaving.g3.lpm.LowPowerMode$1 */
    class C03781 extends BroadcastReceiver {
        C03781() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                LowPowerMode.mContext = context;
                String action = intent.getAction();
                if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                    LowPowerMode.mNowBatteryLevel = intent.getIntExtra("level", 0);
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver () mNowBatteryLevel = " + LowPowerMode.mNowBatteryLevel);
                    long nowTime = System.currentTimeMillis();
                    if (nowTime - LowPowerMode.OldReceiveTime.longValue() >= ((long) LowPowerMode.BATTERY_INTENT_MIN_INTERVAL)) {
                        LowPowerMode.OldReceiveTime = Long.valueOf(nowTime);
                        LowPowerMode.ApplySettingCheck(LowPowerMode.mContext, false);
                    }
                } else if (action.equals(ACTION.ACTION_DC_APPLY_END_BUT_STILL_HAS_LPM)) {
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() ACTION_DC_APPLY_END_BUT_STILL_HAS_LPM");
                    boolean mWifi = PowerSavingUtils.GetWiFiEnableByDB(context);
                    boolean mMobileDataEnable = LpmUtils.GetMobileDataEnable(context);
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() ACTION_DC_APPLY_END_BUT_STILL_HAS_LPM now is only dc restore result mWifi=" + mWifi + " mMobileDataEnable =" + mMobileDataEnable);
                    PowerSavingItem mPSDBItem = LpmUtils.GetValueFromDB(context);
                    PowerSavingItem mPowerSavingBackupItem = LpmUtils.GetValueFromBackupFile(context);
                    if (mPSDBItem.mWifi.equals(SWITCHER.KEEP)) {
                        mPowerSavingBackupItem.mWifi = SWITCHER.KEEP;
                    } else {
                        mPowerSavingBackupItem.mWifi = LpmUtils.BooleanToString_NoKeep(mWifi);
                    }
                    if (mPSDBItem.mMobileData.equals(SWITCHER.KEEP)) {
                        mPowerSavingBackupItem.mMobileData = SWITCHER.KEEP;
                    } else {
                        mPowerSavingBackupItem.mMobileData = LpmUtils.BooleanToString_NoKeep(mMobileDataEnable);
                    }
                    LpmUtils.SetValueToBackupFile(LowPowerMode.mContext, mPowerSavingBackupItem);
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() iristest mWifi = " + LpmUtils.GetWifiEnable(context));
                    LpmUtils.SendIntentNotifyIsStillSetting(LowPowerMode.mContext, true);
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() write file LowPowerMode.mHasApplySettingThread=" + LowPowerMode.mHasApplySettingThread);
                    LpmUtils.SetWifiEnable(LowPowerMode.mContext, mPSDBItem.mWifi);
                    LpmUtils.SetMobileDataEnable(LowPowerMode.mContext, mPSDBItem.mMobileData);
                    LowPowerMode.NotifyApplyRestoreFinish(3);
                } else if (action.equals(INTENT.ACTION.ACTION_LPM_UPDATE_SCHEDULE)) {
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() ACTION_LPM_UPDATE_SCHEDULE");
                    LowPowerMode.ApplySettingCheck(LowPowerMode.mContext, false);
                } else if (action.equals(INTENT.ACTION.ACTION_LPM_SMART_AMP_MODE_CHANGED)) {
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() ACTION_LPM_SMART_AMP_MODE_STATUS_CHANGED");
                    int smart_amp_mode = intent.getIntExtra(EXTRA.IN_LPM_SMART_AMP_MODE, 2);
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() smart_amp_mode: " + smart_amp_mode);
                    LpmObserverUtils.SmartAmpSettingObserver(smart_amp_mode);
                } else if (action.equals(INTENT.ACTION.ACTION_LPM_RECHECK_BATTERY_STATUS)) {
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]: mLowPowerModeReceiver() ACTION_LPM_RECHECK_BATTERY_STATUS");
                    LowPowerMode.ApplySettingCheck(LowPowerMode.mContext, false);
                }
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.lpm.LowPowerMode$2 */
    static class C03792 extends Handler {
        C03792() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]:mHandler() [APPLY_FINISH],");
                    LpmUtils.SendIntentNotifyIsStillSetting(LowPowerMode.mContext, false);
                    LpmUtils.GetSettingsFromPhone(LowPowerMode.mContext, false);
                    return;
                case 1:
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]:mHandler() [RESTORE_FINISH],");
                    LpmUtils.SendIntentNotifyIsStillSetting(LowPowerMode.mContext, false);
                    LpmUtils.GetSettingsFromPhone(LowPowerMode.mContext, false);
                    if (LpmDcUtils.IsDCApply(LowPowerMode.mContext)) {
                        LpmDcUtils.NotifyLpmOrDc(LowPowerMode.mContext, 2);
                        return;
                    }
                    return;
                case 2:
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]:mHandler() [RESTORE_FINISH_WHEN_RESTART],");
                    LpmUtils.SendIntentNotifyIsStillSetting(LowPowerMode.mContext, false);
                    LpmUtils.GetSettingsFromPhone(LowPowerMode.mContext, false);
                    return;
                case 3:
                    Log.i(LowPowerMode.TAG, "[LowPowerMode]:mHandler() [APPLY_AGAIN_FINISH],");
                    LpmUtils.SendIntentNotifyIsStillSetting(LowPowerMode.mContext, false);
                    LpmUtils.GetSettingsFromPhone(LowPowerMode.mContext, false);
                    return;
                default:
                    return;
            }
        }
    }

    public LowPowerMode(Context context, int reason) {
        Log.i(TAG, "[LowPowerMode] init");
        mContext = context;
        boolean IsLPMApply = LpmDcUtils.IsLPMApply(mContext);
        try {
            BATTERY_INTENT_MIN_INTERVAL = Integer.valueOf(PowerSavingUtils.getStringItemFromDB(context, LPMDB.BATTERY_INTENT_MIN_INTERVAL)).intValue();
        } catch (Exception e) {
            Log.i(TAG, "[LowPowerMode] Unable to change battery event interval.");
        }
        if (reason == 1) {
            if (IsLPMApply) {
                boolean psEnabled = PowerSavingUtils.GetPowerSavingModeEnable(mContext);
                boolean lpmEnabled = PowerSavingUtils.GetLPMEnable(mContext);
                if (psEnabled && lpmEnabled) {
                    Log.i(TAG, "[LowPowerMode]: [SERVICE_CRASH] PowerSaving / LPM ON,still apply , update mAlreadyApplied flag");
                    mAlreadyApplied = true;
                    mHasApplySettingThread = false;
                    mObserver = new LpmObserverUtils(mContext);
                    LpmObserverUtils.RegisterContentObserver(mContext);
                } else {
                    Log.i(TAG, "[LowPowerMode]: [SERVICE_CRASH] PowerSaving / LPM OFF,need restore");
                    LpmUtils.SetSettingsToPhoneForRestore(mContext);
                }
            }
        } else if (reason == 0 && IsLPMApply) {
            Log.i(TAG, "[LowPowerMode]: [BOOT_COMPLETED] ,need restore");
            LpmUtils.SetSettingsToPhoneForRestore(mContext);
        }
        mNowBatteryLevel = PowerSavingUtils.GetBatteryLevel(mContext);
    }

    public void registerReceiver(Context ctx) {
        Log.i(TAG, "[LowPowerMode] registerReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction(ACTION.ACTION_DC_APPLY_END_BUT_STILL_HAS_LPM);
        filter.addAction(INTENT.ACTION.ACTION_LPM_UPDATE_SCHEDULE);
        filter.addAction(INTENT.ACTION.ACTION_LPM_RECHECK_BATTERY_STATUS);
        filter.addAction(INTENT.ACTION.ACTION_LPM_SMART_AMP_MODE_CHANGED);
        ctx.registerReceiver(this.mLowPowerModeReceiver, filter);
        mThreshold = Integer.parseInt(PowerSavingUtils.getStringItemFromDB(mContext, LPMDB.BEGIN));
        ApplySettingCheck(ctx, true);
    }

    public void unregisterReceiver(Context ctx) {
        Log.i(TAG, "[LowPowerMode] unregisterReceiver");
        Log.i(TAG, "mHasApplySettingThread =" + mHasApplySettingThread + " mAlreadyApplied =" + mAlreadyApplied);
        if (LpmDcUtils.IsLPMApply(mContext)) {
            if (mObserver != null) {
                LpmObserverUtils lpmObserverUtils = mObserver;
                LpmObserverUtils.UnRegisterContentObserver(mContext);
            }
            LpmUtils.SetSettingsToPhoneForRestore(mContext);
            PowerSavingUtils.CancelNotification(mContext, NOTIFICATION.LPM_MODE);
        }
        try {
            if (this.mLowPowerModeReceiver != null) {
                ctx.unregisterReceiver(this.mLowPowerModeReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ApplySettingCheck(Context mContext, boolean skipCheckBatteryLevel) {
        Log.i(TAG, "[LowPowerMode] ApplySettingCheck() mNowBatteryLevel =" + mNowBatteryLevel + " mThreshold =" + mThreshold);
        Log.i(TAG, "[LowPowerMode] ApplySettingCheck() mHasApplySettingThread =" + mHasApplySettingThread + " mAlreadyApplied =" + mAlreadyApplied);
        Log.i(TAG, "[LowPowerMode] ApplySettingCheck() skipCheckBatteryLevel =" + skipCheckBatteryLevel);
        PowerSavingUtils.SetBatteryLevel(mContext, mNowBatteryLevel);
        LpmObserverUtils lpmObserverUtils;
        if (mNowBatteryLevel < mThreshold || skipCheckBatteryLevel) {
            if (mHasApplySettingThread || mAlreadyApplied) {
                Log.i(TAG, "[LowPowerMode] [has ApplySettingThread] or [already applied ] ,SO Apply LPM Mode SKIP!!!");
                return;
            }
            Log.i(TAG, "[LowPowerMode] [no ApplySettingThread] or [no applied ] ,SO Apply LPM Mode!!!");
            if (VERSION.SDK_INT <= 22) {
                LpmUtils.SetSettingsToPhoneForApply(mContext, true);
                mObserver = new LpmObserverUtils(mContext);
                lpmObserverUtils = mObserver;
                LpmObserverUtils.RegisterContentObserver(mContext);
            } else if (PowerSavingUtils.checkPermission(mContext, TYPE.WRITE_SETTINGS)) {
                Log.i(TAG, "[LowPowerMode] [no ApplySettingThread] or [no applied ] , granted permission");
                PowerSavingUtils.CancelNotification(mContext, 2002);
                LpmUtils.SetSettingsToPhoneForApply(mContext, true);
                mObserver = new LpmObserverUtils(mContext);
                lpmObserverUtils = mObserver;
                LpmObserverUtils.RegisterContentObserver(mContext);
            } else {
                PowerSavingUtils.ShowPermissionNotification(mContext, TYPE.WRITE_SETTINGS, Function.LPM, 0);
            }
        } else if (mNowBatteryLevel < mThreshold + 5 && !skipCheckBatteryLevel) {
        } else {
            if (mHasApplySettingThread || !mAlreadyApplied) {
                Log.i(TAG, "[LowPowerMode] [has ApplySettingThread] or [no applied ] ,SO Restore SKIP!!!");
                return;
            }
            Log.i(TAG, "[LowPowerMode] [no ApplySettingThread] or [no applied ] ,SO do restore and Exit LPM Mode!!!");
            if (VERSION.SDK_INT <= 22) {
                try {
                    if (mObserver != null) {
                        lpmObserverUtils = mObserver;
                        LpmObserverUtils.UnRegisterContentObserver(mContext);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                LpmUtils.SetSettingsToPhoneForRestore(mContext);
                PowerSavingUtils.CancelNotification(mContext, NOTIFICATION.LPM_MODE);
            } else if (PowerSavingUtils.checkPermission(mContext, TYPE.WRITE_SETTINGS)) {
                Log.i(TAG, "[LowPowerMode] [no ApplySettingThread] or [no applied ] , granted permission");
                try {
                    if (mObserver != null) {
                        lpmObserverUtils = mObserver;
                        LpmObserverUtils.UnRegisterContentObserver(mContext);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                LpmUtils.SetSettingsToPhoneForRestore(mContext);
                PowerSavingUtils.CancelNotification(mContext, NOTIFICATION.LPM_MODE);
            } else {
                PowerSavingUtils.ShowPermissionNotification(mContext, TYPE.WRITE_SETTINGS, Function.LPM, 0);
            }
        }
    }

    public static void NotifyApplyRestoreFinish(int WhyFinish) {
        mHandler.removeMessages(WhyFinish);
        Message msg1 = Message.obtain(mHandler);
        int mTime;
        if (VERSION.SDK_INT <= 22) {
            mTime = NOTIFICATION.PS_MODE;
            if (WhyFinish == 0) {
                msg1.what = WhyFinish;
            } else if (WhyFinish == 1) {
                mTime = NOTIFICATION.PS_MODE;
                msg1.what = WhyFinish;
            } else if (WhyFinish == 2) {
                msg1.what = WhyFinish;
            } else if (WhyFinish == 3) {
                mTime = 1100;
                msg1.what = WhyFinish;
            }
            mHandler.sendMessageDelayed(msg1, (long) mTime);
            return;
        }
        mTime = 4200;
        if (WhyFinish == 0) {
            if (LpmUtils.GetLPMBDApply()) {
                mTime = 10000;
            }
            msg1.what = WhyFinish;
        } else if (WhyFinish == 1) {
            mTime = 4000;
            msg1.what = WhyFinish;
        } else if (WhyFinish == 2) {
            mTime = 4200;
            msg1.what = WhyFinish;
        } else if (WhyFinish == 3) {
            mTime = TYPE.SYSTEM_ALERT_WINDOW;
            msg1.what = WhyFinish;
        }
        mHandler.sendMessageDelayed(msg1, (long) mTime);
    }
}
