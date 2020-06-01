package com.fihtdc.push_system.lib.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.fihtdc.push_system.lib.app.IFihPushReceiveService.Stub;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import com.fihtdc.push_system.lib.common.PushProp;
import com.fihtdc.push_system.lib.utils.PushMessageUtil;

public abstract class FihPushReceiveService extends Service {
    private final Stub mBinder = new C01031();

    /* renamed from: com.fihtdc.push_system.lib.app.FihPushReceiveService$1 */
    class C01031 extends Stub {
        C01031() {
        }

        public Bundle getPushInfos() throws RemoteException {
            return FihPushReceiveService.this.getPushInfos();
        }

        public void newPushMessage(Bundle datas) throws RemoteException {
            if (!FihPushReceiveService.this.newPushMessage(datas) && PushMessageContract.TYPE_MESSAGE.equals(datas.getString(PushMessageContract.MESSAGE_KEY_TYPE))) {
                PushMessageUtil.showNotification(FihPushReceiveService.this.getApplicationContext(), datas, FihPushReceiveService.this.getDefaultNotificationSmallIcon(), FihPushReceiveService.this.getDefaultNotificationBigIcon());
            }
        }

        public Bundle getApplicationInfo() throws RemoteException {
            Bundle info = new Bundle();
            try {
                info.putString(PushProp.KEY_APP_INFO_ACCESS_ID, FihPushReceiveService.this.getAccessId());
                info.putString(PushProp.KEY_APP_INFO_ACCESS_KEY, FihPushReceiveService.this.getAccessKey());
                info.putString(PushProp.KEY_APP_INFO_SECRET_kEY, FihPushReceiveService.this.getSecretKey());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return info;
        }
    }

    public abstract String getAccessId();

    public abstract String getAccessKey();

    public abstract int getDefaultNotificationSmallIcon();

    public abstract Bundle getPushInfos();

    public abstract String getSecretKey();

    public abstract boolean newPushMessage(Bundle bundle);

    public Bitmap getDefaultNotificationBigIcon() {
        return null;
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }
}
