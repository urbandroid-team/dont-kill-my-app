package com.evenwell.powersaving.g3.appops;

import android.app.AppOpsManager;
import android.content.Intent;
import android.util.Log;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.List;

public class UpdateBootCompleteService extends UpdateAppOpsService {
    private static final String TAG = "UpdateBootCompleteService";
    public static final String UPDATE_ALL_APPS_BC = "update_all_apps_boot_complete";
    public static final String UPDATE_APPS_BC = "update_apps_boot_complete";
    public static final String UPDATE_APPS_BC_CN = "update_apps_boot_complete_cn";

    public UpdateBootCompleteService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        try {
            updateBootCompleteOps(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBootCompleteOps(Intent intent) {
        boolean cta = PSUtils.isCTA(this);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",Ignore update boot complete AppOps.");
            return;
        }
        Log.i(TAG, "intent.getAction() = " + intent.getAction());
        int code = AppOpsManager.strOpToOp("android:boot_completed");
        List<String> bootApps;
        if (intent.getAction().equals(UPDATE_ALL_APPS_BC)) {
            if (BackgroundPolicyExecutor.getInstance(this).isCNModel()) {
                bootApps = BackgroundPolicyExecutor.getInstance(this).getWhiteListApp(4);
            } else {
                bootApps = BackgroundPolicyExecutor.getInstance(this).getWhiteListApp(1);
            }
            List<String> allApps = PSUtils.getAllApps(this);
            allApps.removeAll(bootApps);
            updateAppOps(allApps, code, 1);
            updateAppOps(bootApps, code, 0);
        } else if (intent.getAction().equals(UPDATE_APPS_BC)) {
            apps = intent.getStringArrayListExtra(UpdateAppOpsService.KEY_APPS);
            if (apps != null) {
                int mode = intent.getIntExtra(UpdateAppOpsService.KEY_MODE, -1);
                if (mode != -1) {
                    updateAppOps(apps, code, mode);
                }
            }
        } else if (intent.getAction().equals(UPDATE_APPS_BC_CN)) {
            apps = intent.getStringArrayListExtra(UpdateAppOpsService.KEY_APPS);
            if (apps != null) {
                bootApps = BackgroundPolicyExecutor.getInstance(this).getWhiteListApp(4);
                List<String> ignoreApps = new ArrayList();
                ignoreApps.addAll(apps);
                ignoreApps.removeAll(bootApps);
                updateAppOps(ignoreApps, code, 1);
                apps.removeAll(ignoreApps);
                updateAppOps(apps, code, 0);
            }
        }
    }
}
