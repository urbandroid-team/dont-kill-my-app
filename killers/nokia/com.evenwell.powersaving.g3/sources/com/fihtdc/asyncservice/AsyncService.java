package com.fihtdc.asyncservice;

import android.os.Bundle;
import android.os.Messenger;
import com.fihtdc.backuptool.BackupTool;
import java.util.Map;
import java.util.UUID;

public final class AsyncService {
    public static final boolean DEBUG = true;
    private static final String EXCEPTION = "exception";
    private static final String METHOD_NAME = "methodName";
    private static final String PROGRESS = "progress";
    private static final String PROGRESS_INFO = "progressInfo";
    private static final String REPLIER = "replier";
    private static final String REQUEST_ID = "requestId";
    private static final String REQUEST_PARAMS = "requestParams";
    private static final String REQUEST_RESULTS = "requestResults";
    private static final String SERVICE_ACTION = "serviceAction";
    private static final String SERVICE_CLASS = "serviceClass";
    private static final String SERVICE_PACKAGE = "servicePackage";
    private static final String TAG = "BackupRestoreService/AsyncService";

    public interface MessageType {
        public static final int MSG_ADD_REQUEST = 3;
        public static final int MSG_CALLBACK_CANCEL = 6;
        public static final int MSG_CALLBACK_COMPLETE = 4;
        public static final int MSG_CALLBACK_EXCEPTION = 5;
        public static final int MSG_CALLBACK_PROGRESS = 7;
        public static final int MSG_REGISTER_CLIENT = 1;
        public static final int MSG_UNREGISTER_CLIENT = 2;
    }

    public static String generateUuid() {
        String uuid = UUID.randomUUID().toString();
        LogUtils.logD(TAG, "generateUuid() uuid: " + uuid);
        return uuid;
    }

    public static RequestListener getRequestListener(Map<String, RequestListener> requestListenerMap, Bundle task) {
        RequestListener requestListener = null;
        String requestId = getRequestId(task);
        if (requestId != null) {
            requestListener = (RequestListener) requestListenerMap.get(requestId);
        }
        LogUtils.logD(TAG, "getRequestListener() requestListener: " + requestListener);
        LogUtils.logD(TAG, "getRequestListener() requestId: " + requestId);
        return requestListener;
    }

    public static RequestListener removeRequestListener(Map<String, RequestListener> requestListenerMap, Bundle task) {
        LogUtils.logD(TAG, "removeRequestListener() -- task: " + task);
        RequestListener requestListener = null;
        String requestId = getRequestId(task);
        if (requestId != null) {
            requestListener = (RequestListener) requestListenerMap.remove(requestId);
        }
        LogUtils.logD(TAG, "removeRequestListener() requestListener: " + requestListener);
        LogUtils.logD(TAG, "removeRequestListener() requestId: " + requestId);
        return requestListener;
    }

    public static String getRequestId(Bundle task) {
        String value = null;
        if (task.containsKey(REQUEST_ID)) {
            value = task.getString(REQUEST_ID);
        }
        LogUtils.logD(TAG, "getRequestId() -- task: " + task);
        LogUtils.logD(TAG, "getRequestId() -- value: " + value);
        return value;
    }

    public static void putRequestId(Bundle task, String requestId) {
        task.putString(REQUEST_ID, requestId);
    }

    public static String getMethodName(Bundle task) {
        if (task.containsKey(METHOD_NAME)) {
            return task.getString(METHOD_NAME);
        }
        return null;
    }

    public static void putMethodName(Bundle task, String methodName) {
        task.putString(METHOD_NAME, methodName);
    }

    public static String getServiceAction(Bundle task) {
        if (task.containsKey(SERVICE_ACTION)) {
            return task.getString(SERVICE_ACTION);
        }
        return null;
    }

    public static void putServiceAction(Bundle task, String value) {
        task.putString(SERVICE_ACTION, value);
    }

    public static String getServicePackage(Bundle task) {
        if (task.containsKey(SERVICE_PACKAGE)) {
            return task.getString(SERVICE_PACKAGE);
        }
        return null;
    }

    public static void putServicePackage(Bundle task, String value) {
        task.putString(SERVICE_PACKAGE, value);
    }

    public static String getServiceClass(Bundle task) {
        if (task.containsKey(SERVICE_CLASS)) {
            return task.getString(SERVICE_CLASS);
        }
        return null;
    }

    public static void putServiceClass(Bundle task, String value) {
        task.putString(SERVICE_CLASS, value);
    }

    public static Bundle getRequestParams(Bundle task) {
        if (task.containsKey(REQUEST_PARAMS)) {
            return task.getBundle(REQUEST_PARAMS);
        }
        return null;
    }

    public static void putRequestParams(Bundle task, Bundle requestParams) {
        task.putBundle(REQUEST_PARAMS, requestParams);
    }

    public static Bundle getRequestResults(Bundle task) {
        if (task.containsKey(REQUEST_RESULTS)) {
            return task.getBundle(REQUEST_RESULTS);
        }
        return null;
    }

    public static void putRequestResults(Bundle task, Bundle requestResults) {
        task.putBundle(REQUEST_RESULTS, requestResults);
    }

    public static Exception getException(Bundle task) {
        if (task.containsKey(EXCEPTION)) {
            return (Exception) task.getSerializable(EXCEPTION);
        }
        return null;
    }

    public static void putException(Bundle task, Throwable e) {
        task.putSerializable(EXCEPTION, e);
    }

    public static int getProgress(Bundle task) {
        if (task.containsKey("progress")) {
            return task.getInt("progress");
        }
        return -1;
    }

    public static void putProgress(Bundle task, int progress) {
        task.putInt("progress", progress);
    }

    public static Bundle getProgressInfo(Bundle task) {
        if (task.containsKey(PROGRESS_INFO)) {
            return task.getBundle(PROGRESS_INFO);
        }
        return null;
    }

    public static void putProgressInfo(Bundle task, Bundle progressInfo) {
        task.putBundle(PROGRESS_INFO, progressInfo);
    }

    public static Messenger getReplier(Bundle task) {
        if (task.containsKey(REPLIER)) {
            return (Messenger) task.getParcelable(REPLIER);
        }
        return null;
    }

    public static void putReplier(Bundle task, Messenger replier) {
        task.putParcelable(REPLIER, replier);
    }

    public static Messenger removeReplier(Bundle task) {
        Messenger replier = getReplier(task);
        task.remove(REPLIER);
        return replier;
    }

    public static void putVersionCode(Bundle task) {
        task.putString(BackupTool.VERSION_CODE, BackupTool.LIB_VERSION);
    }

    private AsyncService() {
    }
}
