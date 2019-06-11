package android.support.v4.view.accessibility;

import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccessibilityNodeInfoCompat {
    public static final int ACTION_ACCESSIBILITY_FOCUS = 64;
    public static final String ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN = "ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN";
    public static final String ACTION_ARGUMENT_HTML_ELEMENT_STRING = "ACTION_ARGUMENT_HTML_ELEMENT_STRING";
    public static final String ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT = "ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT";
    public static final String ACTION_ARGUMENT_SELECTION_END_INT = "ACTION_ARGUMENT_SELECTION_END_INT";
    public static final String ACTION_ARGUMENT_SELECTION_START_INT = "ACTION_ARGUMENT_SELECTION_START_INT";
    public static final String ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE = "ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE";
    public static final int ACTION_CLEAR_ACCESSIBILITY_FOCUS = 128;
    public static final int ACTION_CLEAR_FOCUS = 2;
    public static final int ACTION_CLEAR_SELECTION = 8;
    public static final int ACTION_CLICK = 16;
    public static final int ACTION_COLLAPSE = 524288;
    public static final int ACTION_COPY = 16384;
    public static final int ACTION_CUT = 65536;
    public static final int ACTION_DISMISS = 1048576;
    public static final int ACTION_EXPAND = 262144;
    public static final int ACTION_FOCUS = 1;
    public static final int ACTION_LONG_CLICK = 32;
    public static final int ACTION_NEXT_AT_MOVEMENT_GRANULARITY = 256;
    public static final int ACTION_NEXT_HTML_ELEMENT = 1024;
    public static final int ACTION_PASTE = 32768;
    public static final int ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = 512;
    public static final int ACTION_PREVIOUS_HTML_ELEMENT = 2048;
    public static final int ACTION_SCROLL_BACKWARD = 8192;
    public static final int ACTION_SCROLL_FORWARD = 4096;
    public static final int ACTION_SELECT = 4;
    public static final int ACTION_SET_SELECTION = 131072;
    public static final int ACTION_SET_TEXT = 2097152;
    public static final int FOCUS_ACCESSIBILITY = 2;
    public static final int FOCUS_INPUT = 1;
    private static final AccessibilityNodeInfoImpl IMPL;
    public static final int MOVEMENT_GRANULARITY_CHARACTER = 1;
    public static final int MOVEMENT_GRANULARITY_LINE = 4;
    public static final int MOVEMENT_GRANULARITY_PAGE = 16;
    public static final int MOVEMENT_GRANULARITY_PARAGRAPH = 8;
    public static final int MOVEMENT_GRANULARITY_WORD = 2;
    private final Object mInfo;

    public static class AccessibilityActionCompat {
        public static final AccessibilityActionCompat ACTION_ACCESSIBILITY_FOCUS = new AccessibilityActionCompat(64, null);
        public static final AccessibilityActionCompat ACTION_CLEAR_ACCESSIBILITY_FOCUS = new AccessibilityActionCompat(128, null);
        public static final AccessibilityActionCompat ACTION_CLEAR_FOCUS = new AccessibilityActionCompat(2, null);
        public static final AccessibilityActionCompat ACTION_CLEAR_SELECTION = new AccessibilityActionCompat(8, null);
        public static final AccessibilityActionCompat ACTION_CLICK = new AccessibilityActionCompat(16, null);
        public static final AccessibilityActionCompat ACTION_COLLAPSE = new AccessibilityActionCompat(524288, null);
        public static final AccessibilityActionCompat ACTION_COPY = new AccessibilityActionCompat(16384, null);
        public static final AccessibilityActionCompat ACTION_CUT = new AccessibilityActionCompat(65536, null);
        public static final AccessibilityActionCompat ACTION_DISMISS = new AccessibilityActionCompat(1048576, null);
        public static final AccessibilityActionCompat ACTION_EXPAND = new AccessibilityActionCompat(262144, null);
        public static final AccessibilityActionCompat ACTION_FOCUS = new AccessibilityActionCompat(1, null);
        public static final AccessibilityActionCompat ACTION_LONG_CLICK = new AccessibilityActionCompat(32, null);
        public static final AccessibilityActionCompat ACTION_NEXT_AT_MOVEMENT_GRANULARITY = new AccessibilityActionCompat(256, null);
        public static final AccessibilityActionCompat ACTION_NEXT_HTML_ELEMENT = new AccessibilityActionCompat(1024, null);
        public static final AccessibilityActionCompat ACTION_PASTE = new AccessibilityActionCompat(32768, null);
        public static final AccessibilityActionCompat ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY = new AccessibilityActionCompat(512, null);
        public static final AccessibilityActionCompat ACTION_PREVIOUS_HTML_ELEMENT = new AccessibilityActionCompat(2048, null);
        public static final AccessibilityActionCompat ACTION_SCROLL_BACKWARD = new AccessibilityActionCompat(8192, null);
        public static final AccessibilityActionCompat ACTION_SCROLL_FORWARD = new AccessibilityActionCompat(4096, null);
        public static final AccessibilityActionCompat ACTION_SELECT = new AccessibilityActionCompat(4, null);
        public static final AccessibilityActionCompat ACTION_SET_SELECTION = new AccessibilityActionCompat(131072, null);
        public static final AccessibilityActionCompat ACTION_SET_TEXT = new AccessibilityActionCompat(2097152, null);
        private final Object mAction;

        public AccessibilityActionCompat(int actionId, CharSequence label) {
            this(AccessibilityNodeInfoCompat.IMPL.newAccessibilityAction(actionId, label));
        }

        private AccessibilityActionCompat(Object action) {
            this.mAction = action;
        }

        public int getId() {
            return AccessibilityNodeInfoCompat.IMPL.getAccessibilityActionId(this.mAction);
        }

        public CharSequence getLabel() {
            return AccessibilityNodeInfoCompat.IMPL.getAccessibilityActionLabel(this.mAction);
        }
    }

    interface AccessibilityNodeInfoImpl {
        void addAction(Object obj, int i);

        void addAction(Object obj, Object obj2);

        void addChild(Object obj, View view);

        void addChild(Object obj, View view, int i);

        boolean canOpenPopup(Object obj);

        List<Object> findAccessibilityNodeInfosByText(Object obj, String str);

        List<Object> findAccessibilityNodeInfosByViewId(Object obj, String str);

        Object findFocus(Object obj, int i);

        Object focusSearch(Object obj, int i);

        int getAccessibilityActionId(Object obj);

        CharSequence getAccessibilityActionLabel(Object obj);

        List<Object> getActionList(Object obj);

        int getActions(Object obj);

        void getBoundsInParent(Object obj, Rect rect);

        void getBoundsInScreen(Object obj, Rect rect);

        Object getChild(Object obj, int i);

        int getChildCount(Object obj);

        CharSequence getClassName(Object obj);

        Object getCollectionInfo(Object obj);

        int getCollectionInfoColumnCount(Object obj);

        int getCollectionInfoRowCount(Object obj);

        int getCollectionItemColumnIndex(Object obj);

        int getCollectionItemColumnSpan(Object obj);

        Object getCollectionItemInfo(Object obj);

        int getCollectionItemRowIndex(Object obj);

        int getCollectionItemRowSpan(Object obj);

        CharSequence getContentDescription(Object obj);

        CharSequence getError(Object obj);

        Bundle getExtras(Object obj);

        int getInputType(Object obj);

        Object getLabelFor(Object obj);

        Object getLabeledBy(Object obj);

        int getLiveRegion(Object obj);

        int getMaxTextLength(Object obj);

        int getMovementGranularities(Object obj);

        CharSequence getPackageName(Object obj);

        Object getParent(Object obj);

        Object getRangeInfo(Object obj);

        CharSequence getText(Object obj);

        int getTextSelectionEnd(Object obj);

        int getTextSelectionStart(Object obj);

        Object getTraversalAfter(Object obj);

        Object getTraversalBefore(Object obj);

        String getViewIdResourceName(Object obj);

        Object getWindow(Object obj);

        int getWindowId(Object obj);

        boolean isAccessibilityFocused(Object obj);

        boolean isCheckable(Object obj);

        boolean isChecked(Object obj);

        boolean isClickable(Object obj);

        boolean isCollectionInfoHierarchical(Object obj);

        boolean isCollectionItemHeading(Object obj);

        boolean isCollectionItemSelected(Object obj);

        boolean isContentInvalid(Object obj);

        boolean isDismissable(Object obj);

        boolean isEditable(Object obj);

        boolean isEnabled(Object obj);

        boolean isFocusable(Object obj);

        boolean isFocused(Object obj);

        boolean isLongClickable(Object obj);

        boolean isMultiLine(Object obj);

        boolean isPassword(Object obj);

        boolean isScrollable(Object obj);

        boolean isSelected(Object obj);

        boolean isVisibleToUser(Object obj);

        Object newAccessibilityAction(int i, CharSequence charSequence);

        Object obtain();

        Object obtain(View view);

        Object obtain(View view, int i);

        Object obtain(Object obj);

        Object obtainCollectionInfo(int i, int i2, boolean z, int i3);

        Object obtainCollectionItemInfo(int i, int i2, int i3, int i4, boolean z, boolean z2);

        boolean performAction(Object obj, int i);

        boolean performAction(Object obj, int i, Bundle bundle);

        void recycle(Object obj);

        boolean refresh(Object obj);

        boolean removeAction(Object obj, Object obj2);

        boolean removeChild(Object obj, View view);

        boolean removeChild(Object obj, View view, int i);

        void setAccessibilityFocused(Object obj, boolean z);

        void setBoundsInParent(Object obj, Rect rect);

        void setBoundsInScreen(Object obj, Rect rect);

        void setCanOpenPopup(Object obj, boolean z);

        void setCheckable(Object obj, boolean z);

        void setChecked(Object obj, boolean z);

        void setClassName(Object obj, CharSequence charSequence);

        void setClickable(Object obj, boolean z);

        void setCollectionInfo(Object obj, Object obj2);

        void setCollectionItemInfo(Object obj, Object obj2);

        void setContentDescription(Object obj, CharSequence charSequence);

        void setContentInvalid(Object obj, boolean z);

        void setDismissable(Object obj, boolean z);

        void setEditable(Object obj, boolean z);

        void setEnabled(Object obj, boolean z);

        void setError(Object obj, CharSequence charSequence);

        void setFocusable(Object obj, boolean z);

        void setFocused(Object obj, boolean z);

        void setInputType(Object obj, int i);

        void setLabelFor(Object obj, View view);

        void setLabelFor(Object obj, View view, int i);

        void setLabeledBy(Object obj, View view);

        void setLabeledBy(Object obj, View view, int i);

        void setLiveRegion(Object obj, int i);

        void setLongClickable(Object obj, boolean z);

        void setMaxTextLength(Object obj, int i);

        void setMovementGranularities(Object obj, int i);

        void setMultiLine(Object obj, boolean z);

        void setPackageName(Object obj, CharSequence charSequence);

        void setParent(Object obj, View view);

        void setParent(Object obj, View view, int i);

        void setPassword(Object obj, boolean z);

        void setRangeInfo(Object obj, Object obj2);

        void setScrollable(Object obj, boolean z);

        void setSelected(Object obj, boolean z);

        void setSource(Object obj, View view);

        void setSource(Object obj, View view, int i);

        void setText(Object obj, CharSequence charSequence);

        void setTextSelection(Object obj, int i, int i2);

        void setTraversalAfter(Object obj, View view);

        void setTraversalAfter(Object obj, View view, int i);

        void setTraversalBefore(Object obj, View view);

        void setTraversalBefore(Object obj, View view, int i);

        void setViewIdResourceName(Object obj, String str);

        void setVisibleToUser(Object obj, boolean z);
    }

    static class AccessibilityNodeInfoStubImpl implements AccessibilityNodeInfoImpl {
        AccessibilityNodeInfoStubImpl() {
        }

        public Object newAccessibilityAction(int actionId, CharSequence label) {
            return null;
        }

        public Object obtain() {
            return null;
        }

        public Object obtain(View source) {
            return null;
        }

        public Object obtain(View root, int virtualDescendantId) {
            return null;
        }

        public Object obtain(Object info) {
            return null;
        }

        public void addAction(Object info, int action) {
        }

        public void addAction(Object info, Object action) {
        }

        public boolean removeAction(Object info, Object action) {
            return false;
        }

        public int getAccessibilityActionId(Object action) {
            return 0;
        }

        public CharSequence getAccessibilityActionLabel(Object action) {
            return null;
        }

        public void addChild(Object info, View child) {
        }

        public void addChild(Object info, View child, int virtualDescendantId) {
        }

        public boolean removeChild(Object info, View child) {
            return false;
        }

        public boolean removeChild(Object info, View root, int virtualDescendantId) {
            return false;
        }

        public List<Object> findAccessibilityNodeInfosByText(Object info, String text) {
            return Collections.emptyList();
        }

        public int getActions(Object info) {
            return 0;
        }

        public void getBoundsInParent(Object info, Rect outBounds) {
        }

        public void getBoundsInScreen(Object info, Rect outBounds) {
        }

        public Object getChild(Object info, int index) {
            return null;
        }

        public int getChildCount(Object info) {
            return 0;
        }

        public CharSequence getClassName(Object info) {
            return null;
        }

        public CharSequence getContentDescription(Object info) {
            return null;
        }

        public CharSequence getPackageName(Object info) {
            return null;
        }

        public Object getParent(Object info) {
            return null;
        }

        public CharSequence getText(Object info) {
            return null;
        }

        public int getWindowId(Object info) {
            return 0;
        }

        public boolean isCheckable(Object info) {
            return false;
        }

        public boolean isChecked(Object info) {
            return false;
        }

        public boolean isClickable(Object info) {
            return false;
        }

        public boolean isEnabled(Object info) {
            return false;
        }

        public boolean isFocusable(Object info) {
            return false;
        }

        public boolean isFocused(Object info) {
            return false;
        }

        public boolean isVisibleToUser(Object info) {
            return false;
        }

        public boolean isAccessibilityFocused(Object info) {
            return false;
        }

        public boolean isLongClickable(Object info) {
            return false;
        }

        public boolean isPassword(Object info) {
            return false;
        }

        public boolean isScrollable(Object info) {
            return false;
        }

        public boolean isSelected(Object info) {
            return false;
        }

        public boolean performAction(Object info, int action) {
            return false;
        }

        public boolean performAction(Object info, int action, Bundle arguments) {
            return false;
        }

        public void setMovementGranularities(Object info, int granularities) {
        }

        public int getMovementGranularities(Object info) {
            return 0;
        }

        public void setBoundsInParent(Object info, Rect bounds) {
        }

        public void setBoundsInScreen(Object info, Rect bounds) {
        }

        public void setCheckable(Object info, boolean checkable) {
        }

        public void setChecked(Object info, boolean checked) {
        }

        public void setClassName(Object info, CharSequence className) {
        }

        public void setClickable(Object info, boolean clickable) {
        }

        public void setContentDescription(Object info, CharSequence contentDescription) {
        }

        public void setEnabled(Object info, boolean enabled) {
        }

        public void setFocusable(Object info, boolean focusable) {
        }

        public void setFocused(Object info, boolean focused) {
        }

        public void setVisibleToUser(Object info, boolean visibleToUser) {
        }

        public void setAccessibilityFocused(Object info, boolean focused) {
        }

        public void setLongClickable(Object info, boolean longClickable) {
        }

        public void setPackageName(Object info, CharSequence packageName) {
        }

        public void setParent(Object info, View parent) {
        }

        public void setPassword(Object info, boolean password) {
        }

        public void setScrollable(Object info, boolean scrollable) {
        }

        public void setSelected(Object info, boolean selected) {
        }

        public void setSource(Object info, View source) {
        }

        public void setSource(Object info, View root, int virtualDescendantId) {
        }

        public Object findFocus(Object info, int focus) {
            return null;
        }

        public Object focusSearch(Object info, int direction) {
            return null;
        }

        public void setText(Object info, CharSequence text) {
        }

        public void recycle(Object info) {
        }

        public void setParent(Object info, View root, int virtualDescendantId) {
        }

        public String getViewIdResourceName(Object info) {
            return null;
        }

        public void setViewIdResourceName(Object info, String viewId) {
        }

        public int getLiveRegion(Object info) {
            return 0;
        }

        public void setLiveRegion(Object info, int mode) {
        }

        public Object getCollectionInfo(Object info) {
            return null;
        }

        public void setCollectionInfo(Object info, Object collectionInfo) {
        }

        public Object getCollectionItemInfo(Object info) {
            return null;
        }

        public void setCollectionItemInfo(Object info, Object collectionItemInfo) {
        }

        public Object getRangeInfo(Object info) {
            return null;
        }

        public void setRangeInfo(Object info, Object rangeInfo) {
        }

        public List<Object> getActionList(Object info) {
            return null;
        }

        public Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return null;
        }

        public int getCollectionInfoColumnCount(Object info) {
            return 0;
        }

        public int getCollectionInfoRowCount(Object info) {
            return 0;
        }

        public boolean isCollectionInfoHierarchical(Object info) {
            return false;
        }

        public Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return null;
        }

        public int getCollectionItemColumnIndex(Object info) {
            return 0;
        }

        public int getCollectionItemColumnSpan(Object info) {
            return 0;
        }

        public int getCollectionItemRowIndex(Object info) {
            return 0;
        }

        public int getCollectionItemRowSpan(Object info) {
            return 0;
        }

        public boolean isCollectionItemHeading(Object info) {
            return false;
        }

        public boolean isCollectionItemSelected(Object info) {
            return false;
        }

        public Object getTraversalBefore(Object info) {
            return null;
        }

        public void setTraversalBefore(Object info, View view) {
        }

        public void setTraversalBefore(Object info, View root, int virtualDescendantId) {
        }

        public Object getTraversalAfter(Object info) {
            return null;
        }

        public void setTraversalAfter(Object info, View view) {
        }

        public void setTraversalAfter(Object info, View root, int virtualDescendantId) {
        }

        public void setContentInvalid(Object info, boolean contentInvalid) {
        }

        public boolean isContentInvalid(Object info) {
            return false;
        }

        public void setError(Object info, CharSequence error) {
        }

        public CharSequence getError(Object info) {
            return null;
        }

        public void setLabelFor(Object info, View labeled) {
        }

        public void setLabelFor(Object info, View root, int virtualDescendantId) {
        }

        public Object getLabelFor(Object info) {
            return null;
        }

        public void setLabeledBy(Object info, View labeled) {
        }

        public void setLabeledBy(Object info, View root, int virtualDescendantId) {
        }

        public Object getLabeledBy(Object info) {
            return null;
        }

        public boolean canOpenPopup(Object info) {
            return false;
        }

        public void setCanOpenPopup(Object info, boolean opensPopup) {
        }

        public List<Object> findAccessibilityNodeInfosByViewId(Object info, String viewId) {
            return Collections.emptyList();
        }

        public Bundle getExtras(Object info) {
            return new Bundle();
        }

        public int getInputType(Object info) {
            return 0;
        }

        public void setInputType(Object info, int inputType) {
        }

        public void setMaxTextLength(Object info, int max) {
        }

        public int getMaxTextLength(Object info) {
            return -1;
        }

        public void setTextSelection(Object info, int start, int end) {
        }

        public int getTextSelectionStart(Object info) {
            return -1;
        }

        public int getTextSelectionEnd(Object info) {
            return -1;
        }

        public Object getWindow(Object info) {
            return null;
        }

        public boolean isDismissable(Object info) {
            return false;
        }

        public void setDismissable(Object info, boolean dismissable) {
        }

        public boolean isEditable(Object info) {
            return false;
        }

        public void setEditable(Object info, boolean editable) {
        }

        public boolean isMultiLine(Object info) {
            return false;
        }

        public void setMultiLine(Object info, boolean multiLine) {
        }

        public boolean refresh(Object info) {
            return false;
        }
    }

    static class AccessibilityNodeInfoIcsImpl extends AccessibilityNodeInfoStubImpl {
        AccessibilityNodeInfoIcsImpl() {
        }

        public Object obtain() {
            return AccessibilityNodeInfoCompatIcs.obtain();
        }

        public Object obtain(View source) {
            return AccessibilityNodeInfoCompatIcs.obtain(source);
        }

        public Object obtain(Object info) {
            return AccessibilityNodeInfoCompatIcs.obtain(info);
        }

        public void addAction(Object info, int action) {
            AccessibilityNodeInfoCompatIcs.addAction(info, action);
        }

        public void addChild(Object info, View child) {
            AccessibilityNodeInfoCompatIcs.addChild(info, child);
        }

        public List<Object> findAccessibilityNodeInfosByText(Object info, String text) {
            return AccessibilityNodeInfoCompatIcs.findAccessibilityNodeInfosByText(info, text);
        }

        public int getActions(Object info) {
            return AccessibilityNodeInfoCompatIcs.getActions(info);
        }

        public void getBoundsInParent(Object info, Rect outBounds) {
            AccessibilityNodeInfoCompatIcs.getBoundsInParent(info, outBounds);
        }

        public void getBoundsInScreen(Object info, Rect outBounds) {
            AccessibilityNodeInfoCompatIcs.getBoundsInScreen(info, outBounds);
        }

        public Object getChild(Object info, int index) {
            return AccessibilityNodeInfoCompatIcs.getChild(info, index);
        }

        public int getChildCount(Object info) {
            return AccessibilityNodeInfoCompatIcs.getChildCount(info);
        }

        public CharSequence getClassName(Object info) {
            return AccessibilityNodeInfoCompatIcs.getClassName(info);
        }

        public CharSequence getContentDescription(Object info) {
            return AccessibilityNodeInfoCompatIcs.getContentDescription(info);
        }

        public CharSequence getPackageName(Object info) {
            return AccessibilityNodeInfoCompatIcs.getPackageName(info);
        }

        public Object getParent(Object info) {
            return AccessibilityNodeInfoCompatIcs.getParent(info);
        }

        public CharSequence getText(Object info) {
            return AccessibilityNodeInfoCompatIcs.getText(info);
        }

        public int getWindowId(Object info) {
            return AccessibilityNodeInfoCompatIcs.getWindowId(info);
        }

        public boolean isCheckable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isCheckable(info);
        }

        public boolean isChecked(Object info) {
            return AccessibilityNodeInfoCompatIcs.isChecked(info);
        }

        public boolean isClickable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isClickable(info);
        }

        public boolean isEnabled(Object info) {
            return AccessibilityNodeInfoCompatIcs.isEnabled(info);
        }

        public boolean isFocusable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isFocusable(info);
        }

        public boolean isFocused(Object info) {
            return AccessibilityNodeInfoCompatIcs.isFocused(info);
        }

        public boolean isLongClickable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isLongClickable(info);
        }

        public boolean isPassword(Object info) {
            return AccessibilityNodeInfoCompatIcs.isPassword(info);
        }

        public boolean isScrollable(Object info) {
            return AccessibilityNodeInfoCompatIcs.isScrollable(info);
        }

        public boolean isSelected(Object info) {
            return AccessibilityNodeInfoCompatIcs.isSelected(info);
        }

        public boolean performAction(Object info, int action) {
            return AccessibilityNodeInfoCompatIcs.performAction(info, action);
        }

        public void setBoundsInParent(Object info, Rect bounds) {
            AccessibilityNodeInfoCompatIcs.setBoundsInParent(info, bounds);
        }

        public void setBoundsInScreen(Object info, Rect bounds) {
            AccessibilityNodeInfoCompatIcs.setBoundsInScreen(info, bounds);
        }

        public void setCheckable(Object info, boolean checkable) {
            AccessibilityNodeInfoCompatIcs.setCheckable(info, checkable);
        }

        public void setChecked(Object info, boolean checked) {
            AccessibilityNodeInfoCompatIcs.setChecked(info, checked);
        }

        public void setClassName(Object info, CharSequence className) {
            AccessibilityNodeInfoCompatIcs.setClassName(info, className);
        }

        public void setClickable(Object info, boolean clickable) {
            AccessibilityNodeInfoCompatIcs.setClickable(info, clickable);
        }

        public void setContentDescription(Object info, CharSequence contentDescription) {
            AccessibilityNodeInfoCompatIcs.setContentDescription(info, contentDescription);
        }

        public void setEnabled(Object info, boolean enabled) {
            AccessibilityNodeInfoCompatIcs.setEnabled(info, enabled);
        }

        public void setFocusable(Object info, boolean focusable) {
            AccessibilityNodeInfoCompatIcs.setFocusable(info, focusable);
        }

        public void setFocused(Object info, boolean focused) {
            AccessibilityNodeInfoCompatIcs.setFocused(info, focused);
        }

        public void setLongClickable(Object info, boolean longClickable) {
            AccessibilityNodeInfoCompatIcs.setLongClickable(info, longClickable);
        }

        public void setPackageName(Object info, CharSequence packageName) {
            AccessibilityNodeInfoCompatIcs.setPackageName(info, packageName);
        }

        public void setParent(Object info, View parent) {
            AccessibilityNodeInfoCompatIcs.setParent(info, parent);
        }

        public void setPassword(Object info, boolean password) {
            AccessibilityNodeInfoCompatIcs.setPassword(info, password);
        }

        public void setScrollable(Object info, boolean scrollable) {
            AccessibilityNodeInfoCompatIcs.setScrollable(info, scrollable);
        }

        public void setSelected(Object info, boolean selected) {
            AccessibilityNodeInfoCompatIcs.setSelected(info, selected);
        }

        public void setSource(Object info, View source) {
            AccessibilityNodeInfoCompatIcs.setSource(info, source);
        }

        public void setText(Object info, CharSequence text) {
            AccessibilityNodeInfoCompatIcs.setText(info, text);
        }

        public void recycle(Object info) {
            AccessibilityNodeInfoCompatIcs.recycle(info);
        }
    }

    static class AccessibilityNodeInfoJellybeanImpl extends AccessibilityNodeInfoIcsImpl {
        AccessibilityNodeInfoJellybeanImpl() {
        }

        public Object obtain(View root, int virtualDescendantId) {
            return AccessibilityNodeInfoCompatJellyBean.obtain(root, virtualDescendantId);
        }

        public Object findFocus(Object info, int focus) {
            return AccessibilityNodeInfoCompatJellyBean.findFocus(info, focus);
        }

        public Object focusSearch(Object info, int direction) {
            return AccessibilityNodeInfoCompatJellyBean.focusSearch(info, direction);
        }

        public void addChild(Object info, View child, int virtualDescendantId) {
            AccessibilityNodeInfoCompatJellyBean.addChild(info, child, virtualDescendantId);
        }

        public void setSource(Object info, View root, int virtualDescendantId) {
            AccessibilityNodeInfoCompatJellyBean.setSource(info, root, virtualDescendantId);
        }

        public boolean isVisibleToUser(Object info) {
            return AccessibilityNodeInfoCompatJellyBean.isVisibleToUser(info);
        }

        public void setVisibleToUser(Object info, boolean visibleToUser) {
            AccessibilityNodeInfoCompatJellyBean.setVisibleToUser(info, visibleToUser);
        }

        public boolean isAccessibilityFocused(Object info) {
            return AccessibilityNodeInfoCompatJellyBean.isAccessibilityFocused(info);
        }

        public void setAccessibilityFocused(Object info, boolean focused) {
            AccessibilityNodeInfoCompatJellyBean.setAccesibilityFocused(info, focused);
        }

        public boolean performAction(Object info, int action, Bundle arguments) {
            return AccessibilityNodeInfoCompatJellyBean.performAction(info, action, arguments);
        }

        public void setMovementGranularities(Object info, int granularities) {
            AccessibilityNodeInfoCompatJellyBean.setMovementGranularities(info, granularities);
        }

        public int getMovementGranularities(Object info) {
            return AccessibilityNodeInfoCompatJellyBean.getMovementGranularities(info);
        }

        public void setParent(Object info, View root, int virtualDescendantId) {
            AccessibilityNodeInfoCompatJellyBean.setParent(info, root, virtualDescendantId);
        }
    }

    static class AccessibilityNodeInfoJellybeanMr1Impl extends AccessibilityNodeInfoJellybeanImpl {
        AccessibilityNodeInfoJellybeanMr1Impl() {
        }

        public void setLabelFor(Object info, View labeled) {
            AccessibilityNodeInfoCompatJellybeanMr1.setLabelFor(info, labeled);
        }

        public void setLabelFor(Object info, View root, int virtualDescendantId) {
            AccessibilityNodeInfoCompatJellybeanMr1.setLabelFor(info, root, virtualDescendantId);
        }

        public Object getLabelFor(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr1.getLabelFor(info);
        }

        public void setLabeledBy(Object info, View labeled) {
            AccessibilityNodeInfoCompatJellybeanMr1.setLabeledBy(info, labeled);
        }

        public void setLabeledBy(Object info, View root, int virtualDescendantId) {
            AccessibilityNodeInfoCompatJellybeanMr1.setLabeledBy(info, root, virtualDescendantId);
        }

        public Object getLabeledBy(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr1.getLabeledBy(info);
        }
    }

    static class AccessibilityNodeInfoJellybeanMr2Impl extends AccessibilityNodeInfoJellybeanMr1Impl {
        AccessibilityNodeInfoJellybeanMr2Impl() {
        }

        public String getViewIdResourceName(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr2.getViewIdResourceName(info);
        }

        public void setViewIdResourceName(Object info, String viewId) {
            AccessibilityNodeInfoCompatJellybeanMr2.setViewIdResourceName(info, viewId);
        }

        public List<Object> findAccessibilityNodeInfosByViewId(Object info, String viewId) {
            return AccessibilityNodeInfoCompatJellybeanMr2.findAccessibilityNodeInfosByViewId(info, viewId);
        }

        public void setTextSelection(Object info, int start, int end) {
            AccessibilityNodeInfoCompatJellybeanMr2.setTextSelection(info, start, end);
        }

        public int getTextSelectionStart(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr2.getTextSelectionStart(info);
        }

        public int getTextSelectionEnd(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr2.getTextSelectionEnd(info);
        }

        public boolean isEditable(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr2.isEditable(info);
        }

        public void setEditable(Object info, boolean editable) {
            AccessibilityNodeInfoCompatJellybeanMr2.setEditable(info, editable);
        }

        public boolean refresh(Object info) {
            return AccessibilityNodeInfoCompatJellybeanMr2.refresh(info);
        }
    }

    static class AccessibilityNodeInfoKitKatImpl extends AccessibilityNodeInfoJellybeanMr2Impl {
        AccessibilityNodeInfoKitKatImpl() {
        }

        public int getLiveRegion(Object info) {
            return AccessibilityNodeInfoCompatKitKat.getLiveRegion(info);
        }

        public void setLiveRegion(Object info, int mode) {
            AccessibilityNodeInfoCompatKitKat.setLiveRegion(info, mode);
        }

        public Object getCollectionInfo(Object info) {
            return AccessibilityNodeInfoCompatKitKat.getCollectionInfo(info);
        }

        public void setCollectionInfo(Object info, Object collectionInfo) {
            AccessibilityNodeInfoCompatKitKat.setCollectionInfo(info, collectionInfo);
        }

        public Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return AccessibilityNodeInfoCompatKitKat.obtainCollectionInfo(rowCount, columnCount, hierarchical, selectionMode);
        }

        public Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return AccessibilityNodeInfoCompatKitKat.obtainCollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading);
        }

        public int getCollectionInfoColumnCount(Object info) {
            return CollectionInfo.getColumnCount(info);
        }

        public int getCollectionInfoRowCount(Object info) {
            return CollectionInfo.getRowCount(info);
        }

        public boolean isCollectionInfoHierarchical(Object info) {
            return CollectionInfo.isHierarchical(info);
        }

        public Object getCollectionItemInfo(Object info) {
            return AccessibilityNodeInfoCompatKitKat.getCollectionItemInfo(info);
        }

        public Object getRangeInfo(Object info) {
            return AccessibilityNodeInfoCompatKitKat.getRangeInfo(info);
        }

        public void setRangeInfo(Object info, Object rangeInfo) {
            AccessibilityNodeInfoCompatKitKat.setRangeInfo(info, rangeInfo);
        }

        public int getCollectionItemColumnIndex(Object info) {
            return CollectionItemInfo.getColumnIndex(info);
        }

        public int getCollectionItemColumnSpan(Object info) {
            return CollectionItemInfo.getColumnSpan(info);
        }

        public int getCollectionItemRowIndex(Object info) {
            return CollectionItemInfo.getRowIndex(info);
        }

        public int getCollectionItemRowSpan(Object info) {
            return CollectionItemInfo.getRowSpan(info);
        }

        public boolean isCollectionItemHeading(Object info) {
            return CollectionItemInfo.isHeading(info);
        }

        public void setCollectionItemInfo(Object info, Object collectionItemInfo) {
            AccessibilityNodeInfoCompatKitKat.setCollectionItemInfo(info, collectionItemInfo);
        }

        public void setContentInvalid(Object info, boolean contentInvalid) {
            AccessibilityNodeInfoCompatKitKat.setContentInvalid(info, contentInvalid);
        }

        public boolean isContentInvalid(Object info) {
            return AccessibilityNodeInfoCompatKitKat.isContentInvalid(info);
        }

        public boolean canOpenPopup(Object info) {
            return AccessibilityNodeInfoCompatKitKat.canOpenPopup(info);
        }

        public void setCanOpenPopup(Object info, boolean opensPopup) {
            AccessibilityNodeInfoCompatKitKat.setCanOpenPopup(info, opensPopup);
        }

        public Bundle getExtras(Object info) {
            return AccessibilityNodeInfoCompatKitKat.getExtras(info);
        }

        public int getInputType(Object info) {
            return AccessibilityNodeInfoCompatKitKat.getInputType(info);
        }

        public void setInputType(Object info, int inputType) {
            AccessibilityNodeInfoCompatKitKat.setInputType(info, inputType);
        }

        public boolean isDismissable(Object info) {
            return AccessibilityNodeInfoCompatKitKat.isDismissable(info);
        }

        public void setDismissable(Object info, boolean dismissable) {
            AccessibilityNodeInfoCompatKitKat.setDismissable(info, dismissable);
        }

        public boolean isMultiLine(Object info) {
            return AccessibilityNodeInfoCompatKitKat.isMultiLine(info);
        }

        public void setMultiLine(Object info, boolean multiLine) {
            AccessibilityNodeInfoCompatKitKat.setMultiLine(info, multiLine);
        }
    }

    static class AccessibilityNodeInfoApi21Impl extends AccessibilityNodeInfoKitKatImpl {
        AccessibilityNodeInfoApi21Impl() {
        }

        public Object newAccessibilityAction(int actionId, CharSequence label) {
            return AccessibilityNodeInfoCompatApi21.newAccessibilityAction(actionId, label);
        }

        public List<Object> getActionList(Object info) {
            return AccessibilityNodeInfoCompatApi21.getActionList(info);
        }

        public Object obtainCollectionInfo(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return AccessibilityNodeInfoCompatApi21.obtainCollectionInfo(rowCount, columnCount, hierarchical, selectionMode);
        }

        public void addAction(Object info, Object action) {
            AccessibilityNodeInfoCompatApi21.addAction(info, action);
        }

        public boolean removeAction(Object info, Object action) {
            return AccessibilityNodeInfoCompatApi21.removeAction(info, action);
        }

        public int getAccessibilityActionId(Object action) {
            return AccessibilityNodeInfoCompatApi21.getAccessibilityActionId(action);
        }

        public CharSequence getAccessibilityActionLabel(Object action) {
            return AccessibilityNodeInfoCompatApi21.getAccessibilityActionLabel(action);
        }

        public Object obtainCollectionItemInfo(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return AccessibilityNodeInfoCompatApi21.obtainCollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected);
        }

        public boolean isCollectionItemSelected(Object info) {
            return CollectionItemInfo.isSelected(info);
        }

        public CharSequence getError(Object info) {
            return AccessibilityNodeInfoCompatApi21.getError(info);
        }

        public void setError(Object info, CharSequence error) {
            AccessibilityNodeInfoCompatApi21.setError(info, error);
        }

        public void setMaxTextLength(Object info, int max) {
            AccessibilityNodeInfoCompatApi21.setMaxTextLength(info, max);
        }

        public int getMaxTextLength(Object info) {
            return AccessibilityNodeInfoCompatApi21.getMaxTextLength(info);
        }

        public Object getWindow(Object info) {
            return AccessibilityNodeInfoCompatApi21.getWindow(info);
        }

        public boolean removeChild(Object info, View child) {
            return AccessibilityNodeInfoCompatApi21.removeChild(info, child);
        }

        public boolean removeChild(Object info, View root, int virtualDescendantId) {
            return AccessibilityNodeInfoCompatApi21.removeChild(info, root, virtualDescendantId);
        }
    }

    static class AccessibilityNodeInfoApi22Impl extends AccessibilityNodeInfoApi21Impl {
        AccessibilityNodeInfoApi22Impl() {
        }

        public Object getTraversalBefore(Object info) {
            return AccessibilityNodeInfoCompatApi22.getTraversalBefore(info);
        }

        public void setTraversalBefore(Object info, View view) {
            AccessibilityNodeInfoCompatApi22.setTraversalBefore(info, view);
        }

        public void setTraversalBefore(Object info, View root, int virtualDescendantId) {
            AccessibilityNodeInfoCompatApi22.setTraversalBefore(info, root, virtualDescendantId);
        }

        public Object getTraversalAfter(Object info) {
            return AccessibilityNodeInfoCompatApi22.getTraversalAfter(info);
        }

        public void setTraversalAfter(Object info, View view) {
            AccessibilityNodeInfoCompatApi22.setTraversalAfter(info, view);
        }

        public void setTraversalAfter(Object info, View root, int virtualDescendantId) {
            AccessibilityNodeInfoCompatApi22.setTraversalAfter(info, root, virtualDescendantId);
        }
    }

    public static class CollectionInfoCompat {
        public static final int SELECTION_MODE_MULTIPLE = 2;
        public static final int SELECTION_MODE_NONE = 0;
        public static final int SELECTION_MODE_SINGLE = 1;
        final Object mInfo;

        public static CollectionInfoCompat obtain(int rowCount, int columnCount, boolean hierarchical, int selectionMode) {
            return new CollectionInfoCompat(AccessibilityNodeInfoCompat.IMPL.obtainCollectionInfo(rowCount, columnCount, hierarchical, selectionMode));
        }

        private CollectionInfoCompat(Object info) {
            this.mInfo = info;
        }

        public int getColumnCount() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionInfoColumnCount(this.mInfo);
        }

        public int getRowCount() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionInfoRowCount(this.mInfo);
        }

        public boolean isHierarchical() {
            return AccessibilityNodeInfoCompat.IMPL.isCollectionInfoHierarchical(this.mInfo);
        }
    }

    public static class CollectionItemInfoCompat {
        private final Object mInfo;

        public static CollectionItemInfoCompat obtain(int rowIndex, int rowSpan, int columnIndex, int columnSpan, boolean heading, boolean selected) {
            return new CollectionItemInfoCompat(AccessibilityNodeInfoCompat.IMPL.obtainCollectionItemInfo(rowIndex, rowSpan, columnIndex, columnSpan, heading, selected));
        }

        private CollectionItemInfoCompat(Object info) {
            this.mInfo = info;
        }

        public int getColumnIndex() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemColumnIndex(this.mInfo);
        }

        public int getColumnSpan() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemColumnSpan(this.mInfo);
        }

        public int getRowIndex() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemRowIndex(this.mInfo);
        }

        public int getRowSpan() {
            return AccessibilityNodeInfoCompat.IMPL.getCollectionItemRowSpan(this.mInfo);
        }

        public boolean isHeading() {
            return AccessibilityNodeInfoCompat.IMPL.isCollectionItemHeading(this.mInfo);
        }

        public boolean isSelected() {
            return AccessibilityNodeInfoCompat.IMPL.isCollectionItemSelected(this.mInfo);
        }
    }

    public static class RangeInfoCompat {
        public static final int RANGE_TYPE_FLOAT = 1;
        public static final int RANGE_TYPE_INT = 0;
        public static final int RANGE_TYPE_PERCENT = 2;
        private final Object mInfo;

        private RangeInfoCompat(Object info) {
            this.mInfo = info;
        }

        public float getCurrent() {
            return RangeInfo.getCurrent(this.mInfo);
        }

        public float getMax() {
            return RangeInfo.getMax(this.mInfo);
        }

        public float getMin() {
            return RangeInfo.getMin(this.mInfo);
        }

        public int getType() {
            return RangeInfo.getType(this.mInfo);
        }
    }

    static {
        if (VERSION.SDK_INT >= 22) {
            IMPL = new AccessibilityNodeInfoApi22Impl();
        } else if (VERSION.SDK_INT >= 21) {
            IMPL = new AccessibilityNodeInfoApi21Impl();
        } else if (VERSION.SDK_INT >= 19) {
            IMPL = new AccessibilityNodeInfoKitKatImpl();
        } else if (VERSION.SDK_INT >= 18) {
            IMPL = new AccessibilityNodeInfoJellybeanMr2Impl();
        } else if (VERSION.SDK_INT >= 17) {
            IMPL = new AccessibilityNodeInfoJellybeanMr1Impl();
        } else if (VERSION.SDK_INT >= 16) {
            IMPL = new AccessibilityNodeInfoJellybeanImpl();
        } else if (VERSION.SDK_INT >= 14) {
            IMPL = new AccessibilityNodeInfoIcsImpl();
        } else {
            IMPL = new AccessibilityNodeInfoStubImpl();
        }
    }

    static AccessibilityNodeInfoCompat wrapNonNullInstance(Object object) {
        if (object != null) {
            return new AccessibilityNodeInfoCompat(object);
        }
        return null;
    }

    public AccessibilityNodeInfoCompat(Object info) {
        this.mInfo = info;
    }

    public Object getInfo() {
        return this.mInfo;
    }

    public static AccessibilityNodeInfoCompat obtain(View source) {
        return wrapNonNullInstance(IMPL.obtain(source));
    }

    public static AccessibilityNodeInfoCompat obtain(View root, int virtualDescendantId) {
        return wrapNonNullInstance(IMPL.obtain(root, virtualDescendantId));
    }

    public static AccessibilityNodeInfoCompat obtain() {
        return wrapNonNullInstance(IMPL.obtain());
    }

    public static AccessibilityNodeInfoCompat obtain(AccessibilityNodeInfoCompat info) {
        return wrapNonNullInstance(IMPL.obtain(info.mInfo));
    }

    public void setSource(View source) {
        IMPL.setSource(this.mInfo, source);
    }

    public void setSource(View root, int virtualDescendantId) {
        IMPL.setSource(this.mInfo, root, virtualDescendantId);
    }

    public AccessibilityNodeInfoCompat findFocus(int focus) {
        return wrapNonNullInstance(IMPL.findFocus(this.mInfo, focus));
    }

    public AccessibilityNodeInfoCompat focusSearch(int direction) {
        return wrapNonNullInstance(IMPL.focusSearch(this.mInfo, direction));
    }

    public int getWindowId() {
        return IMPL.getWindowId(this.mInfo);
    }

    public int getChildCount() {
        return IMPL.getChildCount(this.mInfo);
    }

    public AccessibilityNodeInfoCompat getChild(int index) {
        return wrapNonNullInstance(IMPL.getChild(this.mInfo, index));
    }

    public void addChild(View child) {
        IMPL.addChild(this.mInfo, child);
    }

    public void addChild(View root, int virtualDescendantId) {
        IMPL.addChild(this.mInfo, root, virtualDescendantId);
    }

    public boolean removeChild(View child) {
        return IMPL.removeChild(this.mInfo, child);
    }

    public boolean removeChild(View root, int virtualDescendantId) {
        return IMPL.removeChild(this.mInfo, root, virtualDescendantId);
    }

    public int getActions() {
        return IMPL.getActions(this.mInfo);
    }

    public void addAction(int action) {
        IMPL.addAction(this.mInfo, action);
    }

    public void addAction(AccessibilityActionCompat action) {
        IMPL.addAction(this.mInfo, action.mAction);
    }

    public boolean removeAction(AccessibilityActionCompat action) {
        return IMPL.removeAction(this.mInfo, action.mAction);
    }

    public boolean performAction(int action) {
        return IMPL.performAction(this.mInfo, action);
    }

    public boolean performAction(int action, Bundle arguments) {
        return IMPL.performAction(this.mInfo, action, arguments);
    }

    public void setMovementGranularities(int granularities) {
        IMPL.setMovementGranularities(this.mInfo, granularities);
    }

    public int getMovementGranularities() {
        return IMPL.getMovementGranularities(this.mInfo);
    }

    public List<AccessibilityNodeInfoCompat> findAccessibilityNodeInfosByText(String text) {
        List<AccessibilityNodeInfoCompat> result = new ArrayList();
        List<Object> infos = IMPL.findAccessibilityNodeInfosByText(this.mInfo, text);
        int infoCount = infos.size();
        for (int i = 0; i < infoCount; i++) {
            result.add(new AccessibilityNodeInfoCompat(infos.get(i)));
        }
        return result;
    }

    public AccessibilityNodeInfoCompat getParent() {
        return wrapNonNullInstance(IMPL.getParent(this.mInfo));
    }

    public void setParent(View parent) {
        IMPL.setParent(this.mInfo, parent);
    }

    public void setParent(View root, int virtualDescendantId) {
        IMPL.setParent(this.mInfo, root, virtualDescendantId);
    }

    public void getBoundsInParent(Rect outBounds) {
        IMPL.getBoundsInParent(this.mInfo, outBounds);
    }

    public void setBoundsInParent(Rect bounds) {
        IMPL.setBoundsInParent(this.mInfo, bounds);
    }

    public void getBoundsInScreen(Rect outBounds) {
        IMPL.getBoundsInScreen(this.mInfo, outBounds);
    }

    public void setBoundsInScreen(Rect bounds) {
        IMPL.setBoundsInScreen(this.mInfo, bounds);
    }

    public boolean isCheckable() {
        return IMPL.isCheckable(this.mInfo);
    }

    public void setCheckable(boolean checkable) {
        IMPL.setCheckable(this.mInfo, checkable);
    }

    public boolean isChecked() {
        return IMPL.isChecked(this.mInfo);
    }

    public void setChecked(boolean checked) {
        IMPL.setChecked(this.mInfo, checked);
    }

    public boolean isFocusable() {
        return IMPL.isFocusable(this.mInfo);
    }

    public void setFocusable(boolean focusable) {
        IMPL.setFocusable(this.mInfo, focusable);
    }

    public boolean isFocused() {
        return IMPL.isFocused(this.mInfo);
    }

    public void setFocused(boolean focused) {
        IMPL.setFocused(this.mInfo, focused);
    }

    public boolean isVisibleToUser() {
        return IMPL.isVisibleToUser(this.mInfo);
    }

    public void setVisibleToUser(boolean visibleToUser) {
        IMPL.setVisibleToUser(this.mInfo, visibleToUser);
    }

    public boolean isAccessibilityFocused() {
        return IMPL.isAccessibilityFocused(this.mInfo);
    }

    public void setAccessibilityFocused(boolean focused) {
        IMPL.setAccessibilityFocused(this.mInfo, focused);
    }

    public boolean isSelected() {
        return IMPL.isSelected(this.mInfo);
    }

    public void setSelected(boolean selected) {
        IMPL.setSelected(this.mInfo, selected);
    }

    public boolean isClickable() {
        return IMPL.isClickable(this.mInfo);
    }

    public void setClickable(boolean clickable) {
        IMPL.setClickable(this.mInfo, clickable);
    }

    public boolean isLongClickable() {
        return IMPL.isLongClickable(this.mInfo);
    }

    public void setLongClickable(boolean longClickable) {
        IMPL.setLongClickable(this.mInfo, longClickable);
    }

    public boolean isEnabled() {
        return IMPL.isEnabled(this.mInfo);
    }

    public void setEnabled(boolean enabled) {
        IMPL.setEnabled(this.mInfo, enabled);
    }

    public boolean isPassword() {
        return IMPL.isPassword(this.mInfo);
    }

    public void setPassword(boolean password) {
        IMPL.setPassword(this.mInfo, password);
    }

    public boolean isScrollable() {
        return IMPL.isScrollable(this.mInfo);
    }

    public void setScrollable(boolean scrollable) {
        IMPL.setScrollable(this.mInfo, scrollable);
    }

    public CharSequence getPackageName() {
        return IMPL.getPackageName(this.mInfo);
    }

    public void setPackageName(CharSequence packageName) {
        IMPL.setPackageName(this.mInfo, packageName);
    }

    public CharSequence getClassName() {
        return IMPL.getClassName(this.mInfo);
    }

    public void setClassName(CharSequence className) {
        IMPL.setClassName(this.mInfo, className);
    }

    public CharSequence getText() {
        return IMPL.getText(this.mInfo);
    }

    public void setText(CharSequence text) {
        IMPL.setText(this.mInfo, text);
    }

    public CharSequence getContentDescription() {
        return IMPL.getContentDescription(this.mInfo);
    }

    public void setContentDescription(CharSequence contentDescription) {
        IMPL.setContentDescription(this.mInfo, contentDescription);
    }

    public void recycle() {
        IMPL.recycle(this.mInfo);
    }

    public void setViewIdResourceName(String viewId) {
        IMPL.setViewIdResourceName(this.mInfo, viewId);
    }

    public String getViewIdResourceName() {
        return IMPL.getViewIdResourceName(this.mInfo);
    }

    public int getLiveRegion() {
        return IMPL.getLiveRegion(this.mInfo);
    }

    public void setLiveRegion(int mode) {
        IMPL.setLiveRegion(this.mInfo, mode);
    }

    public CollectionInfoCompat getCollectionInfo() {
        Object info = IMPL.getCollectionInfo(this.mInfo);
        if (info == null) {
            return null;
        }
        return new CollectionInfoCompat(info);
    }

    public void setCollectionInfo(Object collectionInfo) {
        IMPL.setCollectionInfo(this.mInfo, ((CollectionInfoCompat) collectionInfo).mInfo);
    }

    public void setCollectionItemInfo(Object collectionItemInfo) {
        IMPL.setCollectionItemInfo(this.mInfo, ((CollectionItemInfoCompat) collectionItemInfo).mInfo);
    }

    public CollectionItemInfoCompat getCollectionItemInfo() {
        Object info = IMPL.getCollectionItemInfo(this.mInfo);
        if (info == null) {
            return null;
        }
        return new CollectionItemInfoCompat(info);
    }

    public RangeInfoCompat getRangeInfo() {
        Object info = IMPL.getRangeInfo(this.mInfo);
        if (info == null) {
            return null;
        }
        return new RangeInfoCompat(info);
    }

    public void setRangeInfo(RangeInfoCompat rangeInfo) {
        IMPL.setRangeInfo(this.mInfo, rangeInfo.mInfo);
    }

    public List<AccessibilityActionCompat> getActionList() {
        List<Object> actions = IMPL.getActionList(this.mInfo);
        if (actions == null) {
            return Collections.emptyList();
        }
        List<AccessibilityActionCompat> arrayList = new ArrayList();
        int actionCount = actions.size();
        for (int i = 0; i < actionCount; i++) {
            arrayList.add(new AccessibilityActionCompat(actions.get(i)));
        }
        return arrayList;
    }

    public void setContentInvalid(boolean contentInvalid) {
        IMPL.setContentInvalid(this.mInfo, contentInvalid);
    }

    public boolean isContentInvalid() {
        return IMPL.isContentInvalid(this.mInfo);
    }

    public void setError(CharSequence error) {
        IMPL.setError(this.mInfo, error);
    }

    public CharSequence getError() {
        return IMPL.getError(this.mInfo);
    }

    public void setLabelFor(View labeled) {
        IMPL.setLabelFor(this.mInfo, labeled);
    }

    public void setLabelFor(View root, int virtualDescendantId) {
        IMPL.setLabelFor(this.mInfo, root, virtualDescendantId);
    }

    public AccessibilityNodeInfoCompat getLabelFor() {
        return wrapNonNullInstance(IMPL.getLabelFor(this.mInfo));
    }

    public void setLabeledBy(View label) {
        IMPL.setLabeledBy(this.mInfo, label);
    }

    public void setLabeledBy(View root, int virtualDescendantId) {
        IMPL.setLabeledBy(this.mInfo, root, virtualDescendantId);
    }

    public AccessibilityNodeInfoCompat getLabeledBy() {
        return wrapNonNullInstance(IMPL.getLabeledBy(this.mInfo));
    }

    public boolean canOpenPopup() {
        return IMPL.canOpenPopup(this.mInfo);
    }

    public void setCanOpenPopup(boolean opensPopup) {
        IMPL.setCanOpenPopup(this.mInfo, opensPopup);
    }

    public List<AccessibilityNodeInfoCompat> findAccessibilityNodeInfosByViewId(String viewId) {
        List<Object> nodes = IMPL.findAccessibilityNodeInfosByViewId(this.mInfo, viewId);
        if (nodes == null) {
            return Collections.emptyList();
        }
        List<AccessibilityNodeInfoCompat> arrayList = new ArrayList();
        for (Object node : nodes) {
            arrayList.add(new AccessibilityNodeInfoCompat(node));
        }
        return arrayList;
    }

    public Bundle getExtras() {
        return IMPL.getExtras(this.mInfo);
    }

    public int getInputType() {
        return IMPL.getInputType(this.mInfo);
    }

    public void setInputType(int inputType) {
        IMPL.setInputType(this.mInfo, inputType);
    }

    public void setMaxTextLength(int max) {
        IMPL.setMaxTextLength(this.mInfo, max);
    }

    public int getMaxTextLength() {
        return IMPL.getMaxTextLength(this.mInfo);
    }

    public void setTextSelection(int start, int end) {
        IMPL.setTextSelection(this.mInfo, start, end);
    }

    public int getTextSelectionStart() {
        return IMPL.getTextSelectionStart(this.mInfo);
    }

    public int getTextSelectionEnd() {
        return IMPL.getTextSelectionEnd(this.mInfo);
    }

    public AccessibilityNodeInfoCompat getTraversalBefore() {
        return wrapNonNullInstance(IMPL.getTraversalBefore(this.mInfo));
    }

    public void setTraversalBefore(View view) {
        IMPL.setTraversalBefore(this.mInfo, view);
    }

    public void setTraversalBefore(View root, int virtualDescendantId) {
        IMPL.setTraversalBefore(this.mInfo, root, virtualDescendantId);
    }

    public AccessibilityNodeInfoCompat getTraversalAfter() {
        return wrapNonNullInstance(IMPL.getTraversalAfter(this.mInfo));
    }

    public void setTraversalAfter(View view) {
        IMPL.setTraversalAfter(this.mInfo, view);
    }

    public void setTraversalAfter(View root, int virtualDescendantId) {
        IMPL.setTraversalAfter(this.mInfo, root, virtualDescendantId);
    }

    public AccessibilityWindowInfoCompat getWindow() {
        return AccessibilityWindowInfoCompat.wrapNonNullInstance(IMPL.getWindow(this.mInfo));
    }

    public boolean isDismissable() {
        return IMPL.isDismissable(this.mInfo);
    }

    public void setDismissable(boolean dismissable) {
        IMPL.setDismissable(this.mInfo, dismissable);
    }

    public boolean isEditable() {
        return IMPL.isEditable(this.mInfo);
    }

    public void setEditable(boolean editable) {
        IMPL.setEditable(this.mInfo, editable);
    }

    public boolean isMultiLine() {
        return IMPL.isMultiLine(this.mInfo);
    }

    public void setMultiLine(boolean multiLine) {
        IMPL.setMultiLine(this.mInfo, multiLine);
    }

    public boolean refresh() {
        return IMPL.refresh(this.mInfo);
    }

    public int hashCode() {
        return this.mInfo == null ? 0 : this.mInfo.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AccessibilityNodeInfoCompat other = (AccessibilityNodeInfoCompat) obj;
        if (this.mInfo == null) {
            if (other.mInfo != null) {
                return false;
            }
            return true;
        } else if (this.mInfo.equals(other.mInfo)) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        Rect bounds = new Rect();
        getBoundsInParent(bounds);
        builder.append("; boundsInParent: " + bounds);
        getBoundsInScreen(bounds);
        builder.append("; boundsInScreen: " + bounds);
        builder.append("; packageName: ").append(getPackageName());
        builder.append("; className: ").append(getClassName());
        builder.append("; text: ").append(getText());
        builder.append("; contentDescription: ").append(getContentDescription());
        builder.append("; viewId: ").append(getViewIdResourceName());
        builder.append("; checkable: ").append(isCheckable());
        builder.append("; checked: ").append(isChecked());
        builder.append("; focusable: ").append(isFocusable());
        builder.append("; focused: ").append(isFocused());
        builder.append("; selected: ").append(isSelected());
        builder.append("; clickable: ").append(isClickable());
        builder.append("; longClickable: ").append(isLongClickable());
        builder.append("; enabled: ").append(isEnabled());
        builder.append("; password: ").append(isPassword());
        builder.append("; scrollable: " + isScrollable());
        builder.append("; [");
        int actionBits = getActions();
        while (actionBits != 0) {
            int action = 1 << Integer.numberOfTrailingZeros(actionBits);
            actionBits &= action ^ -1;
            builder.append(getActionSymbolicName(action));
            if (actionBits != 0) {
                builder.append(", ");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private static String getActionSymbolicName(int action) {
        switch (action) {
            case 1:
                return "ACTION_FOCUS";
            case 2:
                return "ACTION_CLEAR_FOCUS";
            case 4:
                return "ACTION_SELECT";
            case 8:
                return "ACTION_CLEAR_SELECTION";
            case 16:
                return "ACTION_CLICK";
            case 32:
                return "ACTION_LONG_CLICK";
            case 64:
                return "ACTION_ACCESSIBILITY_FOCUS";
            case 128:
                return "ACTION_CLEAR_ACCESSIBILITY_FOCUS";
            case 256:
                return "ACTION_NEXT_AT_MOVEMENT_GRANULARITY";
            case 512:
                return "ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY";
            case 1024:
                return "ACTION_NEXT_HTML_ELEMENT";
            case 2048:
                return "ACTION_PREVIOUS_HTML_ELEMENT";
            case 4096:
                return "ACTION_SCROLL_FORWARD";
            case 8192:
                return "ACTION_SCROLL_BACKWARD";
            case 16384:
                return "ACTION_COPY";
            case 32768:
                return "ACTION_PASTE";
            case 65536:
                return "ACTION_CUT";
            case 131072:
                return "ACTION_SET_SELECTION";
            default:
                return "ACTION_UNKNOWN";
        }
    }
}
