package android.support.v4.media;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;

class IMediaBrowserServiceAdapterApi21 {

    static abstract class Stub extends Binder implements IInterface {
        private static final String DESCRIPTOR = "android.service.media.IMediaBrowserService";
        private static final int TRANSACTION_addSubscription = 3;
        private static final int TRANSACTION_connect = 1;
        private static final int TRANSACTION_disconnect = 2;
        private static final int TRANSACTION_getMediaItem = 5;
        private static final int TRANSACTION_removeSubscription = 4;

        public abstract void addSubscription(String str, Object obj);

        public abstract void connect(String str, Bundle bundle, Object obj);

        public abstract void disconnect(Object obj);

        public abstract void getMediaItem(String str, ResultReceiver resultReceiver);

        public abstract void removeSubscription(String str, Object obj);

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String arg0;
            switch (code) {
                case 1:
                    Bundle arg1;
                    data.enforceInterface(DESCRIPTOR);
                    arg0 = data.readString();
                    if (data.readInt() != 0) {
                        arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        arg1 = null;
                    }
                    connect(arg0, arg1, Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect(Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    addSubscription(data.readString(), Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    removeSubscription(data.readString(), Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 5:
                    ResultReceiver arg12;
                    data.enforceInterface(DESCRIPTOR);
                    arg0 = data.readString();
                    if (data.readInt() != 0) {
                        arg12 = (ResultReceiver) ResultReceiver.CREATOR.createFromParcel(data);
                    } else {
                        arg12 = null;
                    }
                    getMediaItem(arg0, arg12);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IMediaBrowserServiceAdapterApi21() {
    }
}
