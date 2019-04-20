package com.evenwell.powersaving.g3.exception;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_DATA;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.PREF;
import com.evenwell.powersaving.g3.utils.PSConst.PACKAGENAME;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class NotificationIntentService extends IntentService {
    private static String TAG = TAG.PSLOG;
    private static final int mNotificationID = 2004;

    /* renamed from: com.evenwell.powersaving.g3.exception.NotificationIntentService$1 */
    class C03571 implements Runnable {
        C03571() {
        }

        public void run() {
            NotificationIntentService.this.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
            PowerSavingUtils.disableNotificationListener(NotificationIntentService.this);
            BamNotificationReceiver.isNotRemoveAction = true;
            NotificationIntentService.this.cancelNotification();
            Intent intent = new Intent(NotificationIntentService.this, PowerSaverExceptionActivity.class);
            intent.setFlags(805306368);
            NotificationIntentService.this.startActivity(intent);
            Log.i(NotificationIntentService.TAG, "[NotificationIntentService] send CLICK_NOTI event to PowerMonitor");
            Intent i = new Intent(ACTION.BAM_NOTIFICATION_EVENT);
            i.putExtra(EXTRA_KEY.TYPE, EXTRA_DATA.CLICK_NOTI);
            i.setPackage(PACKAGENAME.POWERMONITOR);
            NotificationIntentService.this.sendBroadcast(i);
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.exception.NotificationIntentService$2 */
    class C03582 implements Runnable {
        C03582() {
        }

        public void run() {
            NotificationIntentService.this.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
            PowerSavingUtils.disableNotificationListener(NotificationIntentService.this);
            BamNotificationReceiver.isNotRemoveAction = true;
            NotificationIntentService.this.cancelNotification();
            Intent intent = new Intent(NotificationIntentService.this, PowerSaverExceptionActivity.class);
            intent.setFlags(805306368);
            if (BackgroundPolicyExecutor.getInstance(NotificationIntentService.this).getDisAutoAppList().size() == 0) {
                intent.setAction(ACTION.BAM_TURN_ON);
            }
            NotificationIntentService.this.startActivity(intent);
            Log.i(NotificationIntentService.TAG, "[NotificationIntentService] send TURN_ON event to PowerMonitor");
            Intent i = new Intent(ACTION.BAM_NOTIFICATION_EVENT);
            i.putExtra(EXTRA_KEY.TYPE, EXTRA_DATA.TURN_ON);
            i.setPackage(PACKAGENAME.POWERMONITOR);
            NotificationIntentService.this.sendBroadcast(i);
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.exception.NotificationIntentService$3 */
    class C03593 implements Runnable {
        C03593() {
        }

        public void run() {
            PowerSavingUtils.disableNotificationListener(NotificationIntentService.this);
            BamNotificationReceiver.isNotRemoveAction = true;
            NotificationIntentService.this.cancelNotification();
            PowerSavingUtils.SetPreferencesStatus(NotificationIntentService.this, PREF.NEVER_SHOW, true);
            Intent intent = new Intent(ACTION.BAM_NEVER_SHOW);
            intent.setPackage(PACKAGENAME.POWERMONITOR);
            NotificationIntentService.this.sendBroadcast(intent);
            Log.i(NotificationIntentService.TAG, "[NotificationIntentService] send notify to PowerMonitor");
            Log.i(NotificationIntentService.TAG, "[NotificationIntentService] send NEVER_SHOW event to PowerMonitor");
            Intent i = new Intent(ACTION.BAM_NOTIFICATION_EVENT);
            i.putExtra(EXTRA_KEY.TYPE, EXTRA_DATA.NEVER_SHOW);
            i.setPackage(PACKAGENAME.POWERMONITOR);
            NotificationIntentService.this.sendBroadcast(i);
        }
    }

    public NotificationIntentService(String name) {
        super(name);
    }

    public NotificationIntentService() {
        super(NotificationIntentService.class.getSimpleName());
    }

    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Object obj = -1;
        switch (action.hashCode()) {
            case -1778562212:
                if (action.equals("TurnOn")) {
                    obj = 1;
                    break;
                }
                break;
            case 2289459:
                if (action.equals("Item")) {
                    obj = null;
                    break;
                }
                break;
            case 1033311113:
                if (action.equals("NeverShow")) {
                    obj = 2;
                    break;
                }
                break;
        }
        switch (obj) {
            case null:
                Log.i(TAG, "[NotificationIntentService] Item intent received");
                new Handler(Looper.getMainLooper()).post(new C03571());
                return;
            case 1:
                Log.i(TAG, "[NotificationIntentService] TurnOn intent received");
                new Handler(Looper.getMainLooper()).post(new C03582());
                return;
            case 2:
                Log.i(TAG, "[NotificationIntentService] NeverShow intent received");
                new Handler(Looper.getMainLooper()).post(new C03593());
                return;
            default:
                return;
        }
    }

    private void cancelNotification() {
        ((NotificationManager) getSystemService("notification")).cancel(2004);
    }
}
