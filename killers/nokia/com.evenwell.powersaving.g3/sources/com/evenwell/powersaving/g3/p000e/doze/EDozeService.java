package com.evenwell.powersaving.g3.p000e.doze;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.media.AudioManager;
import android.net.Uri;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.background.BAMMode;
import com.evenwell.powersaving.g3.background.BackgroundCleanUtil;
import com.evenwell.powersaving.g3.dataconnection.DataConnectionUtils;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.p000e.doze.IEDozeService.Stub;
import com.evenwell.powersaving.g3.p000e.doze.function.BluetoothHotSpot;
import com.evenwell.powersaving.g3.p000e.doze.function.Data;
import com.evenwell.powersaving.g3.p000e.doze.function.DataSaverBlackList;
import com.evenwell.powersaving.g3.p000e.doze.function.Function;
import com.evenwell.powersaving.g3.p000e.doze.function.WifiHotSpot;
import com.evenwell.powersaving.g3.p000e.doze.record.AlarmRecord;
import com.evenwell.powersaving.g3.p000e.doze.record.AlarmRecord.Record;
import com.evenwell.powersaving.g3.provider.BackDataDb.SaveData;
import com.evenwell.powersaving.g3.utils.DbgConfig;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.evenwell.powersaving.g3.utils.TimeUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;

/* renamed from: com.evenwell.powersaving.g3.e.doze.EDozeService */
public class EDozeService extends Service {
    public static final String ACTION_TO_ADJUST_FUNCTION = "com.evenwell.action.powersaving.g3.adjust.function";
    private static final int MUSIC_IS_NOT_PLAYING = 16;
    private static final int MUSIC_IS_PLAYING = 8;
    private static final int MUSIC_PLAYING_STATUS_UNKNOWN = 4;
    private static final int NETWORK_IS_NOT_USING = 2;
    private static final int NETWORK_IS_USING = 1;
    private static final int NETWORK_USING_STATUS_UNKNOWN = 0;
    private static final int RESTRICTION_TYPE_DATA = 1;
    private static final int RESTRICTION_TYPE_DATASAVER = 2;
    private static final String TAG = "EDozeService";
    private AlarmInfo mAlarmRecordInDeepDoze = null;
    private AlarmInfo mAlarmRecordInLightDoze = null;
    private int mAmountofDeepDozeTime = 0;
    private AudioManager mAudioManager;
    private final Stub mBinder = new C03515();
    private BluetoothHotSpot mBluetoothHotSpot;
    private CloseFunction mCloseFunction;
    private DataSaverBlackList mDataSaverBlackList;
    private int mDeepState;
    private final DisplayListener mDisplayListener = new C03471();
    private DisplayManager mDisplayManager;
    private Handler mHandlerThread;
    private int mLightState;
    private Data mMobileData;
    private int mNetWorkIsUsingWhileScreenIsOff;
    private int mPlayingMusicWhileScreenIsOff;
    private PowerManager mPowerManger;
    private EdozeBroadcastReceiver mReceiver = new EdozeBroadcastReceiver();
    private WifiHotSpot mWifiHotSpot;
    private boolean mbCloseDataAtNextTime;
    private boolean mbCloseDataInNight;
    private FileObserver mfobForDataDir;

    /* renamed from: com.evenwell.powersaving.g3.e.doze.EDozeService$1 */
    class C03471 implements DisplayListener {
        C03471() {
        }

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            boolean screenOn = false;
            if (displayId == 0) {
                if (EDozeService.this.mDisplayManager.getDisplay(0).getState() == 2) {
                    screenOn = true;
                }
                EDozeService.this.mHandlerThread.post(new Runnable() {
                    public void run() {
                        if (screenOn) {
                            Log.d(EDozeService.TAG, "screenOn=" + screenOn);
                            try {
                                EDozeService.this.mNetWorkIsUsingWhileScreenIsOff = 0;
                                EDozeService.this.mPlayingMusicWhileScreenIsOff = 4;
                                if (PSUtils.enableTestFunction()) {
                                    EDozeService.this.unapplyRestriction(3);
                                }
                                EDozeService.this.updateDozeStatus(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.EDozeService$2 */
    class C03482 implements Runnable {
        C03482() {
        }

        public void run() {
            EDozeService.this.mCloseFunction.restoreDeepFunction();
            EDozeService.this.mCloseFunction.restoreLightFunction();
            if (PSUtils.enableTestFunction()) {
                EDozeService.this.mMobileData.restore();
                EDozeService.this.mDataSaverBlackList.restore();
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.EDozeService$4 */
    class C03504 implements Runnable {
        C03504() {
        }

        public void run() {
            EDozeService.this.mCloseFunction.restoreDeepFunction();
            EDozeService.this.mCloseFunction.restoreLightFunction();
            EDozeService.this.mCloseFunction.release();
            EDozeService.this.mMobileData.restore();
            EDozeService.this.mMobileData.release();
            EDozeService.this.mDataSaverBlackList.restore();
            EDozeService.this.mDataSaverBlackList.release();
            if (PSUtils.enableTestFunction()) {
                EDozeService.this.unapplyRestriction(3);
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.EDozeService$5 */
    class C03515 extends Stub {
        C03515() {
        }

        public boolean activated(String functionName) {
            Log.i(EDozeService.TAG, "get activated functionName = " + functionName);
            Function function = EDozeService.this.mCloseFunction.getFunction(functionName);
            if (function != null) {
                boolean result = function.activated();
                Log.i(EDozeService.TAG, functionName + " is " + (result ? "activated" : "non-activated"));
                return result;
            }
            Log.i(EDozeService.TAG, "can't find " + functionName + " function");
            return false;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.EDozeService$AlarmInfo */
    private class AlarmInfo {
        public Map<String, Map<String, Record>> alarm;
        public long current;

        private AlarmInfo() {
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.e.doze.EDozeService$EdozeBroadcastReceiver */
    public class EdozeBroadcastReceiver extends BroadcastReceiver {
        public void onReceive(Context context, final Intent intent) {
            Log.i(EDozeService.TAG, "intent.getAction() = " + intent.getAction());
            EDozeService.this.mHandlerThread.post(new Runnable() {
                public void run() {
                    if (intent.getAction().equals("android.os.action.DEVICE_IDLE_MODE_CHANGED") || intent.getAction().equals("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED")) {
                        try {
                            EDozeService.this.updateDozeStatus(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (intent.getAction().equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
                        try {
                            if (EDozeService.this.mPowerManger.isPowerSaveMode()) {
                                Log.i(EDozeService.TAG, "PowerSaveMode enabled");
                                EDozeService.this.mAudioManager.setParameters("display_enableHDRkey=1;display_enableHDR=1");
                            } else {
                                Log.i(EDozeService.TAG, "PowerSaveMode disabled");
                                EDozeService.this.mAudioManager.setParameters("display_enableHDRkey=1;display_enableHDR=0");
                            }
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (PSUtils.enableTestFunction()) {
                        if (EDozeService.ACTION_TO_ADJUST_FUNCTION.equals(intent.getAction())) {
                            if (intent.getBooleanExtra("IsNetworkUsing", true)) {
                                EDozeService.this.mNetWorkIsUsingWhileScreenIsOff = 1;
                            } else {
                                EDozeService.this.mNetWorkIsUsingWhileScreenIsOff = 2;
                            }
                            if (intent.getBooleanExtra("isPlayingMusic", true)) {
                                EDozeService.this.mPlayingMusicWhileScreenIsOff = 8;
                            } else {
                                EDozeService.this.mPlayingMusicWhileScreenIsOff = 16;
                            }
                            EDozeService.this.doImprovmentInDeepDoze();
                        }
                        if ("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(intent.getAction()) || "android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED".equals(intent.getAction())) {
                            if (EDozeService.this.mDeepState == 5) {
                                EDozeService.this.mAmountofDeepDozeTime = EDozeService.this.mAmountofDeepDozeTime + 1;
                                Log.d(EDozeService.TAG, "mAmountofDeepDozeTime = " + EDozeService.this.mAmountofDeepDozeTime);
                            }
                            EDozeService.this.doImprovmentInDeepDoze();
                        }
                    }
                }
            });
        }
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCreate() {
        Log.i(TAG, "onCreate()");
        this.mCloseFunction = new CloseFunction(this);
        this.mMobileData = new Data(this);
        this.mDataSaverBlackList = new DataSaverBlackList(this);
        this.mWifiHotSpot = new WifiHotSpot(this);
        this.mBluetoothHotSpot = new BluetoothHotSpot(this);
        HandlerThread handlerThread = new HandlerThread("closeFunctin");
        handlerThread.start();
        this.mHandlerThread = new Handler(handlerThread.getLooper());
        this.mHandlerThread.post(new C03482());
        this.mPowerManger = (PowerManager) getSystemService("power");
        this.mAudioManager = (AudioManager) getSystemService("audio");
        registerReceiver();
        this.mDisplayManager = (DisplayManager) getSystemService("display");
        registerDisplayReceiver();
        this.mDeepState = 0;
        this.mLightState = 0;
        this.mNetWorkIsUsingWhileScreenIsOff = 0;
        this.mPlayingMusicWhileScreenIsOff = 4;
        this.mbCloseDataInNight = false;
        this.mbCloseDataAtNextTime = false;
    }

    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand()");
        this.mHandlerThread.post(new Runnable() {
            public void run() {
                try {
                    if (intent == null) {
                        try {
                            PowerSavingUtils.insertTimeStampToRestartServiceTable(EDozeService.this, "ES");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    EDozeService.this.updateDozeStatus(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return 1;
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        this.mHandlerThread.post(new C03504());
        unregisterReceiver();
        unregisterDisplayReceiver();
        this.mHandlerThread.getLooper().quitSafely();
    }

    private void registerDisplayReceiver() {
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
    }

    private void unregisterDisplayReceiver() {
        try {
            this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerReceiver() {
        IntentFilter idleFilter = new IntentFilter();
        idleFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        idleFilter.addAction("android.os.action.LIGHT_DEVICE_IDLE_MODE_CHANGED");
        idleFilter.addAction("android.intent.action.TIME_SET");
        idleFilter.addAction(ACTION_TO_ADJUST_FUNCTION);
        if (!BackgroundPolicyExecutor.getInstance(this).isCNModel()) {
            idleFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
        }
        registerReceiver(this.mReceiver, idleFilter);
    }

    private void unregisterReceiver() {
        try {
            unregisterReceiver(this.mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateDozeStatus(boolean forceActive) {
        int deepDozeState = 0;
        int lightDozeState = 0;
        boolean cta = PSUtils.isCTA(this);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",Ignore EDoze function.");
            forceActive = true;
        }
        DozeStatus dozeStauts = new DozeStatus();
        if (forceActive) {
            Log.i(TAG, "force active");
            deepDozeState = 0;
            lightDozeState = 0;
        } else {
            try {
                deepDozeState = dozeStauts.getDeepDozeStatus();
                lightDozeState = dozeStauts.getLightDozeStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateCloseFunction(lightDozeState, deepDozeState, this.mLightState, this.mDeepState);
        try {
            if (BMS.getInstance(this).getBMSValue()) {
                updateAlarmRecord(lightDozeState, deepDozeState, this.mLightState, this.mDeepState);
            }
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        if (this.mLightState != lightDozeState) {
            Log.i(TAG, "lightDozeState to " + DozeStatus.lightStateToString(lightDozeState) + " from " + DozeStatus.lightStateToString(this.mLightState));
            this.mLightState = lightDozeState;
        }
        if (this.mDeepState != deepDozeState) {
            Log.i(TAG, "deepDozeState to " + DozeStatus.stateToString(deepDozeState) + " from " + DozeStatus.stateToString(this.mDeepState));
            this.mDeepState = deepDozeState;
        }
    }

    private void updateCloseFunction(int lightDozeState, int deepDozeState, int oldLightDozeState, int oldDeepDozeState) {
        if (oldLightDozeState != lightDozeState) {
            if (lightDozeState == 7 || lightDozeState == 4) {
                this.mCloseFunction.closeLightFunction();
            } else {
                this.mCloseFunction.restoreLightFunction();
            }
        }
        if (oldDeepDozeState == deepDozeState) {
            return;
        }
        if (deepDozeState >= 5) {
            this.mCloseFunction.closeDeepFunction();
        } else {
            this.mCloseFunction.restoreDeepFunction();
        }
    }

    private AlarmInfo getAlarmInfo(Context context) {
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.current = System.currentTimeMillis();
        alarmInfo.alarm = AlarmRecord.getAlarmDumpInfo(context);
        return alarmInfo;
    }

    private AlarmInfo diffAlarmInfo(AlarmInfo oldAlarmInfo, AlarmInfo newAlarmInfo) {
        AlarmInfo diffAlarmInfo = new AlarmInfo();
        diffAlarmInfo.current = newAlarmInfo.current - oldAlarmInfo.current;
        diffAlarmInfo.alarm = AlarmRecord.getDiffRecords(oldAlarmInfo.alarm, newAlarmInfo.alarm);
        return diffAlarmInfo;
    }

    private void dumpAlarmInfo(AlarmInfo alarmInfo, String tag) {
        Log.i(TAG, tag + ",current=" + alarmInfo.current);
        if (alarmInfo.alarm.size() <= 0) {
            Log.i(TAG, tag + ",pkgName empty");
            return;
        }
        for (Entry<String, Map<String, Record>> entry : alarmInfo.alarm.entrySet()) {
            Log.i(TAG, tag + ",pkgName=" + ((String) entry.getKey()));
        }
    }

    private void updateAlarmRecord(int lightDozeState, int deepDozeState, int oldLightDozeState, int oldDeepDozeState) {
        if (oldLightDozeState != lightDozeState) {
            if (lightDozeState == 4) {
                if (this.mAlarmRecordInLightDoze == null) {
                    this.mAlarmRecordInLightDoze = getAlarmInfo(this);
                    saveAlarm(DozeStatus.lightStateToString(lightDozeState), DozeStatus.stateToString(deepDozeState), "light", this.mAlarmRecordInLightDoze, false);
                }
            } else if (this.mAlarmRecordInLightDoze != null) {
                AlarmInfo newAlarmInfo = getAlarmInfo(this);
                AlarmInfo diffAlarmInfo = diffAlarmInfo(this.mAlarmRecordInLightDoze, newAlarmInfo);
                saveAlarm(DozeStatus.lightStateToString(lightDozeState), DozeStatus.stateToString(deepDozeState), "light", newAlarmInfo, false);
                saveAlarm(DozeStatus.lightStateToString(lightDozeState), DozeStatus.stateToString(deepDozeState), AlarmRecord.ALARM_DIFF, diffAlarmInfo, true);
                dumpAlarmInfo(diffAlarmInfo, "Light Doze, Diff Alarm");
                this.mAlarmRecordInLightDoze = null;
            }
        }
        if (oldDeepDozeState == deepDozeState) {
            return;
        }
        if (deepDozeState == 5) {
            if (this.mAlarmRecordInDeepDoze == null) {
                this.mAlarmRecordInDeepDoze = getAlarmInfo(this);
                saveAlarm(DozeStatus.lightStateToString(lightDozeState), DozeStatus.stateToString(deepDozeState), "deep", this.mAlarmRecordInDeepDoze, false);
            }
        } else if (this.mAlarmRecordInDeepDoze != null) {
            newAlarmInfo = getAlarmInfo(this);
            diffAlarmInfo = diffAlarmInfo(this.mAlarmRecordInDeepDoze, newAlarmInfo);
            saveAlarm(DozeStatus.lightStateToString(lightDozeState), DozeStatus.stateToString(deepDozeState), "deep", newAlarmInfo, false);
            saveAlarm(DozeStatus.lightStateToString(lightDozeState), DozeStatus.stateToString(deepDozeState), AlarmRecord.ALARM_DIFF, diffAlarmInfo, true);
            dumpAlarmInfo(diffAlarmInfo, "Deep Doze, Diff Alarm");
            this.mAlarmRecordInDeepDoze = null;
        }
    }

    private void saveAlarm(String lightDozeStatus, String deepDozeStatus, String tag, AlarmInfo alarmInfo, boolean diff) {
        if (alarmInfo != null && alarmInfo.alarm != null) {
            int size = alarmInfo.alarm.size();
            if (size > 0) {
                String current;
                if (diff) {
                    current = String.valueOf(alarmInfo.current);
                } else {
                    current = new SimpleDateFormat("yyyyMMddHHmmss").format(Long.valueOf(alarmInfo.current));
                }
                ContentValues[] contentValueses = new ContentValues[size];
                int i = 0;
                for (Entry<String, Map<String, Record>> entry : alarmInfo.alarm.entrySet()) {
                    String pkg = (String) entry.getKey();
                    int wakeupTimes = AlarmRecord.getTotalWakeTimes((Map) entry.getValue());
                    int nonWakeupTimes = AlarmRecord.getTotalNonWakeTimes((Map) entry.getValue());
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("time", current);
                    contentValues.put("pkg_name", pkg);
                    contentValues.put(SaveData.LIGHT_DOZE_STATUS, lightDozeStatus);
                    contentValues.put(SaveData.DEEP_DOZE_STATUS, deepDozeStatus);
                    contentValues.put(SaveData.WAKEUP_ALARMS, Integer.valueOf(wakeupTimes));
                    contentValues.put(SaveData.NON_WAKEUP_ALARMS, Integer.valueOf(nonWakeupTimes));
                    contentValues.put(SaveData.TAG, tag);
                    int i2 = i + 1;
                    contentValueses[i] = contentValues;
                    i = i2;
                }
                getContentResolver().bulkInsert(Uri.parse(AlarmRecord.ALARM_IN_DOZE_URI), contentValueses);
            }
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        if (args != null) {
            try {
                if (args.length <= 0) {
                    return;
                }
                if (args[0].equals("-a")) {
                    writer.println("LightState: " + DozeStatus.lightStateToString(this.mLightState) + ", DeepState: " + DozeStatus.stateToString(this.mDeepState));
                    writer.println("CloseFunction: " + this.mCloseFunction.dump());
                    writer.println("CTA = " + PSUtils.isCTA(this) + ", CTS = " + PSUtils.isCTS());
                    writer.println("isCN = " + BackgroundPolicyExecutor.getInstance(this).isCNModel());
                    writer.println("enableTestFunction = " + PSUtils.enableTestFunction());
                    writer.println("mAmountofDeepDozeTime = " + this.mAmountofDeepDozeTime);
                    writer.println("Current Interval " + TimeUtil.getInstance().getCurrentInterval());
                    writer.println("BluetoothHotSpot = " + this.mBluetoothHotSpot.get() + ",WifiHotSpot = " + this.mWifiHotSpot.get() + ",WifiTetheringSize=" + this.mWifiHotSpot.getTetheringSize());
                    writer.println("mNetWorkIsUsingWhileScreenIsOff = " + statusToString(this.mNetWorkIsUsingWhileScreenIsOff));
                    writer.println("mPlayingMusicWhileScreenIsOff = " + statusToString(this.mPlayingMusicWhileScreenIsOff));
                    writer.println("mbCloseDataInNight = " + this.mbCloseDataInNight);
                    writer.println("mbCloseDataAtNextTime = " + this.mbCloseDataAtNextTime);
                    writer.println("DbgConfig isLogcatMainOn = " + DbgConfig.getInstance().isLogcatMainOn());
                    writer.println("BMS = " + BMS.getInstance(this).getBMSValue());
                    writer.println("DefaultSmsAppPackageName = " + BackgroundCleanUtil.getDefaultSmsAppPackageName(this));
                } else if (args[0].equals("-d")) {
                    if (args.length < 2 || TextUtils.isEmpty(args[1])) {
                        writer.println("package is empty.");
                        return;
                    }
                    pkg = args[1];
                    if (BackgroundPolicyExecutor.getInstance(this).isInDisautoList(pkg)) {
                        writer.println(pkg + " is in disauto black list.");
                    } else {
                        writer.println(pkg + " is not in disauto black list.");
                    }
                } else if (args[0].equals("-b")) {
                    if (args.length < 2 || TextUtils.isEmpty(args[1])) {
                        writer.println("package is empty.");
                        return;
                    }
                    pkg = args[1];
                    if (BackgroundPolicyExecutor.getInstance(this).isWhitelisted(pkg)) {
                        writer.println(pkg + " is in bam white list.");
                    } else {
                        writer.println(pkg + " is not in bam white list.");
                    }
                } else if (args[0].equals("-r")) {
                    if (args.length < 2 || TextUtils.isEmpty(args[1])) {
                        writer.println("package is empty.");
                        return;
                    }
                    pkg = args[1];
                    if (new BAMMode(this).hasRestrictComponent(pkg)) {
                        writer.println(pkg + " has restricted component.");
                    } else {
                        writer.println(pkg + " does not has restricted component.");
                    }
                } else if (!args[0].equals("--resource")) {
                } else {
                    if (args.length < 4 || TextUtils.isEmpty(args[1]) || TextUtils.isEmpty(args[2]) || TextUtils.isEmpty(args[3])) {
                        writer.println("argument error.");
                        return;
                    }
                    String name = args[1];
                    String defType = args[2];
                    int resId = getResources().getIdentifier(name, defType, args[3]);
                    String result = "";
                    if (defType.equals("array")) {
                        result = TextUtils.join(",", getResources().getStringArray(resId));
                    } else if (defType.equals("string")) {
                        result = getResources().getString(resId);
                    } else if (defType.equals("bool")) {
                        result = String.valueOf(getResources().getBoolean(resId));
                    }
                    writer.println(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void applyRestriction(int flag) {
        Log.d(TAG, "mNetWorkIsUsingWhileScreenIsOff " + statusToString(this.mNetWorkIsUsingWhileScreenIsOff));
        Log.d(TAG, "mPlayingMusicWhileScreenIsOff " + statusToString(this.mPlayingMusicWhileScreenIsOff));
        if (this.mNetWorkIsUsingWhileScreenIsOff == 2 && this.mPlayingMusicWhileScreenIsOff == 16 && !isUsingHotSpot() && !DataConnectionUtils.isAirplaneModeOn(this) && this.mMobileData.get()) {
            if ((flag & 1) == 1) {
                Log.d(TAG, "applyRestriction RESTRICTION_TYPE_DATA");
                this.mMobileData.close();
            }
            if ((flag & 2) == 2) {
                Log.d(TAG, "applyRestriction RESTRICTION_TYPE_DATASAVER");
                this.mDataSaverBlackList.close();
            }
        }
    }

    private void unapplyRestriction(int flag) {
        if ((flag & 1) == 1) {
            Log.d(TAG, "unapplyRestriction RESTRICTION_TYPE_DATA");
            this.mMobileData.restore();
        }
        if ((flag & 2) == 2) {
            Log.d(TAG, "unapplyRestriction RESTRICTION_TYPE_DATASAVER");
            this.mDataSaverBlackList.restore();
        }
        this.mAmountofDeepDozeTime = 0;
        this.mbCloseDataInNight = false;
        this.mbCloseDataAtNextTime = false;
    }

    private void adjustFunctionWhileInDeepDoze() {
        if (this.mDeepState >= 5) {
            if (!TimeUtil.getInstance().IsInRestrictionTimeInterval()) {
                unapplyRestriction(3);
            } else if (TimeUtil.getInstance().isInFirstBreakTimeRestrictionTimeInterval()) {
                Log.d(TAG, "InFirstBreakTime");
                applyRestriction(2);
                if (this.mAmountofDeepDozeTime == 2) {
                    applyRestriction(1);
                }
            } else if (TimeUtil.getInstance().isInNightRestrictionTimeInteveral()) {
                Log.d(TAG, "InNight");
                applyRestriction(3);
                this.mAmountofDeepDozeTime = 0;
                this.mbCloseDataInNight = true;
            } else if (TimeUtil.getInstance().isInSecondBreakTimeRestrictionTimeInterval()) {
                Log.d(TAG, "InSecondBreakTime");
                if (this.mbCloseDataAtNextTime) {
                    applyRestriction(1);
                    this.mbCloseDataAtNextTime = false;
                }
                if (this.mbCloseDataInNight) {
                    unapplyRestriction(1);
                    this.mbCloseDataInNight = false;
                    this.mbCloseDataAtNextTime = true;
                }
                applyRestriction(2);
                if (this.mAmountofDeepDozeTime == 2) {
                    applyRestriction(1);
                }
            }
        }
        if (this.mDeepState == 1) {
            unapplyRestriction(3);
        }
    }

    private void doImprovmentInDeepDoze() {
        Log.d(TAG, "MobileData " + this.mMobileData.get() + " AirplaneMode " + DataConnectionUtils.isAirplaneModeOn(this));
        adjustFunctionWhileInDeepDoze();
    }

    private boolean isUsingHotSpot() {
        Log.d(TAG, "BluetoothHotSpot " + this.mBluetoothHotSpot.get() + " WifiHotSpot " + this.mWifiHotSpot.get());
        return this.mBluetoothHotSpot.get() && this.mWifiHotSpot.get();
    }

    private String statusToString(int status) {
        if (status == 0) {
            return "NETWORK_USING_STATUS_UNKNOWN";
        }
        if (status == 1) {
            return "NETWORK_IS_USING";
        }
        if (status == 2) {
            return "NETWORK_IS_NOT_USING";
        }
        if (status == 4) {
            return "MUSIC_PLAYING_STATUS_UNKNOWN";
        }
        if (status == 8) {
            return "MUSIC_IS_PLAYING";
        }
        if (status == 16) {
            return "MUSIC_IS_NOT_PLAYING";
        }
        return "";
    }
}
