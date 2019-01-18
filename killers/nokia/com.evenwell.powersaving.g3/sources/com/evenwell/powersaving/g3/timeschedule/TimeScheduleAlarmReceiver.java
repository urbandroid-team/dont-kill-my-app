package com.evenwell.powersaving.g3.timeschedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class TimeScheduleAlarmReceiver extends BroadcastReceiver {
    private static String TAG = TAG.PSLOG;
    private Context mContext;
    private TimeScheduler mTimeScheduler;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        String action = intent.getAction();
        Log.i(TAG, "[TimeScheduleAlarmReceiver] Intent action: " + action);
        this.mTimeScheduler = new TimeScheduler(this.mContext);
        if (action == null || !TimeScheduleUtils.isTimeScheduleEnabled(this.mContext)) {
            return;
        }
        if (action.equals(TimeScheduler.ACTION_TIME_SCHEDULE_BOOT_HANDLE)) {
            Log.i(TAG, "[TimeScheduleAlarmReceiver] boot handle");
            Log.i(TAG, "[TimeScheduleAlarmReceiver] isCharging = " + PowerSavingUtils.isCharging(this.mContext));
            if (!PowerSavingUtils.isCharging(this.mContext)) {
                TimeScheduleUtils.handleApplyOrDisablePowerSaving(this.mContext, this.mTimeScheduler);
            }
            Log.i(TAG, "[TimeScheduleAlarmReceiver] Time schedule enabled -> set alarm !");
            this.mTimeScheduler.setStartAlarm();
            this.mTimeScheduler.setEndAlarm();
        } else if (action.equals("android.intent.action.TIME_SET") || action.equals("android.intent.action.DATE_CHANGED") || action.equals("android.intent.action.TIMEZONE_CHANGED")) {
            Log.i(TAG, "[TimeScheduleAlarmReceiver] time or time zone changed");
            Log.i(TAG, "[TimeScheduleAlarmReceiver] isCharging = " + PowerSavingUtils.isCharging(this.mContext));
            if (!PowerSavingUtils.isCharging(this.mContext)) {
                TimeScheduleUtils.handleApplyOrDisablePowerSaving(this.mContext, this.mTimeScheduler);
            }
            Log.i(TAG, "[TimeScheduleAlarmReceiver] Time schedule enabled -> update alarm !");
            this.mTimeScheduler.setStartAlarm();
            this.mTimeScheduler.setEndAlarm();
        } else if (action.equals(TimeScheduler.ACTION_TIME_SCHEDULE_START_TIME)) {
            Log.i(TAG, "[TimeScheduleAlarmReceiver] isCharging = " + PowerSavingUtils.isCharging(this.mContext));
            if (!(PowerSavingUtils.isCharging(this.mContext) || isStartEndSettingEql())) {
                TimeScheduleUtils.handleApplyPowerSaving_2(this.mContext, this.mTimeScheduler);
            }
            this.mTimeScheduler.setStartAlarm();
        } else if (action.equals(TimeScheduler.ACTION_TIME_SCHEDULE_END_TIME)) {
            Log.i(TAG, "[TimeScheduleAlarmReceiver] isCharging = " + PowerSavingUtils.isCharging(this.mContext));
            if (!(PowerSavingUtils.isCharging(this.mContext) || isStartEndSettingEql())) {
                TimeScheduleUtils.handleDisablePowerSaving(this.mContext, this.mTimeScheduler);
            }
            this.mTimeScheduler.setEndAlarm();
        }
    }

    private boolean isStartEndSettingEql() {
        if (TimeScheduleUtils.getTimeScheduleStartTime(this.mContext).equals(TimeScheduleUtils.getTimeScheduleEndTime(this.mContext))) {
            return true;
        }
        return false;
    }
}
