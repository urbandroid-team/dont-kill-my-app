package com.evenwell.powersaving.g3.background;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.provider.DisAutoStartProvider;

public class CheckDBService extends IntentService {
    public static final String FORCE_REFRESH = "force_refresh";
    private static final String TAG = "CheckDBService";

    public CheckDBService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent start");
        boolean forceRefresh = false;
        if (intent != null && FORCE_REFRESH.equals(intent.getAction())) {
            forceRefresh = true;
        }
        Log.d(TAG, "forceRefresh = " + forceRefresh);
        BackgroundPolicyExecutor.getInstance(this).checkDB(this, forceRefresh);
        DisAutoStartProvider.isCheckDB = true;
        PowerSavingUtils.SetCheckDataBaseStatus(this, true);
    }
}
