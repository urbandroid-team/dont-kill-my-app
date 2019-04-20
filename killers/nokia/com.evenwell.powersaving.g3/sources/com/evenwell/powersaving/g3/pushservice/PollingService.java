package com.evenwell.powersaving.g3.pushservice;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.IBinder;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.CHECK_CP_REASON;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.util.Calendar;

public class PollingService extends IntentService {
    public static final String ACTION_DETECT_PULL_TIME = "com.evenwell.powersaving.g3.detect_pull_time";
    public static final String ACTION_REGISTER_DEVICE = "com.evenwell.powersaving.g3.register_device";
    private static final boolean DBG = true;
    public static final String EXTRA_KEY_ACTION = "ACTION";
    private static final long ONE_DAY = 86400000;
    private static final long ONE_HOUR = 3600000;
    public static int POLLING_INTERVAL = 90;
    private static final int RETRY_INTERVAL = 180000;
    private static final int RETRY_TIMES = 5;
    private static String TAG = TAG.PSLOG;
    public static boolean isServiceLive = false;
    private static int mRetryTimes = 0;
    private int interval;
    private long mDelayTimeToAlarm = 2592000000L;
    private PullServerCommand mPullServerCommand;

    public PollingService() {
        super("PollingService");
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "[PollingService]: onHandleIntent Action = " + intent.getStringExtra(EXTRA_KEY_ACTION));
        ConnectivityManager connectMgr = (ConnectivityManager) getSystemService("connectivity");
        if (intent.getStringExtra(EXTRA_KEY_ACTION) != null) {
            NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(0);
            NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(1);
            this.mPullServerCommand = new PullServerCommand(this);
            if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()) {
                if (intent.getStringExtra(EXTRA_KEY_ACTION).equalsIgnoreCase(ACTION_REGISTER_DEVICE)) {
                    connectMgr.unregisterNetworkCallback(getPendingIntent(intent.getStringExtra(EXTRA_KEY_ACTION)));
                    if (this.mPullServerCommand.checkRegisterDevice()) {
                        this.mPullServerCommand.RegisterDevice(PackageCategory.BLACK_LIST);
                        this.mPullServerCommand.RegisterDevice(PackageCategory.WHITE_LIST);
                        return;
                    }
                    return;
                }
                if (intent.getStringExtra(EXTRA_KEY_ACTION).equalsIgnoreCase(ACTION_DETECT_PULL_TIME)) {
                    cancelAlarmToCheckPullTime();
                    connectMgr.unregisterNetworkCallback(getPendingIntent(intent.getStringExtra(EXTRA_KEY_ACTION)));
                    if (checkPullTime()) {
                        Log.i(TAG, "[PollingService] checkPullTime = " + mRetryTimes);
                        mRetryTimes = 0;
                        setAlarmDayChange(this.mDelayTimeToAlarm);
                        return;
                    } else if (mRetryTimes < 5) {
                        mRetryTimes++;
                        Log.d(TAG, "[PollingService] retry times = " + mRetryTimes);
                        setAlarmDayChange((long) (RETRY_INTERVAL * mRetryTimes));
                        return;
                    } else {
                        PowerSavingUtils.SetPreferencesStatus((Context) this, PSSPREF.PULL_SERVER_TIME, getCurrentTimeInMs());
                        Log.d(TAG, "[PollingService] don't retry = " + mRetryTimes);
                        mRetryTimes = 0;
                        setAlarmDayChange(this.mDelayTimeToAlarm);
                        return;
                    }
                }
                return;
            }
            Log.d(TAG, "[PollingService]: no network , Register networkcallback to " + intent.getStringExtra(EXTRA_KEY_ACTION) + " when network is on");
            Builder builderForWifi = new Builder();
            builderForWifi.addCapability(12).addTransportType(1);
            NetworkRequest networkRequestForWifi = builderForWifi.build();
            Builder builderForCellular = new Builder();
            builderForCellular.addCapability(12).addTransportType(0);
            NetworkRequest networkRequestForCellular = builderForCellular.build();
            PendingIntent pi = getPendingIntent(intent.getStringExtra(EXTRA_KEY_ACTION));
            try {
                connectMgr.unregisterNetworkCallback(pi);
                connectMgr.unregisterNetworkCallback(pi);
                connectMgr.registerNetworkCallback(networkRequestForWifi, pi);
                connectMgr.registerNetworkCallback(networkRequestForCellular, pi);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean checkPullTime() {
        long now = getCurrentTimeInMs();
        Log.i(TAG, "[PollingService] checkPullTime now :" + now);
        boolean pullBlackSuccess;
        if (PowerSavingUtils.GetPreferencesStatusLong(this, PSSPREF.PULL_SERVER_TIME) == -1) {
            Log.i(TAG, "[PollingService] No PULL_SERVER_TIME, set time first and polling to server!");
            pullBlackSuccess = this.mPullServerCommand.CheckCP(PackageCategory.BLACK_LIST, CHECK_CP_REASON.reqular_polling);
            if (!pullBlackSuccess) {
                return pullBlackSuccess;
            }
            Log.d(TAG, "[PollingService]SetPreferencesStatus PULL_SERVER_TIME : " + now);
            PowerSavingUtils.SetPreferencesStatus((Context) this, PSSPREF.PULL_SERVER_TIME, now);
            return pullBlackSuccess;
        }
        long pref_time = PowerSavingUtils.GetPreferencesStatusLong(this, PSSPREF.PULL_SERVER_TIME);
        Log.i(TAG, "[PollingService] Has  PULL_SERVER_TIME = " + pref_time);
        long dayDiff = (now - pref_time) / ONE_DAY;
        Log.i(TAG, "[PollingService] dayDiff = " + dayDiff);
        if (now > pref_time) {
            ConnectivityManager connectMgr = (ConnectivityManager) getSystemService("connectivity");
            NetworkInfo mobNetInfo = connectMgr.getNetworkInfo(0);
            NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(1);
            this.interval = PowerSavingUtils.GetPreferencesStatusInt(this, PSSPREF.POLLING_INTERVAL);
            Log.d(TAG, "[PollingService] polling  interval : " + this.interval);
            if (dayDiff < ((long) this.interval)) {
                Log.d(TAG, "[PollingService]dayDiff < interval no need pull server");
                return true;
            } else if (mobNetInfo.isConnected() || wifiNetInfo.isConnected()) {
                Log.d(TAG, "[PollingService]Has network,start pull server ");
                pullBlackSuccess = this.mPullServerCommand.CheckCP(PackageCategory.BLACK_LIST, CHECK_CP_REASON.reqular_polling);
                Log.d(TAG, "[PollingService]pullBlackSuccess : " + pullBlackSuccess);
                if (!pullBlackSuccess) {
                    return pullBlackSuccess;
                }
                Log.d(TAG, "[PollingService]SetPreferencesStatus PULL_SERVER_TIME : " + now);
                PowerSavingUtils.SetPreferencesStatus((Context) this, PSSPREF.PULL_SERVER_TIME, now);
                return pullBlackSuccess;
            } else {
                Log.d(TAG, "[PollingService]No network to pull server ");
                return false;
            }
        }
        Log.i(TAG, "Error Time! ");
        return false;
    }

    private void setAlarmDayChange(long delay) {
        if (delay > 0) {
            Log.i(TAG, "[PollingService]setAlarmDayChange, delay " + delay);
            AlarmManager am = (AlarmManager) getSystemService("alarm");
            Intent intent = new Intent(this, PollingService.class);
            intent.putExtra(EXTRA_KEY_ACTION, ACTION_DETECT_PULL_TIME);
            PendingIntent pi = null;
            try {
                pi = PendingIntent.getService(this, 1, intent, 134217728);
            } catch (Exception e) {
                Log.e(TAG, "[PollingService]AlarmManager failed to start " + e.toString());
            }
            am.setExactAndAllowWhileIdle(1, System.currentTimeMillis() + delay, pi);
        }
    }

    private void cancelAlarmToCheckPullTime() {
        Intent intent = new Intent(this, PollingService.class);
        intent.putExtra(EXTRA_KEY_ACTION, ACTION_DETECT_PULL_TIME);
        ((AlarmManager) getSystemService("alarm")).cancel(PendingIntent.getService(this, 1, intent, 134217728));
    }

    private void checkAndSetPollingInterVal() {
        if (PowerSavingUtils.GetPreferencesStatusInt(this, PSSPREF.POLLING_INTERVAL) == -1) {
            PowerSavingUtils.SetPreferencesStatus((Context) this, PSSPREF.POLLING_INTERVAL, POLLING_INTERVAL);
        }
    }

    private PendingIntent getPendingIntent(String action) {
        Intent intent = new Intent(this, PollingService.class);
        intent.putExtra(EXTRA_KEY_ACTION, action);
        PendingIntent pi = null;
        try {
            pi = PendingIntent.getService(this, 1, intent, 134217728);
        } catch (Exception e) {
            Log.e(TAG, "[PollingService] failed to start " + e.toString());
        }
        return pi;
    }

    public void onDestroy() {
        super.onDestroy();
        isServiceLive = false;
    }

    private long getCurrentTimeInMs() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.getTimeInMillis();
    }
}
