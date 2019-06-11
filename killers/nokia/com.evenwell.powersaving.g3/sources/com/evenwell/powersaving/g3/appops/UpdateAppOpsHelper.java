package com.evenwell.powersaving.g3.appops;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.util.CollectionUtils;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.List;

public class UpdateAppOpsHelper {
    private static final String TAG = "UpdateAppOpsHelper";

    public static void UpdateBackgroundOps(Context context, ArrayList<String> apps, int mode) {
        if (!CollectionUtils.isEmpty(apps)) {
            Intent updateBackgroundOpsService = new Intent(context, UpdateBackgroundOpsService.class);
            updateBackgroundOpsService.setAction(UpdateBackgroundOpsService.UPDATE_APPS_BG);
            updateBackgroundOpsService.putExtra(UpdateAppOpsService.KEY_MODE, mode);
            updateBackgroundOpsService.putStringArrayListExtra(UpdateAppOpsService.KEY_APPS, apps);
            context.startServiceAsUser(updateBackgroundOpsService, UserHandle.CURRENT);
        }
    }

    public static void updateRunAnyInBackgroundOps(Context context, ArrayList<String> pkgNames, int mode) {
        if (!CollectionUtils.isEmpty(pkgNames)) {
            Intent updateRunAnyInBackgroundOpsService = new Intent(context, UpdateRunAnyInBackgroundOpsService.class);
            updateRunAnyInBackgroundOpsService.setAction(UpdateRunAnyInBackgroundOpsService.UPDATE_APPS_RUN_ANY);
            updateRunAnyInBackgroundOpsService.putExtra(UpdateAppOpsService.KEY_MODE, mode);
            updateRunAnyInBackgroundOpsService.putStringArrayListExtra(UpdateAppOpsService.KEY_APPS, pkgNames);
            context.startServiceAsUser(updateRunAnyInBackgroundOpsService, UserHandle.CURRENT);
        }
    }

    public static boolean updateRunAnyInBackgroundOps(Context context, String pkgName, int mode) {
        if (mode == checkRunAnyInBackgroundOps(context, pkgName)) {
            return false;
        }
        ArrayList pkgNames = new ArrayList();
        pkgNames.add(pkgName);
        updateRunAnyInBackgroundOps(context, pkgNames, mode);
        return true;
    }

    public static int checkRunAnyInBackgroundOps(Context context, String pkgName) {
        int uid = PSUtils.getUid(context, pkgName);
        if (uid == -1) {
            return 2;
        }
        return checkOps(context, 70, uid, pkgName);
    }

    public static int checkOps(Context context, int code, int uid, String pkgName) {
        return ((AppOpsManager) context.getSystemService("appops")).checkOpNoThrow(code, uid, pkgName);
    }

    public static boolean isPreOApp(Context context, String packageName) {
        try {
            if (context.getPackageManager().getApplicationInfo(packageName, 128).targetSdkVersion < 26) {
                return true;
            }
            return false;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find package: " + packageName, e);
            return false;
        }
    }

    public static List<String> getExamptApp(Context context) {
        List<String> ret = new ArrayList();
        for (ResolveInfo ri : context.getPackageManager().queryBroadcastReceivers(new Intent("android.media.AUDIO_BECOMING_NOISY"), 0)) {
            if (ri.activityInfo != null) {
                ret.add(ri.activityInfo.packageName);
            }
        }
        return ret;
    }
}
