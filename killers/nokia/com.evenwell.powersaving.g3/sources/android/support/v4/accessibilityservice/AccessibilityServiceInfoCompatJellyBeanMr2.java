package android.support.v4.accessibilityservice;

import android.accessibilityservice.AccessibilityServiceInfo;

class AccessibilityServiceInfoCompatJellyBeanMr2 {
    AccessibilityServiceInfoCompatJellyBeanMr2() {
    }

    public static int getCapabilities(AccessibilityServiceInfo info) {
        return info.getCapabilities();
    }
}
