package android.support.v4.view;

import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

class AccessibilityDelegateCompatIcs {

    public interface AccessibilityDelegateBridge {
        boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        void onInitializeAccessibilityNodeInfo(View view, Object obj);

        void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent);

        void sendAccessibilityEvent(View view, int i);

        void sendAccessibilityEventUnchecked(View view, AccessibilityEvent accessibilityEvent);
    }

    AccessibilityDelegateCompatIcs() {
    }

    public static Object newAccessibilityDelegateDefaultImpl() {
        return new AccessibilityDelegate();
    }

    public static Object newAccessibilityDelegateBridge(final AccessibilityDelegateBridge bridge) {
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
        };
    }

    public static boolean dispatchPopulateAccessibilityEvent(Object delegate, View host, AccessibilityEvent event) {
        return ((AccessibilityDelegate) delegate).dispatchPopulateAccessibilityEvent(host, event);
    }

    public static void onInitializeAccessibilityEvent(Object delegate, View host, AccessibilityEvent event) {
        ((AccessibilityDelegate) delegate).onInitializeAccessibilityEvent(host, event);
    }

    public static void onInitializeAccessibilityNodeInfo(Object delegate, View host, Object info) {
        ((AccessibilityDelegate) delegate).onInitializeAccessibilityNodeInfo(host, (AccessibilityNodeInfo) info);
    }

    public static void onPopulateAccessibilityEvent(Object delegate, View host, AccessibilityEvent event) {
        ((AccessibilityDelegate) delegate).onPopulateAccessibilityEvent(host, event);
    }

    public static boolean onRequestSendAccessibilityEvent(Object delegate, ViewGroup host, View child, AccessibilityEvent event) {
        return ((AccessibilityDelegate) delegate).onRequestSendAccessibilityEvent(host, child, event);
    }

    public static void sendAccessibilityEvent(Object delegate, View host, int eventType) {
        ((AccessibilityDelegate) delegate).sendAccessibilityEvent(host, eventType);
    }

    public static void sendAccessibilityEventUnchecked(Object delegate, View host, AccessibilityEvent event) {
        ((AccessibilityDelegate) delegate).sendAccessibilityEventUnchecked(host, event);
    }
}
