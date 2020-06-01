package com.evenwell.powersaving.g3.background;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IFihProcessListener;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.Service;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManager.DisplayListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.Display;
import android.view.inputmethod.InputMethodInfo;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.background.TempWhiteList.OnListChangeListener;
import com.evenwell.powersaving.g3.component.RestrictedUtils;
import com.evenwell.powersaving.g3.element.LimitedSizeList;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.provider.BackDataDb;
import com.evenwell.powersaving.g3.provider.ProcessMonitorDB;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class ProcessMonitorService extends Service {
    private static final boolean DBG = true;
    private static final boolean DBG_FOR_WRITE_FILE = false;
    private static final String HOST_TYPE_ACTIVITY = "activity";
    private static final String HOST_TYPE_BROADCAST = "broadcast";
    private static final String HOST_TYPE_PROVIDER = "content provider";
    private static final String HOST_TYPE_SERVICE = "service";
    private static final String KEY_ACTIVITY_INTENT = "ACTIVITY_INTENT";
    private static final String KEY_BROADCAST_INTENT = "BROADCAST_INTENT";
    private static final String KEY_CALLEE_APP_INFO = "CALLEE_APP_INFO";
    private static final String KEY_CALLER_APP_INFO = "CALLER_APP_INFO";
    private static final String KEY_CALLER_PACKAGE = "CALLER_PACKAGE";
    private static final String KEY_HOSTING_TYPE = "HOSTING_TYPE";
    private static final String KEY_SERVICE_INTENT = "SERVICE_INTENT";
    private static final String PREF_SCREEN_ON_ELAPSED_TIME = "screen_on_elapsed_time";
    public static final String REFRESH_HMD_WHITELIST = "com.evenwell.powersaving.g3.refresh_hmd_whitelist";
    private static final long SCREEN_OFF_ELAPSED_TIME = -1;
    private static final String TAG = "[PowerSavingAppG3]ProcessMonitorService";
    private Set<String> HMDPreload_whitelist;
    private Runnable UpdateTopApListRunnable = new C03302();
    IFihProcessListener listener = new C03335();
    private ActivityManager mAm;
    private BackDataDb mBackDataDb;
    private BroadcastReceiver mBroadcastReceiver = new C03313();
    private final DisplayListener mDisplayListener = new C03324();
    private DisplayManager mDisplayManager;
    private List<String> mExceptionalActivities;
    private List<String> mExemptPrefix;
    private Handler mHandlerThread;
    private long mLastedScreenOnTime;
    private PackageManager mPackageManager;
    private PowerManager mPowerManger;
    private ProcessMonitorDB mProcessMonitorDB = null;
    private IProcessObserver mProcessObserver = new C03291();
    private String mProtectedPackage = "";
    private List<String> mTempWhiteList = new ArrayList();
    private LimitedSizeList<String> mTopApList;
    private SharedPreferences prefStatus;

    /* renamed from: com.evenwell.powersaving.g3.background.ProcessMonitorService$1 */
    class C03291 extends Stub {
        C03291() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                try {
                    String calleeApp = PSUtils.getProcessNameByPID(ProcessMonitorService.this, pid);
                    Log.i(ProcessMonitorService.TAG, "pid: " + pid + ", calleeApp: " + calleeApp);
                    if (TextUtils.indexOf(calleeApp, ':') > 0) {
                        Log.i(ProcessMonitorService.TAG, "ignore sub-process, calleeApp: " + calleeApp);
                    } else if (PowerSavingUtils.isLauncherAP(ProcessMonitorService.this, calleeApp)) {
                        long delay;
                        Log.i(ProcessMonitorService.TAG, calleeApp + " add to mTopApList.");
                        ProcessMonitorService.this.mTopApList.add(calleeApp);
                        ProcessMonitorService.this.mHandlerThread.removeCallbacks(ProcessMonitorService.this.UpdateTopApListRunnable);
                        if (ProcessMonitorService.this.mPowerManger.isScreenOn()) {
                            delay = 5000;
                        } else {
                            delay = 0;
                        }
                        ProcessMonitorService.this.mHandlerThread.postDelayed(ProcessMonitorService.this.UpdateTopApListRunnable, delay);
                    } else {
                        Log.i(ProcessMonitorService.TAG, calleeApp + " is not launcher app.");
                    }
                } catch (Exception e) {
                    Log.e(ProcessMonitorService.TAG, "Can't get running App process info", e);
                }
            }
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }

        public void onProcessDied(int pid, int uid) {
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.background.ProcessMonitorService$2 */
    class C03302 implements Runnable {
        C03302() {
        }

        public void run() {
            try {
                Log.i(ProcessMonitorService.TAG, "original mTopApList = " + ProcessMonitorService.this.mTopApList);
                if (PSUtils.isCNModel(ProcessMonitorService.this)) {
                    ProcessMonitorService.this.mTopApList.keepLatestElement(1);
                } else {
                    ProcessMonitorService.this.mTopApList.keepLatestElement(3);
                }
                Log.i(ProcessMonitorService.TAG, "keep mTopApList = " + ProcessMonitorService.this.mTopApList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.background.ProcessMonitorService$3 */
    class C03313 extends BroadcastReceiver {
        C03313() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.TIME_SET".equals(intent.getAction())) {
                Log.d(ProcessMonitorService.TAG, "ACTION_TIME_CHANGED");
                ProcessMonitorService.this.mHandlerThread.post(new ProcessMonitorService$3$$Lambda$0(this));
            }
        }

        final /* synthetic */ void lambda$onReceive$0$ProcessMonitorService$3() {
            if (BackgroundCleanUtil.isScreenOn(ProcessMonitorService.this)) {
                ProcessMonitorService.this.mLastedScreenOnTime = System.currentTimeMillis();
            }
            ProcessMonitorService.this.prefStatus.edit().putLong(ProcessMonitorService.PREF_SCREEN_ON_ELAPSED_TIME, ProcessMonitorService.this.mLastedScreenOnTime).apply();
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.background.ProcessMonitorService$4 */
    class C03324 implements DisplayListener {
        C03324() {
        }

        public void onDisplayAdded(int displayId) {
        }

        public void onDisplayRemoved(int displayId) {
        }

        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                ProcessMonitorService.this.mHandlerThread.post(new ProcessMonitorService$4$$Lambda$0(this));
            }
        }

        final /* synthetic */ void lambda$onDisplayChanged$0$ProcessMonitorService$4() {
            Display curDisplay = ProcessMonitorService.this.mDisplayManager.getDisplay(0);
            if (curDisplay.getState() == 2) {
                Log.i(ProcessMonitorService.TAG, "onDisplayChanged  Display.STATE_ON");
                ProcessMonitorService.this.mLastedScreenOnTime = System.currentTimeMillis();
            } else if (curDisplay.getState() == 1) {
                Log.i(ProcessMonitorService.TAG, "onDisplayChanged  Display.STATE_OFF");
                ProcessMonitorService.this.mLastedScreenOnTime = -1;
                ProcessMonitorService.this.mHandlerThread.removeCallbacks(ProcessMonitorService.this.UpdateTopApListRunnable);
                ProcessMonitorService.this.mHandlerThread.post(ProcessMonitorService.this.UpdateTopApListRunnable);
            }
            ProcessMonitorService.this.prefStatus.edit().putLong(ProcessMonitorService.PREF_SCREEN_ON_ELAPSED_TIME, ProcessMonitorService.this.mLastedScreenOnTime).apply();
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.background.ProcessMonitorService$5 */
    class C03335 extends IFihProcessListener.Stub {
        List<String> tmpTopApps = new ArrayList();

        C03335() {
        }

        public void processStart(Bundle info) {
            ProcessMonitorService.this.mHandlerThread.postDelayed(new ProcessMonitorService$5$$Lambda$0(this, info), 300);
        }

        final /* synthetic */ void lambda$processStart$0$ProcessMonitorService$5(Bundle info) {
            try {
                boolean cta = PSUtils.isCTA(ProcessMonitorService.this);
                boolean cts = PSUtils.isCTS();
                if (cta || cts) {
                    Log.i(ProcessMonitorService.TAG, "cta = " + cta + ",cts = " + cts + ",Ignore ProcessMonitor Cleaner.");
                    return;
                }
                String calleePackageName = ((ApplicationInfo) info.getParcelable(ProcessMonitorService.KEY_CALLEE_APP_INFO)).packageName;
                String hostingType = info.getString(ProcessMonitorService.KEY_HOSTING_TYPE);
                String callerPacageName = info.getString(ProcessMonitorService.KEY_CALLER_PACKAGE);
                if (callerPacageName == null) {
                    ApplicationInfo callerInfo = (ApplicationInfo) info.getParcelable(ProcessMonitorService.KEY_CALLER_APP_INFO);
                    if (callerInfo != null) {
                        callerPacageName = callerInfo.packageName;
                    }
                }
                PMSMode pMSMode = new PMSMode(ProcessMonitorService.this);
                if (pMSMode.getMode() == 0 || pMSMode.getMode() == 4) {
                    RestrictedUtils.restricted(ProcessMonitorService.this, calleePackageName, false);
                }
                String intentInfo = "";
                String intentAction = "";
                String component = "";
                String category = "";
                Intent intent = null;
                if ("service".equals(hostingType)) {
                    intent = (Intent) info.getParcelable(ProcessMonitorService.KEY_SERVICE_INTENT);
                } else if (ProcessMonitorService.HOST_TYPE_BROADCAST.equals(hostingType)) {
                    intent = (Intent) info.getParcelable(ProcessMonitorService.KEY_BROADCAST_INTENT);
                } else if ("activity".equals(hostingType)) {
                    intent = (Intent) info.getParcelable(ProcessMonitorService.KEY_ACTIVITY_INTENT);
                }
                if (intent != null) {
                    intentAction = intent.getAction();
                    if (intent.getComponent() != null) {
                        component = intent.getComponent().getClassName();
                    }
                    if (intent.getCategories() != null) {
                        category = intent.getCategories().toString();
                    }
                }
                boolean isExceptionalActivitiy = ProcessMonitorService.this.mExceptionalActivities.contains(component) && !category.contains("android.intent.category.LAUNCHER");
                String pkginfo = "calleePackageName = " + calleePackageName + ",callerPacageName = " + callerPacageName + ",hostingType = " + hostingType + ",screenOn = " + ProcessMonitorService.this.mPowerManger.isScreenOn() + ",cmp = " + component + ",cat = " + category + ",action = " + intentAction + ",isExceptionalActivitiy = " + isExceptionalActivitiy;
                Log.d(ProcessMonitorService.TAG, pkginfo);
                ProcessMonitorService.this.writeToFile(pkginfo, ProcessMonitorService.this);
                intentInfo = "cmp=" + component + ",cat=" + category;
                try {
                    if (!TextUtils.equals(calleePackageName, callerPacageName)) {
                        ProcessMonitorService.this.mProcessMonitorDB.insertToProcessMonitor(callerPacageName, calleePackageName, hostingType, intentAction, intentInfo, ProcessMonitorService.this.mPowerManger.isScreenOn());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if ((!"activity".equals(hostingType) || isExceptionalActivitiy) && !(ProcessMonitorService.HOST_TYPE_PROVIDER.equals(hostingType) && ProcessMonitorService.this.mPowerManger.isScreenOn())) {
                    this.tmpTopApps.clear();
                    this.tmpTopApps.addAll(ProcessMonitorService.this.mTopApList);
                    Collections.reverse(this.tmpTopApps);
                    if (ProcessMonitorService.this.canStopPackage(info, intent, this.tmpTopApps) && pMSMode.process(calleePackageName)) {
                        Log.d(ProcessMonitorService.TAG, pMSMode.modeForLog(calleePackageName));
                        ProcessMonitorService.this.writeToFile(pMSMode.modeForLog(calleePackageName), ProcessMonitorService.this);
                        try {
                            ProcessMonitorService.this.mProcessMonitorDB.insertProcessWasForceStopped(calleePackageName + pMSMode.modeForDatabase("", calleePackageName));
                            return;
                        } catch (Exception ex2) {
                            ex2.printStackTrace();
                            return;
                        }
                    }
                    return;
                }
                Log.d(ProcessMonitorService.TAG, "Ignore package " + calleePackageName + ",component = " + component);
            } catch (Exception ex22) {
                ex22.printStackTrace();
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.background.ProcessMonitorService$6 */
    class C03346 implements OnListChangeListener {
        C03346() {
        }

        public void onChange() {
            ProcessMonitorService.this.mHandlerThread.post(new ProcessMonitorService$6$$Lambda$0(this));
        }

        final /* synthetic */ void lambda$onChange$0$ProcessMonitorService$6() {
            ProcessMonitorService.this.mTempWhiteList = TempWhiteList.getInstance(ProcessMonitorService.this).get();
            Log.d(ProcessMonitorService.TAG, "TempWhiteList onChange " + ProcessMonitorService.this.mTempWhiteList);
        }
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        this.prefStatus = getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0);
        HandlerThread handlerThread = new HandlerThread("ProcessMonior");
        handlerThread.start();
        this.mHandlerThread = new Handler(handlerThread.getLooper());
        this.mProcessMonitorDB = new ProcessMonitorDB(this);
        this.mAm = (ActivityManager) getSystemService("activity");
        this.mPowerManger = (PowerManager) getSystemService("power");
        this.mPackageManager = getPackageManager();
        this.mBackDataDb = new BackDataDb(this);
        this.mExceptionalActivities = RestrictedUtils.getRestrictedByType(this, "activity");
        this.mTopApList = new LimitedSizeList(3);
        this.HMDPreload_whitelist = new ArraySet();
        this.mHandlerThread.post(new ProcessMonitorService$$Lambda$0(this));
        try {
            TempWhiteList.getInstance(this).setOnListChangeListener(new C03346());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.mDisplayManager = (DisplayManager) getSystemService("display");
        this.mDisplayManager.registerDisplayListener(this.mDisplayListener, null);
        try {
            this.mAm.registerProcessListener(this.listener);
            ActivityManagerNative.getDefault().registerProcessObserver(this.mProcessObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.TIME_SET"));
        this.mExemptPrefix = BackgroundPolicyExecutor.getInstance(this).getExemptPrefix();
    }

    final /* synthetic */ void lambda$onCreate$0$ProcessMonitorService() {
        this.mLastedScreenOnTime = this.prefStatus.getLong(PREF_SCREEN_ON_ELAPSED_TIME, -1);
        if (BackgroundCleanUtil.isScreenOn(this) && this.mLastedScreenOnTime == -1) {
            this.mLastedScreenOnTime = System.currentTimeMillis();
        }
        this.prefStatus.edit().putLong(PREF_SCREEN_ON_ELAPSED_TIME, this.mLastedScreenOnTime).apply();
        refreshHMDWhiteList();
        try {
            clearDB();
            this.mTempWhiteList = TempWhiteList.getInstance(this).get();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent == null) {
            this.mHandlerThread.post(new ProcessMonitorService$$Lambda$1(this));
        } else {
            Log.d(TAG, "action = " + intent.getAction());
            if (REFRESH_HMD_WHITELIST.equals(intent.getAction())) {
                this.mHandlerThread.post(new ProcessMonitorService$$Lambda$2(this));
            }
        }
        return 1;
    }

    final /* synthetic */ void lambda$onStartCommand$1$ProcessMonitorService() {
        try {
            this.mBackDataDb.insertTimeStampToServiceRestartTable("PM");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    final /* synthetic */ void lambda$onStartCommand$2$ProcessMonitorService() {
        refreshHMDWhiteList();
    }

    public void onDestroy() {
        try {
            this.mAm.unregisterProcessListener(this.listener);
            ActivityManagerNative.getDefault().unregisterProcessObserver(this.mProcessObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mHandlerThread.getLooper().quitSafely();
        this.mProcessMonitorDB.close();
        this.mBackDataDb.close();
        this.mDisplayManager.unregisterDisplayListener(this.mDisplayListener);
        unregisterReceiver(this.mBroadcastReceiver);
        super.onDestroy();
    }

    private boolean canStopPackage(Bundle pkgInfo, Intent intent, List<String> topApps) {
        long timediff = 0;
        String message = "";
        String pkgname = ((ApplicationInfo) pkgInfo.getParcelable(KEY_CALLEE_APP_INFO)).packageName;
        String callerPackage = pkgInfo.getString(KEY_CALLER_PACKAGE);
        String componentName = "";
        if (this.mLastedScreenOnTime == -1) {
            Log.d(TAG, "screen on time = " + this.mLastedScreenOnTime);
        } else {
            Log.d(TAG, "screen on time = " + new SimpleDateFormat("yyyy/MM/dd HH:mm").format(new Date(this.mLastedScreenOnTime)) + "(" + this.mLastedScreenOnTime + ")");
        }
        UsageStats usageStats = BackgroundCleanUtil.getAppUsageStats(this, pkgname);
        if (usageStats != null) {
            timediff = usageStats.getLastTimeUsed() - this.mLastedScreenOnTime;
            String str = TAG;
            String str2 = str;
            Log.d(str2, "Package Name = " + usageStats.getPackageName() + ",getLastTimeUsed = " + DateUtils.formatSameDayTime(usageStats.getLastTimeUsed(), System.currentTimeMillis(), 2, 2) + " (" + usageStats.getLastTimeUsed() + "),time diff  = " + timediff);
        } else {
            Log.d(TAG, "usageStats for " + pkgname + " is null");
        }
        if (timediff <= 0 || this.mLastedScreenOnTime == -1 || !callerPackage.equals(pkgname)) {
            for (String prefix : this.mExemptPrefix) {
                if (pkgname.contains(prefix)) {
                    Log.d(TAG, "Ignore: " + pkgname + " has prefix ---" + prefix);
                    return false;
                }
            }
            if (this.mTempWhiteList.contains(pkgname)) {
                boolean isInRecent = false;
                for (RecentTaskInfo recentInfo : BackgroundCleanUtil.getRecentList(this.mAm)) {
                    if (recentInfo.baseIntent.getComponent().getPackageName().equals(pkgname)) {
                        isInRecent = true;
                    }
                }
                if (isInRecent) {
                    Log.d(TAG, pkgname + " is in temp white list and recent list");
                    return false;
                }
                Log.d(TAG, pkgname + " is in temp white list but not in recent list");
            }
            if ("com.android.nfc".equals(callerPackage)) {
                Log.d(TAG, "ignore this is called by NFC Service");
                return false;
            } else if (this.mProtectedPackage.equals(pkgname)) {
                Log.d(TAG, "ignore mProtectedPackage :" + this.mProtectedPackage);
                return false;
            } else {
                this.mProtectedPackage = "";
                Log.d(TAG, "reset mProtectedPackage :" + this.mProtectedPackage);
                if (!(intent == null || intent.getComponent() == null)) {
                    componentName = intent.getComponent().getClassName();
                }
                if (TextUtils.equals(pkgname, BackgroundCleanUtil.getDefaultSmsAppPackageName(this))) {
                    Log.i(TAG, "Ignore " + pkgname + ", it is default sms app.");
                    return false;
                } else if (BackgroundCleanUtil.isPhoneRing(this)) {
                    message = "Ignore all process as phone is in Ringing...";
                    Log.d(TAG, message);
                    writeToFile(message, this);
                    return false;
                } else {
                    if (BackgroundCleanUtil.isAccountAuthenticator(componentName, this.mPackageManager) && this.mPowerManger.isScreenOn()) {
                        message = "Ignore " + pkgname + " is AccountAuthenticator";
                        this.mProtectedPackage = pkgname;
                        Log.d(TAG, message);
                        writeToFile(message, this);
                        if (topApps.size() <= 0 || !"com.ss.android.article.news".equals(topApps.get(0)) || !"com.ss.android.essay.joke".equals(pkgname)) {
                            return false;
                        }
                        Log.d(TAG, "top app = " + ((String) topApps.get(0)) + ", pkgname = " + pkgname + " , don't protect");
                    }
                    if (BackgroundCleanUtil.isMediaRouteProviderServiceApp(this.mPackageManager, pkgname)) {
                        message = "Ignore " + pkgname + " is using MediaRouteProviderService";
                        Log.d(TAG, message);
                        writeToFile(message, this);
                        return false;
                    }
                    List<Integer> importances = BackgroundCleanUtil.getPkgImportancesofAllProcesses(this.mAm, pkgname);
                    Log.d(TAG, pkgname + " importances " + importances);
                    String defaultLauncher = BackgroundCleanUtil.getDefaultLauncher(this.mPackageManager);
                    Log.d(TAG, "topApps = " + topApps);
                    if (topApps.contains(pkgname)) {
                        return false;
                    }
                    if (BMS.getInstance(this).getBMSValue() || PSUtils.isCNModel(this)) {
                        if (!this.mBackDataDb.getAllDisAutoStartPkg().contains(pkgname)) {
                            message = "Ignore: not in blackList---" + pkgname;
                            Log.d(TAG, message);
                            writeToFile(message, this);
                            return false;
                        }
                    } else if (this.HMDPreload_whitelist.contains(pkgname)) {
                        message = "Ignore: in HMD WhiteList ---" + pkgname;
                        Log.d(TAG, message);
                        writeToFile(message, this);
                        return false;
                    }
                    if (BackgroundCleanUtil.isAppPlayingMusic(this, pkgname)) {
                        Log.d(TAG, "ignore: pkgName:" + pkgname + " is playing---");
                        return false;
                    } else if (BackgroundCleanUtil.getLiveWallpaperPackageName(this).contains(pkgname)) {
                        message = "Ignore: liveWallpaper app--- " + pkgname;
                        Log.d(TAG, message);
                        writeToFile(message, this);
                        return false;
                    } else if (pkgname.equals(defaultLauncher)) {
                        message = "Ignore: default launcher---" + pkgname;
                        Log.d(TAG, message);
                        writeToFile(message, this);
                        return false;
                    } else if (BackgroundCleanUtil.getWidgetPackageName(defaultLauncher).contains(pkgname)) {
                        message = "Ignore: widgetPkg app--- " + pkgname;
                        Log.d(TAG, message);
                        writeToFile(message, this);
                        return false;
                    } else {
                        for (InputMethodInfo inputMethodInfo : BackgroundCleanUtil.getInputMethods(this)) {
                            if (inputMethodInfo.getPackageName().equals(pkgname)) {
                                message = "Ignore: input method app--- " + pkgname;
                                Log.d(TAG, message);
                                writeToFile(message, this);
                                return false;
                            }
                        }
                        if (125 < ((Integer) importances.get(0)).intValue()) {
                            return true;
                        }
                        if (!importances.contains(Integer.valueOf(125))) {
                            if (importances.contains(Integer.valueOf(100)) && !((topApps.size() > 0 && ((String) topApps.get(0)).equals(pkgname)) || callerPackage == null || callerPackage.equals("android") || callerPackage.equals(pkgname))) {
                                if (!BackgroundCleanUtil.isInAuthList(this, this.mPackageManager, pkgname)) {
                                    return true;
                                }
                            }
                        }
                        message = "Ignore: IMPORTANCE = " + importances.get(0) + "---" + pkgname;
                        Log.d(TAG, message);
                        writeToFile(message, this);
                        return false;
                    }
                }
            }
        }
        Log.d(TAG, "ignore ,timediff = " + timediff + " > 0");
        return false;
    }

    private void writeToFile(String data, Context context) {
    }

    private void clearDB() {
        long rowCountProcessMonitor = this.mProcessMonitorDB.queryRowCountFromProcessMonitor();
        long rowCountForceStopAppList = this.mProcessMonitorDB.queryRowCountFromForceStopAppList();
        Log.d(TAG, "rowCountProcessMonitor = " + rowCountProcessMonitor + ",rowCountForceStopAppList = " + rowCountForceStopAppList);
        if (rowCountProcessMonitor > ((long) ProcessMonitorDB.rowCountThreshold)) {
            this.mProcessMonitorDB.deleteAllFromProcessMonitor();
            Log.d(TAG, "After delete : rowCountProcessMonitor = " + this.mProcessMonitorDB.queryRowCountFromProcessMonitor());
        }
        if (rowCountForceStopAppList > ((long) ProcessMonitorDB.rowCountThreshold)) {
            this.mProcessMonitorDB.deleteFromForceStopAppList();
            Log.d(TAG, "After delete : rowCountForceStopAppList = " + this.mProcessMonitorDB.queryRowCountFromForceStopAppList());
        }
    }

    private void refreshHMDWhiteList() {
        this.HMDPreload_whitelist.clear();
        this.HMDPreload_whitelist.addAll(BackgroundPolicyExecutor.getInstance(this).getWhiteListApp());
        for (String pkg : BackgroundPolicyExecutor.getInstance(this).getAppsShowOnBAMUI()) {
            this.HMDPreload_whitelist.remove(pkg);
        }
        this.HMDPreload_whitelist.addAll(BackgroundPolicyExecutor.getInstance(this).getWhiteListApp(8));
        this.HMDPreload_whitelist.addAll(BackgroundPolicyExecutor.getInstance(this).getWhiteListApp(64));
    }
}
