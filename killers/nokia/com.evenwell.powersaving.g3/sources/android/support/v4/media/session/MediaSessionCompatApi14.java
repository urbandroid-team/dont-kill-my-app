package android.support.v4.media.session;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Bundle;

class MediaSessionCompatApi14 {
    private static final long ACTION_FAST_FORWARD = 64;
    private static final long ACTION_PAUSE = 2;
    private static final long ACTION_PLAY = 4;
    private static final long ACTION_PLAY_PAUSE = 512;
    private static final long ACTION_REWIND = 8;
    private static final long ACTION_SKIP_TO_NEXT = 32;
    private static final long ACTION_SKIP_TO_PREVIOUS = 16;
    private static final long ACTION_STOP = 1;
    private static final String METADATA_KEY_ALBUM = "android.media.metadata.ALBUM";
    private static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    private static final String METADATA_KEY_ALBUM_ARTIST = "android.media.metadata.ALBUM_ARTIST";
    private static final String METADATA_KEY_ART = "android.media.metadata.ART";
    private static final String METADATA_KEY_ARTIST = "android.media.metadata.ARTIST";
    private static final String METADATA_KEY_AUTHOR = "android.media.metadata.AUTHOR";
    private static final String METADATA_KEY_COMPILATION = "android.media.metadata.COMPILATION";
    private static final String METADATA_KEY_COMPOSER = "android.media.metadata.COMPOSER";
    private static final String METADATA_KEY_DATE = "android.media.metadata.DATE";
    private static final String METADATA_KEY_DISC_NUMBER = "android.media.metadata.DISC_NUMBER";
    private static final String METADATA_KEY_DURATION = "android.media.metadata.DURATION";
    private static final String METADATA_KEY_GENRE = "android.media.metadata.GENRE";
    private static final String METADATA_KEY_TITLE = "android.media.metadata.TITLE";
    private static final String METADATA_KEY_TRACK_NUMBER = "android.media.metadata.TRACK_NUMBER";
    private static final String METADATA_KEY_WRITER = "android.media.metadata.WRITER";
    static final int RCC_PLAYSTATE_NONE = 0;
    static final int STATE_BUFFERING = 6;
    static final int STATE_CONNECTING = 8;
    static final int STATE_ERROR = 7;
    static final int STATE_FAST_FORWARDING = 4;
    static final int STATE_NONE = 0;
    static final int STATE_PAUSED = 2;
    static final int STATE_PLAYING = 3;
    static final int STATE_REWINDING = 5;
    static final int STATE_SKIPPING_TO_NEXT = 10;
    static final int STATE_SKIPPING_TO_PREVIOUS = 9;
    static final int STATE_SKIPPING_TO_QUEUE_ITEM = 11;
    static final int STATE_STOPPED = 1;

    MediaSessionCompatApi14() {
    }

    public static Object createRemoteControlClient(PendingIntent mbIntent) {
        return new RemoteControlClient(mbIntent);
    }

    public static void setState(Object rccObj, int state) {
        ((RemoteControlClient) rccObj).setPlaybackState(getRccStateFromState(state));
    }

    public static void setTransportControlFlags(Object rccObj, long actions) {
        ((RemoteControlClient) rccObj).setTransportControlFlags(getRccTransportControlFlagsFromActions(actions));
    }

    public static void setMetadata(Object rccObj, Bundle metadata) {
        MetadataEditor editor = ((RemoteControlClient) rccObj).editMetadata(true);
        buildOldMetadata(metadata, editor);
        editor.apply();
    }

    public static void registerRemoteControlClient(Context context, Object rccObj) {
        ((AudioManager) context.getSystemService("audio")).registerRemoteControlClient((RemoteControlClient) rccObj);
    }

    public static void unregisterRemoteControlClient(Context context, Object rccObj) {
        ((AudioManager) context.getSystemService("audio")).unregisterRemoteControlClient((RemoteControlClient) rccObj);
    }

    static int getRccStateFromState(int state) {
        switch (state) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 6:
            case 8:
                return 8;
            case 7:
                return 9;
            case 9:
                return 7;
            case 10:
            case 11:
                return 6;
            default:
                return -1;
        }
    }

    static int getRccTransportControlFlagsFromActions(long actions) {
        int transportControlFlags = 0;
        if ((1 & actions) != 0) {
            transportControlFlags = 0 | 32;
        }
        if ((2 & actions) != 0) {
            transportControlFlags |= 16;
        }
        if ((4 & actions) != 0) {
            transportControlFlags |= 4;
        }
        if ((8 & actions) != 0) {
            transportControlFlags |= 2;
        }
        if ((16 & actions) != 0) {
            transportControlFlags |= 1;
        }
        if ((32 & actions) != 0) {
            transportControlFlags |= 128;
        }
        if ((64 & actions) != 0) {
            transportControlFlags |= 64;
        }
        if ((512 & actions) != 0) {
            return transportControlFlags | 8;
        }
        return transportControlFlags;
    }

    static void buildOldMetadata(Bundle metadata, MetadataEditor editor) {
        if (metadata != null) {
            if (metadata.containsKey("android.media.metadata.ART")) {
                editor.putBitmap(100, (Bitmap) metadata.getParcelable("android.media.metadata.ART"));
            } else if (metadata.containsKey("android.media.metadata.ALBUM_ART")) {
                editor.putBitmap(100, (Bitmap) metadata.getParcelable("android.media.metadata.ALBUM_ART"));
            }
            if (metadata.containsKey("android.media.metadata.ALBUM")) {
                editor.putString(1, metadata.getString("android.media.metadata.ALBUM"));
            }
            if (metadata.containsKey("android.media.metadata.ALBUM_ARTIST")) {
                editor.putString(13, metadata.getString("android.media.metadata.ALBUM_ARTIST"));
            }
            if (metadata.containsKey("android.media.metadata.ARTIST")) {
                editor.putString(2, metadata.getString("android.media.metadata.ARTIST"));
            }
            if (metadata.containsKey("android.media.metadata.AUTHOR")) {
                editor.putString(3, metadata.getString("android.media.metadata.AUTHOR"));
            }
            if (metadata.containsKey("android.media.metadata.COMPILATION")) {
                editor.putString(15, metadata.getString("android.media.metadata.COMPILATION"));
            }
            if (metadata.containsKey("android.media.metadata.COMPOSER")) {
                editor.putString(4, metadata.getString("android.media.metadata.COMPOSER"));
            }
            if (metadata.containsKey("android.media.metadata.DATE")) {
                editor.putString(5, metadata.getString("android.media.metadata.DATE"));
            }
            if (metadata.containsKey("android.media.metadata.DISC_NUMBER")) {
                editor.putLong(14, metadata.getLong("android.media.metadata.DISC_NUMBER"));
            }
            if (metadata.containsKey("android.media.metadata.DURATION")) {
                editor.putLong(9, metadata.getLong("android.media.metadata.DURATION"));
            }
            if (metadata.containsKey("android.media.metadata.GENRE")) {
                editor.putString(6, metadata.getString("android.media.metadata.GENRE"));
            }
            if (metadata.containsKey("android.media.metadata.TITLE")) {
                editor.putString(7, metadata.getString("android.media.metadata.TITLE"));
            }
            if (metadata.containsKey("android.media.metadata.TRACK_NUMBER")) {
                editor.putLong(0, metadata.getLong("android.media.metadata.TRACK_NUMBER"));
            }
            if (metadata.containsKey("android.media.metadata.WRITER")) {
                editor.putString(11, metadata.getString("android.media.metadata.WRITER"));
            }
        }
    }
}
