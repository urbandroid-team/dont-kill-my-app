package com.evenwell.powersaving.g3.exception;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.appops.UpdateAppOpsHelper;
import com.evenwell.powersaving.g3.appops.UpdateAppOpsService;
import com.evenwell.powersaving.g3.appops.UpdateBootCompleteService;
import com.evenwell.powersaving.g3.background.BAMMode;
import com.evenwell.powersaving.g3.background.BackgroundCleanUtil;
import com.evenwell.powersaving.g3.component.RestrictedUtils;
import com.evenwell.powersaving.g3.exception.BlackFile.BlackList;
import com.evenwell.powersaving.g3.provider.BackDataDb;
import com.evenwell.powersaving.g3.provider.WakePathInfo;
import com.evenwell.powersaving.g3.pushservice.PackageCategory;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BackgroundPolicyExecutor {
    private static final String DATA_APK_PATH = "/data/app/";
    private static final boolean DBG = true;
    public static final int DISAUTO_LIST_CURRENT = 1;
    public static final int DISAUTO_LIST_DEVICE_DEFAULT = 2;
    private static final String TAG = "[PowerSavingAppG3]BackgroundPolicyExecutor";
    public static final int WHITE_LIST_BOOT = 4;
    public static final int WHITE_LIST_CTS = 8;
    public static final int WHITE_LIST_CURRENT = 1;
    public static final int WHITE_LIST_SYSTEM = 2;
    public static final int WHITE_LIST_XML_SYSTEM_APP = 32;
    public static final int WHITE_LIST_XML_USER_INSTALL = 64;
    private static BackgroundPolicyExecutor mInstance;
    private ActivityManager am;
    private Context ctx;
    private boolean isCN;
    private boolean isClearPhone;
    private List<String> listNonStop = new ArrayList();
    private List<String> mAppsTobePreload;
    private BackDataDb mBackDataDb;
    private BackgroundCleanWhitelist mBackgroundCleanWhitelist;
    private BlackFile mBlackFile;
    private List<String> mDataApks;
    private List<String> mDisautoWhiteList;
    private List<String> mExemptPrefix;
    private List<String> mHideNonSystemAppList;
    private PowerWhitelistBackend mPowerWhitelistBackend;
    private List<String> mSystemAppisNeedToShow;

    private BackgroundPolicyExecutor(Context context) {
        boolean z;
        this.ctx = context;
        this.am = (ActivityManager) this.ctx.getSystemService("activity");
        this.isCN = this.ctx.getResources().getBoolean(C0321R.bool.region_cn);
        for (String pkg : this.ctx.getResources().getStringArray(C0321R.array.powersaving_nonforcestop_ap)) {
            this.listNonStop.add(pkg);
        }
        this.mDisautoWhiteList = Arrays.asList(this.ctx.getResources().getStringArray(C0321R.array.disauto_white_list));
        this.mHideNonSystemAppList = Arrays.asList(this.ctx.getResources().getStringArray(C0321R.array.hide_app_list_in_bam));
        this.mSystemAppisNeedToShow = Arrays.asList(this.ctx.getResources().getStringArray(C0321R.array.app_list_is_needed_to_show_in_bam));
        this.mAppsTobePreload = Arrays.asList(this.ctx.getResources().getStringArray(C0321R.array.apps_to_be_preloaded));
        this.mExemptPrefix = Arrays.asList(this.ctx.getResources().getStringArray(C0321R.array.exempt_prefix));
        this.mPowerWhitelistBackend = PowerWhitelistBackend.getInstance(this.ctx);
        this.mBackgroundCleanWhitelist = BackgroundCleanWhitelist.getInstance(this.ctx);
        this.mBackDataDb = new BackDataDb(this.ctx);
        this.mBlackFile = BlackFile.getInstance(this.ctx);
        try {
            this.mDataApks = getDataAPks();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        List<String> whitelistPrev = getWhiteListApp();
        if (getDisAutoAppList().size() == 0 && whitelistPrev.size() == 0) {
            z = true;
        } else {
            z = false;
        }
        this.isClearPhone = z;
    }

    private List<String> getDataAPks() {
        Log.d(TAG, "data apk Path: /data/app/");
        File[] files = new File(DATA_APK_PATH).listFiles();
        List<String> array = new ArrayList();
        for (File name : files) {
            array.add(name.getName());
        }
        return array;
    }

    public static BackgroundPolicyExecutor getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new BackgroundPolicyExecutor(ctx);
        }
        return mInstance;
    }

    public boolean isCNModel() {
        return this.isCN;
    }

    public void checkDB(Context context, boolean forceRefresh) {
        if (this.isCN) {
            List<String> apps = PowerSavingUtils.getAllApList(context);
            CheckDBCN_BAM(context, forceRefresh, apps);
            Log.d(TAG, "CheckDBCN_BAM");
            CheckDBCN_Disauto(context, forceRefresh, apps);
            Log.d(TAG, "CheckDBCN_Disauto");
            syncDozeWhiteList();
            Log.d(TAG, "syncDozeWhiteList");
            return;
        }
        checkDBWW(context, forceRefresh);
        Log.d(TAG, "checkDBWW");
    }

    private void CheckDBCN_Disauto(Context context, boolean forceRefresh, List<String> apps) {
        SharedPreferences prefStatus = context.getSharedPreferences(FILENAME.NEW_ADD_PRELOAD_APP_STATUS_FILE, 0);
        List<String> ctsList = getWhiteListApp(8);
        BlackList disautoBlackList = this.mBlackFile.getDisautoBlackList();
        List<String> system_app_list = PSUtils.getSystemApps(this.ctx);
        if (disautoBlackList.isNeedToRefresh || forceRefresh) {
            List<String> disautokListPrev = getDisAutoAppList();
            List<String> excludeList = new ArrayList();
            for (String app : disautokListPrev) {
                if (system_app_list.contains(app) && !this.mSystemAppisNeedToShow.contains(app)) {
                    removeAppFromDisAutoList(app);
                } else if (this.mHideNonSystemAppList.contains(app)) {
                    removeAppFromDisAutoList(app);
                } else {
                    Log.d(TAG, "[checkDB] add app " + app + " to exclude list");
                    excludeList.add(app);
                }
            }
            Log.d(TAG, "[checkDB] disautokListPrev size = " + disautokListPrev.size() + ",isClearPhone  = " + this.isClearPhone);
            Log.d(TAG, "[checkDB] mSystemAppisNeedToShow " + this.mSystemAppisNeedToShow);
            Log.d(TAG, "[checkDB] mHideNonSystemAppList " + this.mHideNonSystemAppList);
            Log.d(TAG, "[checkDB] excludeList " + excludeList);
            Log.d(TAG, "[checkDB] disautoBlackList  " + disautoBlackList);
            for (String app2 : apps) {
                if (disautoBlackList.contains(app2)) {
                    if (this.isClearPhone) {
                        addAppToDisAutoList(app2);
                        Log.i(TAG, "[checkDB] 1 addAppToDisAutoList : " + app2);
                    }
                    boolean bisInDataApp = isInDataApp(app2);
                    Log.d(TAG, "[checkDB] " + app2 + " bisInDataApp = " + bisInDataApp);
                    if (this.mAppsTobePreload.contains(app2) && !bisInDataApp) {
                        String tag = app2 + "_disautoisset";
                        Log.d(TAG, "[checkDB] mAppsTobePreload " + this.mAppsTobePreload);
                        boolean isPreSet = prefStatus.getBoolean(tag, false);
                        Log.d(TAG, "[checkDB] isPreSet = " + isPreSet + ", isInDisautoList() " + isInDisautoList(app2));
                        if (!isPreSet) {
                            if (!isInDisautoList(app2)) {
                                addAppToDisAutoList(app2);
                                Log.i(TAG, "[checkDB] 2 addAppToDisAutoList : " + app2);
                            }
                            prefStatus.edit().putBoolean(tag, true).commit();
                        }
                    }
                } else if (ctsList.contains(app2)) {
                    removeAppFromDisAutoList(app2);
                    addAppToDozeWhiteList(app2);
                }
            }
            return;
        }
        for (String app22 : apps) {
            if (ctsList.contains(app22)) {
                Log.i(TAG, "[checkDB] deleteFromDisAutoStartDb " + app22);
                removeAppFromDisAutoList(app22);
                addAppToDozeWhiteList(app22);
            }
        }
    }

    private void CheckDBCN_BAM(Context context, boolean forceRefresh, List<String> apps) {
        SharedPreferences prefStatus = context.getSharedPreferences(FILENAME.NEW_ADD_PRELOAD_APP_STATUS_FILE, 0);
        List<String> ctsList = getWhiteListApp(8);
        BlackList bamBlackList = this.mBlackFile.getBAMBlackList();
        List<String> system_app_list = PSUtils.getSystemApps(this.ctx);
        List<PowerSaverExceptionAppInfoItem> appAreNeedToShow = getAllApList(false);
        String postfix = "_bamisset";
        if (this.mBlackFile.getBAMBlackList().isNeedToRefresh || forceRefresh) {
            Log.d(TAG, "[checkDB] whitelistPrev size = " + getWhiteListApp().size());
            for (String app : apps) {
                if (bamBlackList.contains(app)) {
                    if (system_app_list.contains(app) && !this.mSystemAppisNeedToShow.contains(app)) {
                        Log.i(TAG, "[checkDB] 1 removeAppFromWhiteList " + app + " from whiteList");
                        removeAppFromWhiteList(app);
                    }
                    if (this.mHideNonSystemAppList.contains(app)) {
                        Log.i(TAG, "[checkDB] 2 removeAppFromWhiteList " + app + " from whiteList");
                        removeAppFromWhiteList(app);
                    }
                    boolean bisInDataApp = isInDataApp(app);
                    Log.d(TAG, "[checkDB] " + app + " bisInDataApp = " + bisInDataApp);
                    if (this.mAppsTobePreload.contains(app) && !bisInDataApp) {
                        String tag = app + postfix;
                        Log.d(TAG, "[checkDB] mAppsTobePreload " + this.mAppsTobePreload);
                        boolean isPreSet = prefStatus.getBoolean(tag, false);
                        Log.d(TAG, "[checkDB] isPreSet = " + isPreSet + ", isWhitelisted() " + isWhitelisted(app));
                        if (!isPreSet) {
                            removeAppFromWhiteList(app);
                            Log.i(TAG, "[checkDB] 1 removeAppFromWhiteList : " + app);
                            prefStatus.edit().putBoolean(tag, true).commit();
                        }
                    }
                } else if (this.isClearPhone) {
                    addAppToWhiteList(app);
                    Log.i(TAG, "[checkDB] 1 addAppToWhiteList " + app + " to whiteList");
                } else {
                    boolean bCanBeAddToWhitelist = true;
                    for (PowerSaverExceptionAppInfoItem item : appAreNeedToShow) {
                        if (item.GetPackageName().equals(app)) {
                            bCanBeAddToWhitelist = false;
                            break;
                        }
                    }
                    if (this.mAppsTobePreload.contains(app)) {
                        bCanBeAddToWhitelist = true;
                    }
                    Log.d(TAG, "[checkDB] " + app + " bCanBeAddToWhitelist " + bCanBeAddToWhitelist);
                    if (bCanBeAddToWhitelist) {
                        addAppToWhiteList(app);
                        Log.i(TAG, "[checkDB] 2 addAppToWhiteList " + app + " to whiteList");
                    }
                }
            }
            return;
        }
        for (String app2 : apps) {
            if (ctsList.contains(app2) && !isWhitelisted(app2)) {
                Log.i(TAG, "[checkDB] addAppToWhiteList " + app2 + " to whiteList");
                addAppToWhiteList(app2);
            }
        }
    }

    private void checkDBWW(Context context, boolean forceRefresh) {
        boolean z = false;
        SharedPreferences prefStatus = context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_STATUS_FILE, 0);
        Boolean isRefresh = Boolean.valueOf(prefStatus.getBoolean(PSSPREF.IS_REFRESH, false));
        List<String> apps = PowerSavingUtils.getLauncherApList(context);
        List<String> ctsList = getWhiteListApp(8);
        if (!isRefresh.booleanValue()) {
            z = true;
        }
        int i;
        if (Boolean.valueOf(z).booleanValue() || forceRefresh) {
            i = 0;
            while (i < apps.size()) {
                if (isInDisautoList((String) apps.get(i)) || isWhitelisted((String) apps.get(i))) {
                    Log.d(TAG, "[checkDB] " + ((String) apps.get(i)) + " has set before");
                } else {
                    Log.d(TAG, "[checkDB] addAppToWhiteList " + ((String) apps.get(i)));
                    addAppToWhiteList((String) apps.get(i));
                    Log.d(TAG, "[checkDB] removeAppFromDisAutoList " + ((String) apps.get(i)));
                    removeAppFromDisAutoList((String) apps.get(i));
                }
                i++;
            }
        } else {
            for (i = 0; i < apps.size(); i++) {
                if (ctsList.contains(apps.get(i))) {
                    Log.d(TAG, "[checkDB] addAppToWhiteList " + ((String) apps.get(i)));
                    addAppToWhiteList((String) apps.get(i));
                    Log.d(TAG, "[checkDB] removeAppFromDisAutoList " + ((String) apps.get(i)));
                    removeAppFromDisAutoList((String) apps.get(i));
                }
            }
        }
        prefStatus.edit().putBoolean(PSSPREF.IS_REFRESH, true).commit();
    }

    public List<PowerSaverExceptionAppInfoItem> getAllApList() {
        return getAllApList(true);
    }

    public List<String> getAppsShowOnBAMUI() {
        Log.d(TAG, "mHideNonSystemAppList = " + this.mHideNonSystemAppList);
        Log.d(TAG, "mSystemAppisNeedToShow = " + this.mSystemAppisNeedToShow);
        List<String> ret = new ArrayList();
        List<ApplicationInfo> packages = this.ctx.getPackageManager().getInstalledApplications(0);
        List<String> system_app_list = PSUtils.getSystemApps(this.ctx);
        for (ApplicationInfo packageInfo : packages) {
            if (system_app_list == null || !system_app_list.contains(packageInfo.packageName)) {
                if (!this.mHideNonSystemAppList.contains(packageInfo.packageName)) {
                    ret.add(packageInfo.packageName);
                }
            } else if (isCNModel() && this.mSystemAppisNeedToShow.contains(packageInfo.packageName)) {
                ret.add(packageInfo.packageName);
            }
        }
        return ret;
    }

    public List<PowerSaverExceptionAppInfoItem> getAllApList(boolean bloadIcon) {
        List<PowerSaverExceptionAppInfoItem> app = new ArrayList();
        PackageManager pm = this.ctx.getPackageManager();
        HighPower highPower = new HighPower(new File(this.ctx.getFilesDir(), PackageCategory.BLACK_LIST.getValue()));
        for (String pkg : getAppsShowOnBAMUI()) {
            PowerSaverExceptionAppInfoItem appinfoitem = new PowerSaverExceptionAppInfoItem();
            if (pm.getLaunchIntentForPackage(pkg) != null) {
                ApplicationInfo applicationInfo = new ApplicationInfo();
                try {
                    applicationInfo = pm.getApplicationInfo(pkg, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                appinfoitem.mUid = applicationInfo.uid;
                appinfoitem.mPackageName = pkg;
                appinfoitem.mAppName = applicationInfo.loadLabel(pm).toString();
                if (bloadIcon) {
                    appinfoitem.mIcon = applicationInfo.loadIcon(pm);
                } else {
                    appinfoitem.mIcon = null;
                }
                String packageVersion = "";
                try {
                    packageVersion = this.ctx.getPackageManager().getPackageInfo(appinfoitem.mPackageName, 128).versionName.trim();
                } catch (Exception e) {
                    packageVersion = "unKnown";
                }
                if (highPower.APInHighPowerList(appinfoitem.mPackageName, packageVersion)) {
                    appinfoitem.mHighConsumption = true;
                } else {
                    appinfoitem.mHighConsumption = false;
                }
                app.add(appinfoitem);
            }
        }
        return app;
    }

    public synchronized List<String> getWhiteListApp() {
        return getWhiteListApp(1);
    }

    public synchronized List<String> getWhiteListApp(int flag) {
        List<String> ret;
        int i = 0;
        synchronized (this) {
            int length;
            String[] system;
            ret = new ArrayList();
            if ((flag & 1) == 1) {
                this.mBackgroundCleanWhitelist.refreshList();
                Set<String> mWhiteList = this.mBackgroundCleanWhitelist.getWhiteList();
                if (mWhiteList != null) {
                    for (String pkg : mWhiteList) {
                        ret.add(pkg);
                    }
                }
            }
            if ((flag & 2) == 2) {
                this.mPowerWhitelistBackend.refreshList();
                Set<String> mSysWhiteList = this.mPowerWhitelistBackend.geDozeWhiteList();
                if (mSysWhiteList != null) {
                    for (String pkg2 : mSysWhiteList) {
                        ret.add(pkg2);
                    }
                }
            }
            if ((flag & 4) == 4) {
                String[] boot = this.ctx.getResources().getStringArray(C0321R.array.boot_app);
                if (boot != null) {
                    for (String pkg22 : boot) {
                        ret.add(pkg22);
                    }
                }
            }
            if ((flag & 8) == 8) {
                String[] cts = this.ctx.getResources().getStringArray(C0321R.array.cts_app);
                if (cts != null) {
                    for (String pkg222 : cts) {
                        ret.add(pkg222);
                    }
                }
            }
            if ((flag & 32) == 32) {
                system = this.ctx.getResources().getStringArray(C0321R.array.powersaving_dozemode_system_app);
                if (system != null) {
                    for (String pkg2222 : system) {
                        ret.add(pkg2222);
                    }
                }
            }
            if ((flag & 64) == 64) {
                if (this.isCN) {
                    system = this.ctx.getResources().getStringArray(C0321R.array.bam_white_list);
                } else {
                    system = this.ctx.getResources().getStringArray(C0321R.array.bam_white_list_ww);
                }
                if (system != null) {
                    length = system.length;
                    while (i < length) {
                        ret.add(system[i]);
                        i++;
                    }
                }
            }
        }
        return ret;
    }

    public List<String> getDisAutoAppList() {
        return getDisAutoAppList(1);
    }

    public List<String> getDisAutoAppList(int flag) {
        List<String> ret = new ArrayList();
        if ((flag & 1) == 1) {
            ret.addAll(this.mBackDataDb.getAllDisAutoStartPkg());
        }
        if ((flag & 2) == 2) {
            BlackList list = this.mBlackFile.getDisautoBlackList();
            if (list != null) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    ret.add((String) it.next());
                }
            }
        }
        return ret;
    }

    private void applyPolicy(String pkgName, boolean isForbid) {
        int mode;
        long id = getWakeUpId(this.ctx, pkgName);
        if (id > 0) {
            PowerSavingUtils.setForbidStatu(this.ctx, id, isForbid);
        }
        BAMMode bamMode = new BAMMode(this.ctx);
        if (isForbid) {
            if (!this.listNonStop.contains(pkgName) && BackgroundCleanUtil.canForceStop(this.ctx, pkgName)) {
                Log.d(TAG, "forceStopPackage " + pkgName);
                if (this.isCN) {
                    if (bamMode.getMode() == 0) {
                        RestrictedUtils.restricted(this.ctx, pkgName, true);
                    }
                    this.am.forceStopPackage(pkgName);
                    try {
                        PowerSavingUtils.addForceStoppedApp(this.ctx, pkgName);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            PowerSavingUtils.addToDisAutoStart(this.ctx, pkgName);
        } else {
            if (bamMode.getMode() == 0) {
                RestrictedUtils.restricted(this.ctx, pkgName, false);
            }
            PowerSavingUtils.deleteFromDisAutoStartDb(this.ctx, pkgName);
        }
        Intent updateBootCompleteService = new Intent(this.ctx, UpdateBootCompleteService.class);
        if (this.isCN) {
            updateBootCompleteService.setAction(UpdateBootCompleteService.UPDATE_APPS_BC_CN);
        } else {
            if (isForbid) {
                mode = 1;
            } else {
                mode = 0;
            }
            updateBootCompleteService.setAction(UpdateBootCompleteService.UPDATE_APPS_BC);
            updateBootCompleteService.putExtra(UpdateAppOpsService.KEY_MODE, mode);
        }
        ArrayList<String> bootApps = new ArrayList();
        bootApps.add(pkgName);
        updateBootCompleteService.putStringArrayListExtra(UpdateAppOpsService.KEY_APPS, bootApps);
        this.ctx.startService(updateBootCompleteService);
        if (isForbid) {
            mode = 1;
        } else {
            mode = 0;
        }
        ArrayList<String> backgroundApps = new ArrayList();
        backgroundApps.add(pkgName);
        UpdateAppOpsHelper.UpdateBackgroundOps(this.ctx, backgroundApps, mode);
    }

    public void addAppToWhiteList(String pkgName) {
        Log.i(TAG, "[BackgroundPolicyExecutor] addAppToWhiteList : " + pkgName);
        this.mBackgroundCleanWhitelist.add(pkgName);
    }

    public void addAppsToWhiteList(List<PowerSaverExceptionAppInfoItem> appsList) {
        Log.i(TAG, "[BackgroundPolicyExecutor] addAppsToWhiteList");
        for (PowerSaverExceptionAppInfoItem ap : appsList) {
            addAppToWhiteList(ap.GetPackageName());
        }
    }

    public void addAppsPkgToWhiteList(List<String> appsList) {
        Log.i(TAG, "[BackgroundPolicyExecutor] addAppsPkgToWhiteList");
        for (String ap : appsList) {
            addAppToWhiteList(ap);
        }
    }

    public void removeAppFromWhiteList(String pkgName) {
        Log.i(TAG, "[BackgroundPolicyExecutor] removeAppFromWhiteList: " + pkgName);
        this.mBackgroundCleanWhitelist.remove(pkgName);
    }

    public void removeAppsFromWhiteList(List<PowerSaverExceptionAppInfoItem> appsList) {
        List<String> packageList = new ArrayList();
        for (PowerSaverExceptionAppInfoItem ap : appsList) {
            removeAppFromWhiteList(ap.GetPackageName());
            packageList.add(ap.GetPackageName());
        }
    }

    public boolean isSysWhitelisted(String pkg) {
        return this.mPowerWhitelistBackend.isDozeWhitelisted(pkg);
    }

    public boolean isWhitelisted(String pkg) {
        return this.mBackgroundCleanWhitelist.isWhitelisted(pkg);
    }

    private long getWakeUpId(Context context, String pkg) {
        List<WakePathInfo> wakeList = PowerSavingUtils.getWakeList(context);
        for (int i = 0; i < wakeList.size(); i++) {
            if (pkg.equalsIgnoreCase(((WakePathInfo) wakeList.get(i)).mPackageName)) {
                return ((WakePathInfo) wakeList.get(i)).id;
            }
        }
        return 0;
    }

    public void addAppToDisAutoList(String pkgName) {
        Log.i(TAG, "[BackgroundPolicyExecutor] addAppToDisAutoList : " + pkgName);
        applyPolicy(pkgName, true);
    }

    public void addAppsToDisAutoList(List<PowerSaverExceptionAppInfoItem> appsList) {
        Log.i(TAG, "[BackgroundPolicyExecutor] addAppsToDisAutoList");
        for (PowerSaverExceptionAppInfoItem ap : appsList) {
            addAppToDisAutoList(ap.GetPackageName());
        }
    }

    public void addAppsPkgToDisAutoList(List<String> appsList) {
        Log.i(TAG, "[BackgroundPolicyExecutor] addAppsPkgToDisAutoList");
        for (String ap : appsList) {
            addAppToDisAutoList(ap);
        }
    }

    public void removeAppFromDisAutoList(String pkgName) {
        Log.i(TAG, "[BackgroundPolicyExecutor] removeAppFromDisAutoList: " + pkgName);
        applyPolicy(pkgName, false);
    }

    public void removePkgsFromDisAutoList(List<String> appsList) {
        Log.i(TAG, "[BackgroundPolicyExecutor] removeAppsFromDisAutoList: ");
        for (String app : appsList) {
            removeAppFromDisAutoList(app);
        }
    }

    public void removeAppsFromDisAutoList(List<PowerSaverExceptionAppInfoItem> appsList) {
        for (PowerSaverExceptionAppInfoItem ap : appsList) {
            removeAppFromDisAutoList(ap.GetPackageName());
        }
    }

    public boolean isInDisautoList(String pkg) {
        return this.mBackDataDb.hasDisAutoStartPkg(pkg);
    }

    public boolean isInDisautoWhiteList(String pkg) {
        return this.mDisautoWhiteList.contains(pkg) || getWhiteListApp(8).contains(pkg);
    }

    public void addAppToDozeWhiteList(String pkg) {
        this.mPowerWhitelistBackend.add(pkg);
    }

    public void addAppsToDozeWhiteList(List<PowerSaverExceptionAppInfoItem> appsList) {
        Log.i(TAG, "[BackgroundPolicyExecutor] addAppsToDisAutoList");
        for (PowerSaverExceptionAppInfoItem ap : appsList) {
            addAppToDozeWhiteList(ap.GetPackageName());
        }
    }

    public void removeAppFromDozeWhiteList(String pkg) {
        this.mPowerWhitelistBackend.remove(pkg);
    }

    public void removeAppsFromDozeWhiteList(List<PowerSaverExceptionAppInfoItem> appsList) {
        for (PowerSaverExceptionAppInfoItem ap : appsList) {
            removeAppFromDozeWhiteList(ap.GetPackageName());
        }
    }

    public List<String> getHideNonSystemAppList() {
        return this.mHideNonSystemAppList;
    }

    public List<String> getSystemAppisNeedToShow() {
        return this.mSystemAppisNeedToShow;
    }

    private boolean isInDataApp(String pkgName) {
        try {
            for (String app : this.mDataApks) {
                if (app.startsWith(pkgName)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void syncDozeWhiteList() {
        List<PowerSaverExceptionAppInfoItem> appsShowInBamList = getAllApList(false);
        List<String> disautoApps = getDisAutoAppList();
        for (PowerSaverExceptionAppInfoItem appItem : appsShowInBamList) {
            if (disautoApps.contains(appItem.GetPackageName())) {
                removeAppFromDozeWhiteList(appItem.GetPackageName().trim());
                Log.d(TAG, "remove " + appItem.GetPackageName() + " to Doze WhiteList");
            } else {
                addAppToDozeWhiteList(appItem.GetPackageName().trim());
                Log.d(TAG, "add " + appItem.GetPackageName() + " to Doze WhiteList");
            }
        }
    }

    public List<String> getExemptPrefix() {
        return this.mExemptPrefix;
    }
}
