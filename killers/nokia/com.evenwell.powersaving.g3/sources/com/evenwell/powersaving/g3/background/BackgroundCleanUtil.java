package com.evenwell.powersaving.g3.background;

import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.hardware.display.DisplayManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.provider.Telephony.Sms;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.AtomicFile;
import android.util.Log;
import android.util.Xml;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.provider.BackDataDb.SaveData;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.fihtdc.backuptool.FileOperator;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class BackgroundCleanUtil {
    public static final String ACTION_BACK_CLEAN_START = "android.intent.action.background_clean_start";
    public static final String ACTION_WAKELOCK_CLEAN_START = "android.intent.action.wakelock_clean_start";
    private static final String CURRENT_LAUNCHER_NAME = "current_launcher_name";
    private static String CUST_ACCOUNT_ACTION = "com.cust.settings.action.ACCOUNT";
    private static String CUST_ACCOUNT_METADATA_TYPE = "com.cust.settings.account.type";
    private static boolean DBG = true;
    public static final int MANAGER_MAX_APP_SIZE = 100;
    private static final int NUM_PERSIST_TOP_TASK = 3;
    public static final double SCALE_UID_RX = 20.0d;
    public static final String TAG = "[PowerSavingAppG3]BackgroundCleanUtil";
    private static final String WIDGET_STATE_FILENAME = "appwidgets.xml";

    public static List<ApplicationInfo> getAllUserInstalledPkgList(PackageManager pm) {
        List<ApplicationInfo> userInstalledApps = new ArrayList();
        List<ApplicationInfo> applist = pm.getInstalledApplications(0);
        for (int i = 0; i < applist.size(); i++) {
            ApplicationInfo info = (ApplicationInfo) applist.get(i);
            if (!isSystemApp(info)) {
                userInstalledApps.add(info);
            }
        }
        return userInstalledApps;
    }

    public static List<String> getSameUserIdPkgs(PackageManager pm, String pkg) {
        List<String> pkgs = new ArrayList();
        try {
            ApplicationInfo pkgInfo = pm.getApplicationInfo(pkg, 0);
            if (!isSystemApp(pkgInfo)) {
                String[] sameUserIdPkgs = pm.getPackagesForUid(pkgInfo.uid);
                int i = 0;
                while (i < sameUserIdPkgs.length) {
                    if (!(sameUserIdPkgs[i].equals(pkg) || isSystemApp(pm.getApplicationInfo(sameUserIdPkgs[i], 0)))) {
                        pkgs.add(sameUserIdPkgs[i]);
                        if (DBG) {
                            Log.d(TAG, "getSameUserIdPkgs:   " + sameUserIdPkgs[i]);
                        }
                    }
                    i++;
                }
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException:" + pkg);
            e.printStackTrace();
        }
        return pkgs;
    }

    public static void updateWidgetIfLauncherChanged(Context context, String launcher) {
    }

    public static String getCurrentLaunherName(Context context) {
        return context.getSharedPreferences(CURRENT_LAUNCHER_NAME, 4).getString("launcher_name", getDefaultLauncher(context.getPackageManager()));
    }

    public static void setCurrentLaunherName(Context context, String name) {
        Editor editor = context.getSharedPreferences(CURRENT_LAUNCHER_NAME, 4).edit();
        editor.putString("launcher_name", name);
        editor.commit();
    }

    public static List<String> getForgroundPkgs(ActivityManager am) {
        List<String> topApps = new ArrayList();
        List<RunningTaskInfo> tasks = am.getRunningTasks(3);
        if (!tasks.isEmpty()) {
            int topSize;
            if (tasks.size() > 3) {
                topSize = 3;
            } else {
                topSize = tasks.size();
            }
            for (int i = 0; i < topSize; i++) {
                ComponentName topActivity = ((RunningTaskInfo) tasks.get(i)).topActivity;
                if (DBG) {
                    Log.d(TAG, "topActivity----" + topActivity);
                }
                topApps.add(topActivity.getPackageName());
            }
        }
        return topApps;
    }

    public static String getDefaultInputMethod(Context context) {
        List<InputMethodInfo> mInputMethodProperties = ((InputMethodManager) context.getSystemService("input_method")).getInputMethodList();
        String defaultInputMethod = null;
        try {
            defaultInputMethod = Secure.getString(context.getContentResolver(), "default_input_method");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (defaultInputMethod == null) {
            return "";
        }
        try {
            defaultInputMethod = defaultInputMethod.substring(0, defaultInputMethod.indexOf(47));
            Log.i(TAG, "getDefaultInputMethod: " + defaultInputMethod);
        } catch (Exception ex2) {
            defaultInputMethod = "";
            ex2.printStackTrace();
        }
        return defaultInputMethod;
    }

    public static String getDefaultLauncher(PackageManager pm) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 65536);
        if (resolveInfo == null || resolveInfo.activityInfo == null || resolveInfo.activityInfo.packageName == null) {
            return "";
        }
        return resolveInfo.activityInfo.packageName;
    }

    public static boolean isSystemApp(PackageManager pm, String packageName) {
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "NameNotFoundException: " + e.getMessage());
        }
        return isSystemApp(info);
    }

    public static boolean isSystemApp(ApplicationInfo info) {
        if (info != null && (info.flags & 1) == 0) {
            return false;
        }
        return true;
    }

    private static boolean isLocationEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService("location");
        return locationManager.isProviderEnabled("gps") || locationManager.isProviderEnabled("network");
    }

    public static boolean isAppMonitoringLocation(Context context, String packageName) {
        if (DBG) {
            Log.d(TAG, "isAppMonitoringLocation: " + packageName);
        }
        if (isLocationEnable(context)) {
            List<PackageOps> appOps = ((AppOpsManager) context.getSystemService("appops")).getPackagesForOps(new int[]{41, 42, 0, 1, 58});
            if (appOps == null) {
                return false;
            }
            for (PackageOps ops : appOps) {
                String opsPkgName = ops.getPackageName();
                if (opsPkgName.equals(packageName)) {
                    for (OpEntry entry : ops.getOps()) {
                        if (entry.isRunning()) {
                            String date = new SimpleDateFormat("yyyyMMddHHmmss").format(Long.valueOf(entry.getTime()));
                            if (DBG) {
                                Log.d(TAG, "entry.getTime() = " + date + ", entry.getDuration() = " + entry.getDuration());
                            }
                            if (DBG) {
                                Log.d(TAG, opsPkgName + " requested location");
                            }
                            return true;
                        }
                    }
                    continue;
                }
            }
            return false;
        } else if (!DBG) {
            return false;
        } else {
            Log.d(TAG, "isLocationEnable: false");
            return false;
        }
    }

    public static boolean isAppPlayingMusic(Context context, String packageName) {
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        try {
            return audioManager.isMusicActive() && audioManager.isAppInFocus(packageName);
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isAppDownloading(Context context, String packageName, HashMap<String, Long> map) {
        Long totalRx = (Long) map.get("TotalRx");
        if (totalRx == null) {
            return false;
        }
        long dtTotalRx = TrafficStats.getTotalRxBytes() - totalRx.longValue();
        Long startTm = (Long) map.get("StartTime");
        if (startTm == null) {
            return false;
        }
        long delayTime = System.currentTimeMillis() - startTm.longValue();
        long speed = ((1000 * dtTotalRx) / delayTime) / 1024;
        if (!isNetworkConnected(context) || dtTotalRx <= 0 || map.get(packageName) == null) {
            return false;
        }
        Long pkgRx = (Long) map.get(packageName);
        long lPkgRx = 0;
        if (pkgRx != null) {
            lPkgRx = pkgRx.longValue();
        }
        double uidScaleTotal = ((((double) (getUidRxBytes(context, packageName) - lPkgRx)) * 1.0d) / ((double) dtTotalRx)) * 100.0d;
        if (DBG) {
            Log.d(TAG, "UidRx/TotalRx: " + String.format("%.2f", new Object[]{Double.valueOf(uidScaleTotal)}) + "%, package: " + packageName);
        }
        if (DBG) {
            Log.d(TAG, "isAppDownloading----dtTotalRx:" + dtTotalRx + " delayTime:" + delayTime + " speed:" + speed + "k/s package:" + packageName + " packageBt:" + (getUidRxBytes(context, packageName) - lPkgRx));
        }
        if (uidScaleTotal < 20.0d || getUidRxBytes(context, packageName) - lPkgRx <= 10 * delayTime) {
            return false;
        }
        return true;
    }

    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    public static boolean isPhoneCalling(Context context) {
        return ((TelephonyManager) context.getSystemService("phone")).getCallState() != 0;
    }

    public static boolean isLiveWallpaper(Context ctx, String pkgName) {
        WallpaperInfo info = WallpaperManager.getInstance(ctx).getWallpaperInfo();
        if (info == null || !info.getPackageName().equals(pkgName)) {
            return false;
        }
        return true;
    }

    public static List<String> getLiveWallpaperPackageName(Context ctx) {
        List<String> ret = new ArrayList();
        List<ResolveInfo> infos = ctx.getPackageManager().queryIntentServices(new Intent("android.service.wallpaper.WallpaperService"), 128);
        if (infos.size() > 0) {
            for (ResolveInfo ri : infos) {
                ret.add(ri.serviceInfo.packageName);
            }
        }
        return ret;
    }

    public static long getUidRxBytes(Context context, String packageName) {
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info != null) {
            return TrafficStats.getUidRxBytes(info.uid);
        }
        return 0;
    }

    public static void setPkgRxBytesMap(Context context, PackageManager pm, HashMap<String, Long> map) {
        map.clear();
        map.put("TotalRx", Long.valueOf(TrafficStats.getTotalRxBytes()));
        List<String> proPkgs = BackgroundPolicyExecutor.getInstance(context).getWhiteListApp();
        for (String pkgName : PowerSavingUtils.getAllApList(context)) {
            if (!proPkgs.contains(pkgName)) {
                map.put(pkgName, Long.valueOf(getUidRxBytes(context, pkgName)));
            }
        }
        map.put("StartTime", Long.valueOf(System.currentTimeMillis()));
    }

    private static AtomicFile getSavedStateFile(int userId, String fileName) {
        return new AtomicFile(new File(Environment.getUserSystemDirectory(userId), fileName));
    }

    public static List<String> getWidgetPackageName(String launcher) {
        String hostTag;
        List<String> hostTags;
        List<String> pTagList;
        String widgetPkg;
        List<Map<String, String>> providerList = new ArrayList();
        List<Map<String, String>> hostList = new ArrayList();
        List<Map<String, String>> groupList = new ArrayList();
        List<String> pkgList = new ArrayList();
        try {
            AtomicFile file = getSavedStateFile(UserHandle.myUserId(), WIDGET_STATE_FILENAME);
            if (file != null) {
                InputStream stream = file.openRead();
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(stream, null);
                while (parser != null) {
                    int type = parser.next();
                    if (type == 2) {
                        String tag = parser.getName();
                        String pkg;
                        String tagAttribute;
                        if ("p".equals(tag)) {
                            pkg = parser.getAttributeValue(null, "pkg");
                            String cl = parser.getAttributeValue(null, "cl");
                            tagAttribute = parser.getAttributeValue(null, SaveData.TAG);
                            Map<String, String> provider = new HashMap();
                            provider.put(tagAttribute, pkg);
                            providerList.add(provider);
                        } else if ("h".equals(tag)) {
                            pkg = parser.getAttributeValue(null, "pkg");
                            tagAttribute = parser.getAttributeValue(null, SaveData.TAG);
                            Map<String, String> host = new HashMap();
                            host.put(pkg, tagAttribute);
                            hostList.add(host);
                        } else if ("g".equals(tag)) {
                            hostTag = parser.getAttributeValue(null, "h");
                            String providerString = parser.getAttributeValue(null, "p");
                            Map<String, String> group = new HashMap();
                            group.put(hostTag, providerString);
                            groupList.add(group);
                        }
                    }
                    if (type == 1) {
                        break;
                    }
                }
                hostTags = new ArrayList();
                for (Map<String, String> hMap : hostList) {
                    if (hMap.containsKey(launcher)) {
                        hostTags.add(hMap.get(launcher));
                    }
                }
                pTagList = new ArrayList();
                for (Map<String, String> gMap : groupList) {
                    for (String hostTag2 : hostTags) {
                        if (gMap.containsKey(hostTag2)) {
                            pTagList.add(gMap.get(hostTag2));
                        }
                    }
                }
                for (Map<String, String> pMap : providerList) {
                    for (String pTag : pTagList) {
                        if (pMap.containsKey(pTag)) {
                            widgetPkg = (String) pMap.get(pTag);
                            if (!pkgList.contains(widgetPkg)) {
                                pkgList.add(widgetPkg);
                                if (DBG) {
                                    Log.d(TAG, "widget pkg----" + widgetPkg);
                                }
                            }
                        }
                    }
                }
                return pkgList;
            }
            Log.d(TAG, "file == null ");
            return pkgList;
        } catch (Exception e) {
            e = e;
        } catch (Exception e2) {
            e = e2;
        } catch (Exception e22) {
            e = e22;
        } catch (Exception e222) {
            e = e222;
        } catch (Exception e2222) {
            e = e2222;
        }
        Exception e3;
        Log.d(TAG, "failed parsing " + e3);
        hostTags = new ArrayList();
        for (Map<String, String> hMap2 : hostList) {
            if (hMap2.containsKey(launcher)) {
                hostTags.add(hMap2.get(launcher));
            }
        }
        pTagList = new ArrayList();
        for (Map<String, String> gMap2 : groupList) {
            for (String hostTag22 : hostTags) {
                if (gMap2.containsKey(hostTag22)) {
                    pTagList.add(gMap2.get(hostTag22));
                }
            }
        }
        for (Map<String, String> pMap2 : providerList) {
            for (String pTag2 : pTagList) {
                if (pMap2.containsKey(pTag2)) {
                    widgetPkg = (String) pMap2.get(pTag2);
                    if (!pkgList.contains(widgetPkg)) {
                        pkgList.add(widgetPkg);
                        if (DBG) {
                            Log.d(TAG, "widget pkg----" + widgetPkg);
                        }
                    }
                }
            }
        }
        return pkgList;
    }

    public static boolean canForceStop(Context ctx, String pkgName) {
        boolean cta = PSUtils.isCTA(ctx);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",can not force stop.");
            return false;
        }
        ActivityManager am = (ActivityManager) ctx.getSystemService("activity");
        String defaultLauncher = getDefaultLauncher(ctx.getPackageManager());
        String defaultInput = getDefaultInputMethod(ctx);
        List<String> inputMethods = null;
        if ("".equalsIgnoreCase(defaultInput)) {
            if (DBG) {
                Log.d(TAG, "Can not get default IME---");
            }
            inputMethods = getAllInputMethods(ctx);
        }
        List<String> topApps = getForgroundPkgs(am);
        if (isSystemApp(ctx.getPackageManager(), pkgName)) {
            if (!DBG) {
                return false;
            }
            Log.d(TAG, "Ignore: system app--- " + pkgName);
            return false;
        } else if (pkgName.equals(defaultLauncher)) {
            if (!DBG) {
                return false;
            }
            Log.d(TAG, "Ignore: default launcher---" + pkgName);
            return false;
        } else if (pkgName.equals(defaultInput)) {
            if (!DBG) {
                return false;
            }
            Log.d(TAG, "Ignore: default IME---" + pkgName);
            return false;
        } else if (inputMethods == null || !inputMethods.contains(pkgName)) {
            if (topApps.contains(pkgName)) {
                if (!DBG) {
                    return false;
                }
                Log.d(TAG, "Ignore: top 3 of running tasks---" + pkgName);
                return false;
            } else if (isAppPlayingMusic(ctx, pkgName)) {
                if (!DBG) {
                    return false;
                }
                Log.d(TAG, "Ignore: is Playing---" + pkgName);
                return false;
            } else if (ctx.getResources().getBoolean(C0321R.bool.monitor_location) && isAppMonitoringLocation(ctx, pkgName)) {
                if (!DBG) {
                    return false;
                }
                Log.d(TAG, "Ignore: is locating---" + pkgName);
                return false;
            } else if (!isLiveWallpaper(ctx, pkgName)) {
                return true;
            } else {
                if (!DBG) {
                    return false;
                }
                Log.d(TAG, "Ignore: LiveWallpaper---" + pkgName);
                return false;
            }
        } else if (!DBG) {
            return false;
        } else {
            Log.d(TAG, "Ignore: IME---" + pkgName);
            return false;
        }
    }

    public static List<String> getAllInputMethods(Context context) {
        List<InputMethodInfo> inputMethodProperties = ((InputMethodManager) context.getSystemService("input_method")).getEnabledInputMethodList();
        List<String> lstInputMethod = new ArrayList();
        int N = inputMethodProperties.size();
        for (int i = 0; i < N; i++) {
            lstInputMethod.add(((InputMethodInfo) inputMethodProperties.get(i)).getPackageName());
        }
        return lstInputMethod;
    }

    public static List<String> getMapApps(Context context) {
        List<String> mapApps = Arrays.asList(context.getResources().getStringArray(C0321R.array.mappapps));
        return mapApps != null ? mapApps : new ArrayList();
    }

    public static List<RecentTaskInfo> getRecentList(ActivityManager am) {
        return am.getRecentTasks(20, 2);
    }

    public static boolean isMediaRouteProviderServiceApp(PackageManager pm, String pkg) {
        for (ResolveInfo ri : pm.queryIntentServices(new Intent("android.media.MediaRouteProviderService"), 0)) {
            if (ri.serviceInfo != null && ri.serviceInfo.packageName.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> getCustAccountTypes(PackageManager pm) {
        Intent intent = new Intent(CUST_ACCOUNT_ACTION);
        ArrayList<String> retList = new ArrayList();
        for (ResolveInfo resolved : pm.queryIntentServices(intent, 128)) {
            ServiceInfo serviceInfo = resolved.serviceInfo;
            Bundle metaData = serviceInfo.metaData;
            if (metaData == null || !metaData.containsKey(CUST_ACCOUNT_METADATA_TYPE)) {
                Log.d(TAG, "Found " + serviceInfo.name + " for intent " + intent + " missing metadata ");
            } else {
                retList.add(metaData.getString(CUST_ACCOUNT_METADATA_TYPE));
            }
        }
        return retList;
    }

    public static boolean isAccountAuthenticator(String serviceName, PackageManager pm) {
        for (ResolveInfo ri : pm.queryIntentServices(new Intent("android.accounts.AccountAuthenticator"), 0)) {
            if (ri.serviceInfo.name.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInAuthList(Context context, PackageManager pm, String pkgName) {
        List<String> accountToHide = Arrays.asList(context.getResources().getStringArray(C0321R.array.settings_account_to_hide));
        AuthenticatorDescription[] authDescs = AccountManager.get(context).getAuthenticatorTypes();
        int i = 0;
        while (i < authDescs.length) {
            String accountType = authDescs[i].type;
            if (!getCustAccountTypes(pm).contains(accountType) && !accountToHide.contains(accountType) && authDescs[i].packageName.equals(pkgName)) {
                return true;
            }
            i++;
        }
        return false;
    }

    public static List<Integer> getPkgImportancesofAllProcesses(ActivityManager am, String pkgName) {
        List<Integer> importances = new ArrayList();
        int importance = FileOperator.MAX_DIR_LENGTH;
        for (RunningAppProcessInfo appProcess : am.getRunningAppProcesses()) {
            String appPkgname = appProcess.processName.split(":")[0];
            if (appPkgname == null) {
                appPkgname = "";
            }
            if (pkgName.equals(appPkgname) && importance > appProcess.importance) {
                importance = appProcess.importance;
                importances.add(Integer.valueOf(importance));
            }
        }
        if (importances.size() == 0) {
            importances.add(Integer.valueOf(FileOperator.MAX_DIR_LENGTH));
        }
        Collections.sort(importances);
        return importances;
    }

    public static boolean isScreenOn(Context context) {
        if (((DisplayManager) context.getSystemService("display")).getDisplay(0).getState() == 2) {
            return true;
        }
        return false;
    }

    public static List<InputMethodInfo> getInputMethods(Context context) {
        return ((InputMethodManager) context.getSystemService("input_method")).getInputMethodList();
    }

    public static List<String> getPackagesbyOPs(int[] codes, Context context) {
        List<String> pkgs = new ArrayList();
        List<PackageOps> appOps = ((AppOpsManager) context.getSystemService("appops")).getPackagesForOps(codes);
        if (appOps != null) {
            for (PackageOps ops : appOps) {
                pkgs.add(ops.getPackageName());
            }
        }
        return pkgs;
    }

    public static boolean isPhoneRing(Context context) {
        if (((TelephonyManager) context.getSystemService("phone")).getCallState() == 1) {
            return true;
        }
        return false;
    }

    public static boolean hasMethod(Class<?> klass, String methodName, Class<?>... paramTypes) {
        try {
            klass.getDeclaredMethod(methodName, paramTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static List<String> getPackagesbyOPAndMode(int code, int mode, Context context) {
        List<String> pkgs = new ArrayList();
        AppOpsManager aoManager = (AppOpsManager) context.getSystemService("appops");
        List<PackageOps> appOps = aoManager.getPackagesForOps(new int[]{code});
        if (appOps != null) {
            for (PackageOps ops : appOps) {
                String opsPkgName = ops.getPackageName();
                int uid = PSUtils.getUid(context, opsPkgName);
                if (uid != -1 && aoManager.checkOp(code, uid, opsPkgName) == mode) {
                    pkgs.add(opsPkgName);
                }
            }
        }
        return pkgs;
    }

    public static UsageStats getAppUsageStats(Context context, String pkg) {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService("usagestats");
        Calendar cal = Calendar.getInstance();
        cal.add(6, -1);
        List<UsageStats> stats = mUsageStatsManager.queryUsageStats(4, cal.getTimeInMillis(), System.currentTimeMillis());
        if (stats == null) {
            return null;
        }
        UsageStats usageStats = null;
        int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            UsageStats pkgStats = (UsageStats) stats.get(i);
            try {
                if (pkgStats.getPackageName().equals(pkg)) {
                    if (usageStats == null) {
                        usageStats = pkgStats;
                    } else {
                        usageStats.add(pkgStats);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return usageStats;
    }

    @Nullable
    public static String getDefaultSmsAppPackageName(@NonNull Context context) {
        if (VERSION.SDK_INT >= 19) {
            return Sms.getDefaultSmsPackage(context);
        }
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(new Intent("android.intent.action.VIEW").addCategory("android.intent.category.DEFAULT").setType("vnd.android-dir/mms-sms"), 0);
        if (resolveInfos == null || resolveInfos.isEmpty()) {
            return null;
        }
        return ((ResolveInfo) resolveInfos.get(0)).activityInfo.packageName;
    }
}
