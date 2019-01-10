package com.fihtdc.asyncservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class RequestHandler implements Callback {
    private static final boolean DEBUG = true;
    public static final int STATUS_FAILED = 0;
    public static final int STATUS_SUCCESS = 2;
    public static final int STATUS_WAIT = 1;
    private static final String TAG = "BackupRestoreService/RequestHandler";
    private String mClassName;
    private HashMap<Integer, RequestServiceConnection> mConnections;
    private Context mContext;
    protected Handler mHandler;
    private String mPackageName;
    private Messenger mReplier;
    protected Map<String, RequestListener> mRequestListenerMap;
    private Object mRequestLock;
    private String mServiceAction;
    private ServiceConnectionCallback mServiceConnectionCallback;

    class RequestServiceConnection implements ServiceConnection {
        private boolean mBound = false;
        private boolean mConnectdStatus = false;
        private Messenger mService;
        private ArrayList<Object> mTasks = new ArrayList();

        public RequestServiceConnection(Object task) {
            this.mTasks.add(task);
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.logD(RequestHandler.TAG, "onServiceConnected() name: " + name);
            this.mBound = true;
            this.mService = new Messenger(service);
            Iterator it = this.mTasks.iterator();
            while (it.hasNext()) {
                Object task = it.next();
                if (task != null) {
                    RequestHandler.this.request(task);
                }
            }
            this.mTasks.clear();
            if (RequestHandler.this.mServiceConnectionCallback != null) {
                RequestHandler.this.mServiceConnectionCallback.onServiceConnected(name, service);
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            LogUtils.logD(RequestHandler.TAG, "onServiceDisconnected() name: ");
            this.mBound = false;
            this.mService = null;
            for (Entry<Integer, RequestServiceConnection> conn : RequestHandler.this.mConnections.entrySet()) {
                if (equals(conn.getValue())) {
                    RequestHandler.this.mConnections.remove(conn.getKey());
                }
            }
            if (RequestHandler.this.mServiceConnectionCallback != null) {
                RequestHandler.this.mServiceConnectionCallback.onServiceDisconnected(name);
            }
        }
    }

    public RequestHandler(Context context, String serviceAction) {
        this(context, serviceAction, null, null);
    }

    public RequestHandler(Context context, String serviceAction, String packageName) {
        this(context, serviceAction, packageName, null);
    }

    public RequestHandler(Context context, String serviceAction, String packageName, String className) {
        this.mConnections = new HashMap();
        this.mRequestLock = new Object();
        this.mRequestListenerMap = new HashMap();
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        } else if (serviceAction == null) {
            throw new IllegalArgumentException("Service action cannot be null");
        } else {
            this.mContext = context;
            this.mServiceAction = serviceAction;
            this.mPackageName = packageName;
            this.mClassName = className;
            this.mHandler = new Handler(this);
            this.mReplier = new Messenger(this.mHandler);
        }
    }

    public boolean handleMessage(Message msg) {
        LogUtils.logD(TAG, "handle message -- the current type is " + msg.what);
        LogUtils.logD("BackupRestoreService/RequestHandler131", "--->Start mHandlerTask: ThreadID is " + Thread.currentThread().getId());
        if (!(msg.obj instanceof Bundle)) {
            if (msg.obj instanceof RequestTask) {
                LogUtils.logD(TAG, "handle message -- msg.obj instanceof RequestTask");
                RequestTask task = msg.obj;
                LogUtils.logD(TAG, "handle message -- task.getRequestListener(): " + task.getRequestListener());
                if (task.getRequestListener() != null) {
                    switch (msg.what) {
                        case 4:
                            task.getRequestListener().onComplete(task);
                            break;
                        case 5:
                            task.getRequestListener().onException(task, task.getException());
                            break;
                        case 7:
                            if (task.getProgressInfo() != null) {
                                task.getRequestListener().updateProgress(task.getProgress(), task.getProgressInfo());
                                break;
                            }
                            task.getRequestListener().updateProgress(task.getProgress());
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        RequestListener requestListener;
        LogUtils.logD(TAG, "handle message -- msg.obj instanceof Bundle");
        Bundle task2 = msg.obj;
        LogUtils.logD(TAG, "handle message -- task: " + task2);
        if (7 == msg.what) {
            requestListener = AsyncService.getRequestListener(this.mRequestListenerMap, task2);
        } else {
            requestListener = AsyncService.removeRequestListener(this.mRequestListenerMap, task2);
        }
        LogUtils.logD(TAG, "handle message -- requestListener: " + requestListener);
        if (requestListener != null) {
            if (AsyncService.getRequestResults(task2) != null) {
                task2.putAll(AsyncService.getRequestResults(task2));
            }
            switch (msg.what) {
                case 4:
                    requestListener.onComplete(task2);
                    break;
                case 5:
                    requestListener.onException(task2, AsyncService.getException(task2));
                    break;
                case 7:
                    LogUtils.logD("BackupRestoreService/RequestHandler164", "--->Start mHandlerTask: ThreadID is " + Thread.currentThread().getId());
                    Bundle progressInfo = AsyncService.getProgressInfo(task2);
                    if (progressInfo != null) {
                        requestListener.updateProgress(AsyncService.getProgress(task2), progressInfo);
                        break;
                    }
                    requestListener.updateProgress(AsyncService.getProgress(task2));
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    public void startRequest(RequestTask task) {
        LogUtils.logD(TAG, "startRequest(1) -- task: " + task);
        if (task != null) {
            synchronized (this.mRequestLock) {
                if (this.mConnections.get(Integer.valueOf(task.getServiceId())) == null) {
                    Log.d(TAG, "-- " + (bindService(task) ? "Connected " : "Cannot connect ") + "service: (" + this.mServiceAction + "|" + this.mPackageName + "|" + this.mClassName + ")");
                } else if (((RequestServiceConnection) this.mConnections.get(Integer.valueOf(task.getServiceId()))).mBound) {
                    request(task);
                } else {
                    waiting(task);
                }
            }
        }
    }

    protected boolean request(RequestTask task) {
        LogUtils.logD(TAG, "request(1) -- task: " + task);
        if (task.getRequestListener() != null) {
            LogUtils.logD(TAG, "request -- task.getRequestListener() is not null ");
            task.getRequestListener().onStart(task);
        }
        return sendRequest(task);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int startRequest(android.os.Bundle r11, com.fihtdc.asyncservice.RequestListener r12) {
        /*
        r10 = this;
        r5 = 2;
        r3 = 0;
        r4 = 1;
        r6 = "BackupRestoreService/RequestHandler";
        r7 = new java.lang.StringBuilder;
        r7.<init>();
        r8 = "startRequest(2) -- task: ";
        r7 = r7.append(r8);
        r7 = r7.append(r11);
        r7 = r7.toString();
        com.fihtdc.asyncservice.LogUtils.logD(r6, r7);
        if (r11 != 0) goto L_0x001e;
    L_0x001d:
        return r3;
    L_0x001e:
        r6 = r10.mRequestLock;
        monitor-enter(r6);
        r7 = r10.mConnections;	 Catch:{ all -> 0x00c5 }
        r8 = 1;
        r8 = java.lang.Integer.valueOf(r8);	 Catch:{ all -> 0x00c5 }
        r0 = r7.get(r8);	 Catch:{ all -> 0x00c5 }
        r0 = (com.fihtdc.asyncservice.RequestHandler.RequestServiceConnection) r0;	 Catch:{ all -> 0x00c5 }
        if (r12 == 0) goto L_0x00eb;
    L_0x0030:
        r5 = "BackupRestoreService/RequestHandler";
        r7 = "startRequest(2) -- requestListener is not null";
        com.fihtdc.asyncservice.LogUtils.logD(r5, r7);	 Catch:{ all -> 0x00c5 }
        r5 = "BackupRestoreService/RequestHandler249";
        r7 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00c5 }
        r7.<init>();	 Catch:{ all -> 0x00c5 }
        r8 = "--->Start mHandlerTask: ThreadID is ";
        r7 = r7.append(r8);	 Catch:{ all -> 0x00c5 }
        r8 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x00c5 }
        r8 = r8.getId();	 Catch:{ all -> 0x00c5 }
        r7 = r7.append(r8);	 Catch:{ all -> 0x00c5 }
        r7 = r7.toString();	 Catch:{ all -> 0x00c5 }
        com.fihtdc.asyncservice.LogUtils.logD(r5, r7);	 Catch:{ all -> 0x00c5 }
        r2 = com.fihtdc.asyncservice.AsyncService.generateUuid();	 Catch:{ all -> 0x00c5 }
        com.fihtdc.asyncservice.AsyncService.putRequestId(r11, r2);	 Catch:{ all -> 0x00c5 }
        r5 = r10.mRequestListenerMap;	 Catch:{ all -> 0x00c5 }
        r5.put(r2, r12);	 Catch:{ all -> 0x00c5 }
        if (r0 != 0) goto L_0x00cb;
    L_0x0065:
        r1 = r10.bindService(r11);	 Catch:{ all -> 0x00c5 }
        r5 = r10.mConnections;	 Catch:{ all -> 0x00c5 }
        r7 = 1;
        r7 = java.lang.Integer.valueOf(r7);	 Catch:{ all -> 0x00c5 }
        r0 = r5.get(r7);	 Catch:{ all -> 0x00c5 }
        r0 = (com.fihtdc.asyncservice.RequestHandler.RequestServiceConnection) r0;	 Catch:{ all -> 0x00c5 }
        r7 = "BackupRestoreService/RequestHandler";
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00c5 }
        r5.<init>();	 Catch:{ all -> 0x00c5 }
        r8 = "-- ";
        r8 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        if (r1 == 0) goto L_0x00c8;
    L_0x0085:
        r5 = "Connected ";
    L_0x0087:
        r5 = r8.append(r5);	 Catch:{ all -> 0x00c5 }
        r8 = "service: (";
        r5 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        r8 = r10.mServiceAction;	 Catch:{ all -> 0x00c5 }
        r5 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        r8 = "|";
        r5 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        r8 = r10.mPackageName;	 Catch:{ all -> 0x00c5 }
        r5 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        r8 = "|";
        r5 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        r8 = r10.mClassName;	 Catch:{ all -> 0x00c5 }
        r5 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        r8 = ")";
        r5 = r5.append(r8);	 Catch:{ all -> 0x00c5 }
        r5 = r5.toString();	 Catch:{ all -> 0x00c5 }
        android.util.Log.d(r7, r5);	 Catch:{ all -> 0x00c5 }
        r0.mConnectdStatus = r1;	 Catch:{ all -> 0x00c5 }
        if (r1 == 0) goto L_0x00c2;
    L_0x00c1:
        r3 = r4;
    L_0x00c2:
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        goto L_0x001d;
    L_0x00c5:
        r3 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        throw r3;
    L_0x00c8:
        r5 = "Cannot connect ";
        goto L_0x0087;
    L_0x00cb:
        r5 = r0.mBound;	 Catch:{ all -> 0x00c5 }
        if (r5 != 0) goto L_0x00e1;
    L_0x00d1:
        r5 = r0.mConnectdStatus;	 Catch:{ all -> 0x00c5 }
        if (r5 != 0) goto L_0x00da;
    L_0x00d7:
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        goto L_0x001d;
    L_0x00da:
        r10.waiting(r11);	 Catch:{ all -> 0x00c5 }
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        r3 = r4;
        goto L_0x001d;
    L_0x00e1:
        r5 = r10.request(r11);	 Catch:{ all -> 0x00c5 }
        if (r5 == 0) goto L_0x00e8;
    L_0x00e7:
        r3 = r4;
    L_0x00e8:
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        goto L_0x001d;
    L_0x00eb:
        r3 = "BackupRestoreService/RequestHandler";
        r7 = "startRequest(2) -- requestListener is null, it is cancel operation";
        com.fihtdc.asyncservice.LogUtils.logD(r3, r7);	 Catch:{ all -> 0x00c5 }
        r2 = com.fihtdc.asyncservice.AsyncService.generateUuid();	 Catch:{ all -> 0x00c5 }
        com.fihtdc.asyncservice.AsyncService.putRequestId(r11, r2);	 Catch:{ all -> 0x00c5 }
        r3 = r10.mRequestListenerMap;	 Catch:{ all -> 0x00c5 }
        r3.put(r2, r12);	 Catch:{ all -> 0x00c5 }
        if (r0 != 0) goto L_0x0104;
    L_0x0100:
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        r3 = r5;
        goto L_0x001d;
    L_0x0104:
        r3 = r0.mBound;	 Catch:{ all -> 0x00c5 }
        if (r3 != 0) goto L_0x0115;
    L_0x010a:
        r3 = r0.mTasks;	 Catch:{ all -> 0x00c5 }
        r3.clear();	 Catch:{ all -> 0x00c5 }
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        r3 = r5;
        goto L_0x001d;
    L_0x0115:
        r3 = r10.request(r11);	 Catch:{ all -> 0x00c5 }
        if (r3 == 0) goto L_0x011f;
    L_0x011b:
        r3 = r4;
    L_0x011c:
        monitor-exit(r6);	 Catch:{ all -> 0x00c5 }
        goto L_0x001d;
    L_0x011f:
        r3 = r5;
        goto L_0x011c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fihtdc.asyncservice.RequestHandler.startRequest(android.os.Bundle, com.fihtdc.asyncservice.RequestListener):int");
    }

    protected boolean request(Bundle task) {
        LogUtils.logD(TAG, "request(2) -- task: " + task);
        RequestListener requestListener = AsyncService.getRequestListener(this.mRequestListenerMap, task);
        if (requestListener != null) {
            requestListener.onStart(task);
        }
        AsyncService.putRequestParams(task, new Bundle(task));
        return sendRequest(task);
    }

    protected boolean request(Object task) {
        boolean ret = false;
        LogUtils.logD(TAG, "request(3) -- task: " + task);
        if (task instanceof RequestTask) {
            ret = request((RequestTask) task);
        } else if (task instanceof Bundle) {
            ret = request((Bundle) task);
        } else {
            Log.e(TAG, "Not supported task received.");
        }
        LogUtils.logD(TAG, "request(3) -- return: " + ret);
        return ret;
    }

    private void waiting(Object task) {
        LogUtils.logD(TAG, "waiting() -- task: " + task);
        RequestServiceConnection con = (RequestServiceConnection) this.mConnections.get(Integer.valueOf(getServiceId(task)));
        if (con != null) {
            con.mTasks.add(task);
        } else {
            Log.e(TAG, "Bad service id:" + getServiceId(task));
        }
    }

    private int getServiceId(Object task) {
        if (task instanceof RequestTask) {
            LogUtils.logD(TAG, "getServiceId() -- task instanceof RequestTask: " + task);
            return ((RequestTask) task).getServiceId();
        } else if (task instanceof Bundle) {
            LogUtils.logD(TAG, "getServiceId() -- task instanceof Bundle: " + task);
            return 1;
        } else {
            LogUtils.logW(TAG, "getServiceId() -- task is not right" + task);
            return -1;
        }
    }

    private boolean sendRequest(Object task) {
        LogUtils.logW(TAG, "sendRequest() -- task:" + task);
        Message msg = Message.obtain(null, 3, task);
        msg.replyTo = this.mReplier;
        try {
            RequestServiceConnection con = (RequestServiceConnection) this.mConnections.get(Integer.valueOf(getServiceId(task)));
            if (!(con == null || con.mService == null)) {
                LogUtils.logW(TAG, "sendRequest() send message");
                con.mService.send(msg);
                return true;
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Send request to http request service error", e);
        }
        return false;
    }

    public boolean bindService() {
        LogUtils.logW(TAG, "bindService(1)");
        return bindService(null);
    }

    private boolean bindService(Object task) {
        LogUtils.logW(TAG, "bindService(2)");
        Intent intent = this.mServiceAction == null ? new Intent() : new Intent(this.mServiceAction);
        if (this.mPackageName == null || this.mPackageName.equals(this.mContext.getPackageName())) {
            LogUtils.logW(TAG, "bindService(2) package name is null or doesn't equal mContext.getPackageName()");
            if (this.mClassName == null) {
                LogUtils.logW(TAG, "bindService(2) mClassName is not null");
                intent.setPackage(this.mContext.getPackageName());
            } else {
                intent.setClassName(this.mContext.getPackageName(), this.mClassName);
            }
        } else {
            LogUtils.logW(TAG, "bindService(2) package name is not null and equals mContext.getPackageName()");
            if (this.mClassName == null) {
                LogUtils.logW(TAG, "bindService(2) mClassName is not null");
                intent.setPackage(this.mPackageName);
            } else {
                intent.setClassName(this.mPackageName, this.mClassName);
            }
        }
        RequestServiceConnection conn = new RequestServiceConnection(task);
        this.mConnections.put(Integer.valueOf(getServiceId(task)), conn);
        return this.mContext.bindService(intent, conn, 1);
    }

    public void finish() {
        LogUtils.logW(TAG, "finish()");
        unbindService();
    }

    private void unbindService() {
        LogUtils.logW(TAG, "unbindService()");
        synchronized (this.mRequestLock) {
            for (Entry<Integer, RequestServiceConnection> conn : this.mConnections.entrySet()) {
                if (((RequestServiceConnection) conn.getValue()).mBound) {
                    try {
                        this.mContext.unbindService((ServiceConnection) conn.getValue());
                    } catch (Exception e) {
                        Log.e(TAG, "RequestHelper is unbindService exception is " + e);
                    }
                }
            }
            this.mConnections.clear();
        }
    }

    public void setServiceConnectionCallback(ServiceConnectionCallback serviceConnectionCallback) {
        LogUtils.logW(TAG, "setServiceConnectionCallback()");
        this.mServiceConnectionCallback = serviceConnectionCallback;
    }
}
