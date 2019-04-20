package android.support.v4.media;

import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.Rating;
import android.os.Parcel;
import java.util.Set;

class MediaMetadataCompatApi21 {

    public static class Builder {
        public static Object newInstance() {
            return new android.media.MediaMetadata.Builder();
        }

        public static void putBitmap(Object builderObj, String key, Bitmap value) {
            ((android.media.MediaMetadata.Builder) builderObj).putBitmap(key, value);
        }

        public static void putLong(Object builderObj, String key, long value) {
            ((android.media.MediaMetadata.Builder) builderObj).putLong(key, value);
        }

        public static void putRating(Object builderObj, String key, Object ratingObj) {
            ((android.media.MediaMetadata.Builder) builderObj).putRating(key, (Rating) ratingObj);
        }

        public static void putText(Object builderObj, String key, CharSequence value) {
            ((android.media.MediaMetadata.Builder) builderObj).putText(key, value);
        }

        public static void putString(Object builderObj, String key, String value) {
            ((android.media.MediaMetadata.Builder) builderObj).putString(key, value);
        }

        public static Object build(Object builderObj) {
            return ((android.media.MediaMetadata.Builder) builderObj).build();
        }
    }

    MediaMetadataCompatApi21() {
    }

    public static Set<String> keySet(Object metadataObj) {
        return ((MediaMetadata) metadataObj).keySet();
    }

    public static Bitmap getBitmap(Object metadataObj, String key) {
        return ((MediaMetadata) metadataObj).getBitmap(key);
    }

    public static long getLong(Object metadataObj, String key) {
        return ((MediaMetadata) metadataObj).getLong(key);
    }

    public static Object getRating(Object metadataObj, String key) {
        return ((MediaMetadata) metadataObj).getRating(key);
    }

    public static CharSequence getText(Object metadataObj, String key) {
        return ((MediaMetadata) metadataObj).getText(key);
    }

    public static void writeToParcel(Object metadataObj, Parcel dest, int flags) {
        ((MediaMetadata) metadataObj).writeToParcel(dest, flags);
    }

    public static Object createFromParcel(Parcel in) {
        return MediaMetadata.CREATOR.createFromParcel(in);
    }
}
