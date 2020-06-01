package com.evenwell.powersaving.g3.background;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.util.CollectionUtils;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.appops.UpdateAppOpsHelper;
import com.evenwell.powersaving.g3.component.RestrictedUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.utils.AppStandbyBucketUtils;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.evenwell.powersaving.g3.utils.PackageManagerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net2.lingala.zip4j.util.InternalZipConstants;

public class BAMMode {
    private static final boolean DBG = true;
    private static final String KEY_ADAPTIVE_BATTERY_MANAGEMENT_ENABLED = "key_adaptive_battery_management_enabled";
    private static final String KEY_APP_STANDBY_ENABLED = "key_app_standby_enabled";
    private static final String KEY_RESTRICTED_APPS = "key_restricted_apps";
    public static final int MODE_FORCE_STOP = 0;
    public static final int MODE_PMS_DEBUG_00WW = 4;
    public static final int MODE_RESTRICTED = 3;
    public static final int MODE_RUN_ANY_IN_BACKGROUND = 2;
    public static final int MODE_SET_APP_STANDBY_BUCKET = 1;
    private static final String TAG = "BAMMode";
    private ActivityManager mAm;
    protected Context mContext;

    public @interface Mode {
    }

    public BAMMode(Context context) {
        this.mContext = context;
        this.mAm = (ActivityManager) context.getSystemService("activity");
    }

    public String modeToString() {
        switch (getMode()) {
            case 0:
                return "MODE_FORCE_STOP";
            case 1:
                return "MODE_SET_APP_STANDBY_BUCKET";
            case 2:
                return "MODE_RUN_ANY_IN_BACKGROUND";
            case 3:
                return "MODE_RESTRICTED";
            case 4:
                return "MODE_PMS_DEBUG_00WW";
            default:
                return "Error";
        }
    }

    public String modeForLog(String pkgName) {
        List<String> examptApps = UpdateAppOpsHelper.getExamptApp(this.mContext);
        switch (getMode()) {
            case 0:
                return "forceStopPackage " + pkgName;
            case 1:
                return "setAppStandbyBucket " + pkgName + " to Frequent";
            case 2:
                return "setRunAnyInBackground " + pkgName + " to ignore";
            case 3:
                String log = "";
                if (BackgroundPolicyExecutor.getInstance(this.mContext).getDisAutoAppList().contains(pkgName)) {
                    log = "setAppStandbyBucket " + pkgName + " to Frequent";
                }
                if (examptApps.contains(pkgName)) {
                    return log;
                }
                if (!TextUtils.isEmpty(log)) {
                    log = log + ", and ";
                }
                return log + "setRunAnyInBackground " + pkgName + " to ignore";
            case 4:
                if (hasRestrictComponent(pkgName)) {
                    return "forceStopPackage " + pkgName;
                }
                return "setAppStandbyBucket " + pkgName + " to Frequent";
            default:
                return "";
        }
    }

    public String modeForDatabase(String extra, String pkgName) {
        String reason = "";
        List<String> examptApps = UpdateAppOpsHelper.getExamptApp(this.mContext);
        switch (getMode()) {
            case 0:
                reason = "";
                break;
            case 1:
                reason = "b";
                break;
            case 2:
                if (!examptApps.contains(pkgName)) {
                    reason = InternalZipConstants.READ_MODE;
                    break;
                }
                break;
            case 3:
                if (BackgroundPolicyExecutor.getInstance(this.mContext).getDisAutoAppList().contains(pkgName)) {
                    reason = "b";
                }
                if (!examptApps.contains(pkgName)) {
                    reason = reason + InternalZipConstants.READ_MODE;
                    break;
                }
                break;
            case 4:
                if (!hasRestrictComponent(pkgName)) {
                    reason = "b";
                    break;
                }
                reason = "";
                break;
            default:
                reason = "";
                break;
        }
        reason = reason + extra;
        if (TextUtils.isEmpty(reason)) {
            return reason;
        }
        return "(" + reason + ")";
    }

    public boolean hasRestrictComponent(String pkgName) {
        Set<String> components = PackageManagerUtils.getAllComponents(this.mContext, pkgName);
        components.retainAll(RestrictedUtils.getRestrictedComponents(this.mContext));
        return CollectionUtils.size(components) > 0;
    }

    @Mode
    public int getMode() {
        if (PSUtils.isCNModel(this.mContext)) {
            return 0;
        }
        return 3;
    }

    public boolean process(String pkgName) {
        int mode = getMode();
        boolean success = true;
        if (mode == 0) {
            RestrictedUtils.restricted(this.mContext, pkgName, true);
            this.mAm.forceStopPackage(pkgName);
            return true;
        } else if (mode == 1) {
            setAppStandByBucketSettingsIfNeed();
            return AppStandbyBucketUtils.setAppStandbyBucketIfLarger(this.mContext, pkgName, 30);
        } else if (mode == 2) {
            if (!UpdateAppOpsHelper.getExamptApp(this.mContext).contains(pkgName)) {
                success = UpdateAppOpsHelper.updateRunAnyInBackgroundOps(this.mContext, pkgName, 1);
            }
            if (!success) {
                return success;
            }
            saveRestrictedApp(pkgName);
            return success;
        } else if (mode == 3) {
            if (BackgroundPolicyExecutor.getInstance(this.mContext).getDisAutoAppList().contains(pkgName)) {
                setAppStandByBucketSettingsIfNeed();
                success = AppStandbyBucketUtils.setAppStandbyBucketIfLarger(this.mContext, pkgName, 30);
            }
            if (UpdateAppOpsHelper.getExamptApp(this.mContext).contains(pkgName)) {
                Log.i(TAG, pkgName + " is in examptApp, ignore set run_any_in_background.");
            } else {
                success = UpdateAppOpsHelper.updateRunAnyInBackgroundOps(this.mContext, pkgName, 1);
            }
            if (!success) {
                return success;
            }
            saveRestrictedApp(pkgName);
            return success;
        } else if (mode != 4) {
            Log.i(TAG, "mode = " + mode);
            return false;
        } else if (hasRestrictComponent(pkgName)) {
            RestrictedUtils.restricted(this.mContext, pkgName, true);
            this.mAm.forceStopPackage(pkgName);
            return true;
        } else {
            setAppStandByBucketSettingsIfNeed();
            return AppStandbyBucketUtils.setAppStandbyBucketIfLarger(this.mContext, pkgName, 30);
        }
    }

    public void recoverStandybyBucketSettingsIfNeed() {
        int appStandByEnabled = PowerSavingUtils.GetPreferencesStatusInt(this.mContext, KEY_APP_STANDBY_ENABLED);
        if (appStandByEnabled != -1) {
            Global.putInt(this.mContext.getContentResolver(), "app_standby_enabled", appStandByEnabled);
        }
        PowerSavingUtils.removePreferneceStatus(this.mContext, KEY_APP_STANDBY_ENABLED);
        int adaptiveBatteryManagementEnabled = PowerSavingUtils.GetPreferencesStatusInt(this.mContext, KEY_ADAPTIVE_BATTERY_MANAGEMENT_ENABLED);
        if (adaptiveBatteryManagementEnabled != -1) {
            Global.putInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", adaptiveBatteryManagementEnabled);
        }
        PowerSavingUtils.removePreferneceStatus(this.mContext, KEY_ADAPTIVE_BATTERY_MANAGEMENT_ENABLED);
        appStandByEnabled = Global.getInt(this.mContext.getContentResolver(), "app_standby_enabled", 1);
        adaptiveBatteryManagementEnabled = Global.getInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", 1);
        Log.i(TAG, "recover appStandByEnabled=" + appStandByEnabled);
        Log.i(TAG, "recover adaptiveBatteryManagementEnabled=" + adaptiveBatteryManagementEnabled);
    }

    private void setAppStandByBucketSettingsIfNeed() {
        int appStandByEnabled = Global.getInt(this.mContext.getContentResolver(), "app_standby_enabled", 1);
        int adaptiveBatteryManagementEnabled = Global.getInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", 1);
        if (appStandByEnabled != 1) {
            PowerSavingUtils.SetPreferencesStatus(this.mContext, KEY_APP_STANDBY_ENABLED, appStandByEnabled);
            Global.putInt(this.mContext.getContentResolver(), "app_standby_enabled", 1);
            Log.i(TAG, "appStandByEnabled to 1 from " + appStandByEnabled);
        }
        if (adaptiveBatteryManagementEnabled != 1) {
            PowerSavingUtils.SetPreferencesStatus(this.mContext, KEY_ADAPTIVE_BATTERY_MANAGEMENT_ENABLED, adaptiveBatteryManagementEnabled);
            Global.putInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", 1);
            Log.i(TAG, "adaptiveBatteryManagementEnabled to 1 from " + adaptiveBatteryManagementEnabled);
        }
    }

    public void recoverRestrictedApps() {
        String restrictedAppsString = PowerSavingUtils.GetPreferencesStatusString(this.mContext, KEY_RESTRICTED_APPS);
        if (!TextUtils.isEmpty(restrictedAppsString)) {
            UpdateAppOpsHelper.updateRunAnyInBackgroundOps(this.mContext, new ArrayList(Arrays.asList(TextUtils.split(restrictedAppsString, "\\|"))), 0);
            PowerSavingUtils.removePreferneceStatus(this.mContext, KEY_RESTRICTED_APPS);
            Log.i(TAG, "recover restrictedAppsString=" + restrictedAppsString);
        }
    }

    private void saveRestrictedApp(String restrictedApp) {
        if (!TextUtils.isEmpty(restrictedApp)) {
            Set<String> apps = new ArraySet();
            String oldString = PowerSavingUtils.GetPreferencesStatusString(this.mContext, KEY_RESTRICTED_APPS);
            if (!TextUtils.isEmpty(oldString)) {
                apps.addAll(Arrays.asList(TextUtils.split(oldString, "\\|")));
            }
            apps.add(restrictedApp);
            String restrictedAppsString = TextUtils.join("|", apps);
            PowerSavingUtils.SetPreferencesStatus(this.mContext, KEY_RESTRICTED_APPS, restrictedAppsString);
            Log.i(TAG, "set restrictedAppsString=" + restrictedAppsString);
        }
    }
}
