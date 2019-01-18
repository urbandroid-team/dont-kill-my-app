package com.evenwell.powersaving.g3.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.util.ArraySet;
import com.android.internal.util.ArrayUtils;
import java.util.List;
import java.util.Set;

public class PackageManagerUtils {
    public static void setComponentState(Context context, ComponentName componentName, boolean enabled) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        int state = enabled ? 1 : 2;
        if (state != pm.getComponentEnabledSetting(componentName)) {
            pm.setComponentEnabledSetting(componentName, state, 1);
        }
    }

    public static List<ResolveInfo> queryIntentServices(Context context, Intent intent) {
        return context.getPackageManager().queryIntentServices(intent, 512);
    }

    public static List<ResolveInfo> queryBroadcastReceivers(Context context, Intent intent) {
        return context.getPackageManager().queryBroadcastReceivers(intent, 512);
    }

    public static List<ResolveInfo> queryIntentActivities(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, 512);
    }

    public static List<ResolveInfo> queryIntentContentProviders(Context context, Intent intent) {
        return context.getPackageManager().queryIntentContentProviders(intent, 512);
    }

    public static int getComponentState(Context context, ComponentName componentName) {
        return context.getApplicationContext().getPackageManager().getComponentEnabledSetting(componentName);
    }

    public static Set<String> getAllComponents(Context context, String pkgName) {
        Set<String> components = new ArraySet();
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(pkgName, 527);
            components.addAll(getComponets(info.activities));
            components.addAll(getComponets(info.services));
            components.addAll(getComponets(info.receivers));
            components.addAll(getComponets(info.providers));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return components;
    }

    private static <T extends PackageItemInfo> Set<String> getComponets(T[] array) {
        Set<String> components = new ArraySet();
        if (!ArrayUtils.isEmpty(array)) {
            for (PackageItemInfo info : array) {
                components.add(info.name);
            }
        }
        return components;
    }

    public static String componentStateToString(int state) {
        switch (state) {
            case 0:
                return "COMPONENT_ENABLED_STATE_DEFAULT";
            case 1:
                return "COMPONENT_ENABLED_STATE_ENABLED";
            case 2:
                return "COMPONENT_ENABLED_STATE_DISABLED";
            case 3:
                return "COMPONENT_ENABLED_STATE_DISABLED_USER";
            case 4:
                return "COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED";
            default:
                return Integer.toString(state);
        }
    }
}
