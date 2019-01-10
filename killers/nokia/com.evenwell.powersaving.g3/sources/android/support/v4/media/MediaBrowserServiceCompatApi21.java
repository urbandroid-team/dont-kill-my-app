package android.support.v4.media;

import android.content.Intent;
import android.media.MediaDescription.Builder;
import android.media.browse.MediaBrowser.MediaItem;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import java.util.ArrayList;
import java.util.List;

class MediaBrowserServiceCompatApi21 {

    public interface ServiceImplApi21 {
        void addSubscription(String str, ServiceCallbacks serviceCallbacks);

        void connect(String str, Bundle bundle, ServiceCallbacks serviceCallbacks);

        void disconnect(ServiceCallbacks serviceCallbacks);

        void removeSubscription(String str, ServiceCallbacks serviceCallbacks);
    }

    static class MediaBrowserServiceAdaptorApi21 {
        ServiceBinderProxyApi21 mBinder;

        static class ServiceBinderProxyApi21 extends Stub {
            final ServiceImplApi21 mServiceImpl;

            ServiceBinderProxyApi21(ServiceImplApi21 serviceImpl) {
                this.mServiceImpl = serviceImpl;
            }

            public void connect(String pkg, Bundle rootHints, Object callbacks) {
                this.mServiceImpl.connect(pkg, rootHints, new ServiceCallbacksApi21(callbacks));
            }

            public void disconnect(Object callbacks) {
                this.mServiceImpl.disconnect(new ServiceCallbacksApi21(callbacks));
            }

            public void addSubscription(String id, Object callbacks) {
                this.mServiceImpl.addSubscription(id, new ServiceCallbacksApi21(callbacks));
            }

            public void removeSubscription(String id, Object callbacks) {
                this.mServiceImpl.removeSubscription(id, new ServiceCallbacksApi21(callbacks));
            }

            public void getMediaItem(String mediaId, ResultReceiver receiver) {
            }
        }

        MediaBrowserServiceAdaptorApi21() {
        }

        public void onCreate(ServiceImplApi21 serviceImpl) {
            this.mBinder = new ServiceBinderProxyApi21(serviceImpl);
        }

        public IBinder onBind(Intent intent) {
            if (MediaBrowserServiceCompat.SERVICE_INTERFACE.equals(intent.getAction())) {
                return this.mBinder;
            }
            return null;
        }
    }

    public interface ServiceCallbacks {
        IBinder asBinder();

        void onConnect(String str, Object obj, Bundle bundle) throws RemoteException;

        void onConnectFailed() throws RemoteException;

        void onLoadChildren(String str, List<Parcel> list) throws RemoteException;
    }

    public static class ServiceCallbacksApi21 implements ServiceCallbacks {
        private static Object sNullParceledListSliceObj;
        private final IMediaBrowserServiceCallbacksAdapterApi21 mCallbacks;

        static {
            MediaItem nullMediaItem = new MediaItem(new Builder().setMediaId("android.support.v4.media.MediaBrowserCompat.NULL_MEDIA_ITEM").build(), 0);
            List<MediaItem> nullMediaItemList = new ArrayList();
            nullMediaItemList.add(nullMediaItem);
            sNullParceledListSliceObj = ParceledListSliceAdapterApi21.newInstance(nullMediaItemList);
        }

        ServiceCallbacksApi21(Object callbacksObj) {
            this.mCallbacks = new IMediaBrowserServiceCallbacksAdapterApi21(callbacksObj);
        }

        public IBinder asBinder() {
            return this.mCallbacks.asBinder();
        }

        public void onConnect(String root, Object session, Bundle extras) throws RemoteException {
            this.mCallbacks.onConnect(root, session, extras);
        }

        public void onConnectFailed() throws RemoteException {
            this.mCallbacks.onConnectFailed();
        }

        public void onLoadChildren(String mediaId, List<Parcel> list) throws RemoteException {
            List<MediaItem> itemList = null;
            if (list != null) {
                itemList = new ArrayList();
                for (Parcel parcel : list) {
                    parcel.setDataPosition(0);
                    itemList.add(MediaItem.CREATOR.createFromParcel(parcel));
                    parcel.recycle();
                }
            }
            Object pls = VERSION.SDK_INT > 23 ? itemList == null ? null : ParceledListSliceAdapterApi21.newInstance(itemList) : itemList == null ? sNullParceledListSliceObj : ParceledListSliceAdapterApi21.newInstance(itemList);
            this.mCallbacks.onLoadChildren(mediaId, pls);
        }
    }

    MediaBrowserServiceCompatApi21() {
    }

    public static Object createService() {
        return new MediaBrowserServiceAdaptorApi21();
    }

    public static void onCreate(Object serviceObj, ServiceImplApi21 serviceImpl) {
        ((MediaBrowserServiceAdaptorApi21) serviceObj).onCreate(serviceImpl);
    }

    public static IBinder onBind(Object serviceObj, Intent intent) {
        return ((MediaBrowserServiceAdaptorApi21) serviceObj).onBind(intent);
    }
}
