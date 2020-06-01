package com.evenwell.powersaving.g3.dataconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.net.TrafficStats;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings.System;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.LpmDcUtils;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.p000e.doze.WifiTethering;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.ScreeOnHandler;
import com.evenwell.powersaving.g3.utils.PSConst.DC.DCPARM;
import com.evenwell.powersaving.g3.utils.PSConst.DC.DCPARM.ALARMTYPE;
import com.evenwell.powersaving.g3.utils.PSConst.DC.DCPREF;
import com.evenwell.powersaving.g3.utils.PSConst.DC.INTENT.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.DC.INTENT.EXTRA;
import com.evenwell.powersaving.g3.utils.PSConst.DC.TIME.VALUE;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import com.evenwell.powersaving.g3.utils.PSConst.SETTINGDB.DCDB;
import com.evenwell.powersaving.g3.utils.PSConst.SS.SSPARM;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.util.Calendar;

public class DataConnectionUtils {
    private static final boolean DBG = true;
    private static String TAG = TAG.PSLOG;
    private static boolean isPostDelayExist = false;
    public static final BroadcastReceiver mScreenOffReceiver = new C03442();

    /* renamed from: com.evenwell.powersaving.g3.dataconnection.DataConnectionUtils$2 */
    static class C03442 extends BroadcastReceiver {
        C03442() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.BATTERY_CHANGED")) {
                int mPlugged = intent.getIntExtra("plugged", -1);
                int mStatus = intent.getIntExtra("status", -1);
                boolean mTetheringState = PowerSavingUtils.getTetherState(context);
                boolean mWifiState = DataConnectionUtils.isWifiAPEnabled(context);
                Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] Battery plugged is: " + mPlugged);
                Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] Battery status is: " + mStatus);
                Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] Tether state: " + PowerSavingUtils.getTetherState(context));
                Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] WifiAP state: " + DataConnectionUtils.isWifiAPEnabled(context));
                if (mPlugged != 1 && mPlugged != 2 && mPlugged != 4) {
                    if (DataConnectionUtils.getPostDelayExist()) {
                        DataConnectionUtils.unregisterScreenOffReceiver(context);
                    }
                    DataConnectionUtils.setPostDelayExist(false);
                } else if (mStatus == 2 || mStatus == 5 || mTetheringState || mWifiState) {
                    DataConnectionUtils.stopPacketDetect();
                    DataConnectionUtils.stoplistenWakeup();
                    if (DataConnectionUtils.getPostDelayExist()) {
                        DataConnectionUtils.unregisterScreenOffReceiver(context);
                    }
                    DataConnectionUtils.setPostDelayExist(false);
                }
            }
        }
    }

    public enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING,
        WIFI_AP_STATE_DISABLED,
        WIFI_AP_STATE_ENABLING,
        WIFI_AP_STATE_ENABLED,
        WIFI_AP_STATE_FAILED
    }

    public static class listenWakeupThread implements Runnable {
        Context context;
        long currentTime = 0;
        Handler handler;
        int period;
        long preCurrentTime = 0;

        public listenWakeupThread(Context context, Handler handler, int period) {
            this.period = period;
            this.handler = handler;
            this.context = context;
            this.preCurrentTime = System.currentTimeMillis();
        }

        public void run() {
            if (DataConnectionUtils.isDataEnabled(this.context) || PowerSavingUtils.GetWiFiEnableByDB(this.context)) {
                this.currentTime = System.currentTimeMillis();
                if (this.currentTime - this.preCurrentTime >= 30000) {
                    Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] listenWakeupThread-system wakeup so disable ALLPDP");
                    Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] sleep time: " + Long.toString(this.currentTime - this.preCurrentTime));
                    DataConnectionUtils.DisableAllPDP(this.context);
                    DataConnectionUtils.saveIntToPref(this.context, DCPREF.TXPACK, 0);
                    DataConnectionUtils.saveIntToPref(this.context, DCPREF.RXPACK, 0);
                    return;
                }
                this.preCurrentTime = this.currentTime;
                this.handler.postDelayed(this, (long) this.period);
            }
        }
    }

    public static class packetDetectThread implements Runnable {
        Context context;
        Handler handler;
        int period;

        public packetDetectThread(Context context, Handler handler, int period) {
            this.period = period;
            this.handler = handler;
            this.context = context;
            DataConnectionUtils.saveIntToPref(context, DCPREF.TXPACK, 0);
            DataConnectionUtils.saveIntToPref(context, DCPREF.RXPACK, 0);
        }

        public void run() {
            if (DataConnectionUtils.isDataEnabled(this.context) || PowerSavingUtils.GetWiFiEnableByDB(this.context)) {
                Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] packetDetectThread-mobile or wifi still enable !!");
                if (!DataConnectionUtils.disableConnection(this.context)) {
                    this.handler.postDelayed(this, (long) this.period);
                    return;
                }
                return;
            }
            Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] packetDetectThread-mobile and wifi disable !!");
        }
    }

    public static boolean isInTimeInterval(Context context, String nowTime) {
        String mStartTime = PowerSavingUtils.getStringItemFromDB(context, DCDB.PW_DATA_STARTTIME);
        String mEndTime = PowerSavingUtils.getStringItemFromDB(context, DCDB.PW_DATA_ENDTIME);
        int mStartTime_hour = Integer.valueOf(mStartTime.substring(0, 2)).intValue();
        int mStartTime_min = Integer.valueOf(mStartTime.substring(3)).intValue();
        int mEndTime_hour = Integer.valueOf(mEndTime.substring(0, 2)).intValue();
        int mEndTime_min = Integer.valueOf(mEndTime.substring(3)).intValue();
        int mNowTime_hour = Integer.valueOf(nowTime.substring(0, 2)).intValue();
        int mNowTime_min = Integer.valueOf(nowTime.substring(3)).intValue();
        Log.i(TAG, "mStartTime_hour: " + mStartTime_hour + " || mStartTime_min: " + mStartTime_min);
        Log.i(TAG, "mEndTime_hour: " + mEndTime_hour + " || mEndTime_min: " + mEndTime_min);
        Log.i(TAG, "mNowTime_hour: " + mNowTime_hour + " || mNowTime_min: " + mNowTime_min);
        if (mStartTime_hour > mEndTime_hour) {
            if (mStartTime_hour - mNowTime_hour > 0 && mEndTime_hour - mNowTime_hour > 0) {
                return true;
            }
            if (mStartTime_hour - mNowTime_hour > 0 && mEndTime_hour - mNowTime_hour < 0) {
                return false;
            }
            if (mStartTime_hour - mNowTime_hour < 0 && mEndTime_hour - mNowTime_hour < 0) {
                return true;
            }
            if (mStartTime_hour - mNowTime_hour != 0 || mEndTime_hour - mNowTime_hour >= 0) {
                if (mStartTime_hour - mNowTime_hour > 0 && mEndTime_hour - mNowTime_hour == 0) {
                    if (mStartTime_min - mNowTime_min >= 0) {
                        return true;
                    }
                    if (mStartTime_min - mNowTime_min < 0) {
                        return true;
                    }
                }
            } else if (mStartTime_min - mNowTime_min > 0) {
                return false;
            } else {
                if (mStartTime_min - mNowTime_min <= 0) {
                    return true;
                }
            }
        } else if (mStartTime_hour == mEndTime_hour) {
            if (mStartTime_hour - mNowTime_hour != 0) {
                return false;
            }
            if (mStartTime_min > mEndTime_min) {
                if (mStartTime_min - mNowTime_min > 0 && mEndTime_min - mNowTime_min > 0) {
                    return true;
                }
                if (mStartTime_min - mNowTime_min < 0 && mEndTime_min - mNowTime_min < 0) {
                    return true;
                }
                if (mStartTime_min - mNowTime_min > 0 && mEndTime_min - mNowTime_min == 0) {
                    return false;
                }
                if (mStartTime_min - mNowTime_min == 0 && mEndTime_min - mNowTime_min < 0) {
                    return true;
                }
                if (mStartTime_min - mNowTime_min > 0 && mEndTime_min - mNowTime_min < 0) {
                    return false;
                }
            } else if (mStartTime_min == mEndTime_min) {
                if (mStartTime_min - mNowTime_min == 0) {
                    return true;
                }
            } else if (mStartTime_min - mNowTime_min > 0 && mEndTime_min - mNowTime_min > 0) {
                return false;
            } else {
                if (mStartTime_min - mNowTime_min < 0 && mEndTime_min - mNowTime_min < 0) {
                    return false;
                }
                if (mStartTime_min - mNowTime_min < 0 && mEndTime_min - mNowTime_min > 0) {
                    return true;
                }
                if (mStartTime_min - mNowTime_min == 0 && mEndTime_min - mNowTime_min > 0) {
                    return true;
                }
                if (mStartTime_min - mNowTime_min < 0 && mEndTime_min - mNowTime_min == 0) {
                    return false;
                }
            }
        } else if (mStartTime_hour - mNowTime_hour > 0 && mEndTime_hour - mNowTime_hour > 0) {
            return false;
        } else {
            if (mStartTime_hour - mNowTime_hour < 0 && mEndTime_hour - mNowTime_hour > 0) {
                return true;
            }
            if (mStartTime_hour - mNowTime_hour < 0 && mEndTime_hour - mNowTime_hour < 0) {
                return false;
            }
            if (mStartTime_hour - mNowTime_hour >= 0 || mEndTime_hour - mNowTime_hour != 0) {
                if (mStartTime_hour - mNowTime_hour == 0 && mEndTime_hour - mNowTime_hour > 0) {
                    if (mStartTime_min - mNowTime_min > 0) {
                        return false;
                    }
                    if (mStartTime_min - mNowTime_min <= 0) {
                        return true;
                    }
                }
            } else if (mEndTime_min - mNowTime_min > 0) {
                return true;
            } else {
                if (mEndTime_min - mNowTime_min <= 0) {
                    return false;
                }
            }
        }
        return false;
    }

    public static String getNowTime() {
        Calendar calendar = Calendar.getInstance();
        String mHour = String.valueOf(calendar.get(11));
        String mMinuts = String.valueOf(calendar.get(12));
        if (mHour.length() < 2) {
            mHour = SYMBOLS.ZERO + mHour;
        }
        if (mMinuts.length() < 2) {
            mMinuts = SYMBOLS.ZERO + mMinuts;
        }
        return mHour + ":" + mMinuts;
    }

    public static void SetSettingsToPhoneForRestore(Context mContext) {
        LpmDcUtils.WiFiAndMobileDataRestore(mContext, 2);
        LpmDcUtils.SetLpmOrDcApplyStatus(mContext, 2, false);
        if (LpmDcUtils.IsLPMApply(mContext)) {
            LpmDcUtils.NotifyLpmOrDc(mContext, 1);
        }
    }

    public static void screenOnAction(Context context, final ScreeOnHandler mHandler) {
        new Thread() {
            public void run() {
                Log.i(DataConnectionUtils.TAG, "[DataConnectionUtils] screenOnAction_()");
                Message message = new Message();
                message.what = DCPARM.SCREENON_MESSAGE;
                mHandler.sendMessage(message);
            }
        }.start();
    }

    public static void screenOnActionHandler(Context context) {
        Log.i(TAG, "[DataConnectionUtils] screenOnActionHandler()");
        stopPacketDetect();
        stoplistenWakeup();
        if (getPostDelayExist()) {
            unregisterScreenOffReceiver(context);
        }
        setPostDelayExist(false);
        Log.i(TAG, "Receive screen on intent, restore network state !!");
        if (LpmDcUtils.IsDCApply(context)) {
            SetSettingsToPhoneForRestore(context);
        }
        saveIntToPref(context, DCPREF.TXPACK, 0);
        saveIntToPref(context, DCPREF.RXPACK, 0);
    }

    public static void screenOffAction(Context context) {
        Log.i(TAG, "[DataConnectionUtils] screenOffAction()");
        if (isDataEnabled(context) || PowerSavingUtils.GetWiFiEnableByDB(context)) {
            Log.i(TAG, "[DataConnectionUtils] Have data connection: mobile status: " + isDataEnabled(context) + "|| wifi status is: " + PowerSavingUtils.GetWiFiEnableByDB(context));
            String detectTime = PowerSavingUtils.getStringItemFromDB(context, DCDB.PW_DATA_DETECTTIME);
            if (detectTime == null) {
                detectTime = context.getResources().getString(C0321R.string.powersaving_db_dc_detect_time);
            }
            startPacketDetect(context, DataConnection.handler, Integer.valueOf(detectTime).intValue());
            startlistenWakeup(context, DataConnection.handler, VALUE.DETECT_WAKE_UP_TIME);
            registerScreenOffReceiver(context);
            setPostDelayExist(true);
            return;
        }
        Log.i(TAG, "[DataConnectionUtils] No data connection");
        if (LpmDcUtils.IsLPMApply(context)) {
            LpmDcUtils.SetLpmOrDcApplyStatus(context, 2, true);
            saveToPref(context, DCPREF.MOBILE, isDataEnabled(context));
            saveToPref(context, DCPREF.WIFI, PowerSavingUtils.GetWiFiEnableByDB(context));
            return;
        }
        Log.i(TAG, "[DataConnectionUtils] No data connection [user choose]");
    }

    public static boolean isDataEnabled(Context context) {
        return PowerSavingUtils.GetMobileDataEnable(context);
    }

    public static boolean isWiFiEnable(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getSystemService(SSPARM.WIFI);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (!mWifiManager.isWifiEnabled() || ipAddress == 0) {
            return false;
        }
        return true;
    }

    public static boolean disableConnection(Context context) {
        Log.i(TAG, "[DataConnectionUtils] disableConnection()");
        long preTxPkts = (long) getIntFromPref(context, DCPREF.TXPACK);
        long preRxPkts = (long) getIntFromPref(context, DCPREF.RXPACK);
        long txPkts = TrafficStats.getTotalTxPackets();
        long rxPkts = TrafficStats.getTotalRxPackets();
        Log.i(TAG, "[DataConnectionUtils] disableConnection()tx_pkts: " + txPkts + "||rxPkts: " + rxPkts);
        Log.i(TAG, "[DataConnectionUtils] disableConnection()preTxPkts: " + preTxPkts + "||preRxPkts: " + preRxPkts);
        saveIntToPref(context, DCPREF.TXPACK, (int) txPkts);
        saveIntToPref(context, DCPREF.RXPACK, (int) rxPkts);
        if (txPkts - preTxPkts > 0 || rxPkts - preRxPkts > 0) {
            Log.i(TAG, "[DataConnectionUtils] disableConnection() - has packet transfer");
            return false;
        }
        Log.i(TAG, "[DataConnectionUtils] disableConnection() - no packet transfer");
        DisableAllPDP(context);
        saveIntToPref(context, DCPREF.TXPACK, 0);
        saveIntToPref(context, DCPREF.RXPACK, 0);
        return true;
    }

    public static void DisableAllPDP(Context context) {
        if (getPostDelayExist()) {
            unregisterScreenOffReceiver(context);
        }
        setPostDelayExist(false);
        Log.i(TAG, "[DataConnectionUtils] DisableAllPDP-in DCTime");
        LpmDcUtils.SetLpmOrDcApplyStatus(context, 2, true);
        saveToPref(context, DCPREF.MOBILE, isDataEnabled(context));
        saveToPref(context, DCPREF.WIFI, PowerSavingUtils.GetWiFiEnableByDB(context));
        if (isDataEnabled(context) && PowerSavingUtils.GetWiFiEnableByDB(context)) {
            setMobileDataEnabled(context, false);
            setWifiEnable(context, false);
        } else if (isDataEnabled(context) && !PowerSavingUtils.GetWiFiEnableByDB(context)) {
            setMobileDataEnabled(context, false);
        } else if (!isDataEnabled(context) && PowerSavingUtils.GetWiFiEnableByDB(context)) {
            setWifiEnable(context, false);
        }
    }

    public static void setWifiEnable(Context context, boolean value) {
        boolean mWifi = false;
        if (value) {
            mWifi = true;
        }
        Log.i(TAG, "[DataConnectionUtils] Turn Wifi: " + mWifi);
        WifiManager mWifiManager = (WifiManager) context.getSystemService(SSPARM.WIFI);
        boolean desiredState = mWifi;
        if (desiredState && !isRadioAllowed(context, SSPARM.WIFI)) {
            Log.i(TAG, "Mobile in airplane mode!!");
        }
        if (mWifiManager != null) {
            int wifiApState = mWifiManager.getWifiApState();
            if (desiredState && (wifiApState == 12 || wifiApState == 13)) {
                try {
                    new WifiTethering(context).setTethering(false);
                } catch (Exception e) {
                    Log.i(TAG, "[DataConnectionUtils] setWifiApEnabled() failed.");
                    e.printStackTrace();
                }
            }
            mWifiManager.setWifiEnabled(desiredState);
        }
    }

    public static boolean isRadioAllowed(Context mContext, String type) {
        if (!isAirplaneModeOn(mContext)) {
            return true;
        }
        String toggleable = System.getString(mContext.getContentResolver(), "airplane_mode_toggleable_radios");
        if (toggleable == null || !toggleable.contains(type)) {
            return false;
        }
        return true;
    }

    public static boolean isAirplaneModeOn(Context mContext) {
        return System.getInt(mContext.getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public static void setMobileDataEnabled(Context context, boolean enabled) {
        Log.i(TAG, "[DataConnectionUtils] Turn mobile data: " + enabled);
        PowerSavingUtils.SetMobileDataEnable(context, enabled);
    }

    public static void saveToPref(Context context, String type, boolean mState) {
        Editor editor = context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_DATA_CONNECTION_FILE, 0).edit();
        editor.putBoolean(type, mState);
        editor.commit();
    }

    public static boolean getFromPref(Context context, String type) {
        return context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_DATA_CONNECTION_FILE, 0).getBoolean(type, false);
    }

    public static void saveIntToPref(Context context, String key, int value) {
        Editor editor = context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_DATA_CONNECTION_FILE, 0).edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getIntFromPref(Context context, String key) {
        return context.getSharedPreferences(FILENAME.PREF_POWER_SAVING_DATA_CONNECTION_FILE, 0).getInt(key, 0);
    }

    public static boolean isScreenOnorOff(Context context) {
        return ((PowerManager) context.getSystemService("power")).isScreenOn();
    }

    public static boolean checkAlwaysOnOrNot(Context context) {
        if (PowerSavingUtils.getStringItemFromDB(context, DCDB.PW_DATA_ALWAYSON).equals(SWITCHER.ON)) {
            return true;
        }
        return false;
    }

    public static void setStartOrEndAlarm(Context context, String alarm_type) {
        int mHour;
        int mMin;
        Calendar calendar = Calendar.getInstance();
        AlarmManager alarmmanager = (AlarmManager) context.getSystemService("alarm");
        Intent intent = new Intent(ACTION.ACTION_DC_APPLY_TIME);
        intent.putExtra(EXTRA.TIME_KEY, alarm_type);
        if (alarm_type.equals(ALARMTYPE.START)) {
            String mStartTime = PowerSavingUtils.getStringItemFromDB(context, DCDB.PW_DATA_STARTTIME);
            mHour = Integer.valueOf(mStartTime.substring(0, 2)).intValue();
            mMin = Integer.valueOf(mStartTime.substring(3)).intValue();
        } else {
            String mEndTime = PowerSavingUtils.getStringItemFromDB(context, DCDB.PW_DATA_ENDTIME);
            mHour = Integer.valueOf(mEndTime.substring(0, 2)).intValue();
            mMin = Integer.valueOf(mEndTime.substring(3)).intValue();
        }
        int nowSec = ((calendar.get(11) * 60) + calendar.get(12)) * 60;
        calendar.set(11, mHour);
        calendar.set(12, mMin);
        calendar.set(13, 0);
        int alarmSec = ((calendar.get(11) * 60) + calendar.get(12)) * 60;
        if (alarmSec <= nowSec) {
            calendar.add(5, 1);
        }
        if (alarm_type.equals(ALARMTYPE.START)) {
            Log.i(TAG, "[DataConnectionUtils] setStartOrEndAlarm() Set start alarm");
            alarmmanager.setRepeating(0, calendar.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 3002, intent, 134217728));
        } else {
            Log.i(TAG, "[DataConnectionUtils] setStartOrEndAlarm() Set end alarm");
            AlarmManager alarmManager = alarmmanager;
            alarmManager.setRepeating(0, calendar.getTimeInMillis(), 86400000, PendingIntent.getBroadcast(context, 3003, intent, 134217728));
        }
        Log.i(TAG, "[DataConnectionUtils] setStartOrEndAlarm()Now sec: " + nowSec + " || Alarm sec: " + alarmSec + " || Alarm time: " + calendar.getTime());
    }

    public static void cancelStartOrEndAlarm(Context context, String alarm_type) {
        AlarmManager alarmmanager = (AlarmManager) context.getSystemService("alarm");
        Intent alarm_intent = new Intent(ACTION.ACTION_DC_APPLY_TIME);
        if (alarm_type.equals(ALARMTYPE.START)) {
            alarmmanager.cancel(PendingIntent.getBroadcast(context, 3002, alarm_intent, 134217728));
        } else {
            alarmmanager.cancel(PendingIntent.getBroadcast(context, 3003, alarm_intent, 134217728));
        }
    }

    public static void checkScreenStateAndAction(Context context) {
        if (isScreenOnorOff(context)) {
            Log.i(TAG, "[DataConnectionUtils] checkScreenStateAndAction() Screen on, do nothing !!");
            return;
        }
        Log.i(TAG, "[DataConnectionUtils] checkScreenStateAndAction() Screen off, ready to disconnect network !!");
        screenOffAction(context);
    }

    public static void startPacketDetect(Context context, Handler handler, int period) {
        if (DataConnection.mPacketDetectThread == null) {
            DataConnection.mPacketDetectThread = new packetDetectThread(context, handler, period);
        }
        DataConnection.handler.removeCallbacks(DataConnection.mPacketDetectThread);
        DataConnection.handler.postDelayed(DataConnection.mPacketDetectThread, (long) period);
    }

    public static void stopPacketDetect() {
        if (DataConnection.mPacketDetectThread != null) {
            Log.i(TAG, "[DataConnectionUtils] mPacketDetectThread is not null");
            DataConnection.handler.removeCallbacks(DataConnection.mPacketDetectThread);
        }
        DataConnection.mPacketDetectThread = null;
    }

    public static void startlistenWakeup(Context context, Handler handler, int period) {
        if (DataConnection.mListenWakeupThread == null) {
            DataConnection.mListenWakeupThread = new listenWakeupThread(context, handler, period);
        }
        DataConnection.handler.removeCallbacks(DataConnection.mListenWakeupThread);
        DataConnection.handler.postDelayed(DataConnection.mListenWakeupThread, (long) period);
    }

    public static void stoplistenWakeup() {
        if (DataConnection.mListenWakeupThread != null) {
            Log.i(TAG, "[DataConnectionUtils] mListenWakeupThread is not null");
            DataConnection.handler.removeCallbacks(DataConnection.mListenWakeupThread);
        }
        DataConnection.mListenWakeupThread = null;
    }

    public static void registerScreenOffReceiver(Context ctx) {
        Log.i(TAG, "[DataConnectionUtils] registerScreenOffReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        ctx.registerReceiver(mScreenOffReceiver, filter);
    }

    public static void unregisterScreenOffReceiver(Context ctx) {
        Log.i(TAG, "[DataConnectionUtils] unregisterScreenOffReceiver");
        try {
            if (mScreenOffReceiver != null) {
                ctx.unregisterReceiver(mScreenOffReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static WIFI_AP_STATE getWifiAPState(Context mContext) {
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(SSPARM.WIFI);
        try {
            int tmp = ((Integer) mWifiManager.getClass().getMethod("getWifiApState", new Class[0]).invoke(mWifiManager, new Object[0])).intValue();
            if (tmp > 10) {
                tmp -= 10;
            }
            return ((WIFI_AP_STATE[]) WIFI_AP_STATE.class.getEnumConstants())[tmp];
        } catch (Exception e) {
            return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
        }
    }

    public static boolean isWifiAPEnabled(Context mContext) {
        return getWifiAPState(mContext) == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
    }

    public static void setPostDelayExist(boolean isUse) {
        isPostDelayExist = isUse;
    }

    public static boolean getPostDelayExist() {
        return isPostDelayExist;
    }
}
