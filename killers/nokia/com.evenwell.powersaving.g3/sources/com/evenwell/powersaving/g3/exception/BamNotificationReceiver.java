package com.evenwell.powersaving.g3.exception;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Action.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_DATA;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.PREF;
import com.evenwell.powersaving.g3.utils.PSConst.PACKAGENAME;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BamNotificationReceiver extends BroadcastReceiver {
    private static final String ACTION_POWER_USAGE_ABNORMAL = "com.evenwell.powersaving.g3.power_usage_abnormal";
    private static final long DAY_IN_MILLI_SECOND = 86400000;
    private static final int SHOW_COUNT_LIMIT = 3;
    private static String TAG = TAG.PSLOG;
    public static boolean isNotRemoveAction = false;
    private Context mContext;

    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        String action = intent.getAction();
        Log.i(TAG, "[BamNotificationReceiver] Intent action: " + action);
        if (action != null) {
            if (PSUtils.isCNModel(this.mContext) || !action.equals(ACTION_POWER_USAGE_ABNORMAL)) {
                if (PSUtils.isCNModel(this.mContext) && action.equals(ACTION_POWER_USAGE_ABNORMAL)) {
                    nofityNeverShow();
                }
            } else if (isNeedSendNotification()) {
                PowerSavingUtils.enableNotificationListener(this.mContext);
                sendNotification();
                handleShowCount();
                handleNeverShow();
            }
        }
    }

    private boolean isNeedSendNotification() {
        Log.i(TAG, "[BamNotificationReceiver] isNeedSendNotification()");
        if (!isNeverShow() && isDayPassedEnough() && isInstalledAppAllWhite() && isNotificationNotShowing()) {
            return true;
        }
        return false;
    }

    private void sendNotification() {
        Log.i(TAG, "[BamNotificationReceiver] sendNotification()");
        int vitalsResID = Resources.getSystem().getIdentifier("stat_sys_vitals", "drawable", "android");
        Intent intent = new Intent(this.mContext, NotificationIntentService.class);
        intent.setAction("TurnOn");
        PendingIntent turnOnPendingIntent = PendingIntent.getService(this.mContext, 0, intent, 134217728);
        intent = new Intent(this.mContext, NotificationIntentService.class);
        intent.setAction("NeverShow");
        PendingIntent neverShowPendingIntent = PendingIntent.getService(this.mContext, 0, intent, 134217728);
        String nBtn1Str = this.mContext.getResources().getString(C0321R.string.bam_notification_btn_turnon);
        String nBtn2Str = this.mContext.getResources().getString(C0321R.string.bam_notification_btn_nevershowthis);
        Action turnOnAction = new Builder(vitalsResID, nBtn1Str, turnOnPendingIntent).build();
        Action neverShowAction = new Builder(vitalsResID, nBtn2Str, neverShowPendingIntent).build();
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        String channelId = "channel_999";
        notificationManager.createNotificationChannel(new NotificationChannel(channelId, this.mContext.getResources().getString(C0321R.string.fih_power_saving_enabled_notify_channel_name), 3));
        String appName = this.mContext.getResources().getString(C0321R.string.lock_screen_ap_protect_title);
        Bundle notificationBundle = new Bundle();
        notificationBundle.putString("android.substName", appName);
        String nContentTitleStr = this.mContext.getResources().getString(C0321R.string.bam_notification_content_title);
        String nContentTextStr = this.mContext.getResources().getString(C0321R.string.bam_notification_content_text);
        Intent itemIntent = new Intent(this.mContext, NotificationIntentService.class);
        itemIntent.setAction("Item");
        notificationManager.notify(2004, new Notification.Builder(this.mContext).setSmallIcon(vitalsResID).setContentTitle(nContentTitleStr).setContentText(nContentTextStr).setAutoCancel(true).setColor(this.mContext.getColor(Resources.getSystem().getIdentifier("system_notification_accent_color", "color", "android"))).setContentIntent(PendingIntent.getService(this.mContext, 0, itemIntent, 134217728)).setActions(new Action[]{turnOnAction, neverShowAction}).setShowWhen(true).setWhen(System.currentTimeMillis()).setChannelId(channelId).addExtras(notificationBundle).build());
        Log.i(TAG, "[BamNotificationReceiver] send SHOW event to PowerMonitor");
        Intent intent2 = new Intent(ACTION.BAM_NOTIFICATION_EVENT);
        intent2.setPackage(PACKAGENAME.POWERMONITOR);
        intent2.putExtra(EXTRA_KEY.TYPE, EXTRA_DATA.SHOW);
        int showCount = PowerSavingUtils.GetPreferencesStatusInt(this.mContext, PREF.SHOW_COUNT);
        if (showCount == -1) {
            showCount = 0;
        }
        intent2.putExtra(EXTRA_KEY.SHOW_COUNT, showCount + 1);
        this.mContext.sendBroadcast(intent2);
    }

    private void handleShowCount() {
        Log.i(TAG, "[BamNotificationReceiver] handleShowCount()");
        int showCount = PowerSavingUtils.GetPreferencesStatusInt(this.mContext, PREF.SHOW_COUNT);
        if (showCount == -1) {
            showCount = 0;
        }
        showCount++;
        PowerSavingUtils.SetPreferencesStatus(this.mContext, PREF.SHOW_COUNT, showCount);
        Log.i(TAG, "[BamNotificationReceiver] showCount = " + showCount);
        if (showCount >= 3) {
            PowerSavingUtils.SetPreferencesStatus(this.mContext, PREF.NEVER_SHOW, true);
        }
        if (showCount == 1) {
            PowerSavingUtils.SetPreferencesStatus(this.mContext, PREF.FIRST_NOTIFY_TIME, getCurrentTimeInMs());
        }
    }

    private void handleNeverShow() {
        if (isNeverShow()) {
            nofityNeverShow();
        }
    }

    private void nofityNeverShow() {
        Intent intent = new Intent(ACTION.BAM_NEVER_SHOW);
        intent.setPackage(PACKAGENAME.POWERMONITOR);
        this.mContext.sendBroadcast(intent);
        Log.i(TAG, "[BamNotificationReceiver] send notify to PowerMonitor");
    }

    private boolean isNeverShow() {
        boolean isNeverShow = PowerSavingUtils.GetPreferencesStatus(this.mContext, PREF.NEVER_SHOW);
        Log.i(TAG, "[BamNotificationReceiver] isNeverShow = " + isNeverShow);
        return isNeverShow;
    }

    private boolean isDayPassedEnough() {
        Log.i(TAG, "[BamNotificationReceiver] isDayPassedEnough()");
        int showCount = PowerSavingUtils.GetPreferencesStatusInt(this.mContext, PREF.SHOW_COUNT);
        if (showCount == -1) {
            showCount = 0;
        }
        if (showCount == 0) {
            Log.i(TAG, "[BamNotificationReceiver] showCount = 0");
            return true;
        }
        long firstNotifyTime = PowerSavingUtils.GetPreferencesStatusLong(this.mContext, PREF.FIRST_NOTIFY_TIME);
        if (firstNotifyTime == -1) {
            Log.i(TAG, "[BamNotificationReceiver] firstNotifyTime = " + firstNotifyTime);
            return true;
        }
        long now = getCurrentTimeInMs();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd(E) HH:mm:ss.S");
        Log.i(TAG, "[BamNotificationReceiver] firstNotifyTimeDate = " + simpleDateFormat.format(new Date(firstNotifyTime)));
        Log.i(TAG, "[BamNotificationReceiver] nowDate = " + simpleDateFormat.format(new Date(now)));
        long dayDiff = (now - firstNotifyTime) / DAY_IN_MILLI_SECOND;
        Log.i(TAG, "[BamNotificationReceiver] dayDiff = " + dayDiff);
        if (showCount == 1 && dayDiff >= 2) {
            Log.i(TAG, "[BamNotificationReceiver] showCount = 1, dayDiff >= 2");
            return true;
        } else if (showCount != 2 || dayDiff < 7) {
            if (dayDiff < 0) {
                Log.i(TAG, "[BamNotificationReceiver] dayDiff < 0, update record time");
                PowerSavingUtils.SetPreferencesStatus(this.mContext, PREF.FIRST_NOTIFY_TIME, now);
            }
            return false;
        } else {
            Log.i(TAG, "[BamNotificationReceiver] showCount = 2, dayDiff >= 7");
            return true;
        }
    }

    private boolean isInstalledAppAllWhite() {
        Log.i(TAG, "[BamNotificationReceiver] isInstalledAppAllWhite()");
        if (BackgroundPolicyExecutor.getInstance(this.mContext).getDisAutoAppList().size() != 0) {
            return false;
        }
        Log.i(TAG, "[BamNotificationReceiver] all white");
        return true;
    }

    private boolean isNotificationNotShowing() {
        Log.i(TAG, "[BamNotificationReceiver] isNotificationNotShowing()");
        NotificationManager mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (mNotificationManager != null) {
            StatusBarNotification[] sbn = mNotificationManager.getActiveNotifications();
            Log.i(TAG, "[BamNotificationReceiver] sbn.length = " + sbn.length);
            int i = 0;
            while (i < sbn.length) {
                if (sbn[i].getId() == 2004 && sbn[i].getPackageName().equals("com.evenwell.powersaving.g3")) {
                    Log.i(TAG, "[BamNotificationReceiver] the notification is existed");
                    return false;
                }
                i++;
            }
        }
        return true;
    }

    private long getCurrentTimeInMs() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        return calendar.getTimeInMillis();
    }
}
