package android.support.v4.view;

import android.os.Bundle;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

class AccessibilityDelegateCompatJellyBean {

    public interface AccessibilityDelegateBridgeJellyBean {
        boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        Object getAccessibilityNodeProvider(View view);

        void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        void onInitializeAccessibilityNodeInfo(View view, Object obj);

        void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent);

        boolean performAccessibilityAction(View view, int i, Bundle bundle);

        void sendAccessibilityEvent(View view, int i);

        void sendAccessibilityEventUnchecked(View view, AccessibilityEvent accessibilityEvent);
    }

    AccessibilityDelegateCompatJellyBean() {
    }

    public static Object newAccessibilityDelegateBridge(final AccessibilityDelegateBridgeJellyBean bridge) {
        return new AccessibilityDelegate() {
            public boolean dispatchPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                return bridge.dispatchPopulateAccessibilityEvent(host, event);
            }

            public void onInitializeAccessibilityEvent(View host, AccessibilityEvent event) {
                bridge.onInitializeAccessibilityEvent(host, event);
            }

            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                bridge.onInitializeAccessibilityNodeInfo(host, info);
            }

            public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
                bridge.onPopulateAccessibilityEvent(host, event);
            }

            public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
                return bridge.onRequestSendAccessibilityEvent(host, child, event);
            }

            public void sendAccessibilityEvent(View host, int eventType) {
                bridge.sendAccessibilityEvent(host, eventType);
            }

            public void sendAccessibilityEventUnchecked(View host, AccessibilityEvent event) {
                bridge.sendAccessibilityEventUnchecked(host, event);
            }

            public AccessibilityNodeProvider getAccessibilityNodeProvider(View host) {
                return (AccessibilityNodeProvider) bridge.getAccessibilityNodeProvider(host);
            }

            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                return bridge.performAccessibilityAction(host, action, args);
            }
        };
    }

    public static Object getAccessibilityNodeProvider(Object delegate, View host) {
        return ((AccessibilityDelegate) delegate).getAccessibilityNodeProvider(host);
    }

    public static boolean performAccessibilityAction(Object delegate, View host, int action, Bundle args) {
        return ((AccessibilityDelegate) delegate).performAccessibilityAction(host, action, args);
    }
}
