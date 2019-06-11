package com.evenwell.powersaving.g3.dataconnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import com.evenwell.powersaving.g3.LpmDcUtils;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.dataconnection.DataConnectionUtils.listenWakeupThread;
import com.evenwell.powersaving.g3.dataconnection.DataConnectionUtils.packetDetectThread;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.ScreeOnHandler;
import com.evenwell.powersaving.g3.utils.PSConst.DC.DCPARM.ALARMTYPE;
import com.evenwell.powersaving.g3.utils.PSConst.DC.DCPREF;
import com.evenwell.powersaving.g3.utils.PSConst.DC.INTENT;
import com.evenwell.powersaving.g3.utils.PSConst.DC.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.DC.TIME.VALUE;
import com.evenwell.powersaving.g3.utils.PSConst.LPM_AND_DC_APPLY.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.iFunctionMode;

public class DataConnection implements iFunctionMode {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    public static Handler handler = null;
    private static boolean isDCTime = false;
    public static ScreeOnHandler mHandler = null;
    public static listenWakeupThread mListenWakeupThread = null;
    public static packetDetectThread mPacketDetectThread = null;
    public static Runnable screenOffThread = null;
    private static Runnable screenOnThread = null;
    private Context mContext;
    private final BroadcastReceiver mDataConnectionReceiver = new C03401();

    /* renamed from: com.evenwell.powersaving.g3.dataconnection.DataConnection$1 */
    class C03401 extends BroadcastReceiver {
        C03401() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                DataConnection.this.mContext = context;
                String action = intent.getAction();
                if (action.equals("android.intent.action.SCREEN_ON")) {
                    Log.i(DataConnection.TAG, "[DataConnection] Receiver ACTION_SCREEN_ON");
                    if (DataConnection.isDCTime) {
                        if (DataConnection.screenOffThread != null) {
                            DataConnection.handler.removeCallbacks(DataConnection.screenOffThread);
                        }
                        DataConnection.handler.postDelayed(DataConnection.screenOnThread, 0);
                    }
                } else if (action.equals("android.intent.action.SCREEN_OFF")) {
                    Log.i(DataConnection.TAG, "[DataConnection] Receiver ACTION_SCREEN_OFF");
                    Log.i(DataConnection.TAG, "ScreenOFF isDCTime: " + DataConnection.isDCTime);
                    if (DataConnection.isDCTime) {
                        if (DataConnection.screenOnThread != null) {
                            DataConnection.handler.removeCallbacks(DataConnection.screenOnThread);
                        }
                        DataConnection.handler.postDelayed(DataConnection.screenOffThread, VALUE.SCREEN_OFF_WAIT_TIME);
                    }
                } else if (action.equals(ACTION.ACTION_LPM_APPLY_END_BUT_STILL_HAS_DC)) {
                    Log.i(DataConnection.TAG, "[LowPowerMode]: mLowPowerModeReceiver() ACTION_LPM_APPLY_END_BUT_STILL_HAS_DC");
                    DataConnectionUtils.saveToPref(context, DCPREF.MOBILE, DataConnectionUtils.isDataEnabled(context));
                    DataConnectionUtils.saveToPref(context, DCPREF.WIFI, PowerSavingUtils.GetWiFiEnableByDB(context));
                    DataConnectionUtils.setMobileDataEnabled(context, false);
                    DataConnectionUtils.setWifiEnable(context, false);
                } else if (action.equals(INTENT.ACTION.ACTION_DC_APPLY_TIME)) {
                    String mAlarmKey = intent.getStringExtra(EXTRA.TIME_KEY);
                    if (mAlarmKey == null) {
                        return;
                    }
                    if (mAlarmKey.equals(ALARMTYPE.START)) {
                        Log.i(DataConnection.TAG, "[DataConnection] Receiver ACTION_DC_APPLY_TIME for start time of arrival");
                        DataConnection.setIsDCTime(DataConnection.this.mContext, true);
                        DataConnectionUtils.checkScreenStateAndAction(DataConnection.this.mContext);
                    } else if (mAlarmKey.equals(ALARMTYPE.END)) {
                        Log.i(DataConnection.TAG, "[DataConnection] Receiver ACTION_DC_APPLY_TIME for end time of arrival");
                        DataConnection.setIsDCTime(DataConnection.this.mContext, false);
                        DataConnectionUtils.screenOnAction(DataConnection.this.mContext, DataConnection.mHandler);
                    }
                } else if (action.equals("android.intent.action.DATE_CHANGED") || action.equals("android.intent.action.TIME_SET") || action.equals("android.intent.action.TIMEZONE_CHANGED")) {
                    Log.i(DataConnection.TAG, "[DataConnection] Receiver TIME_DATE_TIMEZONE_CHANGE_ INTENT !!");
                    DataConnectionUtils.cancelStartOrEndAlarm(DataConnection.this.mContext, ALARMTYPE.START);
                    DataConnectionUtils.cancelStartOrEndAlarm(DataConnection.this.mContext, ALARMTYPE.END);
                    DataConnection.setIsDCTime(DataConnection.this.mContext, false);
                    DataConnectionUtils.screenOnAction(DataConnection.this.mContext, DataConnection.mHandler);
                    if (DataConnectionUtils.checkAlwaysOnOrNot(DataConnection.this.mContext)) {
                        Log.i(DataConnection.TAG, "[DataConnection] checkAlwaysOnOrNot : true");
                        DataConnection.setIsDCTime(DataConnection.this.mContext, true);
                        DataConnectionUtils.checkScreenStateAndAction(DataConnection.this.mContext);
                        return;
                    }
                    if (DataConnectionUtils.isInTimeInterval(DataConnection.this.mContext, DataConnectionUtils.getNowTime())) {
                        Log.i(DataConnection.TAG, "[DataConnection] after time change, in time interval");
                        DataConnection.setIsDCTime(DataConnection.this.mContext, true);
                        DataConnectionUtils.checkScreenStateAndAction(DataConnection.this.mContext);
                    } else {
                        Log.i(DataConnection.TAG, "[DataConnection] after time change, not in time interval");
                        DataConnection.setIsDCTime(DataConnection.this.mContext, false);
                    }
                    DataConnectionUtils.setStartOrEndAlarm(DataConnection.this.mContext, ALARMTYPE.START);
                    DataConnectionUtils.setStartOrEndAlarm(DataConnection.this.mContext, ALARMTYPE.END);
                } else if (action.equals(INTENT.ACTION.ACTION_DC_DO_SCREEN_OFF_ACTION)) {
                    Log.i(DataConnection.TAG, "[DataConnection] Receiver ACTION_DC_DO_SCREEN_OFF_ACTION !!");
                    DataConnectionUtils.screenOffAction(DataConnection.this.mContext);
                }
            }
        }
    }

    public DataConnection(Context context, int reason, ScreeOnHandler screenonhandler) {
        Log.i(TAG, "[DataConnection] init");
        this.mContext = context;
        if (handler == null) {
            handler = new Handler();
        }
        mHandler = screenonhandler;
        isDCTime = DataConnectionUtils.getFromPref(this.mContext, DCPREF.ISDCTIME);
        boolean IsDCApply = LpmDcUtils.IsDCApply(this.mContext);
        if (reason == 1) {
            if (IsDCApply) {
                boolean psEnabled = PowerSavingUtils.GetPowerSavingModeEnable(this.mContext);
                boolean dcEnabled = PowerSavingUtils.GetDataConnectionEnable(this.mContext);
                if (psEnabled && dcEnabled) {
                    Log.i(TAG, "[DataConnection]: [SERVICE_CRASH] PowerSaving / dc ON,still apply ,update");
                    return;
                }
                Log.i(TAG, "[DataConnection]: [SERVICE_CRASH] PowerSaving / dc OFF,need restore");
                DataConnectionUtils.screenOnAction(context, mHandler);
            }
        } else if (reason == 0 && IsDCApply) {
            Log.i(TAG, "[DataConnection]: [BOOT_COMPLETED] restore");
            DataConnectionUtils.screenOnAction(context, mHandler);
        }
    }

    public void registerReceiver(final Context ctx) {
        Log.i(TAG, "[DataConnection] registerReceiver()");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(INTENT.ACTION.ACTION_DC_APPLY_TIME);
        filter.addAction(ACTION.ACTION_LPM_APPLY_END_BUT_STILL_HAS_DC);
        filter.addAction("android.intent.action.DATE_CHANGED");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction(INTENT.ACTION.ACTION_DC_DO_SCREEN_OFF_ACTION);
        ctx.registerReceiver(this.mDataConnectionReceiver, filter);
        DataConnectionUtils.saveIntToPref(ctx, DCPREF.TXPACK, 0);
        DataConnectionUtils.saveIntToPref(ctx, DCPREF.RXPACK, 0);
        if (screenOffThread == null) {
            screenOffThread = new Runnable() {
                public void run() {
                    Log.i(DataConnection.TAG, "[DataConnection] screenoff thread");
                    ctx.sendBroadcast(new Intent(INTENT.ACTION.ACTION_DC_DO_SCREEN_OFF_ACTION));
                }
            };
        }
        if (screenOnThread == null) {
            screenOnThread = new Runnable() {
                public void run() {
                    DataConnectionUtils.screenOnAction(ctx, DataConnection.mHandler);
                }
            };
        }
        if (DataConnectionUtils.checkAlwaysOnOrNot(ctx)) {
            Log.i(TAG, "[DataConnection] register, now Time = Always");
            setIsDCTime(ctx, true);
            DataConnectionUtils.checkScreenStateAndAction(ctx);
            return;
        }
        if (DataConnectionUtils.isInTimeInterval(ctx, DataConnectionUtils.getNowTime())) {
            Log.i(TAG, "[DataConnection] register, now Time is in Time interval");
            setIsDCTime(ctx, true);
            DataConnectionUtils.checkScreenStateAndAction(ctx);
        } else {
            Log.i(TAG, "[DataConnection] register, now Time is NOT in Time interval");
            setIsDCTime(ctx, false);
        }
        DataConnectionUtils.setStartOrEndAlarm(ctx, ALARMTYPE.START);
        DataConnectionUtils.setStartOrEndAlarm(ctx, ALARMTYPE.END);
    }

    public void unregisterReceiver(Context ctx) {
        Log.i(TAG, "[DataConnection] unregisterReceiver");
        DataConnectionUtils.screenOnAction(ctx, mHandler);
        try {
            if (this.mDataConnectionReceiver != null) {
                ctx.unregisterReceiver(this.mDataConnectionReceiver);
            }
            DataConnectionUtils.stopPacketDetect();
            DataConnectionUtils.stoplistenWakeup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataConnectionUtils.cancelStartOrEndAlarm(this.mContext, ALARMTYPE.START);
        DataConnectionUtils.cancelStartOrEndAlarm(this.mContext, ALARMTYPE.END);
    }

    public static void setIsDCTime(Context context, boolean value) {
        isDCTime = value;
        DataConnectionUtils.saveToPref(context, DCPREF.ISDCTIME, isDCTime);
    }
}
