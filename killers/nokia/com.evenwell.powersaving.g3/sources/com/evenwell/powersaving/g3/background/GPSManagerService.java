package com.evenwell.powersaving.g3.background;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings.Global;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.SystemConfig;
import com.evenwell.powersaving.g3.appops.UpdateAppOpsService;
import com.evenwell.powersaving.g3.appops.UpdateGPSOpsService;
import com.evenwell.powersaving.g3.lpm.LpmUtils;
import com.evenwell.powersaving.g3.p000e.doze.DozeStatus;
import com.evenwell.powersaving.g3.provider.BackDataDb;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

public class GPSManagerService extends Service {
    private static final String ACTION_GPS_MANAGER_START = "action_gps_manager_start";
    private static final boolean DBG = true;
    private static final int MSG_MOTION_IS_TRIGGERED = 4;
    private static final int MSG_ON_START_CMD = 2;
    private static final int MSG_SCREEN_OFF = 1;
    private static final int MSG_SCREEN_ON = 0;
    private static final int MSG_SERVICE_IS_RESTARTED = 5;
    private static final int MSG_UPDATE_FOR_INCOMING_WHITELIST = 3;
    private static final long ONE_MIN = 60000;
    private static final long ONE_SEC = 1000;
    private static final String TAG = "[PowerSavingAppG3]GPSManagerService";
    private String PREF_KEY_GPS_IGNORE = "gps_ignore";
    private boolean isRegisterReceivers = false;
    private AppOpsManager mAppOps;
    private int mAutoWakeupCount = 0;
    private BackDataDb mBackDataDb;
    private final ArraySet<String> mBackgroundThrottlePackageWhitelist = new ArraySet();
    private Context mContext;
    private long mDelayTimeToClear = 30000;
    private boolean mDisableGPS = false;
    private DozeStatus mDozeStauts;
    private Handler mHandlerThread;
    private List<String> mLocationRequestIgnoredApps;
    private final MotionListener mMotionListener = new MotionListener();
    private Sensor mMotionSensor;
    private long mRepeatPeriod = 600000;
    private long mRepeatPeriodInDeepDoze = 1800000;
    private ScreenReceiver mScreenReceiver;
    private SensorManager mSensorManager;
    private boolean misDeepDoze;
    private boolean misLightDoze;

    private final class MotionListener extends TriggerEventListener implements SensorEventListener {
        boolean active;

        private MotionListener() {
            this.active = false;
        }

        public void onTrigger(TriggerEvent event) {
            Log.d(GPSManagerService.TAG, "MotionListener onTrigger");
            this.active = false;
            GPSManagerService.this.mHandlerThread.obtainMessage(4).sendToTarget();
        }

        public void onSensorChanged(SensorEvent event) {
            Log.d(GPSManagerService.TAG, "MotionListener onSensorChanged");
            GPSManagerService.this.mSensorManager.unregisterListener(this, GPSManagerService.this.mMotionSensor);
            this.active = false;
            GPSManagerService.this.mHandlerThread.obtainMessage(4).sendToTarget();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public boolean register() {
            boolean success;
            if (GPSManagerService.this.mMotionSensor.getReportingMode() == 2) {
                success = GPSManagerService.this.mSensorManager.requestTriggerSensor(GPSManagerService.this.mMotionListener, GPSManagerService.this.mMotionSensor);
            } else {
                success = GPSManagerService.this.mSensorManager.registerListener(GPSManagerService.this.mMotionListener, GPSManagerService.this.mMotionSensor, 3);
            }
            if (success) {
                this.active = true;
            } else {
                Log.e(GPSManagerService.TAG, "Unable to register for " + GPSManagerService.this.mMotionSensor);
            }
            return success;
        }

        public void unregister() {
            if (GPSManagerService.this.mMotionSensor.getReportingMode() == 2) {
                GPSManagerService.this.mSensorManager.cancelTriggerSensor(GPSManagerService.this.mMotionListener, GPSManagerService.this.mMotionSensor);
            } else {
                GPSManagerService.this.mSensorManager.unregisterListener(GPSManagerService.this.mMotionListener);
            }
            this.active = false;
        }
    }

    private final class ScreenReceiver extends BroadcastReceiver {
        private ScreenReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(GPSManagerService.TAG, " ScreenReceiver onReceive : " + action);
            int i = -1;
            switch (action.hashCode()) {
                case -2128145023:
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        i = 0;
                        break;
                    }
                    break;
                case -1454123155:
                    if (action.equals("android.intent.action.SCREEN_ON")) {
                        i = 1;
                        break;
                    }
                    break;
                case 870701415:
                    if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                        i = 2;
                        break;
                    }
                    break;
            }
            switch (i) {
                case 0:
                    GPSManagerService.this.mHandlerThread.obtainMessage(1).sendToTarget();
                    return;
                case 1:
                    GPSManagerService.this.mHandlerThread.obtainMessage(0).sendToTarget();
                    return;
                case 2:
                    GPSManagerService.this.checkIsInDeepDoze();
                    GPSManagerService.this.stopMonitoringMotion();
                    if (GPSManagerService.this.misDeepDoze && GPSManagerService.this.isDisableGPSLocked()) {
                        boolean isNeedToAllowGPSRequest = GPSManagerService.this.isGpsIgnored();
                        Log.d(GPSManagerService.TAG, "isNeedToAllowGPSRequest " + isNeedToAllowGPSRequest);
                        if (isNeedToAllowGPSRequest) {
                            GPSManagerService.this.allowPkgsGPSRequest();
                        }
                        GPSManagerService.this.enableGPSLocked();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        this.mContext = this;
        HandlerThread handlerThread = new HandlerThread("GPSManagerService");
        handlerThread.start();
        this.mHandlerThread = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                boolean isNeedToAllowGPSRequest;
                switch (msg.what) {
                    case 0:
                        GPSManagerService.this.stopMonitoringMotion();
                        GPSManagerService.this.cancelAlarmBgRunService();
                        GPSManagerService.this.enableGPSLocked();
                        SharedPreferences prefStatus = GPSManagerService.this.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0);
                        if (prefStatus.getBoolean("gps_ignore_backward_compatible", true)) {
                            Log.d(GPSManagerService.TAG, "backwardCompatible");
                            GPSManagerService.this.allowAllPkgsGPSRequest();
                            prefStatus.edit().putBoolean("gps_ignore_backward_compatible", false).apply();
                            return;
                        }
                        isNeedToAllowGPSRequest = GPSManagerService.this.isGpsIgnored();
                        Log.d(GPSManagerService.TAG, "isNeedToAllowGPSRequest " + isNeedToAllowGPSRequest);
                        if (isNeedToAllowGPSRequest) {
                            GPSManagerService.this.allowPkgsGPSRequest();
                            return;
                        }
                        return;
                    case 1:
                        if (LpmUtils.GetGPSEnable(GPSManagerService.this.mContext)) {
                            Log.d(GPSManagerService.TAG, "GPS function is enable");
                            if (GPSManagerService.this.startMonitoringMotion()) {
                                GPSManagerService.this.stopMonitoringMotion();
                                GPSManagerService.this.setAlarmBgRunService(GPSManagerService.this.mDelayTimeToClear);
                                GPSManagerService.this.mAutoWakeupCount = GPSManagerService.this.mAutoWakeupCount + 1;
                            } else {
                                Log.d(GPSManagerService.TAG, "register motion listener fail , do nothing");
                            }
                            GPSManagerService.this.enableGPSLocked();
                        } else {
                            Log.d(GPSManagerService.TAG, "GPS function is disable , do nothing");
                        }
                        GPSManagerService.this.mLocationRequestIgnoredApps = BackgroundCleanUtil.getPackagesbyOPAndMode(0, 1, GPSManagerService.this);
                        Log.d(GPSManagerService.TAG, "OP_COARSE_LOCATION ignore  " + GPSManagerService.this.mLocationRequestIgnoredApps);
                        Editor prefStatusEditor = GPSManagerService.this.getSharedPreferences(FILENAME.POWER_SAVING_PKGS_CLOSE_GPS_FILE, 0).edit();
                        prefStatusEditor.clear();
                        for (String pkg : GPSManagerService.this.mLocationRequestIgnoredApps) {
                            prefStatusEditor.putString(pkg, "close");
                        }
                        prefStatusEditor.apply();
                        return;
                    case 2:
                        GPSManagerService.this.registerReceivers();
                        Intent intent = msg.obj;
                        if (intent != null && GPSManagerService.ACTION_GPS_MANAGER_START.equals(intent.getAction())) {
                            GPSManagerService.this.checkIsInDeepDoze();
                            if (!GPSManagerService.this.misDeepDoze) {
                                Log.d(GPSManagerService.TAG, "isDisableGPSLocked " + GPSManagerService.this.isDisableGPSLocked());
                                if (GPSManagerService.this.isDisableGPSLocked() && !GPSManagerService.this.isGpsIgnored()) {
                                    GPSManagerService.this.ignorePkgsGPSRequest();
                                }
                                GPSManagerService.this.disableGPSLocked();
                                GPSManagerService.this.stopMonitoringMotion();
                                GPSManagerService.this.startMonitoringMotion();
                            }
                            GPSManagerService.this.autoWakeupBgRunService();
                            GPSManagerService.this.mAutoWakeupCount = GPSManagerService.this.mAutoWakeupCount + 1;
                            return;
                        }
                        return;
                    case 3:
                        GPSManagerService.this.updateBackgroundThrottlingWhitelistLocked();
                        if (!BackgroundCleanUtil.isScreenOn(GPSManagerService.this.mContext)) {
                            GPSManagerService.this.allowIncomingPkgsGPSRequest();
                            return;
                        }
                        return;
                    case 4:
                        GPSManagerService.this.enableGPSLocked();
                        isNeedToAllowGPSRequest = GPSManagerService.this.isGpsIgnored();
                        Log.d(GPSManagerService.TAG, "isNeedToAllowGPSRequest " + isNeedToAllowGPSRequest);
                        if (isNeedToAllowGPSRequest) {
                            GPSManagerService.this.allowPkgsGPSRequest();
                        }
                        GPSManagerService.this.insertToMotionTimeTable();
                        return;
                    case 5:
                        try {
                            GPSManagerService.this.mBackDataDb.insertTimeStampToServiceRestartTable("GS");
                            return;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return;
                        }
                    default:
                        return;
                }
            }
        };
        this.mScreenReceiver = new ScreenReceiver();
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mMotionSensor = this.mSensorManager.getDefaultSensor(17, true);
        this.mDozeStauts = new DozeStatus();
        this.misDeepDoze = false;
        this.misLightDoze = false;
        this.mHandlerThread.obtainMessage(0).sendToTarget();
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("location_background_throttle_package_whitelist"), true, new ContentObserver(this.mHandlerThread) {
            public void onChange(boolean selfChange) {
                GPSManagerService.this.mHandlerThread.obtainMessage(3).sendToTarget();
            }
        });
        updateBackgroundThrottlingWhitelistLocked();
        this.mBackDataDb = new BackDataDb(this);
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        cancelAlarmBgRunService();
        this.mHandlerThread.getLooper().quitSafely();
        if (this.isRegisterReceivers) {
            try {
                unregisterReceiver(this.mScreenReceiver);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            this.isRegisterReceivers = false;
        }
        stopMonitoringMotion();
        this.mBackDataDb.close();
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean cta = PSUtils.isCTA(this.mContext);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",Ignore GPSManagerService.");
        } else {
            this.mHandlerThread.obtainMessage(2, intent).sendToTarget();
        }
        if (intent == null) {
            this.mHandlerThread.sendEmptyMessage(5);
        }
        return 1;
    }

    private void registerReceivers() {
        if (!this.isRegisterReceivers) {
            Log.d(TAG, "registerReceivers and isScreenOn = " + BackgroundCleanUtil.isScreenOn(this.mContext));
            if (!BackgroundCleanUtil.isScreenOn(this.mContext)) {
                this.mHandlerThread.obtainMessage(1).sendToTarget();
            }
            IntentFilter filterScreen = new IntentFilter();
            filterScreen.addAction("android.intent.action.SCREEN_ON");
            filterScreen.addAction("android.intent.action.SCREEN_OFF");
            filterScreen.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
            registerReceiver(this.mScreenReceiver, filterScreen);
            this.isRegisterReceivers = true;
        }
    }

    private void checkIsInLightDoze() {
        try {
            int lightState = this.mDozeStauts.getLightDozeStatus();
            Log.i(TAG, "Doze lightState : " + DozeStatus.lightStateToString(lightState));
            if (lightState < 4 || lightState > 6) {
                this.misLightDoze = false;
            } else {
                this.misLightDoze = true;
            }
            Log.d(TAG, "Doze misLightDoze : " + this.misLightDoze);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkIsInDeepDoze() {
        try {
            int deepState = this.mDozeStauts.getDeepDozeStatus();
            Log.i(TAG, "Doze deepState : " + DozeStatus.stateToString(deepState));
            if (deepState >= 5) {
                this.misDeepDoze = true;
            } else {
                this.misDeepDoze = false;
            }
            Log.d(TAG, "Doze misDeepDoze : " + this.misDeepDoze);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAlarmBgRunService(long delay) {
        if (delay > 0) {
            Log.d(TAG, "setAlarmBgRunService, delay " + delay);
            AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
            PendingIntent pi = null;
            Intent intent = new Intent(this.mContext, GPSManagerService.class);
            intent.setAction(ACTION_GPS_MANAGER_START);
            try {
                pi = PendingIntent.getService(this.mContext, 0, intent, 134217728);
            } catch (Exception e) {
                Log.e(TAG, "AlarmManager failed to start " + e.toString());
            }
            am.setExactAndAllowWhileIdle(0, System.currentTimeMillis() + delay, pi);
        }
    }

    private void cancelAlarmBgRunService() {
        Log.d(TAG, "cancel Alarm BgRunService.");
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        Intent intent = new Intent(this.mContext, GPSManagerService.class);
        intent.setAction(ACTION_GPS_MANAGER_START);
        am.cancel(PendingIntent.getService(this.mContext, 0, intent, 134217728));
        this.mAutoWakeupCount = 0;
    }

    private void autoWakeupBgRunService() {
        long timeToAutoWakeup = this.mRepeatPeriod;
        if (this.misDeepDoze) {
            timeToAutoWakeup = this.mRepeatPeriodInDeepDoze;
        }
        setAlarmBgRunService(timeToAutoWakeup);
    }

    private boolean startMonitoringMotion() {
        Log.d(TAG, "startMonitoringMotion()");
        if (this.mMotionSensor == null || this.mMotionListener.active) {
            return false;
        }
        return this.mMotionListener.register();
    }

    private void stopMonitoringMotion() {
        Log.d(TAG, "stopMonitoringMotion()");
        if (this.mMotionSensor != null && this.mMotionListener.active) {
            this.mMotionListener.unregister();
        }
    }

    private void ignorePkgsGPSRequest() {
        Intent intent = new Intent(this.mContext, UpdateGPSOpsService.class);
        intent.setAction(UpdateGPSOpsService.UPDATE_APPS_GPS);
        intent.putExtra(UpdateAppOpsService.KEY_MODE, 1);
        List<String> pkgs = BackgroundCleanUtil.getPackagesbyOPs(new int[]{0}, this);
        synchronized (this) {
            pkgs.removeAll(this.mBackgroundThrottlePackageWhitelist);
            pkgs.removeAll(this.mLocationRequestIgnoredApps);
        }
        Log.d(TAG, "pkgs to ignore  " + pkgs);
        intent.putStringArrayListExtra(UpdateGPSOpsService.TARGET_ARRAY_EXTRA, (ArrayList) pkgs);
        writeGpsignoreToPref(true);
        startService(intent);
    }

    private void allowPkgsGPSRequest() {
        List<String> pkgsToAllow = BackgroundCleanUtil.getPackagesbyOPs(new int[]{0}, this);
        ArrayList<String> pkgs = new ArrayList();
        for (Entry<String, ?> entry : getSharedPreferences(FILENAME.POWER_SAVING_PKGS_CLOSE_GPS_FILE, 0).getAll().entrySet()) {
            pkgs.add(entry.getKey());
        }
        pkgsToAllow.removeAll(pkgs);
        Log.d(TAG, "pkgsToAllow to allow  " + pkgsToAllow);
        Intent intent = new Intent(this.mContext, UpdateGPSOpsService.class);
        intent.setAction(UpdateGPSOpsService.UPDATE_APPS_GPS);
        intent.putExtra(UpdateAppOpsService.KEY_MODE, 0);
        intent.putStringArrayListExtra(UpdateGPSOpsService.TARGET_ARRAY_EXTRA, (ArrayList) pkgsToAllow);
        writeGpsignoreToPref(false);
        startService(intent);
    }

    private void allowAllPkgsGPSRequest() {
        List<String> pkgsToAllow = BackgroundCleanUtil.getPackagesbyOPs(new int[]{0}, this);
        SharedPreferences prefStatus = getSharedPreferences(FILENAME.POWER_SAVING_PKGS_CLOSE_GPS_FILE, 0);
        Log.d(TAG, "pkgsToAllow to allow  " + pkgsToAllow);
        Intent intent = new Intent(this.mContext, UpdateGPSOpsService.class);
        intent.setAction(UpdateGPSOpsService.UPDATE_APPS_GPS);
        intent.putExtra(UpdateAppOpsService.KEY_MODE, 0);
        intent.putStringArrayListExtra(UpdateGPSOpsService.TARGET_ARRAY_EXTRA, (ArrayList) pkgsToAllow);
        writeGpsignoreToPref(false);
        startService(intent);
    }

    private void allowIncomingPkgsGPSRequest() {
        Intent intent = new Intent(this.mContext, UpdateGPSOpsService.class);
        intent.setAction(UpdateGPSOpsService.UPDATE_APPS_GPS);
        intent.putExtra(UpdateAppOpsService.KEY_MODE, 0);
        synchronized (this) {
            intent.putStringArrayListExtra(UpdateGPSOpsService.TARGET_ARRAY_EXTRA, new ArrayList(this.mBackgroundThrottlePackageWhitelist));
        }
        startService(intent);
    }

    private void disableGPSLocked() {
        synchronized (this) {
            this.mDisableGPS = true;
        }
    }

    private void enableGPSLocked() {
        synchronized (this) {
            this.mDisableGPS = false;
        }
    }

    private boolean isDisableGPSLocked() {
        boolean disableGps;
        synchronized (this) {
            disableGps = this.mDisableGPS;
        }
        return disableGps;
    }

    private void updateBackgroundThrottlingWhitelistLocked() {
        String setting = Global.getString(this.mContext.getContentResolver(), "location_background_throttle_package_whitelist");
        if (setting == null) {
            setting = "";
        }
        synchronized (this) {
            this.mBackgroundThrottlePackageWhitelist.clear();
            this.mBackgroundThrottlePackageWhitelist.addAll(Arrays.asList(setting.split(",")));
            this.mBackgroundThrottlePackageWhitelist.add("android");
            this.mBackgroundThrottlePackageWhitelist.add(getPackageName());
            this.mBackgroundThrottlePackageWhitelist.addAll(SystemConfig.getInstance().getAllowUnthrottledLocation());
            Log.d(TAG, "LOCATION_BACKGROUND_THROTTLE_PACKAGE_WHITELIST : " + this.mBackgroundThrottlePackageWhitelist);
        }
    }

    private void insertToMotionTimeTable() {
        try {
            this.mBackDataDb.insertTimeToMotionTable();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void writeGpsignoreToPref(boolean flag) {
        getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).edit().putBoolean(this.PREF_KEY_GPS_IGNORE, flag).apply();
    }

    private boolean isGpsIgnored() {
        return readGpsignoreFromPref();
    }

    private boolean readGpsignoreFromPref() {
        return getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0).getBoolean(this.PREF_KEY_GPS_IGNORE, true);
    }
}
