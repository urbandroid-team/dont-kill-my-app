package com.fihtdc.push_system.lib;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.fihtdc.push_system.lib.IFihPushService.Stub;
import com.fihtdc.push_system.lib.utils.ServiceProxy;
import com.fihtdc.push_system.lib.utils.ServiceProxy.ProxyTask;

public class FihPushServiceProxy extends ServiceProxy implements IFihPushService {
    private static final boolean DEBUG = false;
    private static final String TAG = "FP819ServiceProxy";
    private Object mReturn = null;
    private IFihPushService mService;

    /* renamed from: com.fihtdc.push_system.lib.FihPushServiceProxy$1 */
    class C00981 implements ProxyTask {
        C00981() {
        }

        public void run() throws Exception {
            FihPushServiceProxy.this.mService.startPushService();
        }
    }

    /* renamed from: com.fihtdc.push_system.lib.FihPushServiceProxy$2 */
    class C00992 implements ProxyTask {
        C00992() {
        }

        public void run() throws Exception {
            FihPushServiceProxy.this.mService.stopPush();
        }
    }

    /* renamed from: com.fihtdc.push_system.lib.FihPushServiceProxy$3 */
    class C01003 implements ProxyTask {
        C01003() {
        }

        public void run() throws Exception {
            FihPushServiceProxy.this.mReturn = Boolean.valueOf(FihPushServiceProxy.this.mService.isPushConnected());
        }
    }

    /* renamed from: com.fihtdc.push_system.lib.FihPushServiceProxy$4 */
    class C01014 implements ProxyTask {
        C01014() {
        }

        public void run() throws Exception {
            FihPushServiceProxy.this.mService.shutdown();
        }
    }

    /* renamed from: com.fihtdc.push_system.lib.FihPushServiceProxy$5 */
    class C01025 implements ProxyTask {
        C01025() {
        }

        public void run() throws Exception {
            FihPushServiceProxy.this.mService.disconnect();
        }
    }

    public FihPushServiceProxy(Context context, Intent intent) {
        super(context, intent);
    }

    protected void onConnected(IBinder binder) {
        this.mService = Stub.asInterface(binder);
    }

    public IBinder asBinder() {
        return null;
    }

    public void startPushService() throws RemoteException {
        setTask(new C00981(), "startPushService");
    }

    public void stopPush() throws RemoteException {
        setTask(new C00992(), "stopPush");
        waitForCompletion();
    }

    public boolean isPushConnected() throws RemoteException {
        setTask(new C01003(), "isPushConnected");
        waitForCompletion();
        if (this.mReturn != null) {
            return ((Boolean) this.mReturn).booleanValue();
        }
        Log.e(TAG, "isPushConnected() fail to get result, return false");
        return false;
    }

    public void shutdown() throws RemoteException {
        setTask(new C01014(), "shutdown");
        waitForCompletion();
    }

    public void disconnect() throws RemoteException {
        setTask(new C01025(), "disconnect");
        waitForCompletion();
    }
}
