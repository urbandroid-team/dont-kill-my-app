package com.evenwell.powersaving.g3.appops;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import com.android.internal.util.CollectionUtils;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.List;

public class UpdateRunAnyInBackgroundOpsService extends UpdateAppOpsService {
    private static final String TAG = "UpdateRunAnyInBackgroundOpsService";
    public static final String UPDATE_APPS_RUN_ANY = "update_apps_run_any";
    private PackageManager mPackageManager;

    public UpdateRunAnyInBackgroundOpsService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        try {
            this.mPackageManager = getPackageManager();
            UpdateRunAnyInBackgroundOps(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void UpdateRunAnyInBackgroundOps(Intent intent) {
        boolean cta = PSUtils.isCTA(this);
        boolean cts = PSUtils.isCTS();
        if (cta || cts) {
            Log.i(TAG, "cta = " + cta + ",cts = " + cts + ",Ignore UpdateRunAnyInBackgroundOpsService.");
            return;
        }
        Log.i(TAG, "intent.getAction() = " + intent.getAction());
        if (intent.getAction().equals(UPDATE_APPS_RUN_ANY)) {
            int mode = intent.getIntExtra(UpdateAppOpsService.KEY_MODE, -1);
            List<String> apps = intent.getStringArrayListExtra(UpdateAppOpsService.KEY_APPS);
            if (mode == 1) {
                apps.removeAll(UpdateAppOpsHelper.getExamptApp(this));
            }
            if (!CollectionUtils.isEmpty(apps) && mode != -1) {
                updateAppOps(apps, 70, mode);
            }
        }
    }
}
