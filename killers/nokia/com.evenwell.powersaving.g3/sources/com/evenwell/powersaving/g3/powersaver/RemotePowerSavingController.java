package com.evenwell.powersaving.g3.powersaver;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.powersaver.IPowerSavingController.Stub;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.IStateChangeListener;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.LATEST_EVENT_EXTRA;
import com.evenwell.powersaving.g3.powersaver.PowerSavingController.ServiceBinder;

public class RemotePowerSavingController extends Service {
    private String TAG = getClass().getSimpleName();
    private ServiceConnection conn = new C03941();
    private Intent intent;
    private IStateChangeListener listener = new C03973();
    private boolean mBound = false;
    private RemoteCallbackList<IStatusChangeListener> mCallbacks = new RemoteCallbackList();
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private ServiceBinder mPowerSavingControllerbinder;
    public final Stub remoteBinder = new C03952();

    /* renamed from: com.evenwell.powersaving.g3.powersaver.RemotePowerSavingController$1 */
    class C03941 implements ServiceConnection {
        C03941() {
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(RemotePowerSavingController.this.TAG, "onServiceDisconnected");
            RemotePowerSavingController.this.mPowerSavingControllerbinder.getService().unregisterStateChangeListener(RemotePowerSavingController.this.listener);
            RemotePowerSavingController.this.mBound = false;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(RemotePowerSavingController.this.TAG, "onServiceConnected");
            RemotePowerSavingController.this.mPowerSavingControllerbinder = (ServiceBinder) service;
            RemotePowerSavingController.this.mPowerSavingControllerbinder.getService().registerStateChangeListener(RemotePowerSavingController.this.listener);
            RemotePowerSavingController.this.mBound = true;
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.powersaver.RemotePowerSavingController$2 */
    class C03952 extends Stub {
        C03952() {
        }

        public void applyInAactiveMode(String reason) throws RemoteException {
            if (RemotePowerSavingController.this.getService() != null) {
                RemotePowerSavingController.this.getService().updateEventStatusForQS();
                RemotePowerSavingController.this.getService().applyInAactiveMode(reason);
            }
        }

        public void applyExtremeMode(String reason) throws RemoteException {
            if (RemotePowerSavingController.this.getService() != null && !PowerSavingUtils.isCharging(RemotePowerSavingController.this)) {
                RemotePowerSavingController.this.getService().updateEventStatusForQS();
                RemotePowerSavingController.this.getService().updateApplyEventStatusForOtherUI(LATEST_EVENT_EXTRA.MANUAL);
                RemotePowerSavingController.this.getService().applyExtremeMode(reason);
            }
        }

        public int getCurentMode() throws RemoteException {
            if (RemotePowerSavingController.this.getService() != null) {
                return RemotePowerSavingController.this.getService().getCurentMode();
            }
            return -999;
        }

        public void registerStateChangeListener(IStatusChangeListener listener) throws RemoteException {
            synchronized (this) {
                RemotePowerSavingController.this.mCallbacks.register(listener);
            }
        }

        public void unregisterStateChangeListener(IStatusChangeListener listener) throws RemoteException {
            synchronized (this) {
                RemotePowerSavingController.this.mCallbacks.unregister(listener);
            }
        }
    }

    /* renamed from: com.evenwell.powersaving.g3.powersaver.RemotePowerSavingController$3 */
    class C03973 implements IStateChangeListener {
        C03973() {
        }

        public void onChange(final int status) {
            Log.d(RemotePowerSavingController.this.TAG, "onChange status : " + status);
            RemotePowerSavingController.this.mHandler.post(new Thread() {
                public void run() {
                    super.run();
                    RemotePowerSavingController.this.notifyAll(status);
                }
            });
        }
    }

    private void notifyAll(int status) {
        synchronized (this) {
            int len = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < len; i++) {
                try {
                    ((IStatusChangeListener) this.mCallbacks.getBroadcastItem(i)).onChange(status);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            this.mCallbacks.finishBroadcast();
        }
    }

    public void onCreate() {
        super.onCreate();
        Log.d(this.TAG, "oncreate");
        this.mHandlerThread = new HandlerThread("RemotePowerSavingController");
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());
        this.intent = new Intent(this, PowerSavingController.class);
    }

    public IBinder onBind(Intent intent) {
        if (!this.mBound) {
            bindPowerSavingController();
        }
        return this.remoteBinder;
    }

    private PowerSavingController getService() {
        if (this.mBound) {
            return this.mPowerSavingControllerbinder.getService();
        }
        return null;
    }

    private void bindPowerSavingController() {
        bindService(this.intent, this.conn, 1);
    }

    public void onDestroy() {
        Log.d(this.TAG, "onDestory");
        this.mHandlerThread.getLooper().quitSafely();
        this.mCallbacks.kill();
        this.mPowerSavingControllerbinder.getService().unregisterStateChangeListener(this.listener);
        unbindService(this.conn);
        super.onDestroy();
    }

    public boolean onUnbind(Intent intent) {
        Log.d(this.TAG, "onUnbind");
        return super.onUnbind(intent);
    }
}
