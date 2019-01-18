package com.evenwell.powersaving.g3.background;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.Display;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.element.LimitedSizeList;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.exception.PowerSaverExceptionAppInfoItem;
import com.evenwell.powersaving.g3.p000e.doze.DozeStatus;
import com.evenwell.powersaving.g3.p000e.doze.EDozeService;
import com.evenwell.powersaving.g3.p000e.doze.function.SyncAdapter;
import com.evenwell.powersaving.g3.provider.BackDataDb;
import com.evenwell.powersaving.g3.utils.AlarmUtils;
import com.evenwell.powersaving.g3.utils.AlarmUtils.Alarm;
import com.evenwell.powersaving.g3.utils.PSConst.PACKAGE_NAME;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.evenwell.powersaving.g3.utils.TimeUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BackgroundCleanService extends Service {
    private static final long HALF_HOUR = 1800000;
    private static final int MSG_INPUT_METHOD_CHANGED = 4;
    private static final int MSG_ON_START_CMD = 3;
    private static final int MSG_SCREEN_OFF = 1;
    private static final int MSG_SCREEN_ON = 0;
    private static final int MSG_SERVICE_IS_RESTARTED = 5;
    private static final int MSG_SHUT_DOWN = 2;
    private static final long ONE_HOUR = 3600000;
    private static final long ONE_MIN = 60000;
    private static final long ONE_SEC = 1000;
    private static final String TAG = "[PowerSavingAppG3]BackgroundCleanService";
    private static final int TIME_LIMIT_FOR_RECENT_APP_LOCK = 10;
    private boolean DBG = true;
    private boolean isCN;
    private boolean isRegisterReceivers;
    private ActivityManager mAm;
    private AudioManager mAudioManager;
    private int mAutoWakeupCount = 0;
    private Context mContext;
    private String mCurrentInputMethod;
    private BackDataDb mDB;
    private long mDelayTimeToClear = 30000;
    private final DisplayListener mDisplayListener = new C03221();
    private DisplayManager mDisplayManager;
    private DozeStatus mDozeStauts;
    private List<String> mExemptPrefix;
    private Handler mHandlerThread;
    private InputMethodReceiver mInputMethodReceiver;
    private HashMap<String, Long> mPkgRxMap = new HashMap();
    private PackageManager mPm;
    private IProcessObserver mProcessObserver = new C03232();
    private long mRepeatPeriod = 600000;
    private long mRepeatPeriodInDeepDoze = HALF_HOUR;
    private ScreenReceiver mScreenReceiver;
    private SyncAdapter mSyncAdapter;
    private List<String> mTempWhitelist = new ArrayList();
    private LimitedSizeList<String> mTopApList;
    private List<String> mWhiteList;
    private boolean misAppInFocusExist;
    private boolean misDeepDoze;
    private boolean misLightDoze;

    /* renamed from: com.evenwell.powersaving.g3.background.BackgroundCleanService$1 */
    class C03221 implements DisplayListener {
        C03221() {
        }

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                Display curDisplay = BackgroundCleanService.this.mDisplayManager.getDisplay(0);
                if (curDisplay.getState() == 2) {
                    Log.i(BackgroundCleanService.TAG, "onDisplayChanged  Display.STATE_ON");
                    BackgroundCleanService.this.mHandlerThread.obtainMessage(0).sendToTarget();
                } else if (curDisplay.getState() == 1) {
                    Log.i(BackgroundCleanService.TAG, "onDisplayChanged  Display.STATE_OFF");
                    BackgroundCleanService.this.mHandlerThread.obtainMessage(1).sendToTarget();
                }
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.background.BackgroundCleanService$2 */
    class C03232 extends Stub {
        C03232() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                try {
                    String calleeApp = PSUtils.getProcessNameByPID(BackgroundCleanService.this, pid);
                    if (BackgroundCleanService.this.DBG) {
                        Log.i(BackgroundCleanService.TAG, "pid: " + pid + ", calleeApp: " + calleeApp);
                    }
                    if (TextUtils.indexOf(calleeApp, ':') > 0) {
                        if (BackgroundCleanService.this.DBG) {
                            Log.i(BackgroundCleanService.TAG, "ignore sub-process, calleeApp: " + calleeApp);
                        }
                    } else if (PowerSavingUtils.isLauncherAP(BackgroundCleanService.this, calleeApp)) {
                        if (BackgroundCleanService.this.DBG) {
                            Log.i(BackgroundCleanService.TAG, calleeApp + " add to mTopApList.");
                        }
                        BackgroundCleanService.this.mTopApList.add(calleeApp);
                    } else if (BackgroundCleanService.this.DBG) {
                        Log.i(BackgroundCleanService.TAG, calleeApp + " is not launcher app.");
                    }
                } catch (Exception e) {
                    Log.e(BackgroundCleanService.TAG, "Can't get running App process info", e);
                }
            }
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }

        public void onProcessDied(int pid, int uid) {
        }
    }

    public class InputMethodReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Object obj = -1;
            switch (action.hashCode()) {
                case -873536848:
                    if (action.equals("android.intent.action.INPUT_METHOD_CHANGED")) {
                        obj = null;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    BackgroundCleanService.this.mHandlerThread.obtainMessage(4).sendToTarget();
                    return;
                default:
                    return;
            }
        }
    }

    public class ScreenReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BackgroundCleanService.this.DBG) {
                Log.d(BackgroundCleanService.TAG, " ScreenReceiver onReceive : " + action);
            }
            Object obj = -1;
            switch (action.hashCode()) {
                case 1947666138:
                    if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
                        obj = null;
                        break;
                    }
                    break;
            }
            switch (obj) {
                case null:
                    if (BackgroundCleanService.this.DBG) {
                        Log.i(BackgroundCleanService.TAG, "Receive ACTION_SHUTDOWN");
                    }
                    BackgroundCleanService.this.mHandlerThread.obtainMessage(2).sendToTarget();
                    return;
                default:
                    return;
            }
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        if (this.DBG) {
            Log.d(TAG, "[onCreate]");
        }
        this.mContext = this;
        this.isCN = BackgroundPolicyExecutor.getInstance(this.mContext).isCNModel();
        this.mPm = this.mContext.getPackageManager();
        this.mAm = (ActivityManager) this.mContext.getSystemService("activity");
        this.mScreenReceiver = new ScreenReceiver();
        this.mInputMethodReceiver = new InputMethodReceiver();
        this.isRegisterReceivers = false;
        if (BackgroundPolicyExecutor.getInstance(this).isCNModel()) {
            this.mTopApList = new LimitedSizeList(2);
        } else {
            this.mTopApList = new LimitedSizeList(3);
        }
        HandlerThread handlerThread = new HandlerThread("BCSHT");
        handlerThread.start();
        this.mHandlerThread = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        BackgroundCleanService.this.cancelAlarmBgRunService();
                        try {
                            BackgroundCleanService.this.recoverBAMModeSetting(BackgroundCleanService.this.mContext);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    case 1:
                        String defaultLauncher = BackgroundCleanUtil.getDefaultLauncher(BackgroundCleanService.this.mPm);
                        String oldLauncher = BackgroundCleanUtil.getCurrentLaunherName(BackgroundCleanService.this.mContext);
                        if (BackgroundCleanService.this.DBG) {
                            Log.d(BackgroundCleanService.TAG, " ScreenReceiver defaultLauncher : " + defaultLauncher + " oldLauncher : " + oldLauncher);
                        }
                        if (!oldLauncher.equals(defaultLauncher)) {
                            BackgroundCleanUtil.setCurrentLaunherName(BackgroundCleanService.this.mContext, defaultLauncher);
                            BackgroundCleanUtil.updateWidgetIfLauncherChanged(BackgroundCleanService.this.mContext, defaultLauncher);
                        }
                        BackgroundCleanService.this.setAlarmBgRunService(BackgroundCleanService.this.mDelayTimeToClear);
                        return;
                    case 2:
                        PowerSavingUtils.SetPreferencesStatus(BackgroundCleanService.this.mContext, PSSPREF.IS_BOOT_COMPLETE, false);
                        return;
                    case 3:
                        Intent intent = msg.obj;
                        BackgroundCleanService.this.refreshList();
                        BackgroundCleanService.this.mCurrentInputMethod = BackgroundCleanUtil.getDefaultInputMethod(BackgroundCleanService.this);
                        if (!(BackgroundCleanService.this.mWhiteList == null || BackgroundCleanService.this.mWhiteList.contains(BackgroundCleanService.this.mCurrentInputMethod))) {
                            BackgroundCleanService.this.mWhiteList.add(BackgroundCleanService.this.mCurrentInputMethod);
                            BackgroundPolicyExecutor.getInstance(BackgroundCleanService.this).addAppToWhiteList(BackgroundCleanService.this.mCurrentInputMethod);
                        }
                        if (!(intent == null || intent.getAction() == null || !intent.getAction().equals(BackgroundCleanUtil.ACTION_BACK_CLEAN_START))) {
                            BackgroundCleanService.this.checkIsInLightDoze();
                            BackgroundCleanService.this.checkIsInDeepDoze();
                            if (BackgroundCleanService.this.misDeepDoze && PSUtils.enableTestFunction() && TimeUtil.getInstance().isInTimeInteveralToSendIntent()) {
                                BackgroundCleanService.this.sendIntentToEDozeService();
                            }
                            if (BackgroundCleanService.this.misDeepDoze) {
                                try {
                                    BackgroundCleanService.this.clearAppInDeepDoze();
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                BackgroundCleanService.this.clearAllRunningApps();
                            }
                            if (BMS.getInstance(BackgroundCleanService.this.mContext).getBMSValue()) {
                                Log.d(BackgroundCleanService.TAG, "Don't allow app in disauto black list app to do autosync");
                                BackgroundCleanService.this.mSyncAdapter.setAccountListAutoSyncDisabled(BackgroundCleanService.this);
                            }
                            BackgroundCleanService.this.autoWakeupBgRunService();
                        }
                        BackgroundCleanService.this.registerReceivers();
                        return;
                    case 4:
                        Log.d(BackgroundCleanService.TAG, "MSG_INPUT_METHOD_CHANGED");
                        String defaultInputMethod = BackgroundCleanUtil.getDefaultInputMethod(BackgroundCleanService.this.mContext);
                        BackgroundCleanService.this.refreshList();
                        if (!BackgroundCleanService.this.mWhiteList.contains(defaultInputMethod)) {
                            BackgroundCleanService.this.mWhiteList.add(defaultInputMethod);
                        }
                        if (BackgroundPolicyExecutor.getInstance(BackgroundCleanService.this.mContext).isInDisautoList(defaultInputMethod)) {
                            BackgroundPolicyExecutor.getInstance(BackgroundCleanService.this.mContext).removeAppFromDisAutoList(defaultInputMethod);
                            if (BackgroundCleanService.this.isCN) {
                                BackgroundPolicyExecutor.getInstance(BackgroundCleanService.this.mContext).addAppToDozeWhiteList(defaultInputMethod);
                            }
                        }
                        Log.i(BackgroundCleanService.TAG, "addAppToWhiteList " + defaultInputMethod);
                        BackgroundPolicyExecutor.getInstance(BackgroundCleanService.this.mContext).addAppToWhiteList(defaultInputMethod);
                        BackgroundCleanService.this.mCurrentInputMethod = defaultInputMethod;
                        return;
                    case 5:
                        try {
                            BackgroundCleanService.this.mDB.insertTimeStampToServiceRestartTable("BC");
                            return;
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            return;
                        }
                    default:
                        return;
                }
            }
        };
        this.mDozeStauts = new DozeStatus();
        this.misDeepDoze = false;
        this.misLightDoze = false;
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (RemoteException e) {
            Log.e(TAG, "Can't call IActivityManager.registerProcessObserver", e);
        }
        this.mSyncAdapter = new SyncAdapter(this);
        this.mDisplayManager = (DisplayManager) getSystemService("display");
        this.mDB = new BackDataDb(this);
        this.mAudioManager = (AudioManager) getSystemService("audio");
        this.misAppInFocusExist = BackgroundCleanUtil.hasMethod(this.mAudioManager.getClass(), "isAppInFocus", String.class);
        this.mExemptPrefix = BackgroundPolicyExecutor.getInstance(this).getExemptPrefix();
        try {
            recoverBAMModeSetting(this.mContext);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");
        if (intent == null) {
            this.mHandlerThread.sendEmptyMessage(5);
        }
        this.mHandlerThread.obtainMessage(3, intent).sendToTarget();
        return 1;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.isRegisterReceivers) {
            try {
                unregisterReceiver(this.mScreenReceiver);
                unregisterReceiver(this.mInputMethodReceiver);
                try {
                    this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
            this.isRegisterReceivers = false;
        }
        cancelAlarmBgRunService();
        this.mHandlerThread.getLooper().quitSafely();
        try {
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (RemoteException e3) {
            Log.e(TAG, "Can't call IActivityManager.unregisterProcessObserver", e3);
        }
        this.mDB.close();
        try {
            recoverBAMModeSetting(this.mContext);
        } catch (Exception e4) {
            e4.printStackTrace();
        }
    }

    private ArrayList<String> clearAllRunningApps() {
        ArrayList<String> stopApps = new ArrayList();
        BAMMode bamMode = new BAMMode(this.mContext);
        boolean cta = PSUtils.isCTA(this.mContext);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",Ignore BAM Cleaner.");
        } else {
            Log.i(TAG, "clearAllRunningApps mAutoWakeupCount = " + this.mAutoWakeupCount);
            if (BackgroundCleanUtil.isPhoneCalling(this.mContext)) {
                Log.i(TAG, "Ignore all process as phone is in calling...");
            } else {
                String pkgName;
                if (this.DBG) {
                    Log.d(TAG, "***********background clean start***********");
                }
                refreshList();
                List<RecentTaskInfo> recentList = BackgroundCleanUtil.getRecentList(this.mAm);
                List<RunningTaskInfo> runningTasks = this.mAm.getRunningTasks(100);
                List<String> targetList = new ArrayList();
                List<String> launcherApps = PowerSavingUtils.getLauncherApList(this.mContext);
                for (String pkg : getAllRunningPkgs()) {
                    if (!targetList.contains(pkg) && launcherApps.contains(pkg)) {
                        targetList.add(pkg);
                    }
                }
                Iterator it = this.mTopApList.iterator();
                while (it.hasNext()) {
                    pkgName = (String) it.next();
                    if (!targetList.contains(pkgName) && launcherApps.contains(pkgName)) {
                        targetList.add(pkgName);
                    }
                }
                for (RunningTaskInfo runningTask : runningTasks) {
                    pkgName = runningTask.baseActivity.getPackageName();
                    if (this.DBG) {
                        Log.d(TAG, "RunningTasks: " + pkgName);
                    }
                }
                for (RecentTaskInfo recentInfo : recentList) {
                    pkgName = recentInfo.baseIntent.getComponent().getPackageName();
                    if (this.DBG) {
                        Log.d(TAG, "RecentTasks: " + pkgName);
                    }
                }
                it = this.mTopApList.iterator();
                while (it.hasNext()) {
                    pkgName = (String) it.next();
                    if (this.DBG) {
                        Log.d(TAG, "mTopApList: " + pkgName);
                    }
                }
                for (String pkgName2 : this.mTempWhitelist) {
                    if (this.DBG) {
                        Log.d(TAG, "mTempWhitelist: " + pkgName2);
                    }
                }
                List<String> alarmPkgInLightDoze = PowerSavingUtils.getAlarmRecordsInDoze(this.mContext);
                if (this.misLightDoze) {
                    for (String app : alarmPkgInLightDoze) {
                        Log.d(TAG, "alarmPkgInLightDoze : " + app);
                    }
                    targetList.addAll(alarmPkgInLightDoze);
                    PowerSavingUtils.clearAlarmRecordsInDoze(this.mContext);
                }
                String defaultLauncher = BackgroundCleanUtil.getDefaultLauncher(this.mContext.getPackageManager());
                List<String> widgetPkg = BackgroundCleanUtil.getWidgetPackageName(defaultLauncher);
                String defaultInput = BackgroundCleanUtil.getDefaultInputMethod(this.mContext);
                if (this.DBG) {
                    Log.i(TAG, "targetList.size() = " + targetList.size());
                }
                if (targetList != null) {
                    for (String pkgName22 : targetList) {
                        long timediff = 0;
                        UsageStats usageStats = BackgroundCleanUtil.getAppUsageStats(this, pkgName22);
                        if (usageStats != null) {
                            long currentTime = System.currentTimeMillis();
                            Log.d(TAG, "Package Name = " + usageStats.getPackageName());
                            String str = TAG;
                            String str2 = str;
                            Log.d(str2, "getLastTimeUsed = " + DateUtils.formatSameDayTime(usageStats.getLastTimeUsed(), System.currentTimeMillis(), 2, 2) + " (" + usageStats.getLastTimeUsed() + ")");
                            timediff = ((currentTime - usageStats.getLastTimeUsed()) / ONE_SEC) / 60;
                            Log.d(TAG, "time diff (min) = " + timediff);
                        }
                        if (this.DBG) {
                            Log.d(TAG, "target PackageName---" + pkgName22);
                        }
                        if (!stopApps.contains(pkgName22)) {
                            if (bamMode.getMode() == 3) {
                                boolean find = false;
                                for (PowerSaverExceptionAppInfoItem apps : BackgroundPolicyExecutor.getInstance(this.mContext).getAllApList(false)) {
                                    if (apps.GetPackageName().equals(pkgName22)) {
                                        find = true;
                                        break;
                                    }
                                }
                                if (!find) {
                                    if (this.DBG) {
                                        Log.d(TAG, "Ignore: pkgName can not be handled ---" + pkgName22);
                                    }
                                }
                            } else if (this.mWhiteList.contains(pkgName22)) {
                                if (this.DBG) {
                                    Log.d(TAG, "Ignore: pkgName is in white list---" + pkgName22);
                                }
                            }
                            for (String prefix : this.mExemptPrefix) {
                                if (pkgName22.contains(prefix) && this.DBG) {
                                    Log.d(TAG, "Ignore: " + pkgName22 + " has prefix ---" + prefix);
                                }
                            }
                            if (this.mTempWhitelist.contains(pkgName22)) {
                                boolean isInRecent = false;
                                for (RecentTaskInfo recentInfo2 : recentList) {
                                    if (recentInfo2.baseIntent.getComponent().getPackageName().equals(pkgName22)) {
                                        isInRecent = true;
                                    }
                                }
                                if (isInRecent) {
                                    Log.d(TAG, pkgName22 + " is in temp white list");
                                    if (timediff <= 10) {
                                        Log.d(TAG, "ignore : " + timediff + " <= " + 10 + "---" + pkgName22);
                                    } else {
                                        Log.d(TAG, timediff + " > " + 10 + "---" + pkgName22);
                                    }
                                } else {
                                    Log.d(TAG, "ignore : " + pkgName22 + " is in temp white list but not in recent list");
                                }
                            }
                            if (pkgName22.equals(defaultLauncher)) {
                                if (this.DBG) {
                                    Log.d(TAG, "Ignore: default launcher---" + pkgName22);
                                }
                            } else if (pkgName22.equals(defaultInput)) {
                                if (this.DBG) {
                                    Log.d(TAG, "Ignore: default IME---" + pkgName22);
                                }
                            } else if (this.mAutoWakeupCount > 1 || !this.mTopApList.contains(pkgName22)) {
                                if (widgetPkg.contains(pkgName22)) {
                                    if (this.DBG) {
                                        Log.d(TAG, "Ignore: Widget is attached---" + pkgName22);
                                    }
                                } else if (!checkPersistTask(pkgName22)) {
                                    if (BackgroundCleanUtil.isAppMonitoringLocation(this.mContext, pkgName22)) {
                                        boolean isPkgInRecentList = false;
                                        for (RecentTaskInfo recentInfo22 : recentList) {
                                            if (recentInfo22.baseIntent.getComponent().getPackageName().equals(pkgName22)) {
                                                isPkgInRecentList = true;
                                                break;
                                            }
                                        }
                                        if (isPkgInRecentList) {
                                            if (this.DBG) {
                                                Log.d(TAG, "pkgName:" + pkgName22 + " is locating and is in recent list , continue");
                                            }
                                        }
                                    }
                                    if (!BackgroundCleanUtil.isMediaRouteProviderServiceApp(this.mPm, pkgName22)) {
                                        if (bamMode.process(pkgName22)) {
                                            if (this.DBG) {
                                                Log.d(TAG, bamMode.modeForLog(pkgName22));
                                            }
                                            stopApps.add(pkgName22);
                                        }
                                        for (String pkg2 : BackgroundCleanUtil.getSameUserIdPkgs(this.mPm, pkgName22)) {
                                            if (bamMode.process(pkgName22) && !stopApps.contains(pkg2)) {
                                                if (this.DBG) {
                                                    Log.d(TAG, bamMode.modeForLog(pkgName22));
                                                }
                                                stopApps.add(pkg2);
                                            }
                                        }
                                    } else if (this.DBG) {
                                        Log.d(TAG, "Ignore " + pkgName22 + " is using MediaRouteProviderService");
                                    }
                                }
                            } else if (this.DBG) {
                                Log.d(TAG, "Ignore: top " + this.mTopApList.size() + " of running tasks---" + pkgName22);
                            }
                        }
                    }
                }
                if (stopApps.size() > 0 && this.DBG) {
                    Log.d(TAG, "total clean: " + stopApps.size() + " apps");
                }
                if (this.DBG) {
                    Log.d(TAG, "***********background clean stop***********");
                }
                it = stopApps.iterator();
                while (it.hasNext()) {
                    String stopApp = (String) it.next();
                    try {
                        if (alarmPkgInLightDoze.contains(stopApp)) {
                            this.mDB.insertProcessWasForceStopped(stopApp + bamMode.modeForDatabase("m", stopApp));
                        } else {
                            this.mDB.insertProcessWasForceStopped(stopApp + bamMode.modeForDatabase("", stopApp));
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return stopApps;
    }

    private boolean checkPersistTask(String pkgName) {
        List<Integer> importances = BackgroundCleanUtil.getPkgImportancesofAllProcesses(this.mAm, pkgName);
        Log.d(TAG, pkgName + " importances " + importances);
        if (importances.contains(Integer.valueOf(125))) {
            if ("com.nbc.music".equals(pkgName)) {
                if (!this.DBG) {
                    return true;
                }
                Log.d(TAG, "com.nbc.music  is running foreground Service , continue");
                return true;
            } else if (!this.misAppInFocusExist) {
                return true;
            } else {
                if (this.mAudioManager.isAppInFocus(pkgName)) {
                    if (!this.DBG) {
                        return true;
                    }
                    Log.d(TAG, "pkgName:" + pkgName + " is running foreground Service and holding audio focus, continue");
                    return true;
                }
            }
        }
        if (BackgroundCleanUtil.isAppDownloading(this.mContext, pkgName, this.mPkgRxMap)) {
            if (!this.DBG) {
                return true;
            }
            Log.d(TAG, "pkgName:" + pkgName + " is downloading, continue");
            return true;
        } else if (BackgroundCleanUtil.isAppPlayingMusic(this.mContext, pkgName)) {
            if (!this.DBG) {
                return true;
            }
            Log.d(TAG, "pkgName:" + pkgName + " is playing, continue");
            return true;
        } else if (!BackgroundCleanUtil.isLiveWallpaper(this.mContext, pkgName)) {
            return false;
        } else {
            if (!this.DBG) {
                return true;
            }
            Log.d(TAG, "pkgName:" + pkgName + " LiveWallpaper running, continue");
            return true;
        }
    }

    private void registerReceivers() {
        if (!this.isRegisterReceivers) {
            if (this.DBG) {
                Log.d(TAG, "registerReceivers and isScreenOn = " + BackgroundCleanUtil.isScreenOn(this.mContext));
            }
            if (!BackgroundCleanUtil.isScreenOn(this.mContext)) {
                this.mHandlerThread.obtainMessage(1).sendToTarget();
            }
            IntentFilter filterScreen = new IntentFilter();
            filterScreen.addAction("android.intent.action.ACTION_SHUTDOWN");
            registerReceiver(this.mScreenReceiver, filterScreen);
            IntentFilter filterInput = new IntentFilter();
            filterInput.addAction("android.intent.action.INPUT_METHOD_CHANGED");
            registerReceiver(this.mInputMethodReceiver, filterInput);
            this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
            this.isRegisterReceivers = true;
        }
    }

    private void refreshList() {
        this.mWhiteList = BackgroundPolicyExecutor.getInstance(this.mContext).getWhiteListApp(9);
        this.mTempWhitelist = TempWhiteList.getInstance(this).get();
    }

    private void setAlarmBgRunService(long delay) {
        if (delay > 0) {
            if (this.DBG) {
                Log.d(TAG, "setAlarmBgRunService, delay " + delay);
            }
            AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
            PendingIntent pi = null;
            Intent intent = new Intent(this.mContext, BackgroundCleanService.class);
            intent.setAction(BackgroundCleanUtil.ACTION_BACK_CLEAN_START);
            try {
                pi = PendingIntent.getService(this.mContext, 0, intent, 134217728);
            } catch (Exception e) {
                Log.e(TAG, "AlarmManager failed to start " + e.toString());
            }
            am.setExactAndAllowWhileIdle(0, System.currentTimeMillis() + delay, pi);
            BackgroundCleanUtil.setPkgRxBytesMap(this.mContext, this.mPm, this.mPkgRxMap);
        }
    }

    private void cancelAlarmBgRunService() {
        if (this.DBG) {
            Log.d(TAG, "cancel Alarm BgRunService.");
        }
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        Intent intent = new Intent(this.mContext, BackgroundCleanService.class);
        intent.setAction(BackgroundCleanUtil.ACTION_BACK_CLEAN_START);
        PendingIntent pi = PendingIntent.getService(this.mContext, 0, intent, 134217728);
        am.cancel(pi);
        if (pi != null) {
            pi.cancel();
        }
        this.mAutoWakeupCount = 0;
    }

    private void autoWakeupBgRunService() {
        this.mAutoWakeupCount++;
        long timeToAutoWakeup = this.mRepeatPeriod;
        if (this.misDeepDoze) {
            timeToAutoWakeup = this.mRepeatPeriodInDeepDoze;
        }
        setAlarmBgRunService(timeToAutoWakeup);
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

    private ArrayList<String> clearAppInDeepDoze() {
        BAMMode bamMode = new BAMMode(this.mContext);
        ArrayList<String> stopApps = new ArrayList();
        boolean cta = PSUtils.isCTA(this.mContext);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",Ignore BAM Cleaner.");
        } else {
            Log.d(TAG, "clearAppInDeepDoze mAutoWakeupCount = " + this.mAutoWakeupCount);
            List<String> launcherApps = PowerSavingUtils.getLauncherApList(this.mContext);
            Map<String, Alarm> alarms = AlarmUtils.getAlarm(this.mContext);
            List<String> alarmRecordWhiteList = Arrays.asList(this.mContext.getResources().getStringArray(C0321R.array.alarm_record_white_list));
            for (Entry<String, Alarm> entry : alarms.entrySet()) {
                String pkgName = (String) entry.getKey();
                if (pkgName.equals(PACKAGE_NAME.SYSTEM_UI) && PowerSavingUtils.isDozePusleAOD(this)) {
                    Log.d(TAG, "package = com.android.systemuiand DozePusleAOD is " + PowerSavingUtils.isDozePusleAOD(this) + " continue");
                } else {
                    Alarm alarmRecord = (Alarm) entry.getValue();
                    if (launcherApps.contains(pkgName) && !alarmRecordWhiteList.contains(pkgName)) {
                        Log.i(TAG, "pkgName = " + ((String) entry.getKey()) + " ,alarm = " + entry.getValue());
                        if (this.mWhiteList.contains(pkgName)) {
                            if (((float) alarmRecord.duration) < 600000.0f) {
                                Log.d(TAG, alarmRecord.duration + " < " + 600000.0f);
                            } else {
                                float hours = ((float) alarmRecord.duration) / 3600000.0f;
                                if (hours == 0.0f) {
                                    Log.d(TAG, "hours = 0");
                                } else {
                                    float alarmTimes_hour = ((float) alarmRecord.alarmTimes) / hours;
                                    Log.i(TAG, "alarmTimes_hour = " + alarmTimes_hour + ", hours = " + hours);
                                    if (alarmTimes_hour > 50.0f && bamMode.process(pkgName) && !stopApps.contains(pkgName)) {
                                        if (this.DBG) {
                                            Log.d(TAG, "pkgName " + pkgName + " is in BAM whitelist -- alarmTimes_hour " + alarmTimes_hour + " > " + 50.0f + " , " + bamMode.modeForLog(pkgName));
                                        }
                                        stopApps.add(pkgName);
                                        this.mDB.insertProcessWasForceStopped(pkgName + bamMode.modeForDatabase("m", pkgName));
                                    }
                                }
                            }
                        } else if (alarmRecord.alarmTimes > 0 && bamMode.process(pkgName) && !stopApps.contains(pkgName)) {
                            if (this.DBG) {
                                Log.d(TAG, "pkgName " + pkgName + " is not in BAM whitelist -- alarmRecord.alarmTimes " + alarmRecord.alarmTimes + " > 0 , " + bamMode.modeForLog(pkgName));
                            }
                            stopApps.add(pkgName);
                            this.mDB.insertProcessWasForceStopped(pkgName + bamMode.modeForDatabase("m", pkgName));
                        }
                    }
                }
            }
            PowerSavingUtils.clearAlarmRecordsInDoze(this.mContext);
        }
        return stopApps;
    }

    private void sendIntentToEDozeService() {
        Set<String> appList = getAllRunningPkgs();
        boolean bUsingNertwork = isThereAnyAppUsingNetwork(appList);
        boolean bisPlayingMusic = isThereAnyAppPlayingMusic(appList);
        Intent IntentToEDoze = new Intent();
        IntentToEDoze.setAction(EDozeService.ACTION_TO_ADJUST_FUNCTION);
        IntentToEDoze.putExtra("IsNetworkUsing", bUsingNertwork);
        IntentToEDoze.putExtra("isPlayingMusic", bisPlayingMusic);
        sendBroadcast(IntentToEDoze);
    }

    private boolean isThereAnyAppUsingNetwork(Set<String> appList) {
        for (String pkg : appList) {
            if (BackgroundCleanUtil.isAppDownloading(this.mContext, pkg, this.mPkgRxMap)) {
                Log.d(TAG, "pkg is using network");
                return true;
            }
        }
        return false;
    }

    private boolean isThereAnyAppPlayingMusic(Set<String> appList) {
        for (String pkg : appList) {
            if (BackgroundCleanUtil.isAppPlayingMusic(this.mContext, pkg)) {
                Log.d(TAG, "pkg is playing music");
                return true;
            }
        }
        return false;
    }

    private Set<String> getAllRunningPkgs() {
        Set<String> appList = new ArraySet();
        List<RunningTaskInfo> runningTasks = this.mAm.getRunningTasks(100);
        List<RunningAppProcessInfo> runningApps = this.mAm.getRunningAppProcesses();
        List<RecentTaskInfo> recentList = BackgroundCleanUtil.getRecentList(this.mAm);
        List<RunningServiceInfo> runningSerivces = this.mAm.getRunningServices(100);
        for (RunningTaskInfo runningTask : runningTasks) {
            appList.add(runningTask.baseActivity.getPackageName());
        }
        for (RecentTaskInfo recentInfo : recentList) {
            appList.add(recentInfo.baseIntent.getComponent().getPackageName());
        }
        for (RunningServiceInfo runningService : runningSerivces) {
            appList.add(runningService.service.getPackageName());
        }
        for (RunningAppProcessInfo runningApp : runningApps) {
            for (String pkgName : runningApp.pkgList) {
                appList.add(pkgName);
            }
        }
        return appList;
    }

    private void recoverBAMModeSetting(Context context) {
        BAMMode bamMode = new BAMMode(context);
        bamMode.recoverStandybyBucketSettingsIfNeed();
        bamMode.recoverRestrictedApps();
    }
}
