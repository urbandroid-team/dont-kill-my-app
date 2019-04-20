package com.evenwell.powersaving.g3.utils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class PSUtils {
    private static final String TAG = "PSUtils";
    private static boolean mCTS = false;

    private PSUtils() {
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getSystemApps(Context context) {
        List<ApplicationInfo> appInfos = context.getPackageManager().getInstalledApplications(0);
        List<String> apps = new ArrayList();
        for (ApplicationInfo app : appInfos) {
            if ((app.flags & 128) != 0) {
                apps.add(app.packageName);
            } else if ((app.flags & 1) != 0) {
                apps.add(app.packageName);
            }
        }
        return apps;
    }

    public static List<String> getAllApps(Context context) {
        List<ApplicationInfo> appInfos = context.getPackageManager().getInstalledApplications(0);
        List<String> apps = new ArrayList();
        for (ApplicationInfo app : appInfos) {
            apps.add(app.packageName);
        }
        return apps;
    }

    public static boolean isCNModel(Context context) {
        return context.getResources().getBoolean(C0321R.bool.region_cn);
    }

    public static boolean isCTA(Context context) {
        return context.getResources().getBoolean(C0321R.bool.cta);
    }

    public static void setCTS(boolean cts) {
        mCTS = cts;
    }

    public static boolean isCTS() {
        return mCTS;
    }

    public static int getUid(Context context, String pkgName) {
        int uid = -1;
        try {
            return context.getPackageManager().getApplicationInfo(pkgName, 0).uid;
        } catch (NameNotFoundException e) {
            Log.i(TAG, pkgName + " is not found.");
            return uid;
        }
    }

    public static String getProcessNameByPID(Context context, int pid) {
        for (RunningAppProcessInfo info : ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses()) {
            if (info.pid == pid) {
                if (info.pkgList.length == 1) {
                    return info.pkgList[0];
                }
                for (String pkg : info.pkgList) {
                    if (info.processName.contains(pkg)) {
                        return pkg;
                    }
                }
                return info.processName;
            }
        }
        return "";
    }

    public static int getVersionCodeByPkg(Context ctx, String pkgName) {
        int versionCode = -1;
        try {
            versionCode = ctx.getPackageManager().getPackageInfo(pkgName, 16384).versionCode;
            Log.d(TAG, "getVersionCodeByPkg " + pkgName + ": " + versionCode);
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return versionCode;
        }
    }

    public static void packageAddOnWW(Context context, String packageName) {
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(context);
        BPE.addAppToWhiteList(packageName);
        BPE.removeAppFromDisAutoList(packageName);
    }

    public static boolean enableTestFunction() {
        if (PowerSavingUtils.isDBGcfgtoolEnabled() || PowerSavingUtils.isLogConfigExist() || DbgConfig.getInstance().isLogcatMainOn()) {
            return true;
        }
        return false;
    }
}
