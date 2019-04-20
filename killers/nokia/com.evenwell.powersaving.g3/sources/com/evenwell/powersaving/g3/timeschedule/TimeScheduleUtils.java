package com.evenwell.powersaving.g3.timeschedule;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.EXTRA_NAME;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.LATEST_EVENT_EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.TSDB;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeScheduleUtils {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;

    public static boolean isTimeScheduleEnabled(Context context) {
        String value = PowerSavingUtils.getStringItemFromDB(context, TSDB.TIME_SCHEDULE);
        if (value != null) {
            return !value.equals(SYMBOLS.ZERO);
        } else {
            return false;
        }
    }

    public static void setTimeScheduleEnabled(Context context, boolean enabled) {
        if (enabled) {
            PowerSavingUtils.setStringItemToDB(context, TSDB.TIME_SCHEDULE, "1");
        } else {
            PowerSavingUtils.setStringItemToDB(context, TSDB.TIME_SCHEDULE, SYMBOLS.ZERO);
        }
    }

    public static int getTimeScheduleMode(Context context) {
        return 1;
    }

    public static void setTimeScheduleMode(Context context, int mode) {
        if (mode == 0) {
            PowerSavingUtils.setStringItemToDB(context, TSDB.TIME_SCHEDULE_MODE, SYMBOLS.ZERO);
        } else {
            PowerSavingUtils.setStringItemToDB(context, TSDB.TIME_SCHEDULE_MODE, "1");
        }
    }

    public static String getTimeScheduleStartTime(Context context) {
        return PowerSavingUtils.getStringItemFromDB(context, TSDB.TIME_SCHEDULE_START_TIME);
    }

    public static String getTimeScheduleEndTime(Context context) {
        return PowerSavingUtils.getStringItemFromDB(context, TSDB.TIME_SCHEDULE_END_TIME);
    }

    public static void setTimeScheduleStartTime(Context context, String time) {
        PowerSavingUtils.setStringItemToDB(context, TSDB.TIME_SCHEDULE_START_TIME, time);
    }

    public static void setTimeScheduleEndTime(Context context, String time) {
        PowerSavingUtils.setStringItemToDB(context, TSDB.TIME_SCHEDULE_END_TIME, time);
    }

    public static Date getTimeFromDB(Context context, String timeMode) {
        String value;
        Date date = null;
        if (timeMode.equals("start")) {
            value = getTimeScheduleStartTime(context);
        } else {
            value = getTimeScheduleEndTime(context);
        }
        try {
            date = new SimpleDateFormat("HH:mm").parse(value);
        } catch (ParseException pe) {
            Log.d(TAG, "getTimeFromDB, ParseException : " + pe);
        }
        return date;
    }

    public static String getTheLatestEventFromDB(Context context) {
        String ret = PowerSavingUtils.getStringItemFromSelfDB(context, PowerSavingController.THE_LATEST_EVENT_KEY);
        Log.i(TAG, "getTheLatestEventFromDB() = " + ret);
        return ret;
    }

    public static void handleApplyPowerSaving(Context context, TimeScheduler timeScheduler) {
        if (!getTheLatestEventFromDB(context).equals(LATEST_EVENT_EXTRA.TIME_SCHEDULE)) {
            Log.i(TAG, "[handleApplyPowerSaving] the lastest event is NOT time_schedule");
        } else if (timeScheduler.isTimeInterval()) {
            Log.i(TAG, "[handleApplyPowerSaving] current time is IN time schedule interval");
            int mode = getTimeScheduleMode(context);
            Log.i(TAG, "[handleApplyPowerSaving] turn on power saving, mode = " + mode);
            Intent intent = new Intent(context, PowerSavingController.class);
            intent.putExtra(EXTRA.POWERSAVER_ENABLE, 1);
            intent.putExtra(EXTRA_NAME.MODE, mode);
            intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            context.startService(intent);
        } else {
            Log.i(TAG, "[handleApplyPowerSaving] current time is NOT in time schedule interval");
        }
    }

    public static void handleApplyPowerSaving_2(Context context, TimeScheduler timeScheduler) {
        if (PowerSavingUtils.GetPowerSavingModeEnable(context)) {
            Log.i(TAG, "[handleApplyPowerSaving_2] power saving already enabled");
        } else if (timeScheduler.isTimeInterval()) {
            Log.i(TAG, "[handleApplyPowerSaving_2] current time is IN time schedule interval");
            int mode = getTimeScheduleMode(context);
            Log.i(TAG, "[handleApplyPowerSaving_2] turn on power saving, mode = " + mode);
            Intent intent = new Intent(context, PowerSavingController.class);
            intent.putExtra(EXTRA.POWERSAVER_ENABLE, 1);
            intent.putExtra(EXTRA_NAME.MODE, mode);
            intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
            context.startService(intent);
        } else {
            Log.i(TAG, "[handleApplyPowerSaving_2] current time is NOT in time schedule interval");
        }
    }

    public static void handleDisablePowerSaving(Context context, TimeScheduler timeScheduler) {
        if (!PowerSavingUtils.GetPowerSavingModeEnable(context)) {
            Log.i(TAG, "[handleDisablePowerSaving] power saving already disabled");
        } else if (!getTheLatestEventFromDB(context).equals(LATEST_EVENT_EXTRA.TIME_SCHEDULE)) {
            Log.i(TAG, "[handleDisablePowerSaving] power saving is NOT triggered by time schedule");
        } else if (timeScheduler.isTimeInterval()) {
            Log.i(TAG, "[handleDisablePowerSaving] current time is IN time schedule interval");
        } else {
            Log.i(TAG, "[handleDisablePowerSaving] current time is NOT in time schedule interval");
            Log.i(TAG, "[handleDisablePowerSaving] turn off power saving");
            Intent intent = new Intent(context, PowerSavingController.class);
            intent.putExtra(EXTRA.POWERSAVER_ENABLE, 0);
            context.startService(intent);
        }
    }

    public static void handleApplyOrDisablePowerSaving(Context context, TimeScheduler timeScheduler) {
        if (timeScheduler.isTimeInterval()) {
            Log.i(TAG, "[handleApplyOrDisablePowerSaving] current time is IN time schedule interval");
            if (getTheLatestEventFromDB(context).equals(LATEST_EVENT_EXTRA.TIME_SCHEDULE)) {
                int mode = getTimeScheduleMode(context);
                Log.i(TAG, "[handleApplyOrDisablePowerSaving] turn on power saving, mode = " + mode);
                Intent intent = new Intent(context, PowerSavingController.class);
                intent.putExtra(EXTRA.POWERSAVER_ENABLE, 1);
                intent.putExtra(EXTRA_NAME.MODE, mode);
                intent.putExtra(EXTRA_NAME.LATEST_EVENT, LATEST_EVENT_EXTRA.TIME_SCHEDULE);
                context.startService(intent);
                return;
            }
            Log.i(TAG, "[handleApplyOrDisablePowerSaving] the lastest event is NOT time_schedule");
            return;
        }
        Log.i(TAG, "[handleApplyOrDisablePowerSaving] current time is NOT in time schedule interval");
        if (!PowerSavingUtils.GetPowerSavingModeEnable(context)) {
            Log.i(TAG, "[handleApplyOrDisablePowerSaving] power saving already disabled");
        } else if (getTheLatestEventFromDB(context).equals(LATEST_EVENT_EXTRA.TIME_SCHEDULE)) {
            Log.i(TAG, "[handleApplyOrDisablePowerSaving] turn off power saving");
            intent = new Intent(context, PowerSavingController.class);
            intent.putExtra(EXTRA.POWERSAVER_ENABLE, 0);
            context.startService(intent);
        } else {
            Log.i(TAG, "[handleApplyOrDisablePowerSaving] power saving is NOT triggered by time schedule");
        }
    }
}
