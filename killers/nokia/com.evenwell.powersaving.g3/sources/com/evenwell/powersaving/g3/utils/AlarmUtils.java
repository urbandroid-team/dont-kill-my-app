package com.evenwell.powersaving.g3.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.evenwell.powersaving.g3.p000e.doze.record.AlarmRecord;
import com.evenwell.powersaving.g3.provider.BackDataDb.SaveData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net2.lingala.zip4j.util.InternalZipConstants;

public class AlarmUtils {
    private static final String TAG = "AlarmUtils";

    public static class Alarm {
        public int alarmTimes;
        public long duration;
        public int nonWakUpAlarmTimes;
        public int wakUpAlarmTimes;

        public Alarm(AlarmInternal alarm) {
            this.alarmTimes = alarm.wakeUpTimes + alarm.nonWakeUpTimes;
            this.nonWakUpAlarmTimes = alarm.nonWakeUpTimes;
            this.wakUpAlarmTimes = alarm.wakeUpTimes;
            this.duration = alarm.duration;
        }

        public Alarm add(AlarmInternal alarm) {
            Alarm newAlarm = new Alarm();
            newAlarm.duration = this.duration + alarm.duration;
            newAlarm.alarmTimes = (this.alarmTimes + alarm.wakeUpTimes) + alarm.nonWakeUpTimes;
            newAlarm.nonWakUpAlarmTimes = this.nonWakUpAlarmTimes + alarm.nonWakeUpTimes;
            newAlarm.wakUpAlarmTimes = this.wakUpAlarmTimes + alarm.wakeUpTimes;
            return newAlarm;
        }

        public String toString() {
            return this.alarmTimes + InternalZipConstants.ZIP_FILE_SEPARATOR + this.duration;
        }
    }

    private static class AlarmInternal {
        long duration;
        int nonWakeUpTimes;
        String pkgName;
        int wakeUpTimes;

        private AlarmInternal() {
        }
    }

    public static Map<String, Alarm> getAlarm(Context context) {
        List<AlarmInternal> alarmInternals = getAlarmIntenal(context);
        Map<String, Alarm> ret = new HashMap();
        for (AlarmInternal alarmInternal : alarmInternals) {
            Alarm alarmRecord = (Alarm) ret.get(alarmInternal.pkgName);
            if (alarmRecord == null) {
                ret.put(alarmInternal.pkgName, new Alarm(alarmInternal));
            } else {
                ret.put(alarmInternal.pkgName, alarmRecord.add(alarmInternal));
            }
        }
        return ret;
    }

    private static List<AlarmInternal> getAlarmIntenal(Context context) {
        List<AlarmInternal> ret = new ArrayList();
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse(AlarmRecord.ALARM_IN_DOZE_URI), new String[]{"pkg_name", "time", SaveData.WAKEUP_ALARMS, SaveData.NON_WAKEUP_ALARMS}, "tag= ?", new String[]{AlarmRecord.ALARM_DIFF}, null);
            int pkgIndex = cursor.getColumnIndex("pkg_name");
            int timeIndex = cursor.getColumnIndex("time");
            int wakeUpIndex = cursor.getColumnIndex(SaveData.WAKEUP_ALARMS);
            int nonWakeUpIndex = cursor.getColumnIndex(SaveData.NON_WAKEUP_ALARMS);
            if (pkgIndex == -1 || timeIndex == -1 || wakeUpIndex == -1 || nonWakeUpIndex == -1) {
                Log.i(TAG, "getAlarmIntenal, Index error. pkgIndex=" + pkgIndex + ",timeIndex=" + timeIndex + ",wakeUpIndex=" + wakeUpIndex + ",nonWakeUpIndex=" + nonWakeUpIndex);
                return ret;
            }
            while (cursor.moveToNext()) {
                AlarmInternal alarmInternal = new AlarmInternal();
                alarmInternal.pkgName = cursor.getString(pkgIndex);
                alarmInternal.duration = Long.valueOf(cursor.getString(timeIndex)).longValue();
                alarmInternal.wakeUpTimes = cursor.getInt(wakeUpIndex);
                alarmInternal.nonWakeUpTimes = cursor.getInt(nonWakeUpIndex);
                Log.i(TAG, "getAlarmIntenal pkgName=" + alarmInternal.pkgName + ",duration=" + alarmInternal.duration + ",wakeUpTimes=" + alarmInternal.wakeUpTimes + ",nonWakeUpTimes=" + alarmInternal.nonWakeUpTimes);
                ret.add(alarmInternal);
            }
            PSUtils.closeSilently(cursor);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PSUtils.closeSilently(cursor);
        }
    }
}
