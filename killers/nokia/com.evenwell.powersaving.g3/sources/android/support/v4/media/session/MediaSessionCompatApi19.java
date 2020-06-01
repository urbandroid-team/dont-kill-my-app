package android.support.v4.media.session;

import android.media.Rating;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Bundle;

class MediaSessionCompatApi19 {
    private static final long ACTION_SET_RATING = 128;
    private static final String METADATA_KEY_RATING = "android.media.metadata.RATING";
    private static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";
    private static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";

    interface Callback extends Callback {
        void onSetRating(Object obj);
    }

    static class OnMetadataUpdateListener<T extends Callback> implements android.media.RemoteControlClient.OnMetadataUpdateListener {
        protected final T mCallback;

        public OnMetadataUpdateListener(T callback) {
            this.mCallback = callback;
        }

        public void onMetadataUpdate(int key, Object newValue) {
            if (key == 268435457 && (newValue instanceof Rating)) {
                this.mCallback.onSetRating(newValue);
            }
        }
    }

    MediaSessionCompatApi19() {
    }

    public static void setTransportControlFlags(Object rccObj, long actions) {
        ((RemoteControlClient) rccObj).setTransportControlFlags(getRccTransportControlFlagsFromActions(actions));
    }

    public static Object createMetadataUpdateListener(Callback callback) {
        return new OnMetadataUpdateListener(callback);
    }

    public static void setMetadata(Object rccObj, Bundle metadata, long actions) {
        MetadataEditor editor = ((RemoteControlClient) rccObj).editMetadata(true);
        MediaSessionCompatApi14.buildOldMetadata(metadata, editor);
        addNewMetadata(metadata, editor);
        if ((128 & actions) != 0) {
            editor.addEditableKey(268435457);
        }
        editor.apply();
    }

    public static void setOnMetadataUpdateListener(Object rccObj, Object onMetadataUpdateObj) {
        ((RemoteControlClient) rccObj).setMetadataUpdateListener((android.media.RemoteControlClient.OnMetadataUpdateListener) onMetadataUpdateObj);
    }

    static int getRccTransportControlFlagsFromActions(long actions) {
        int transportControlFlags = MediaSessionCompatApi18.getRccTransportControlFlagsFromActions(actions);
        if ((128 & actions) != 0) {
            return transportControlFlags | 512;
        }
        return transportControlFlags;
    }

    static void addNewMetadata(Bundle metadata, MetadataEditor editor) {
        if (metadata != null) {
            if (metadata.containsKey("android.media.metadata.YEAR")) {
                editor.putLong(8, metadata.getLong("android.media.metadata.YEAR"));
            }
            if (metadata.containsKey("android.media.metadata.RATING")) {
                editor.putObject(101, metadata.getParcelable("android.media.metadata.RATING"));
            }
            if (metadata.containsKey("android.media.metadata.USER_RATING")) {
                editor.putObject(268435457, metadata.getParcelable("android.media.metadata.USER_RATING"));
            }
        }
    }
}
