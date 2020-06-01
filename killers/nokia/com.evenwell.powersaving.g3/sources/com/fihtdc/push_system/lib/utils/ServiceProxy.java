package com.fihtdc.push_system.lib.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.ConditionVariable;
import android.os.Debug;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

public abstract class ServiceProxy {
    private static final boolean DEBUG_PROXY = false;
    private ConditionVariable mCondition;
    private final ServiceConnection mConnection = new ProxyConnection();
    private final Context mContext;
    private boolean mDead = false;
    protected final Intent mIntent;
    private String mName = " unnamed";
    private Runnable mRunnable = new ProxyRunnable();
    private long mStartTime;
    private final String mTag;
    private ProxyTask mTask;
    private int mTimeout = 600;

    public interface ProxyTask {
        void run() throws Exception;
    }

    /* renamed from: com.fihtdc.push_system.lib.utils.ServiceProxy$1 */
    class C01121 implements ProxyTask {
        C01121() {
        }

        public void run() throws RemoteException {
        }
    }

    private class ProxyConnection implements ServiceConnection {

        /* renamed from: com.fihtdc.push_system.lib.utils.ServiceProxy$ProxyConnection$1 */
        class C01131 implements Runnable {
            C01131() {
            }

            public void run() {
                ServiceProxy.this.runTask();
            }
        }

        private ProxyConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder binder) {
            ServiceProxy.this.onConnected(binder);
            new Thread(new C01131()).start();
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private class ProxyRunnable implements Runnable {
        private ProxyRunnable() {
        }

        public void run() {
            try {
                ServiceProxy.this.mTask.run();
            } catch (Exception e) {
                e.printStackTrace();
                ServiceProxy.this.endTask();
            }
        }
    }

    protected abstract void onConnected(IBinder iBinder);

    public ServiceProxy(Context _context, Intent _intent) {
        this.mContext = _context;
        this.mIntent = _intent;
        this.mTag = getClass().getSimpleName();
        if (Debug.isDebuggerConnected()) {
            this.mTimeout <<= 2;
        }
    }

    public ServiceProxy setTimeout(int secs) {
        this.mTimeout = secs;
        return this;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public void endTask() {
        try {
            this.mContext.getApplicationContext().unbindService(this.mConnection);
        } catch (IllegalArgumentException e) {
        }
        this.mDead = true;
        this.mCondition.open();
    }

    private void runTask() {
        Thread thread = new Thread(this.mRunnable);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
        endTask();
    }

    public boolean setTask(ProxyTask task, String name) throws IllegalStateException, RemoteException {
        this.mName = name;
        this.mCondition = new ConditionVariable(false);
        return setTask(task);
    }

    public boolean setTask(ProxyTask task, String name, int timeOut) throws IllegalStateException, RemoteException {
        this.mName = name;
        this.mCondition = new ConditionVariable(false);
        this.mTimeout = timeOut;
        return setTask(task);
    }

    public boolean setTask(ProxyTask task) throws IllegalStateException, RemoteException {
        if (this.mDead) {
            throw new IllegalStateException();
        } else if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("This cannot be called on the main thread.");
        } else {
            this.mTask = task;
            this.mStartTime = System.currentTimeMillis();
            if (SystemAppUtil.bindService(this.mContext.getApplicationContext(), this.mIntent, this.mConnection)) {
                return true;
            }
            throw new RemoteException();
        }
    }

    public void waitForCompletion() {
        long time = System.currentTimeMillis();
        this.mCondition.block(((long) this.mTimeout) * 1000);
        if (System.currentTimeMillis() > (((long) this.mTimeout) * 1000) + time) {
            Log.i(this.mTag, "Wait for " + this.mName + " timeout for " + this.mTimeout + " seconds");
        }
    }

    public void close() throws RemoteException {
        if (this.mDead) {
            throw new RemoteException();
        }
        endTask();
    }

    public boolean test() {
        try {
            return setTask(new C01121(), "test");
        } catch (Exception e) {
            return false;
        }
    }
}
