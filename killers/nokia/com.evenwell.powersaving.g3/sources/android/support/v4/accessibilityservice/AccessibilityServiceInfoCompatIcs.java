package android.support.v4.accessibilityservice;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.pm.ResolveInfo;

class AccessibilityServiceInfoCompatIcs {
    AccessibilityServiceInfoCompatIcs() {
    }

    public static boolean getCanRetrieveWindowContent(AccessibilityServiceInfo info) {
        return info.getCanRetrieveWindowContent();
    }

    public static String getDescription(AccessibilityServiceInfo info) {
        return info.getDescription();
    }

    public static String getId(AccessibilityServiceInfo info) {
        return info.getId();
    }

    public static ResolveInfo getResolveInfo(AccessibilityServiceInfo info) {
        return info.getResolveInfo();
    }

    public static String getSettingsActivityName(AccessibilityServiceInfo info) {
        return info.getSettingsActivityName();
    }
}
