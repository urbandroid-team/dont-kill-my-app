package com.evenwell.powersaving.g3.background;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.IntentService;
import android.content.Intent;
import android.util.ArraySet;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.p000e.doze.DozeStatus;
import com.evenwell.powersaving.g3.provider.ProcessMonitorDB;
import com.evenwell.powersaving.g3.utils.PSConst.PACKAGENAME;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class WakelockCleanService extends IntentService {
    private static final String KEY_PKG_NAME = "KEY_PKG_NAME";
    private static final String TAG = "[PowerSavingAppG3]WakelockCleanService";

    public WakelockCleanService() {
        super("WakelockCleanService");
    }

    protected void onHandleIntent(Intent intent) {
        try {
            handleKillProcess(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleKillProcess(Intent intent) {
        if (!isCTAOrCTS()) {
            if (!isDoze()) {
                Log.i(TAG, "non-Doze, don't kill process");
            } else if (PSUtils.getVersionCodeByPkg(this, PACKAGENAME.POWERMONITOR) == 8001036) {
                Log.i(TAG, "PowerMonitor version = 8001036, return");
            } else if (PSUtils.getVersionCodeByPkg(this, PACKAGENAME.POWERMONITOR) <= 8001024) {
                Log.i(TAG, "PowerMonitor version <= 8001024, return");
            } else if (intent != null && intent.getAction() != null && intent.getAction().equals(BackgroundCleanUtil.ACTION_WAKELOCK_CLEAN_START)) {
                killProcess(intent);
            }
        }
    }

    private boolean isDoze() {
        DozeStatus dozeStatus = new DozeStatus();
        int lightDozeStatus = dozeStatus.getLightDozeStatus();
        int deepDozeStatus = dozeStatus.getDeepDozeStatus();
        Log.i(TAG, "lightDozeStatus=" + DozeStatus.lightStateToString(lightDozeStatus) + ",deepDozeStatus=" + DozeStatus.stateToString(deepDozeStatus));
        if (lightDozeStatus >= 4 || deepDozeStatus >= 5) {
            return true;
        }
        return false;
    }

    private boolean isCTAOrCTS() {
        boolean cta = PSUtils.isCTA(this);
        boolean cts = PSUtils.isCTS();
        if (!cta && !cts) {
            return false;
        }
        Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",ignore WakelockCleanService.");
        return true;
    }

    private void stopProcess(String pkg) {
        if (PowerSavingUtils.isLauncherAP(this, pkg)) {
            forceStopProcess(pkg);
        } else {
            killProcess(pkg);
        }
    }

    private void killProcess(String pkg) {
        ActivityManager am = (ActivityManager) getSystemService("activity");
        if (am != null) {
            am.killBackgroundProcesses(pkg);
            Log.i(TAG, "kill: " + pkg);
            saveToDB(pkg, "(K)");
        }
    }

    private void forceStopProcess(String pkg) {
        ActivityManager am = (ActivityManager) getSystemService("activity");
        if (am != null) {
            am.forceStopPackage(pkg);
            am.forceStopPackage(pkg);
            Log.i(TAG, "forceStopPackage " + pkg);
            saveToDB(pkg, "(F)");
        }
    }

    private void saveToDB(String pkg, String reason) {
        Exception e;
        Throwable th;
        ProcessMonitorDB processMonitorDB = null;
        try {
            ProcessMonitorDB processMonitorDB2 = new ProcessMonitorDB(this);
            try {
                processMonitorDB2.insertProcessWasForceStopped(pkg + reason);
                if (processMonitorDB2 != null) {
                    processMonitorDB2.close();
                    processMonitorDB = processMonitorDB2;
                    return;
                }
            } catch (Exception e2) {
                e = e2;
                processMonitorDB = processMonitorDB2;
                try {
                    e.printStackTrace();
                    if (processMonitorDB != null) {
                        processMonitorDB.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (processMonitorDB != null) {
                        processMonitorDB.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                processMonitorDB = processMonitorDB2;
                if (processMonitorDB != null) {
                    processMonitorDB.close();
                }
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (processMonitorDB != null) {
                processMonitorDB.close();
            }
        }
    }

    private void killProcess(Intent intent) {
        String pkg = intent.getStringExtra(KEY_PKG_NAME);
        Log.i(TAG, "pkg = " + pkg);
        if (pkg == null) {
            Log.i(TAG, "pkg null");
            return;
        }
        List<String> sharedPkgs = null;
        if (pkg.contains(":")) {
            try {
                sharedPkgs = Arrays.asList(getPackageManager().getPackagesForUid(Integer.parseInt(pkg.split(":")[1])));
            } catch (NumberFormatException e) {
                sharedPkgs = null;
                pkg = pkg.split(":")[0];
            }
        }
        if (sharedPkgs == null || sharedPkgs.isEmpty()) {
            stopProcess(pkg);
            return;
        }
        ActivityManager am = (ActivityManager) getSystemService("activity");
        if (am == null) {
            Log.i(TAG, "Abnormal, ActivityManager = null!!");
            return;
        }
        Set<String> targetList = new ArraySet();
        List<RunningTaskInfo> runningTasks = am.getRunningTasks(100);
        List<RunningServiceInfo> runningSerivces = am.getRunningServices(100);
        List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        for (RunningTaskInfo runningTask : runningTasks) {
            String pkgName = runningTask.baseActivity.getPackageName();
            if (sharedPkgs.contains(pkgName)) {
                targetList.add(pkgName);
            }
        }
        for (RunningServiceInfo runningService : runningSerivces) {
            pkgName = runningService.service.getPackageName();
            if (sharedPkgs.contains(pkgName)) {
                targetList.add(pkgName);
            }
        }
        for (RunningAppProcessInfo runningApp : runningApps) {
            for (String pkgName2 : runningApp.pkgList) {
                if (sharedPkgs.contains(pkgName2)) {
                    targetList.add(pkgName2);
                }
            }
        }
        for (String pkgName22 : targetList) {
            stopProcess(pkgName22);
        }
    }
}
