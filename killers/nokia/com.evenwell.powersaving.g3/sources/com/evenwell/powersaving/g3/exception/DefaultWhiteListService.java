package com.evenwell.powersaving.g3.exception;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.android.internal.util.CollectionUtils;
import com.evenwell.powersaving.g3.background.PackageService;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.List;

public class DefaultWhiteListService extends IntentService {
    private static final String TAG = "DefaultWhiteListService";

    public DefaultWhiteListService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(this);
        if (!BPE.isCNModel()) {
            List<String> systemApps = PSUtils.getSystemApps(this);
            List<String> whiteListApps = BPE.getWhiteListApp();
            List<String> disautoApps = BPE.getDisAutoAppList();
            List<String> blackToWhite = new ArrayList();
            for (String app : disautoApps) {
                if (systemApps.contains(app)) {
                    blackToWhite.add(app);
                }
            }
            Log.d(TAG, "blackToWhite" + blackToWhite);
            Log.d(TAG, "disautoApps " + disautoApps);
            systemApps.removeAll(whiteListApps);
            systemApps.addAll(blackToWhite);
            Log.i(TAG, "systemApps.size() = " + systemApps.size() + ", systemApps = " + systemApps);
            BPE.addAppsPkgToWhiteList(systemApps);
            BPE.removePkgsFromDisAutoList(systemApps);
            try {
                updateOutOfControlApp();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startService(new Intent(this, PackageService.class));
    }

    private void updateOutOfControlApp() {
        BackgroundPolicyExecutor BPE = BackgroundPolicyExecutor.getInstance(this);
        List<String> allApps = PSUtils.getAllApps(this);
        List<String> whiteApps = BPE.getWhiteListApp(1);
        List<String> blackApps = BPE.getDisAutoAppList();
        allApps.removeAll(whiteApps);
        allApps.removeAll(blackApps);
        if (CollectionUtils.size(allApps) > 0) {
            for (String pkgName : allApps) {
                Log.i(TAG, "out of control app, add it to white list, pkgName = " + pkgName);
                PSUtils.packageAddOnWW(this, pkgName);
            }
        }
    }
}
