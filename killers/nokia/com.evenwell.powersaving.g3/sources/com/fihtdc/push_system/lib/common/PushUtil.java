package com.fihtdc.push_system.lib.common;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import com.fihtdc.backuptool.FileOperator;
import com.fihtdc.push_system.lib.FihPushServiceProxy;
import com.fihtdc.push_system.lib.utils.PushServiceUtil;
import net2.lingala.zip4j.crypto.PBKDF2.BinTools;

public class PushUtil {
    private static final String[] DEFAULT_PSN = new String[]{EnvironmentCompat.MEDIA_UNKNOWN, BinTools.hex};
    protected static final String TAG = "FP819.PushUtil";

    public static void startPushService(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Intent intent = PushServiceUtil.getPushService(context);
                    if (intent == null) {
                        Log.w(PushUtil.TAG, "startPushService(): fail to get push service");
                        return;
                    }
                    intent.setAction(PushProp.ACTION_SERVICE_START_PUSH);
                    new FihPushServiceProxy(context, intent).startPushService();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "startPushService").start();
    }

    public static void stopPush(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                Intent intent = PushServiceUtil.getPushService(context);
                if (intent != null) {
                    intent.setAction(PushProp.ACTION_SERVICE_STOP_PUSH);
                    try {
                        new FihPushServiceProxy(context, intent).stopPush();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "stopPush").start();
    }

    public static void setPushReceiverEnabled(final Context context, ComponentName pushReceiver, final boolean enabled) {
        boolean receiverEnabled = isPushReceiverEnabled(context, pushReceiver);
        if (receiverEnabled != enabled) {
            String pushServerAddr = null;
            if (receiverEnabled) {
                pushServerAddr = getPushServerAddr(context, pushReceiver);
            }
            Log.d(TAG, "setPushReceiverEnabled(): " + pushReceiver + " = " + enabled);
            context.getPackageManager().setComponentEnabledSetting(pushReceiver, enabled ? 1 : 2, 1);
            if (enabled) {
                pushServerAddr = getPushServerAddr(context, pushReceiver);
            }
            final ServiceInfo activedService = PushServiceUtil.getActivePushService(context, pushServerAddr);
            Log.d(TAG, "setPushReceiverEnabled(): activedService=" + activedService + ", debug=" + pushServerAddr);
            new Thread(new Runnable() {
                public void run() {
                    if (activedService != null) {
                        Intent thisPushService = PushServiceUtil.getServiceIntent(activedService);
                        thisPushService.setAction(PushProp.ACTION_SERVICE_STOP_PUSH);
                        try {
                            new FihPushServiceProxy(context, thisPushService).shutdown();
                            PushUtil.startPushService(context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (enabled) {
                        PushUtil.startPushService(context);
                    }
                }
            }).start();
        }
    }

    private static String getPushServerAddr(Context context, ComponentName pushReceiver) {
        String pushServerAddr = null;
        try {
            ServiceInfo si = context.getPackageManager().getServiceInfo(pushReceiver, 128);
            if (si != null) {
                pushServerAddr = PushServiceUtil.getPushServerAddr(context, si);
            }
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
        }
        return pushServerAddr;
    }

    public static boolean isPushReceiverEnabled(Context context, ComponentName pushReceiver) {
        int status = context.getPackageManager().getComponentEnabledSetting(pushReceiver);
        if (status == 1 || status == 0) {
            return true;
        }
        return false;
    }

    public static String getDeviceId(Context context) {
        Bundle b = context.getContentResolver().call(PushProp.PUSH_PROVIDER_URI, PushProp.METHOD_GET_CONFIG, PushProp.ARG_PUSH_ID, null);
        if (b == null) {
            return null;
        }
        return b.getString(PushProp.ARG_PUSH_ID, null);
    }

    public static String getSystemProperty(String key) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            return (String) c.getMethod("get", new Class[]{String.class}).invoke(c, new Object[]{key});
        } catch (Exception e) {
            Log.w(TAG, "getSystemProperty(" + key + ") fail, " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static void grantAutoStartAppops(Context context, int uid, String packageName) {
        if (VERSION.SDK_INT >= 26 && Process.myUid() == FileOperator.MAX_DIR_LENGTH) {
            try {
                ((AppOpsManager) context.getSystemService("appops")).setMode(72, uid, packageName, 0);
                Log.i(TAG, "grantAutoStartAppops() enable app " + uid + ", " + packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean canAutoStart(Context context, int uid, String packageName) {
        if (VERSION.SDK_INT < 26 || Process.myUid() != FileOperator.MAX_DIR_LENGTH) {
            return true;
        }
        try {
            if (((AppOpsManager) context.getSystemService("appops")).checkOp(72, uid, packageName) == 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "canAutoStart() false, app " + uid + ", " + packageName);
        return false;
    }
}
