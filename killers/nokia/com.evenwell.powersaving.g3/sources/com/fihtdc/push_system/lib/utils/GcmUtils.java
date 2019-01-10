package com.fihtdc.push_system.lib.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Build.VERSION;
import android.util.Log;
import java.util.ArrayList;
import java.util.Iterator;

public class GcmUtils {
    static final ArrayList<String> FCM_APK_PACKAGE_NAME = new ArrayList();

    static {
        FCM_APK_PACKAGE_NAME.add("com.evenwell.pushagent");
        FCM_APK_PACKAGE_NAME.add("com.evenwell.pushagent.dev");
    }

    public static ServiceInfo getPushAgentService(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent query = new Intent(PushServiceUtil.ACTION_PUSH_SERVICE);
        int flag;
        if (VERSION.SDK_INT <= 23) {
            flag = 512;
        } else {
            flag = 512;
        }
        for (ResolveInfo ri : pm.queryIntentServices(query, flag)) {
            if (FCM_APK_PACKAGE_NAME.contains(ri.serviceInfo.packageName)) {
                return ri.serviceInfo;
            }
        }
        Log.d("GcmUtils", "getPushAgentService(): No PushAgent service");
        return null;
    }

    public static boolean isPushAgentExist(Context context) {
        if (isPushAgentApp(context)) {
            return true;
        }
        PackageManager pm = context.getPackageManager();
        Iterator it = FCM_APK_PACKAGE_NAME.iterator();
        while (it.hasNext()) {
            String packagename = (String) it.next();
            try {
                pm.getPackageInfo(packagename, 1);
                Log.d("GcmUtils", "isPushAgentExist(): " + packagename + " exist!");
                return true;
            } catch (NameNotFoundException e) {
            } catch (Exception e2) {
                Log.w("GcmUtils", "isPushAgentExist(): error of " + packagename, e2);
            }
        }
        Log.d("GcmUtils", "isPushAgentExist(): no pushagent exist");
        return false;
    }

    public static boolean isPushAgentApp(Context context) {
        Iterator it = FCM_APK_PACKAGE_NAME.iterator();
        while (it.hasNext()) {
            String packagename = (String) it.next();
            if (context.getPackageName().equals(packagename)) {
                Log.d("GcmUtils", "isPushAgentExist(): I am " + packagename);
                return true;
            }
        }
        return false;
    }
}
