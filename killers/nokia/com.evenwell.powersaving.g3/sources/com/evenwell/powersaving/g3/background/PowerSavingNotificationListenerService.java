package com.evenwell.powersaving.g3.background;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BamNotificationReceiver;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.ACTION;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_DATA;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.evenwell.powersaving.g3.utils.PSConst.PACKAGENAME;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;

public class PowerSavingNotificationListenerService extends NotificationListenerService {
    private static String TAG = TAG.PSLOG;

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "[PowerSavingNotificationListenerService] onCreate");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "[PowerSavingNotificationListenerService] onDestroy");
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn.getId() == 2004 && sbn.getPackageName().equals("com.evenwell.powersaving.g3")) {
            if (!BamNotificationReceiver.isNotRemoveAction) {
                Log.i(TAG, "[PowerSavingNotificationListenerService] send REMOVE event to PowerMonitor");
                Intent intent = new Intent(ACTION.BAM_NOTIFICATION_EVENT);
                intent.putExtra(EXTRA_KEY.TYPE, EXTRA_DATA.REMOVE);
                intent.setPackage(PACKAGENAME.POWERMONITOR);
                sendBroadcast(intent);
                PowerSavingUtils.disableNotificationListener(this);
            }
            BamNotificationReceiver.isNotRemoveAction = false;
        }
    }

    public void onListenerConnected() {
        Log.d(TAG, "[PowerSavingNotificationListenerService] onListenerConnected");
    }

    public void onListenerDisconnected() {
        Log.d(TAG, "[PowerSavingNotificationListenerService] onListenerDisconnected");
    }
}
