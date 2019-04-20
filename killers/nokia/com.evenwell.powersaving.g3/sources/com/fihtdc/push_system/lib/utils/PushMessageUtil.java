package com.fihtdc.push_system.lib.utils;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import com.evenwell.powersaving.g3.provider.PowerSavingProvider.SettingsColumns;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public class PushMessageUtil extends PushMessageContract {
    private static final String NOTIFICATION_CHANNEL_NAME_CHS = "推送通知";
    private static final String NOTIFICATION_CHANNEL_NAME_CHT = "推送通知";
    private static final String NOTIFICATION_CHANNEL_NAME_ENG = "Push notifications";
    private static final String TAG = "FP819.PushMessageUtil";

    public static Notification showNotification(Context context, Bundle data, int smallIconRes, Bitmap bigIcon) {
        Log.d(TAG, "showNotification(): " + data.getString(PushMessageContract.MESSAGE_KEY_PACKAGE_NAME) + ", " + data.getString(PushMessageContract.MESSAGE_KEY_TITLE) + ", " + data.getString(PushMessageContract.MESSAGE_KEY_CONTENT));
        printDatas(data);
        String packageName = data.getString(PushMessageContract.MESSAGE_KEY_PACKAGE_NAME);
        String title = data.getString(PushMessageContract.MESSAGE_KEY_TITLE);
        String content = data.getString(PushMessageContract.MESSAGE_KEY_CONTENT);
        String openType = data.getString(PushMessageContract.MESSAGE_KEY_OPEN_TYPE);
        Intent intent = null;
        if (PushMessageContract.OPEN_TYPE_SELF.equals(openType)) {
            intent = getLauncher(context, packageName);
        } else if ("activity".equals(openType)) {
            String action = data.getString(PushMessageContract.MESSAGE_KEY_OPEN_URL);
            if (action != null) {
                if (action.indexOf(SYMBOLS.SEMICOLON) > 0) {
                    String[] arr = action.split(SYMBOLS.SEMICOLON);
                    if (arr != null && arr.length == 2) {
                        intent = new Intent(action);
                        intent.setClassName(arr[0], arr[1]);
                    }
                } else {
                    intent = new Intent(action);
                }
            }
        } else if (PushMessageContract.OPEN_TYPE_URL.equals(openType)) {
            String url = data.getString(PushMessageContract.MESSAGE_KEY_OPEN_URL);
            intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(url));
        }
        if (intent == null) {
            Log.e(TAG, "showNotification(): error, cannot find intent");
            intent = new Intent();
        }
        if (data.getBoolean(PushMessageContract.MESSAGE_KEY_CUSTOM_ICON, false)) {
            smallIconRes = getResourceDrawable(context, data.getString(PushMessageContract.MESSAGE_KEY_STATUS_ICON));
        }
        String[] parameters = data.getStringArray(PushMessageContract.MESSAGE_KEY_CUSTOM_PARAMETERS);
        if (parameters != null && parameters.length > 0) {
            for (String paramStr : parameters) {
                try {
                    JSONObject param = new JSONObject(paramStr);
                    intent.putExtra(param.getString("key"), param.getString(SettingsColumns.VALUE));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        createNotificationChannel(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Builder builder = new Builder(context, getNotificationChannelId(context));
        if (smallIconRes != 0) {
            builder.setSmallIcon(smallIconRes);
            Notification notification = builder.setContentTitle(title).setContentText(content).setLargeIcon(bigIcon).setAutoCancel(true).setContentIntent(pendingIntent).setOnlyAlertOnce(false).build();
            notificationManager.notify(TAG.hashCode(), notification);
            return notification;
        }
        Log.e(TAG, "showNotification(): error, icon is empty");
        return null;
    }

    public static Intent getLauncher(Context context, String packageName) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    static int getResourceDrawable(Context context, String drawableName) {
        try {
            Class<?> c = Class.forName(context.getPackageName() + ".R$drawable");
            return c.getField(drawableName).getInt(c.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static void printDatas(Bundle data) {
        for (String key : data.keySet()) {
            Log.v(TAG, "data " + key + " = " + data.get(key));
        }
    }

    static String getNotificationChannelId(Context context) {
        return context.getPackageName() + "_PUSH";
    }

    static void createNotificationChannel(Context context) {
        if (VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
            NotificationChannel androidChannel = new NotificationChannel(getNotificationChannelId(context), getNotificationChannelName(context), 3);
            androidChannel.enableLights(true);
            androidChannel.enableVibration(true);
            notificationManager.createNotificationChannel(androidChannel);
        }
    }

    static String getNotificationChannelName(Context context) {
        String name = NOTIFICATION_CHANNEL_NAME_ENG;
        LocaleList localelist = context.getResources().getConfiguration().getLocales();
        if (localelist.size() <= 0) {
            return name;
        }
        Locale locale = localelist.get(0);
        if (!locale.getLanguage().equals("zh")) {
            return name;
        }
        if (locale.getCountry().equals("CN")) {
            return "推送通知";
        }
        return "推送通知";
    }
}
