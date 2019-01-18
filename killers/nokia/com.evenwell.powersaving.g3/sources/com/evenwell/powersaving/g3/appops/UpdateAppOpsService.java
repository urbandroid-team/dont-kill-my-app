package com.evenwell.powersaving.g3.appops;

import android.app.AppOpsManager;
import android.app.IntentService;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.List;

public abstract class UpdateAppOpsService extends IntentService {
    public static final String KEY_APPS = "key_apps";
    public static final String KEY_MODE = "key_mode";
    private final String TAG;

    public UpdateAppOpsService(String name) {
        super(name);
        this.TAG = "[PowerSavingAppG3]" + name;
    }

    protected String modeToString(int mode) {
        switch (mode) {
            case 0:
                return "MODE_ALLOWED";
            case 1:
                return "MODE_IGNORED";
            case 2:
                return "MODE_ERRORED";
            case 3:
                return "MODE_DEFAULT";
            default:
                return String.valueOf(mode);
        }
    }

    protected void updateAppOps(List<String> apps, int code, int mode) {
        AppOpsManager aom = (AppOpsManager) getSystemService("appops");
        for (String pkgName : apps) {
            int uid = PSUtils.getUid(this, pkgName);
            if (!(uid == -1 || UpdateAppOpsHelper.checkOps(this, code, uid, pkgName) == mode)) {
                aom.setMode(code, uid, pkgName, mode);
                Log.i(this.TAG, "uid=" + uid + ",pkgName=" + pkgName + ",op=" + AppOpsManager.opToName(code) + ",mode=" + modeToString(mode));
            }
        }
    }
}
