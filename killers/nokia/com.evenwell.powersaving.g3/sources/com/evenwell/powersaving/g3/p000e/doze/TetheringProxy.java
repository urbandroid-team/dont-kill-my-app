package com.evenwell.powersaving.g3.p000e.doze;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.UserHandle;

/* renamed from: com.evenwell.powersaving.g3.e.doze.TetheringProxy */
public class TetheringProxy {
    private boolean mBound = false;
    private ServiceConnection mConnection = new C03531();
    private Context mContext;
    private TetheringService mService;

    /* renamed from: com.evenwell.powersaving.g3.e.doze.TetheringProxy$1 */
    class C03531 implements ServiceConnection {
        C03531() {
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            TetheringProxy.this.mService = ((TetheringBinder) service).getService();
            TetheringProxy.this.mBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            TetheringProxy.this.mBound = false;
        }
    }

    public TetheringProxy(Context context, Class<? extends TetheringService> cls) {
        this.mContext = context;
        this.mContext.bindServiceAsUser(new Intent(this.mContext, cls), this.mConnection, 1, UserHandle.CURRENT);
    }

    public boolean isTetheringOn() {
        if (this.mBound) {
            return this.mService.isTetheringOn();
        }
        return false;
    }

    public void setTethering(boolean enable) {
        if (this.mBound) {
            this.mService.setTethering(enable);
        }
    }

    public int TetheringSize() {
        if (this.mBound) {
            return this.mService.TetheringSize();
        }
        return 0;
    }

    public void release() {
        try {
            if (this.mBound) {
                this.mContext.unbindService(this.mConnection);
                this.mBound = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.mBound = false;
        }
    }
}
