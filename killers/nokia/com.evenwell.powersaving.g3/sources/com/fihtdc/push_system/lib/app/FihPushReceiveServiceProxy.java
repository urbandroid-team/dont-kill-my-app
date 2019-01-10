package com.fihtdc.push_system.lib.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.fihtdc.push_system.lib.app.IFihPushReceiveService.Stub;
import com.fihtdc.push_system.lib.utils.ServiceProxy;
import com.fihtdc.push_system.lib.utils.ServiceProxy.ProxyTask;

public class FihPushReceiveServiceProxy extends ServiceProxy implements IFihPushReceiveService {
    private static final boolean DEBUG = false;
    private static final String TAG = "FP819ServiceProxy";
    private static final int TIMEOUT_DEBUG = 5;
    private Object mReturn = null;
    private IFihPushReceiveService mService;
    private boolean run = false;
    private int timeout = 30;

    /* renamed from: com.fihtdc.push_system.lib.app.FihPushReceiveServiceProxy$1 */
    class C01041 implements ProxyTask {
        C01041() {
        }

        public void run() throws Exception {
            FihPushReceiveServiceProxy.this.mReturn = FihPushReceiveServiceProxy.this.mService.getPushInfos();
            FihPushReceiveServiceProxy.this.run = true;
        }
    }

    /* renamed from: com.fihtdc.push_system.lib.app.FihPushReceiveServiceProxy$2 */
    class C01052 implements ProxyTask {
        C01052() {
        }

        public void run() throws Exception {
            FihPushReceiveServiceProxy.this.mReturn = FihPushReceiveServiceProxy.this.mService.getApplicationInfo();
            FihPushReceiveServiceProxy.this.run = true;
        }
    }

    public FihPushReceiveServiceProxy(Context context, Intent intent) {
        super(context, intent);
        setTimeout(this.timeout);
    }

    protected void onConnected(IBinder binder) {
        this.mService = Stub.asInterface(binder);
    }

    public IBinder asBinder() {
        return null;
    }

    public Bundle getPushInfos() throws RemoteException {
        setTask(new C01041(), "getPushInfos");
        waitForCompletion();
        if (!this.run) {
            throw new RemoteException("Cannot execute getPushInfos()");
        } else if (this.mReturn != null || !this.run) {
            return (Bundle) this.mReturn;
        } else {
            Log.v(TAG, "getPushInfos() return null");
            return null;
        }
    }

    public Bundle getApplicationInfo() throws RemoteException {
        setTask(new C01052(), "getApplicationInfo");
        waitForCompletion();
        if (!this.run) {
            throw new RemoteException("Cannot execute getApplicationInfo()");
        } else if (this.mReturn != null || !this.run) {
            return (Bundle) this.mReturn;
        } else {
            Log.e(TAG, "getApplicationInfo() fail to get result, return null");
            return null;
        }
    }

    public void newPushMessage(final Bundle datas) throws RemoteException {
        setTask(new ProxyTask() {
            public void run() throws Exception {
                FihPushReceiveServiceProxy.this.mService.newPushMessage(datas);
            }
        }, "newPushMessage");
        waitForCompletion();
    }
}
