package com.evenwell.powersaving.g3.powersaver;

import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.app.IBatteryStats;
import com.android.internal.app.IBatteryStats.Stub;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.MainActivity;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.dataconnection.DataConnection;
import com.evenwell.powersaving.g3.dataconnection.DataConnectionUtils;
import com.evenwell.powersaving.g3.powersaver.function.Animation;
import com.evenwell.powersaving.g3.powersaver.function.AutoSync;
import com.evenwell.powersaving.g3.powersaver.function.BatterySaver;
import com.evenwell.powersaving.g3.powersaver.function.BlueTooth;
import com.evenwell.powersaving.g3.powersaver.function.Function;
import com.evenwell.powersaving.g3.powersaver.function.GPS;
import com.evenwell.powersaving.g3.powersaver.function.Glance;
import com.evenwell.powersaving.g3.powersaver.function.LimitBackgroundData;
import com.evenwell.powersaving.g3.powersaver.function.MobileData;
import com.evenwell.powersaving.g3.powersaver.function.ScreenLight;
import com.evenwell.powersaving.g3.powersaver.function.ScreenTimeout;
import com.evenwell.powersaving.g3.powersaver.function.Vibrate;
import com.evenwell.powersaving.g3.ss.SmartSwitch;
import com.evenwell.powersaving.g3.timeschedule.TimeScheduleAlarmReceiver;
import com.evenwell.powersaving.g3.timeschedule.TimeScheduleUtils;
import com.evenwell.powersaving.g3.timeschedule.TimeScheduler;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.INTENT.FUNCTION.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.COMMON.PARM;
import com.evenwell.powersaving.g3.utils.PSConst.DC.DCPARM;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT;
import com.evenwell.powersaving.g3.utils.PSConst.LPM.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.LPMDB;
import com.evenwell.powersaving.g3.utils.PowerSaverRequestDialog;
import java.util.ArrayList;
import java.util.List;

public class PowerSavingController extends Service {
    private static final int BATTERY_DIFF_THRESHOLD = 5;
    private static final int BATTERY_LOW_THRESHOLD = 2;
    private static final boolean DBG = true;
    public static final int EXTREME_STATE = 1;
    public static final int INACTIVE_STATE = -1;
    private static final int INIT_SAVER_SAVE_TIME_INFO = 4;
    private static final int LOW_POWER_CHECK = 0;
    private static final int LOW_POWER_CHECK_NEW = 2;
    private static final int LOW_POWER_FUNCTION_INIT = 1;
    private static final int LOW_POWER_FUNCTION_INIT_2 = 6;
    public static final int NORMAL_STATE = 0;
    private static final String PROPERTY_PS_REMAIN_TIME = "persist.sys.ps_remain_time";
    private static final int REMAINING_TIME_HANDLE = 3;
    private static final int SERVICE_IS_RESTARTED = 5;
    private static final String TAG = "PowerSavingController";
    public static final String THE_LATEST_APPLY_EVENT_KEY = "PowerSavingController_the_latest_apply_event";
    public static final String THE_LATEST_EVENT_KEY = "PowerSavingController_the_latest_event";
    private String KEY_DISABLED_BY_PLUGIN = "disabled_by_plugin";
    private int SAVINGTIME_ARRAY_AMOUNT = 20;
    private BatteryManager bm = null;
    private List<Function> mBaseFunctions = new ArrayList();
    private IBatteryStats mBatteryInfo = null;
    private final IBinder mBinder = new ServiceBinder();
    private boolean mCanAutoRestore = true;
    private Context mContext = null;
    private DataConnection mDataConnection = null;
    private List<Function> mExtremeFunctions = new ArrayList();
    private List<Long> mExtremeModeSaveTimeList = new ArrayList();
    private int mExtremeThreashold = 5;
    private HandlerThread mHandlerThread;
    private LowPowerHandler mLowPowerHandler;
    private int mNormalThreshold = 15;
    private int mNowBatteryLevel = 0;
    private PowerManager mPowerManger = null;
    private final BroadcastReceiver mPowerSavingControllerReceiver = new C03874();
    private int mSaverAppliedBatteryLevel = -1;
    private ScreeOnHandler mScreeOnHandler = null;
    private SmartSwitch mSmartSwitch = null;
    private int mState = -1;
    private String mTheLatestApplyEvent = LATEST_EVENT_EXTRA.LOW_POWER;
    private String mTheLatestEvent = LATEST_EVENT_EXTRA.LOW_POWER;
    private TimeScheduler mTimeScheduler;
    private UserHandle mUserHandle = new UserHandle(-2);
    private boolean misCloseExtremeFunction = false;
    private boolean misCloseNormalFunction = false;
    private boolean misRegisterDataConnectionRec = false;
    private boolean misRegisterSmartSwitchRec = false;
    private Long mtotalSaveTimeInExtremeMode = Long.valueOf(0);
    private List<IStateChangeListener> stateChangeListenerList;

    /* renamed from: com.evenwell.powersaving.g3.powersaver.PowerSavingController$4 */
    class C03874 extends BroadcastReceiver {
        C03874() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                PowerSavingController.this.mContext = context;
                String action = intent.getAction();
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    PowerSavingController.this.mNowBatteryLevel = intent.getIntExtra("level", 0);
                    int mPlugged = intent.getIntExtra("plugged", -1);
                    int mStatus = intent.getIntExtra("status", -1);
                    if (mPlugged != 1 && mPlugged != 2 && mPlugged != 4) {
                        PowerSavingController.this.mLowPowerHandler.obtainMessage(2, Integer.valueOf(PowerSavingController.this.mNowBatteryLevel)).sendToTarget();
                        PowerSavingController.this.mLowPowerHandler.obtainMessage(3, Integer.valueOf(PowerSavingController.this.mNowBatteryLevel)).sendToTarget();
                    } else if ((mStatus == 2 || mStatus == 5) && PowerSavingController.this.getStateLocked() != -1) {
                        PowerSavingController.this.setDisabledByPluginStatus(PowerSavingController.this.mContext, true);
                        Log.i(PowerSavingController.TAG, "setDisabledByPluginStatus true");
                        PowerSavingController.this.applyInAactiveMode("charging");
                        PowerSavingController.this.mCanAutoRestore = true;
                    }
                } else if (!"android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
                } else {
                    if (PowerSavingController.this.getDisabledByPluginStatus(PowerSavingController.this.mContext)) {
                        Log.i(PowerSavingController.TAG, "mTheLatestApplyEvent = " + PowerSavingController.this.mTheLatestApplyEvent);
                        String reason = "ACTION_POWER_DISCONNECTED";
                        if (PowerSavingController.this.mTheLatestApplyEvent.equals(LATEST_EVENT_EXTRA.MANUAL)) {
                            PowerSavingController.this.applyExtremeMode(reason);
                            PowerSavingController.this.mCanAutoRestore = false;
                        } else if (PowerSavingController.this.mTheLatestApplyEvent.equals(LATEST_EVENT_EXTRA.LOW_POWER)) {
                            if (PowerSavingController.this.mNowBatteryLevel < PowerSavingController.this.mNormalThreshold + 5) {
                                PowerSavingController.this.applyExtremeMode(reason);
                                PowerSavingController.this.mCanAutoRestore = true;
                            } else {
                                PowerSavingController.this.handleDisableBatterySaver();
                            }
                        } else if (PowerSavingController.this.mTimeScheduler == null || !TimeScheduleUtils.isTimeScheduleEnabled(PowerSavingController.this.mContext)) {
                            PowerSavingController.this.handleDisableBatterySaver();
                        } else if (PowerSavingController.this.mTimeScheduler.isTimeInterval()) {
                            PowerSavingController.this.applyExtremeMode(reason);
                            PowerSavingController.this.mCanAutoRestore = false;
                        } else {
                            PowerSavingController.this.handleDisableBatterySaver();
                        }
                        PowerSavingController.this.setDisabledByPluginStatus(PowerSavingController.this.mContext, false);
                    } else if (TimeScheduleUtils.isTimeScheduleEnabled(PowerSavingController.this.mContext)) {
                        TimeScheduleUtils.handleApplyPowerSaving(PowerSavingController.this.mContext, new TimeScheduler(PowerSavingController.this.mContext));
                    }
                }
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.powersaver.PowerSavingController$5 */
    class C03885 implements Runnable {
        C03885() {
        }

        public void run() {
            if (PowerSavingController.this.getStateLocked() == -1 && PowerSavingController.this.mPowerManger != null) {
                Log.i(PowerSavingController.TAG, "disable Battery Saver");
                PowerSavingController.this.mPowerManger.setPowerSaveMode(false);
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.powersaver.PowerSavingController$7 */
    class C03907 implements Runnable {
        C03907() {
        }

        public void run() {
            Intent intent = new Intent(PowerSavingController.this.mContext, TimeScheduleAlarmReceiver.class);
            intent.setAction(TimeScheduler.ACTION_TIME_SCHEDULE_BOOT_HANDLE);
            PowerSavingController.this.mContext.sendBroadcast(intent);
            Log.i(PowerSavingController.TAG, "send broadcast : ACTION_TIME_SCHEDULE_BOOT_HANDLE");
        }
    }

    public static class EXTRA_NAME {
        public static final String LATEST_EVENT = "LATEST_EVENT";
        public static final String MODE = "MODE";
    }

    public interface IStateChangeListener {
        void onChange(int i);
    }

    public static class LATEST_EVENT_EXTRA {
        public static final String LOW_POWER = "LOW_POWER";
        public static final String MANUAL = "MANUAL";
        public static final String TIME_SCHEDULE = "TIME_SCHEDULE";
    }

    private class LowPowerHandler extends Handler {
        private boolean misShowExtremeModeRequestDialog = true;
        private boolean misShowNormalModeRequestDialog = true;

        public LowPowerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = new Intent(PowerSavingController.this.mContext, PowerSaverRequestDialog.class);
            intent.setFlags(268435456);
            int batteryLevel;
            switch (msg.what) {
                case 0:
                    batteryLevel = ((Integer) msg.obj).intValue();
                    Log.i(PowerSavingController.TAG, "batteryLevel = " + batteryLevel + "mCanAutoRestore : " + PowerSavingController.this.mCanAutoRestore);
                    if (batteryLevel > PowerSavingController.this.mNormalThreshold || batteryLevel <= PowerSavingController.this.mExtremeThreashold || PowerSavingController.this.getStateLocked() == 1) {
                        if (batteryLevel <= PowerSavingController.this.mExtremeThreashold) {
                            if (PowerSavingController.this.getStateLocked() != 1 && this.misShowExtremeModeRequestDialog) {
                                intent.putExtra(PowerSaverRequestDialog.POWER_SAVER_MODE_REQUEST, 1);
                                PowerSavingController.this.startActivity(intent);
                                PowerSavingController.this.mCanAutoRestore = true;
                                PowerSavingController.this.setTheLatestEvent(LATEST_EVENT_EXTRA.LOW_POWER);
                                this.misShowExtremeModeRequestDialog = false;
                                this.misShowNormalModeRequestDialog = true;
                                return;
                            }
                            return;
                        } else if (batteryLevel >= PowerSavingController.this.mNormalThreshold + 5) {
                            if (PowerSavingController.this.getStateLocked() != -1 && PowerSavingController.this.mCanAutoRestore) {
                                PowerSavingController.this.applyInAactiveMode("Battery level >= " + (PowerSavingController.this.mNormalThreshold + 5));
                                PowerSavingController.this.setTheLatestEvent(LATEST_EVENT_EXTRA.LOW_POWER);
                            }
                            this.misShowNormalModeRequestDialog = true;
                            this.misShowExtremeModeRequestDialog = true;
                            return;
                        } else if (batteryLevel >= PowerSavingController.this.mExtremeThreashold + 5 && batteryLevel < PowerSavingController.this.mNormalThreshold + 5 && this.misShowNormalModeRequestDialog && PowerSavingController.this.getStateLocked() != -1 && PowerSavingController.this.getStateLocked() != 0 && PowerSavingController.this.mCanAutoRestore) {
                            intent.putExtra(PowerSaverRequestDialog.POWER_SAVER_MODE_REQUEST, 0);
                            PowerSavingController.this.startActivity(intent);
                            PowerSavingController.this.setTheLatestEvent(LATEST_EVENT_EXTRA.LOW_POWER);
                            this.misShowNormalModeRequestDialog = false;
                            this.misShowExtremeModeRequestDialog = true;
                            return;
                        } else {
                            return;
                        }
                    } else if (PowerSavingController.this.getStateLocked() != 0 && this.misShowNormalModeRequestDialog) {
                        intent.putExtra(PowerSaverRequestDialog.POWER_SAVER_MODE_REQUEST, 0);
                        PowerSavingController.this.startActivity(intent);
                        PowerSavingController.this.mCanAutoRestore = true;
                        PowerSavingController.this.setTheLatestEvent(LATEST_EVENT_EXTRA.LOW_POWER);
                        this.misShowNormalModeRequestDialog = false;
                        this.misShowExtremeModeRequestDialog = true;
                        return;
                    } else {
                        return;
                    }
                case 1:
                    PowerSavingController.this.mNormalThreshold = Integer.parseInt(PowerSavingUtils.getStringItemFromDB(PowerSavingController.this.mContext, LPMDB.BEGIN));
                    Log.i(PowerSavingController.TAG, "PSConst.SETTINGDB.LPMDB.BEGIN = " + PowerSavingController.this.mNormalThreshold);
                    PowerSavingController.this.mExtremeThreashold = Integer.parseInt(PowerSavingUtils.getStringItemFromDB(PowerSavingController.this.mContext, LPMDB.EXTREME));
                    Log.i(PowerSavingController.TAG, "PSConst.SETTINGDB.LPMDB.EXTREME = " + PowerSavingController.this.mExtremeThreashold);
                    PowerSavingController.this.iniBaseFunction();
                    PowerSavingController.this.iniExtremeFuntion();
                    PowerSavingController.this.init();
                    PowerSavingController.this.stepState("go to previous state");
                    return;
                case 2:
                    batteryLevel = ((Integer) msg.obj).intValue();
                    Log.i(PowerSavingController.TAG, "batteryLevel = " + batteryLevel + "mCanAutoRestore : " + PowerSavingController.this.mCanAutoRestore);
                    if (batteryLevel > PowerSavingController.this.mNormalThreshold || batteryLevel <= PowerSavingController.this.mExtremeThreashold) {
                        if (batteryLevel <= PowerSavingController.this.mExtremeThreashold) {
                            if (PowerSavingController.this.getStateLocked() != 1 && this.misShowExtremeModeRequestDialog) {
                                intent.putExtra(PowerSaverRequestDialog.POWER_SAVER_MODE_REQUEST, 1);
                                intent.putExtra(PowerSaverRequestDialog.POWER_SAVER_MODE_REQUEST_LEVEL, PowerSavingController.this.mExtremeThreashold);
                                PowerSavingController.this.startActivity(intent);
                                PowerSavingController.this.mCanAutoRestore = true;
                                PowerSavingController.this.setTheLatestEvent(LATEST_EVENT_EXTRA.LOW_POWER);
                                this.misShowExtremeModeRequestDialog = false;
                                this.misShowNormalModeRequestDialog = true;
                                return;
                            }
                            return;
                        } else if (batteryLevel >= PowerSavingController.this.mNormalThreshold + 5) {
                            if (PowerSavingController.this.getStateLocked() != -1 && PowerSavingController.this.mCanAutoRestore) {
                                PowerSavingController.this.applyInAactiveMode("Battery level >= " + (PowerSavingController.this.mNormalThreshold + 5));
                                PowerSavingController.this.setTheLatestEvent(LATEST_EVENT_EXTRA.LOW_POWER);
                            }
                            this.misShowNormalModeRequestDialog = true;
                            this.misShowExtremeModeRequestDialog = true;
                            return;
                        } else {
                            return;
                        }
                    } else if (PowerSavingController.this.getStateLocked() != 1 && this.misShowNormalModeRequestDialog) {
                        intent.putExtra(PowerSaverRequestDialog.POWER_SAVER_MODE_REQUEST, 1);
                        intent.putExtra(PowerSaverRequestDialog.POWER_SAVER_MODE_REQUEST_LEVEL, PowerSavingController.this.mNormalThreshold);
                        PowerSavingController.this.startActivity(intent);
                        PowerSavingController.this.mCanAutoRestore = true;
                        PowerSavingController.this.setTheLatestEvent(LATEST_EVENT_EXTRA.LOW_POWER);
                        this.misShowNormalModeRequestDialog = false;
                        this.misShowExtremeModeRequestDialog = true;
                        return;
                    } else {
                        return;
                    }
                case 3:
                    if (PowerSavingController.this.getStateLocked() != -1) {
                        long saverRemainTime = PowerSavingController.this.getSaverSaveTimeValue() * 1000;
                        if (saverRemainTime != 0) {
                            Log.i(PowerSavingController.TAG, "set PROPERTY_PS_REMAIN_TIME = " + saverRemainTime);
                            SystemProperties.set(PowerSavingController.PROPERTY_PS_REMAIN_TIME, String.valueOf(saverRemainTime));
                        } else {
                            SystemProperties.set(PowerSavingController.PROPERTY_PS_REMAIN_TIME, "-1");
                        }
                        batteryLevel = ((Integer) msg.obj).intValue();
                        if (batteryLevel <= PowerSavingController.this.mSaverAppliedBatteryLevel - 5) {
                            Log.i(PowerSavingController.TAG, "batteryLevel = " + batteryLevel + ", mSaverAppliedBatteryLevel = " + PowerSavingController.this.mSaverAppliedBatteryLevel);
                            Log.i(PowerSavingController.TAG, "switch to Settings estimate value");
                            SystemProperties.set(PowerSavingController.PROPERTY_PS_REMAIN_TIME, "-1");
                        } else if (batteryLevel <= 2) {
                            Log.i(PowerSavingController.TAG, "batteryLevel = " + batteryLevel);
                            Log.i(PowerSavingController.TAG, "switch to Settings estimate value");
                            SystemProperties.set(PowerSavingController.PROPERTY_PS_REMAIN_TIME, "-1");
                        }
                        String psRemainTime = "-1";
                        try {
                            psRemainTime = SystemProperties.get(PowerSavingController.PROPERTY_PS_REMAIN_TIME, "-1");
                        } catch (Exception e) {
                            Log.e(PowerSavingController.TAG, "SystemProperties.get error");
                        }
                        Log.i(PowerSavingController.TAG, "PROPERTY_PS_REMAIN_TIME = " + psRemainTime);
                        return;
                    }
                    return;
                case 4:
                    PowerSavingController.this.initSaverSaveTimeInfo();
                    return;
                case 5:
                    try {
                        PowerSavingUtils.insertTimeStampToRestartServiceTable(PowerSavingController.this, "PSCS");
                        return;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return;
                    }
                case 6:
                    if (!PowerSavingUtils.isCharging(PowerSavingController.this.mContext) && PowerSavingController.this.getStateLocked() == -1 && PowerSavingController.this.getDisabledByPluginStatus(PowerSavingController.this.mContext)) {
                        Log.i(PowerSavingController.TAG, "handle DisabledByPlugin for last boot");
                        Log.i(PowerSavingController.TAG, "mTheLatestApplyEvent = " + PowerSavingController.this.mTheLatestApplyEvent);
                        String reason = "ACTION_POWER_DISCONNECTED";
                        if (PowerSavingController.this.mTheLatestApplyEvent.equals(LATEST_EVENT_EXTRA.MANUAL)) {
                            PowerSavingController.this.applyExtremeMode(reason);
                            PowerSavingController.this.mCanAutoRestore = false;
                        } else if (PowerSavingController.this.mTheLatestApplyEvent.equals(LATEST_EVENT_EXTRA.LOW_POWER)) {
                            if (PowerSavingController.this.mNowBatteryLevel < PowerSavingController.this.mNormalThreshold + 5) {
                                PowerSavingController.this.applyExtremeMode(reason);
                                PowerSavingController.this.mCanAutoRestore = true;
                            } else if (PowerSavingController.this.mPowerManger != null) {
                                Log.i(PowerSavingController.TAG, "disable Battery Saver");
                                PowerSavingController.this.mPowerManger.setPowerSaveMode(false);
                            }
                        } else if (PowerSavingController.this.mTimeScheduler != null && TimeScheduleUtils.isTimeScheduleEnabled(PowerSavingController.this.mContext)) {
                            if (PowerSavingController.this.mTimeScheduler.isTimeInterval()) {
                                PowerSavingController.this.applyExtremeMode(reason);
                                PowerSavingController.this.mCanAutoRestore = false;
                            } else if (PowerSavingController.this.mPowerManger != null) {
                                Log.i(PowerSavingController.TAG, "disable Battery Saver");
                                PowerSavingController.this.mPowerManger.setPowerSaveMode(false);
                            }
                        }
                        PowerSavingController.this.setDisabledByPluginStatus(PowerSavingController.this.mContext, false);
                        return;
                    }
                    PowerSavingController.this.handleTimeScheduleBootEvent();
                    return;
                default:
                    return;
            }
        }
    }

    public class ScreeOnHandler extends Handler {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(PowerSavingController.TAG, "[PowerSavingController]: ScreenOnHandler ");
            switch (msg.what) {
                case DCPARM.SCREENON_MESSAGE /*4002*/:
                    DataConnectionUtils.screenOnActionHandler(PowerSavingController.this.mContext);
                    return;
                default:
                    return;
            }
        }
    }

    public class ServiceBinder extends Binder {
        public PowerSavingController getService() {
            return PowerSavingController.this;
        }
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        Log.i(TAG, "Service onCreate");
        this.mScreeOnHandler = new ScreeOnHandler();
        this.mDataConnection = new DataConnection(this.mContext, 0, this.mScreeOnHandler);
        this.mSmartSwitch = new SmartSwitch(this.mContext, 0);
        this.bm = (BatteryManager) getSystemService("batterymanager");
        if (this.bm != null) {
            this.mNowBatteryLevel = this.bm.getIntProperty(4);
        }
        Log.i(TAG, "mNowBatteryLevel = " + this.mNowBatteryLevel);
        this.mPowerManger = (PowerManager) this.mContext.getSystemService("power");
        this.mTimeScheduler = new TimeScheduler(this.mContext);
        registerReceiver(this.mContext);
        this.stateChangeListenerList = new ArrayList();
        this.mHandlerThread = new HandlerThread("LowPowerHandlerThread");
        this.mHandlerThread.start();
        this.mLowPowerHandler = new LowPowerHandler(this.mHandlerThread.getLooper());
        this.mLowPowerHandler.obtainMessage(1).sendToTarget();
        this.mSaverAppliedBatteryLevel = -1;
        Log.i(TAG, "mSaverAppliedBatteryLevel = " + this.mSaverAppliedBatteryLevel);
        SystemProperties.set(PROPERTY_PS_REMAIN_TIME, "-1");
        this.mBatteryInfo = Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mLowPowerHandler.obtainMessage(4).sendToTarget();
        this.mLowPowerHandler.obtainMessage(6).sendToTarget();
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mContext = this;
        if (intent != null) {
            Log.i(TAG, "SetFunctionByOtherProcess");
            int enable = intent.getIntExtra(EXTRA.POWERSAVER_ENABLE, -1);
            if (enable != -1) {
                boolean isApplyLPM;
                if (enable == 1) {
                    isApplyLPM = true;
                } else {
                    isApplyLPM = false;
                }
                int mode = intent.getIntExtra(EXTRA_NAME.MODE, 0);
                String event = intent.getStringExtra(EXTRA_NAME.LATEST_EVENT);
                if (!TextUtils.isEmpty(event)) {
                    setTheLatestEvent(event);
                }
                if (isApplyLPM) {
                    setFunctionByOtherAPK(this.mContext, mode);
                    setTheLatestApplyEvent(event);
                } else {
                    applyInAactiveMode("SetFunctionByOtherProcess");
                    this.mCanAutoRestore = true;
                }
            }
            PowerSavingUtils.SetPreferencesStatus(this.mContext, PARM.KEY_PS_KEEP_MANUAL_ON, false);
        } else {
            this.mLowPowerHandler.sendEmptyMessage(5);
        }
        return 1;
    }

    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
        stopForeground(true);
        unregisterReceiver(this.mContext);
        this.mLowPowerHandler.getLooper().quitSafely();
        SystemProperties.set(PROPERTY_PS_REMAIN_TIME, "-1");
        Log.i(TAG, "switch to Settings estimate value");
        super.onDestroy();
    }

    private void startForeground(Context mContext) {
        String mTitle = mContext.getResources().getString(C0321R.string.fih_power_saving_enabled_notify_2);
        String mSummary = "";
        String mTicker = mTitle;
        String channelId = "channel_99";
        ((NotificationManager) getSystemService("notification")).createNotificationChannel(new NotificationChannel(channelId, getResources().getString(C0321R.string.fih_power_saving_enabled_notify_channel_name), 2));
        Builder builder = new Builder(mContext);
        builder.setSmallIcon(C0321R.drawable.ic_powersaver_powersaver);
        builder.setContentTitle(mTitle);
        builder.setColor(mContext.getColor(Resources.getSystem().getIdentifier("system_notification_accent_color", "color", "android")));
        builder.setContentText(getResources().getString(C0321R.string.fih_power_saving_enabled_notify_summary));
        builder.setTicker(mTicker);
        builder.setWhen(System.currentTimeMillis());
        builder.setOngoing(true);
        builder.setChannelId(channelId);
        String appName = mContext.getResources().getString(C0321R.string.fih_power_saving_power_saver_title_2);
        Bundle notificationBundle = new Bundle();
        notificationBundle.putString("android.substName", appName);
        builder.addExtras(notificationBundle);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(335544320);
        builder.setContentIntent(PendingIntent.getActivity(mContext, 0, intent, 0));
        startForeground(NOTIFICATION.PS_MODE, builder.build());
    }

    private void iniBaseFunction() {
        this.mBaseFunctions.add(new AutoSync(this.mContext));
        this.mBaseFunctions.add(new Glance(this.mContext));
        this.mBaseFunctions.add(new ScreenLight(this.mContext));
        this.mBaseFunctions.add(new ScreenTimeout(this.mContext));
        this.mBaseFunctions.add(new Animation(this.mContext));
        this.mBaseFunctions.add(new LimitBackgroundData(this.mContext));
        this.mBaseFunctions.add(new Vibrate(this.mContext));
        this.mBaseFunctions.add(new BatterySaver(this.mContext));
    }

    private void iniExtremeFuntion() {
        this.mExtremeFunctions.add(new GPS(this.mContext));
        this.mExtremeFunctions.add(new BlueTooth(this.mContext));
        this.mExtremeFunctions.add(new MobileData(this.mContext));
    }

    public void registerReceiver(Context ctx) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        ctx.registerReceiver(this.mPowerSavingControllerReceiver, filter);
    }

    public void unregisterReceiver(Context ctx) {
        try {
            if (this.mPowerSavingControllerReceiver != null) {
                ctx.unregisterReceiver(this.mPowerSavingControllerReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void applyNormalMode() {
        applyNormalMode("");
    }

    public void applyExtremeMode() {
        applyExtremeMode("");
    }

    public void applyInAactiveMode() {
        applyInAactiveMode("");
    }

    public void applyNormalMode(final String reason) {
        Log.i(TAG, "applyNormalMode");
        this.mLowPowerHandler.post(new Runnable() {
            public void run() {
                if (PowerSavingController.this.getStateLocked() != 0) {
                    PowerSavingController.this.setStateLocked(0);
                    PowerSavingController.this.stepState(reason);
                    PowerSavingController.this.ShowMessage(PowerSavingController.this.getResources().getString(C0321R.string.fih_power_saving_mode_is_running_2));
                    PowerSavingController.this.notifyAllListener();
                }
            }
        });
    }

    public void applyExtremeMode(final String reason) {
        Log.i(TAG, "applyExtremeMode");
        this.mLowPowerHandler.post(new Runnable() {
            public void run() {
                if (PowerSavingController.this.getStateLocked() != 1) {
                    PowerSavingController.this.setStateLocked(1);
                    PowerSavingController.this.stepState(reason);
                    PowerSavingController.this.ShowMessage(PowerSavingController.this.getResources().getString(C0321R.string.fih_power_saving_mode_is_running_2));
                    PowerSavingController.this.notifyAllListener();
                    PowerSavingController.this.mSaverAppliedBatteryLevel = PowerSavingController.this.mNowBatteryLevel;
                    Log.i(PowerSavingController.TAG, "mSaverAppliedBatteryLevel = " + PowerSavingController.this.mSaverAppliedBatteryLevel);
                    PowerSavingController.this.mLowPowerHandler.obtainMessage(3, Integer.valueOf(PowerSavingController.this.mNowBatteryLevel)).sendToTarget();
                }
            }
        });
    }

    public void applyInAactiveMode(final String reason) {
        Log.i(TAG, "applyInAactiveMode");
        this.mLowPowerHandler.post(new Runnable() {
            public void run() {
                if (PowerSavingController.this.getStateLocked() != -1) {
                    PowerSavingController.this.setStateLocked(-1);
                    PowerSavingController.this.stepState(reason);
                    PowerSavingController.this.notifyAllListener();
                    PowerSavingController.this.mSaverAppliedBatteryLevel = -1;
                    Log.i(PowerSavingController.TAG, "mSaverAppliedBatteryLevel = " + PowerSavingController.this.mSaverAppliedBatteryLevel);
                    SystemProperties.set(PowerSavingController.PROPERTY_PS_REMAIN_TIME, "-1");
                }
            }
        });
    }

    public int getCurentMode() {
        return getStateLocked();
    }

    private void handleDisableBatterySaver() {
        this.mLowPowerHandler.postDelayed(new C03885(), 500);
    }

    private void setDisabledByPluginStatus(Context context, boolean value) {
        PowerSavingUtils.SetPreferencesStatus(context, this.KEY_DISABLED_BY_PLUGIN, value);
    }

    private boolean getDisabledByPluginStatus(Context context) {
        return PowerSavingUtils.GetPreferencesStatus(context, this.KEY_DISABLED_BY_PLUGIN);
    }

    private String getStateString(int state) {
        String ret = "";
        switch (state) {
            case -1:
                return "Inactive";
            case 0:
                return "Normal";
            case 1:
                return "Extreme";
            default:
                return "unKnown";
        }
    }

    private synchronized void setStateLocked(int state) {
        this.mState = state;
        saveStateToDB(this.mState);
    }

    private synchronized int getStateLocked() {
        return this.mState;
    }

    private void stepState(String reason) {
        sendIntentNotifyIsStillSetting(this.mContext, true);
        Log.i(TAG, "stepStateLocked: mState= " + getStateString(getStateLocked()) + ", reason = " + reason);
        switch (getStateLocked()) {
            case -1:
                if (this.misRegisterDataConnectionRec) {
                    this.mDataConnection.unregisterReceiver(this.mContext);
                    this.misRegisterDataConnectionRec = false;
                }
                if (this.misRegisterSmartSwitchRec) {
                    this.mSmartSwitch.unregisterReceiver(this.mContext);
                    this.misRegisterSmartSwitchRec = false;
                }
                stopForeground(true);
                if (this.misCloseNormalFunction) {
                    restoreNormalModeFunctions();
                    this.misCloseNormalFunction = false;
                }
                if (this.misCloseExtremeFunction) {
                    restoreExtremeModeFunctions();
                    this.misCloseExtremeFunction = false;
                }
                PowerSavingUtils.SetPowerSavingModeEnable(this.mContext, false);
                break;
            case 0:
                if (!this.misRegisterDataConnectionRec) {
                    this.mDataConnection.registerReceiver(this.mContext);
                    this.misRegisterDataConnectionRec = true;
                }
                if (!this.misRegisterSmartSwitchRec) {
                    this.mSmartSwitch.registerReceiver(this.mContext);
                    this.misRegisterSmartSwitchRec = true;
                }
                if (!this.misCloseNormalFunction) {
                    closeNormalModeFunctions();
                    this.misCloseNormalFunction = true;
                }
                if (this.misCloseExtremeFunction) {
                    restoreExtremeModeFunctions();
                    this.misCloseExtremeFunction = false;
                }
                startForeground(this.mContext);
                PowerSavingUtils.SetPowerSavingModeEnable(this.mContext, true);
                break;
            case 1:
                if (!this.misRegisterDataConnectionRec) {
                    this.mDataConnection.registerReceiver(this.mContext);
                    this.misRegisterDataConnectionRec = true;
                }
                if (!this.misRegisterSmartSwitchRec) {
                    this.mSmartSwitch.registerReceiver(this.mContext);
                    this.misRegisterSmartSwitchRec = true;
                }
                if (!this.misCloseExtremeFunction) {
                    closeExtremeModeFunctions();
                    this.misCloseExtremeFunction = true;
                }
                if (!this.misCloseNormalFunction) {
                    closeNormalModeFunctions();
                    this.misCloseNormalFunction = true;
                }
                startForeground(this.mContext);
                PowerSavingUtils.SetPowerSavingModeEnable(this.mContext, true);
                break;
        }
        sendIntentNotifyIsStillSetting(this.mContext, false);
    }

    private void closeNormalModeFunctions() {
        for (Function function : this.mBaseFunctions) {
            function.saveCurrentStateToBackUpFile();
            function.close();
        }
    }

    private void closeExtremeModeFunctions() {
        for (Function function : this.mExtremeFunctions) {
            function.saveCurrentStateToBackUpFile();
            function.close();
        }
    }

    private void restoreNormalModeFunctions() {
        for (Function function : this.mBaseFunctions) {
            function.restore();
        }
    }

    private void restoreExtremeModeFunctions() {
        for (Function function : this.mExtremeFunctions) {
            function.restore();
        }
    }

    private void sendIntentNotifyIsStillSetting(Context context, boolean status) {
        Log.i(TAG, "sendIntentNotifyIsStillSetting , status = " + status);
        Intent noticeIntent = new Intent(ACTION.ACTION_LPM_STILL_SETTING);
        noticeIntent.putExtra(INTENT.EXTRA.LPM_STILL_SETTING, status);
        context.sendBroadcastAsUser(noticeIntent, this.mUserHandle);
    }

    private void setFunctionByOtherAPK(Context mContext, int mode) {
        String reason = "set by other process";
        if (mode == 0) {
            applyNormalMode(reason);
            this.mCanAutoRestore = false;
        } else if (mode == 1) {
            applyExtremeMode(reason);
            this.mCanAutoRestore = false;
        }
    }

    private int getStateFromDB() {
        String strState = PowerSavingUtils.getStringItemFromSelfDB(this.mContext, "powersaving_db_power_saving_mode");
        int istate = -1;
        if (!TextUtils.isEmpty(strState)) {
            try {
                istate = Integer.parseInt(strState);
            } catch (Exception ex) {
                ex.printStackTrace();
                istate = -1;
            }
        }
        Log.i(TAG, "get istate from DB : " + istate);
        return istate;
    }

    private void saveStateToDB(int state) {
        Log.i(TAG, "saveStateToDB " + Integer.toString(state));
        PowerSavingUtils.setStringItemToSelfDB(this.mContext, "powersaving_db_power_saving_mode", Integer.toString(state));
        if (state != -1) {
            PowerSavingUtils.setPowerSavingModeUiStatus(this.mContext, state);
        }
    }

    private void init() {
        Log.i(TAG, "init parameter");
        this.misRegisterDataConnectionRec = false;
        this.misRegisterSmartSwitchRec = false;
        int state = getStateFromDB();
        Log.i(TAG, "getStateFromDB = " + state);
        setStateLocked(state);
        if (state == 0) {
            this.misCloseNormalFunction = true;
            this.misCloseExtremeFunction = false;
            this.mCanAutoRestore = false;
            for (Function function : this.mBaseFunctions) {
                function.bootHandling(state);
            }
        } else if (state == 1) {
            this.misCloseNormalFunction = true;
            this.misCloseExtremeFunction = true;
            this.mCanAutoRestore = false;
            for (Function function2 : this.mBaseFunctions) {
                function2.bootHandling(state);
            }
            for (Function function22 : this.mExtremeFunctions) {
                function22.bootHandling(state);
            }
        } else {
            this.misCloseNormalFunction = false;
            this.misCloseExtremeFunction = false;
            this.mCanAutoRestore = true;
        }
        String latestEvent = getTheLatestEventFromDB();
        if (TextUtils.isEmpty(latestEvent)) {
            setTheLatestEvent(this.mTheLatestEvent);
        } else {
            this.mTheLatestEvent = latestEvent;
        }
        String latestApplyEvent = getTheLatestApplyEventFromDB();
        if (TextUtils.isEmpty(latestApplyEvent)) {
            setTheLatestApplyEvent(this.mTheLatestApplyEvent);
        } else {
            this.mTheLatestApplyEvent = latestApplyEvent;
        }
    }

    private void setTheLatestEvent(String eventName) {
        this.mTheLatestEvent = eventName;
        saveTheLatestEventToDB(this.mTheLatestEvent);
    }

    private void saveTheLatestEventToDB(String eventName) {
        PowerSavingUtils.setStringItemToSelfDB(this.mContext, THE_LATEST_EVENT_KEY, eventName);
        getTheLatestEventFromDB();
    }

    private String getTheLatestEventFromDB() {
        String ret = PowerSavingUtils.getStringItemFromSelfDB(this.mContext, THE_LATEST_EVENT_KEY);
        Log.i(TAG, "getTheLatestEventFromDB() " + ret);
        return ret;
    }

    private void setTheLatestApplyEvent(String eventName) {
        this.mTheLatestApplyEvent = eventName;
        saveTheLatestApplyEventToDB(this.mTheLatestApplyEvent);
    }

    private void saveTheLatestApplyEventToDB(String eventName) {
        PowerSavingUtils.setStringItemToSelfDB(this.mContext, THE_LATEST_APPLY_EVENT_KEY, eventName);
    }

    private String getTheLatestApplyEventFromDB() {
        String ret = PowerSavingUtils.getStringItemFromSelfDB(this.mContext, THE_LATEST_APPLY_EVENT_KEY);
        Log.i(TAG, "getTheLatestApplyEventFromDB() " + ret);
        return ret;
    }

    private void ShowMessage(final String message) {
        this.mLowPowerHandler.postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(PowerSavingController.this.mContext, message, 0).show();
            }
        }, 1000);
    }

    public synchronized void registerStateChangeListener(IStateChangeListener listener) {
        this.stateChangeListenerList.add(listener);
    }

    public synchronized void unregisterStateChangeListener(IStateChangeListener listener) {
        this.stateChangeListenerList.remove(listener);
    }

    private synchronized void notifyAllListener() {
        for (IStateChangeListener listener : this.stateChangeListenerList) {
            if (listener != null) {
                try {
                    listener.onChange(getStateLocked());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private long getSaverSaveTimeValue() {
        long extraTime = 0;
        try {
            if (this.mBatteryInfo.computeBatteryTimeRemaining() != -1) {
                int batteryLevel = this.bm.getIntProperty(4);
                int savingTimeLevel = (batteryLevel - 1) / 5;
                if (batteryLevel > 0) {
                    int mode = -1;
                    String str = PowerSavingUtils.getStringItemFromSelfDB(this, "powersaving_db_power_saving_mode");
                    if (str != null) {
                        mode = Integer.valueOf(str).intValue();
                    }
                    if (mode == 1 && this.mExtremeModeSaveTimeList.size() > 0 && this.mExtremeModeSaveTimeList.size() <= this.SAVINGTIME_ARRAY_AMOUNT) {
                        Log.i(TAG, "mtotalSaveTimeInExtremeMode = " + this.mtotalSaveTimeInExtremeMode);
                        Log.i(TAG, "( 5 - batteryLevel % 5) * mExtremeModeSaveTimeList.get(savingTimeLevel)  = " + (((long) (5 - (batteryLevel % 5))) * ((Long) this.mExtremeModeSaveTimeList.get(savingTimeLevel)).longValue()));
                        extraTime = this.mtotalSaveTimeInExtremeMode.longValue() - (((Long) this.mExtremeModeSaveTimeList.get(savingTimeLevel)).longValue() * ((long) (5 - (batteryLevel % 5))));
                        for (int i = this.mExtremeModeSaveTimeList.size() - 1; i > savingTimeLevel; i--) {
                            extraTime -= ((Long) this.mExtremeModeSaveTimeList.get(i)).longValue() * 5;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.i(TAG, "extraTime = " + extraTime);
        return extraTime;
    }

    private void initSaverSaveTimeInfo() {
        String[] extremeModeSaveTimeArray = PowerSavingUtils.getExtremeModeSaveTimeList();
        if (extremeModeSaveTimeArray != null) {
            for (String saveTime : extremeModeSaveTimeArray) {
                try {
                    long iTime = Long.parseLong(saveTime);
                    this.mtotalSaveTimeInExtremeMode = Long.valueOf(this.mtotalSaveTimeInExtremeMode.longValue() + (5 * iTime));
                    this.mExtremeModeSaveTimeList.add(Long.valueOf(iTime));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void handleTimeScheduleBootEvent() {
        this.mLowPowerHandler.postDelayed(new C03907(), 500);
    }

    public void updateEventStatusForQS() {
        Log.i(TAG, "updateEventStatusForQS");
        setTheLatestEvent(LATEST_EVENT_EXTRA.MANUAL);
        this.mCanAutoRestore = false;
        PowerSavingUtils.SetPreferencesStatus(this.mContext, PARM.KEY_PS_KEEP_MANUAL_ON, false);
    }

    public void updateApplyEventStatusForOtherUI(String eventName) {
        Log.i(TAG, "updateApplyEventStatusForOtherUI eventName = " + eventName);
        setTheLatestApplyEvent(eventName);
    }
}
