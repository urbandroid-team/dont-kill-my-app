package com.evenwell.powersaving.g3.appops;

import android.content.Intent;
import android.util.Log;
import java.util.List;

public class UpdateGPSOpsService extends UpdateAppOpsService {
    private static final String TAG = "[PowerSavingAppG3]UpdateGPSOpsService";
    public static final String TARGET_ARRAY_EXTRA = "target_array";
    public static final String UPDATE_APPS_GPS = "update_apps_gps";

    public UpdateGPSOpsService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        try {
            updateGPSOps(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateGPSOps(Intent intent) {
        if (intent != null) {
            int mode = intent.getIntExtra(UpdateAppOpsService.KEY_MODE, -1);
            String action = intent.getAction();
            Log.d(TAG, "Action = " + action + ", mode = " + mode);
            if (!UPDATE_APPS_GPS.equals(action)) {
                return;
            }
            if (mode == 0) {
                List<String> apps_OP_GPS_ToAllow = intent.getStringArrayListExtra(TARGET_ARRAY_EXTRA);
                Log.d(TAG, "apps_OP_GPS_ToAllow : " + apps_OP_GPS_ToAllow);
                if (apps_OP_GPS_ToAllow != null) {
                    updateAppOps(apps_OP_GPS_ToAllow, 2, mode);
                }
            } else if (mode == 1) {
                List<String> apps_OP_GPS_ToIgnore = intent.getStringArrayListExtra(TARGET_ARRAY_EXTRA);
                Log.d(TAG, "apps_OP_GPS_ToIgnore : " + apps_OP_GPS_ToIgnore);
                if (apps_OP_GPS_ToIgnore != null) {
                    updateAppOps(apps_OP_GPS_ToIgnore, 2, mode);
                }
            } else {
                Log.d(TAG, "unknown mode " + mode);
            }
        }
    }
}
