package com.evenwell.powersaving.g3.p000e.doze.record;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/* renamed from: com.evenwell.powersaving.g3.e.doze.record.AlarmRecord */
public class AlarmRecord {
    public static final String ALARM_DIFF = "ALARM_DIFF";
    public static final String ALARM_IN_DOZE_URI = "content://com.evenwell.powersaving.g3.whitelistprovider/alarm_in_doze";
    private static final boolean DBG = false;
    private static final String TAG = "AlarmRecord";

    /* renamed from: com.evenwell.powersaving.g3.e.doze.record.AlarmRecord$Record */
    public static class Record {
        public int alarmTimes;
        public int wakeTimes;

        public String toString() {
            return this.wakeTimes + ":" + getNonWakeTimes();
        }

        public int getNonWakeTimes() {
            return this.alarmTimes - this.wakeTimes;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.record.AlarmRecord$StreamGobbler */
    private static class StreamGobbler extends Thread {
        BufferedReader br = null;
        InputStream is;
        String type;

        public StreamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                this.br = new BufferedReader(new InputStreamReader(this.is));
                while (true) {
                    String line = this.br.readLine();
                    if (line == null) {
                        break;
                    }
                    Log.e(AlarmRecord.TAG, this.type + ">" + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                PSUtils.closeSilently(this.br);
            }
        }
    }

    public static Map<String, Map<String, Record>> getAlarmDumpInfo(Context ctx) {
        Exception e;
        Throwable th;
        BufferedReader bufferedReader = null;
        Map<String, Map<String, Record>> records = new LinkedHashMap();
        String line = null;
        try {
            Process proc = Runtime.getRuntime().exec("dumpsys alarm");
            new StreamGobbler(proc.getErrorStream(), "ERROR").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            do {
                try {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                } catch (Exception e2) {
                    e = e2;
                    bufferedReader = reader;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = reader;
                }
            } while (!line.contains("Alarm Stats:"));
            while (line != null && !Thread.currentThread().isInterrupted()) {
                if (line.contains("wakeups:")) {
                    String[] split = line.trim().split("\\s+");
                    if (split[0].equals("*ACTIVE*")) {
                        line = line.trim().substring(split[0].length());
                        split = line.trim().split("\\s+");
                    }
                    if (split.length != 5) {
                        line = reader.readLine();
                        if (line != null) {
                        }
                    } else {
                        String pkgName = line.split(":")[1].split("\\s+")[0];
                        line = reader.readLine();
                        Map<String, Record> actionMap;
                        String[] split2;
                        int alarmTimes;
                        int wakeTimes;
                        String action;
                        Record record;
                        if (line != null) {
                            while (line != null && !Thread.currentThread().isInterrupted() && !line.contains("wakeups:")) {
                                actionMap = new LinkedHashMap();
                                while (line != null && line.contains("alarms, last")) {
                                    split2 = line.trim().split("\\s+");
                                    if (split2[0].equals("*ACTIVE*")) {
                                        split2 = line.trim().substring(split2[0].length()).trim().split("\\s+");
                                    }
                                    alarmTimes = Integer.valueOf(split2[3]).intValue();
                                    wakeTimes = Integer.valueOf(split2[1]).intValue();
                                    line = reader.readLine();
                                    if (line == null) {
                                        if (line != null && line.contains("alarm")) {
                                            action = line.split(":")[1];
                                            record = new Record();
                                            record.wakeTimes = wakeTimes;
                                            record.alarmTimes = alarmTimes;
                                            actionMap.put(action, record);
                                        }
                                        line = reader.readLine();
                                        if (line == null) {
                                        }
                                    } else {
                                        action = line.split(":")[1];
                                        record = new Record();
                                        record.wakeTimes = wakeTimes;
                                        record.alarmTimes = alarmTimes;
                                        actionMap.put(action, record);
                                        line = reader.readLine();
                                        if (line == null) {
                                        }
                                    }
                                }
                                if (!actionMap.isEmpty()) {
                                    break;
                                }
                                records.put(pkgName, actionMap);
                            }
                        } else {
                            while (line != null) {
                                actionMap = new LinkedHashMap();
                                while (line != null) {
                                    split2 = line.trim().split("\\s+");
                                    if (split2[0].equals("*ACTIVE*")) {
                                        split2 = line.trim().substring(split2[0].length()).trim().split("\\s+");
                                    }
                                    alarmTimes = Integer.valueOf(split2[3]).intValue();
                                    wakeTimes = Integer.valueOf(split2[1]).intValue();
                                    line = reader.readLine();
                                    if (line == null) {
                                        action = line.split(":")[1];
                                        record = new Record();
                                        record.wakeTimes = wakeTimes;
                                        record.alarmTimes = alarmTimes;
                                        actionMap.put(action, record);
                                        line = reader.readLine();
                                        if (line == null) {
                                        }
                                    } else {
                                        action = line.split(":")[1];
                                        record = new Record();
                                        record.wakeTimes = wakeTimes;
                                        record.alarmTimes = alarmTimes;
                                        actionMap.put(action, record);
                                        line = reader.readLine();
                                        if (line == null) {
                                        }
                                    }
                                }
                                if (!actionMap.isEmpty()) {
                                    break;
                                }
                                records.put(pkgName, actionMap);
                            }
                        }
                    }
                } else {
                    line = reader.readLine();
                    if (line != null) {
                    }
                }
            }
            Log.i(TAG, "Process exitValue: " + proc.waitFor());
            PSUtils.closeSilently(reader);
            bufferedReader = reader;
        } catch (Exception e3) {
            e = e3;
            try {
                if (!TextUtils.isEmpty(line)) {
                    Log.i(TAG, "error line: " + line);
                }
                e.printStackTrace();
                PSUtils.closeSilently(bufferedReader);
                return records;
            } catch (Throwable th3) {
                th = th3;
                PSUtils.closeSilently(bufferedReader);
                throw th;
            }
        }
        return records;
    }

    public static Map<String, Map<String, Record>> getDiffRecords(Map<String, Map<String, Record>> oldRecords, Map<String, Map<String, Record>> newRecords) {
        Map<String, Map<String, Record>> diffAlarmRecord = new LinkedHashMap();
        if (oldRecords == null || newRecords == null) {
            Log.e(TAG, "diff error, return empty diffAlarmRecord, oldRecords:" + oldRecords + ",newRecords:" + newRecords);
        } else {
            for (Entry<String, Map<String, Record>> entry : newRecords.entrySet()) {
                Map<String, Record> oldDetailRecords = (Map) oldRecords.get(entry.getKey());
                Map<String, Record> newDetailRecords = (Map) entry.getValue();
                if (oldDetailRecords == null) {
                    diffAlarmRecord.put(entry.getKey(), newDetailRecords);
                } else {
                    Map<String, Record> diffDetailRecords = new LinkedHashMap();
                    for (Entry<String, Record> entryDetail : newDetailRecords.entrySet()) {
                        Record oldDetailRecord = (Record) oldDetailRecords.get(entryDetail.getKey());
                        if (oldDetailRecord == null) {
                            diffDetailRecords.put(entryDetail.getKey(), entryDetail.getValue());
                        } else {
                            Record diffDetailRecord = new Record();
                            diffDetailRecord.alarmTimes = ((Record) entryDetail.getValue()).alarmTimes - oldDetailRecord.alarmTimes;
                            diffDetailRecord.wakeTimes = ((Record) entryDetail.getValue()).wakeTimes - oldDetailRecord.wakeTimes;
                            if (diffDetailRecord.alarmTimes > 0 || diffDetailRecord.wakeTimes > 0) {
                                diffDetailRecords.put(entryDetail.getKey(), diffDetailRecord);
                            }
                        }
                    }
                    if (diffDetailRecords.size() > 0) {
                        diffAlarmRecord.put(entry.getKey(), diffDetailRecords);
                    }
                }
            }
        }
        return diffAlarmRecord;
    }

    public static int getTotalWakeTimes(Map<String, Record> detailRecords) {
        int totalWakeTimes = 0;
        for (Entry<String, Record> entry : detailRecords.entrySet()) {
            totalWakeTimes += ((Record) entry.getValue()).wakeTimes;
        }
        return totalWakeTimes;
    }

    public static int getTotalNonWakeTimes(Map<String, Record> detailRecords) {
        int totalWakeTimes = AlarmRecord.getTotalWakeTimes(detailRecords);
        int totalAlarmTimes = 0;
        for (Entry<String, Record> entry : detailRecords.entrySet()) {
            totalAlarmTimes += ((Record) entry.getValue()).alarmTimes;
        }
        return totalAlarmTimes - totalWakeTimes;
    }
}
