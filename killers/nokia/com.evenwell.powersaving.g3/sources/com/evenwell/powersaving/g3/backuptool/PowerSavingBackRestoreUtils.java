package com.evenwell.powersaving.g3.backuptool;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.System;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.EXTRA_NAME;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.LATEST_EVENT_EXTRA;
import com.evenwell.powersaving.g3.timeschedule.TimeScheduleUtils;
import com.evenwell.powersaving.g3.timeschedule.TimeScheduler;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;

public class PowerSavingBackRestoreUtils {
    private static final String FirstPermission = "first_permission";
    private static final String ShowExtraPermissionDialog = "extra_permission";
    private static final String TAG = "PowerSavingBackRestoreUtils";
    private static final String preference_name = "BackupRestorePermission";

    public static boolean getPermissionFirstflag(Context context) {
        return Boolean.valueOf(context.getSharedPreferences(preference_name, 0).getBoolean(FirstPermission, true)).booleanValue();
    }

    public static void setPermissionFirstflag(Context context, boolean flg) {
        Editor edit = context.getSharedPreferences(preference_name, 0).edit();
        edit.putBoolean(FirstPermission, flg);
        edit.commit();
    }

    public static boolean getPermissionDialogflag(Context context) {
        return Boolean.valueOf(context.getSharedPreferences(preference_name, 0).getBoolean(ShowExtraPermissionDialog, false)).booleanValue();
    }

    public static void setPermissionDialogflag(Context context, boolean flg) {
        Editor edit = context.getSharedPreferences(preference_name, 0).edit();
        edit.putBoolean(ShowExtraPermissionDialog, flg);
        edit.commit();
    }

    public static void restoreTimeScheduleSetting(Context context) {
        Log.i(TAG, "restoreTimeScheduleSetting()");
        TimeScheduler mTimeScheduler = new TimeScheduler(context);
        if (TimeScheduleUtils.isTimeScheduleEnabled(context)) {
            Log.i(TAG, "restoreTimeScheduleSetting(): Time Schedule Enabled");
            mTimeScheduler.setStartAlarm();
            mTimeScheduler.setEndAlarm();
            PowerSavingUtils.setStringItemToSelfDB(context, PowerSavingController.THE_LATEST_EVENT_KEY, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            handleApplyPowerSaving_TimeSchedule(context, mTimeScheduler);
            return;
        }
        Log.i(TAG, "restoreTimeScheduleSetting(): Time Schedule Disabled");
        mTimeScheduler.cancelAlarm();
        handleDisablePowerSaving_TimeSchedule(context);
    }

    private static void handleApplyPowerSaving_TimeSchedule(Context context, TimeScheduler mTimeScheduler) {
        if (PowerSavingUtils.isCharging(context)) {
            Log.i(TAG, "isCharging = true -> do nothing");
        } else if (mTimeScheduler.isTimeInterval()) {
            Log.i(TAG, "current time is IN time schedule interval");
            Log.i(TAG, "turn on power saving");
            Intent intent = new Intent(context, PowerSavingController.class);
            intent.putExtra(EXTRA.POWERSAVER_ENABLE, 1);
            intent.putExtra(EXTRA_NAME.MODE, 1);
            intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            context.startService(intent);
        } else {
            Log.i(TAG, "current time is NOT in time schedule interval");
            if (PowerSavingUtils.GetPowerSavingModeEnable(context)) {
                Log.i(TAG, "power saving is already ON by manual");
                PowerSavingUtils.SetPreferencesStatus(context, PARM.KEY_PS_KEEP_MANUAL_ON, true);
            }
        }
    }

    private static void handleDisablePowerSaving_TimeSchedule(Context context) {
        if (!PowerSavingUtils.GetPowerSavingModeEnable(context)) {
            Log.i(TAG, "power saving already disabled");
        } else if (!TimeScheduleUtils.getTheLatestEventFromDB(context).equals(LATEST_EVENT_EXTRA.TIME_SCHEDULE)) {
            Log.i(TAG, "power saving is NOT triggered by time schedule");
        } else if (PowerSavingUtils.GetPreferencesStatus(context, PARM.KEY_PS_KEEP_MANUAL_ON)) {
            Log.i(TAG, "power saving keep manual ON");
            PowerSavingUtils.SetPreferencesStatus(context, PARM.KEY_PS_KEEP_MANUAL_ON, false);
        } else {
            Log.i(TAG, "turn off power saving");
            Intent intent = new Intent(context, PowerSavingController.class);
            intent.putExtra(EXTRA.POWERSAVER_ENABLE, 0);
            context.startService(intent);
        }
    }

    public static void restoreBatteryShowPercentSetting(Context context, int batteryShowPercent) {
        Log.i(TAG, "restoreBatteryShowPercentSetting()");
        try {
            System.putInt(context.getContentResolver(), "status_bar_show_battery_percent", batteryShowPercent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
