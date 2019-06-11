package android.support.v4.media;

import android.graphics.Bitmap;
import android.media.MediaDescription;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;

class MediaDescriptionCompatApi21 {

    static class Builder {
        Builder() {
        }

        public static Object newInstance() {
            return new android.media.MediaDescription.Builder();
        }

        public static void setMediaId(Object builderObj, String mediaId) {
            ((android.media.MediaDescription.Builder) builderObj).setMediaId(mediaId);
        }

        public static void setTitle(Object builderObj, CharSequence title) {
            ((android.media.MediaDescription.Builder) builderObj).setTitle(title);
        }

        public static void setSubtitle(Object builderObj, CharSequence subtitle) {
            ((android.media.MediaDescription.Builder) builderObj).setSubtitle(subtitle);
        }

        public static void setDescription(Object builderObj, CharSequence description) {
            ((android.media.MediaDescription.Builder) builderObj).setDescription(description);
        }

        public static void setIconBitmap(Object builderObj, Bitmap iconBitmap) {
            ((android.media.MediaDescription.Builder) builderObj).setIconBitmap(iconBitmap);
        }

        public static void setIconUri(Object builderObj, Uri iconUri) {
            ((android.media.MediaDescription.Builder) builderObj).setIconUri(iconUri);
        }

        public static void setExtras(Object builderObj, Bundle extras) {
            ((android.media.MediaDescription.Builder) builderObj).setExtras(extras);
        }

        public static Object build(Object builderObj) {
            return ((android.media.MediaDescription.Builder) builderObj).build();
        }
    }

    MediaDescriptionCompatApi21() {
    }

    public static String getMediaId(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getMediaId();
    }

    public static CharSequence getTitle(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getTitle();
    }

    public static CharSequence getSubtitle(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getSubtitle();
    }

    public static CharSequence getDescription(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getDescription();
    }

    public static Bitmap getIconBitmap(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getIconBitmap();
    }

    public static Uri getIconUri(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getIconUri();
    }

    public static Bundle getExtras(Object descriptionObj) {
        return ((MediaDescription) descriptionObj).getExtras();
    }

    public static void writeToParcel(Object descriptionObj, Parcel dest, int flags) {
        ((MediaDescription) descriptionObj).writeToParcel(dest, flags);
    }

    public static Object fromParcel(Parcel in) {
        return MediaDescription.CREATOR.createFromParcel(in);
    }
}
