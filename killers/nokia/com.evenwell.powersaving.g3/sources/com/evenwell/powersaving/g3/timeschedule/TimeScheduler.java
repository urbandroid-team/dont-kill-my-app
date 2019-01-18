package com.evenwell.powersaving.g3.timeschedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeScheduler {
    public static final String ACTION_TIME_SCHEDULE_BOOT_HANDLE = "com.evenwell.powersaving.g3.time_schedule_boot_handle";
    public static final String ACTION_TIME_SCHEDULE_END_TIME = "com.evenwell.powersaving.g3.time_schedule_end_time";
    public static final String ACTION_TIME_SCHEDULE_START_TIME = "com.evenwell.powersaving.g3.time_schedule_start_time";
    private static String TAG = TAG.PSLOG;
    private Calendar mCalendar = Calendar.getInstance();
    private Context mContext;

    public TimeScheduler(Context context) {
        this.mContext = context;
    }

    public boolean isTimeInterval() {
        boolean isTimeInterval = false;
        long start = startOfTimeInterval();
        long end = endOfTimeInterval();
        long now = System.currentTimeMillis();
        if (start > end) {
            if (now >= start || now <= end) {
                isTimeInterval = true;
            }
        } else if (now >= start && now <= end) {
            isTimeInterval = true;
        } else if (start == end) {
            isTimeInterval = true;
        }
        Log.i(TAG, "isTimeInterval = " + isTimeInterval);
        return isTimeInterval;
    }

    public long startOfTimeInterval() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        this.mCalendar.set(13, 0);
        this.mCalendar.set(14, 0);
        int hour = 0;
        int minuate = 0;
        try {
            Date date = new SimpleDateFormat("HH:mm").parse(TimeScheduleUtils.getTimeScheduleStartTime(this.mContext));
            hour = date.getHours();
            minuate = date.getMinutes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.mCalendar.set(11, hour);
        this.mCalendar.set(12, minuate);
        Log.i(TAG, "startOfTimeInterval = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.mCalendar.getTime()));
        return this.mCalendar.getTimeInMillis();
    }

    public long endOfTimeInterval() {
        this.mCalendar.setTimeInMillis(System.currentTimeMillis());
        this.mCalendar.set(13, 0);
        this.mCalendar.set(14, 0);
        int hour = 0;
        int minuate = 0;
        try {
            Date date = new SimpleDateFormat("HH:mm").parse(TimeScheduleUtils.getTimeScheduleEndTime(this.mContext));
            hour = date.getHours();
            minuate = date.getMinutes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.mCalendar.set(11, hour);
        this.mCalendar.set(12, minuate);
        Log.i(TAG, "endOfTimeInterval = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.mCalendar.getTime()));
        return this.mCalendar.getTimeInMillis();
    }

    public long startOfTimeIntervalTomorrow() {
        this.mCalendar.setTimeInMillis(startOfTimeInterval());
        this.mCalendar.add(5, 1);
        Log.i(TAG, "startOfTimeIntervalTomorrow = " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.mCalendar.getTime()));
        return this.mCalendar.getTimeInMillis();
    }

    public boolean isEndTimeSettingSmaller() {
        boolean isSmaller = false;
        if (startOfTimeInterval() >= endOfTimeInterval()) {
            isSmaller = true;
        }
        Log.i(TAG, "isSmaller = " + isSmaller);
        return isSmaller;
    }

    public boolean isStartEndTimeEql() {
        boolean isEql = false;
        if (startOfTimeInterval() == endOfTimeInterval()) {
            isEql = true;
        }
        Log.i(TAG, "isEql = " + isEql);
        return isEql;
    }

    public void setStartAlarm() {
        Log.i(TAG, "[setStartAlarm]");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(13, 0);
        calendar.set(14, 0);
        int hour = 0;
        int minuate = 0;
        try {
            Date date = new SimpleDateFormat("HH:mm").parse(TimeScheduleUtils.getTimeScheduleStartTime(this.mContext));
            hour = date.getHours();
            minuate = date.getMinutes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(11, hour);
        calendar.set(12, minuate);
        SimpleDateFormat logSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "[setStartAlarm] time = " + logSdf.format(calendar.getTime()));
        long nowTime = Calendar.getInstance().getTimeInMillis();
        long startTime = calendar.getTimeInMillis();
        if (startTime < nowTime) {
            calendar.add(5, 1);
            Log.i(TAG, "[setStartAlarm] nowTime = " + logSdf.format(new Date(nowTime)));
            Log.i(TAG, "[setStartAlarm] startTime = " + logSdf.format(new Date(startTime)));
            Log.i(TAG, "[setStartAlarm] adjust to tomorrow time = " + logSdf.format(calendar.getTime()));
        }
        Intent alarm_intent = new Intent(this.mContext, TimeScheduleAlarmReceiver.class);
        alarm_intent.setAction(ACTION_TIME_SCHEDULE_START_TIME);
        ((AlarmManager) this.mContext.getSystemService("alarm")).setExact(0, calendar.getTimeInMillis(), PendingIntent.getBroadcast(this.mContext, 0, alarm_intent, 134217728));
    }

    public void setEndAlarm() {
        Log.i(TAG, "[setEndAlarm]");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(13, 0);
        calendar.set(14, 0);
        int hour = 0;
        int minuate = 0;
        try {
            Date date = new SimpleDateFormat("HH:mm").parse(TimeScheduleUtils.getTimeScheduleEndTime(this.mContext));
            hour = date.getHours();
            minuate = date.getMinutes();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.set(11, hour);
        calendar.set(12, minuate);
        SimpleDateFormat logSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.i(TAG, "[setEndAlarm] time = " + logSdf.format(calendar.getTime()));
        long nowTime = Calendar.getInstance().getTimeInMillis();
        long endTime = calendar.getTimeInMillis();
        if (endTime < nowTime) {
            calendar.add(5, 1);
            Log.i(TAG, "[setEndAlarm] nowTime = " + logSdf.format(new Date(nowTime)));
            Log.i(TAG, "[setEndAlarm] endTime = " + logSdf.format(new Date(endTime)));
            Log.i(TAG, "[setEndAlarm] adjust to tomorrow time = " + logSdf.format(calendar.getTime()));
        }
        Intent alarm_intent = new Intent(this.mContext, TimeScheduleAlarmReceiver.class);
        alarm_intent.setAction(ACTION_TIME_SCHEDULE_END_TIME);
        ((AlarmManager) this.mContext.getSystemService("alarm")).setExact(0, calendar.getTimeInMillis(), PendingIntent.getBroadcast(this.mContext, 0, alarm_intent, 134217728));
    }

    public void cancelAlarm() {
        boolean startAlarmExist;
        boolean endAlarmExist;
        Log.i(TAG, "[cancelAlarm]");
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        Intent startAlarmIntent = new Intent(ACTION_TIME_SCHEDULE_START_TIME);
        if (PendingIntent.getBroadcast(this.mContext, 0, startAlarmIntent, 536870912) != null) {
            startAlarmExist = true;
        } else {
            startAlarmExist = false;
        }
        Log.i(TAG, "[cancelAlarm] startAlarmExist = " + startAlarmExist);
        if (startAlarmExist) {
            am.cancel(PendingIntent.getBroadcast(this.mContext, 0, startAlarmIntent, 134217728));
            Log.i(TAG, "[cancelAlarm] cancel start alarm");
        }
        Intent endAlarmIntent = new Intent(ACTION_TIME_SCHEDULE_END_TIME);
        if (PendingIntent.getBroadcast(this.mContext, 0, endAlarmIntent, 536870912) != null) {
            endAlarmExist = true;
        } else {
            endAlarmExist = false;
        }
        Log.i(TAG, "[cancelAlarm] endAlarmExist = " + endAlarmExist);
        if (endAlarmExist) {
            am.cancel(PendingIntent.getBroadcast(this.mContext, 0, endAlarmIntent, 134217728));
            Log.i(TAG, "[cancelAlarm] cancel end alarm");
        }
    }
}
