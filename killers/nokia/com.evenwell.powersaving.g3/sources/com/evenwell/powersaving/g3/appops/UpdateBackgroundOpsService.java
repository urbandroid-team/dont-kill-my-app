package com.evenwell.powersaving.g3.appops;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.List;

public class UpdateBackgroundOpsService extends UpdateAppOpsService {
    private static final String TAG = "UpdateBackgroundOpsService";
    public static final String UPDATE_ALL_APPS_BG = "update_all_apps_background";
    public static final String UPDATE_APPS_BG = "update_apps_background";
    private PackageManager mPackageManager;

    public UpdateBackgroundOpsService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        try {
            this.mPackageManager = getPackageManager();
            updateBackgroundOps(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBackgroundOps(Intent intent) {
        boolean cta = PSUtils.isCTA(this);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",Ignore update background AppOps.");
            return;
        }
        Log.i(TAG, "intent.getAction() = " + intent.getAction());
        List<String> examptApps = UpdateAppOpsHelper.getExamptApp(this);
        if (intent.getAction().equals(UPDATE_ALL_APPS_BG)) {
            BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(this);
            if (BPE.isCNModel()) {
                List<String> allApps = PowerSavingUtils.getAllApList(this);
                List<String> disautoApps = BPE.getDisAutoAppList();
                List<String> disautoApps_preo = new ArrayList();
                for (String pkg : disautoApps) {
                    if (UpdateAppOpsHelper.isPreOApp(this, pkg)) {
                        disautoApps_preo.add(pkg);
                    }
                }
                disautoApps_preo.removeAll(examptApps);
                allApps.removeAll(disautoApps);
                updateAppOps(disautoApps_preo, 63, 1);
                updateAppOps(allApps, 63, 0);
                return;
            }
            List<String> whiteList = BackgroundPolicyExecutor.getInstance(this).getWhiteListApp(1);
            List<String> launcherApps = PowerSavingUtils.getLauncherApList(this);
            List<String> launcherApps_preo = new ArrayList();
            launcherApps.removeAll(whiteList);
            for (String pkg2 : launcherApps) {
                if (UpdateAppOpsHelper.isPreOApp(this, pkg2)) {
                    launcherApps_preo.add(pkg2);
                }
            }
            launcherApps_preo.removeAll(examptApps);
            updateAppOps(launcherApps_preo, 63, 1);
            updateAppOps(whiteList, 63, 0);
        } else if (intent.getAction().equals(UPDATE_APPS_BG)) {
            List<String> apps = intent.getStringArrayListExtra(UpdateAppOpsService.KEY_APPS);
            if (apps != null) {
                int mode = intent.getIntExtra(UpdateAppOpsService.KEY_MODE, -1);
                if (mode != -1) {
                    List<String> apps_preo = new ArrayList();
                    for (String pkg22 : apps) {
                        if (UpdateAppOpsHelper.isPreOApp(this, pkg22)) {
                            apps_preo.add(pkg22);
                        }
                    }
                    apps_preo.removeAll(examptApps);
                    updateAppOps(apps_preo, 63, mode);
                }
            }
        }
    }
}
