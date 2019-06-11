package android.support.v4.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public final class MediaDescriptionCompat implements Parcelable {
    public static final Creator<MediaDescriptionCompat> CREATOR = new C02111();
    public static final String DESCRIPTION_KEY_MEDIA_URI = "android.support.v4.media.description.MEDIA_URI";
    public static final String DESCRIPTION_KEY_NULL_BUNDLE_FLAG = "android.support.v4.media.description.NULL_BUNDLE_FLAG";
    private final CharSequence mDescription;
    private Object mDescriptionObj;
    private final Bundle mExtras;
    private final Bitmap mIcon;
    private final Uri mIconUri;
    private final String mMediaId;
    private final Uri mMediaUri;
    private final CharSequence mSubtitle;
    private final CharSequence mTitle;

    /* renamed from: android.support.v4.media.MediaDescriptionCompat$1 */
    static class C02111 implements Creator<MediaDescriptionCompat> {
        C02111() {
        }

        public MediaDescriptionCompat createFromParcel(Parcel in) {
            if (VERSION.SDK_INT < 21) {
                return new MediaDescriptionCompat(in);
            }
            return MediaDescriptionCompat.fromMediaDescription(MediaDescriptionCompatApi21.fromParcel(in));
        }

        public MediaDescriptionCompat[] newArray(int size) {
            return new MediaDescriptionCompat[size];
        }
    }

    public static final class Builder {
        private CharSequence mDescription;
        private Bundle mExtras;
        private Bitmap mIcon;
        private Uri mIconUri;
        private String mMediaId;
        private Uri mMediaUri;
        private CharSequence mSubtitle;
        private CharSequence mTitle;

        public Builder setMediaId(@Nullable String mediaId) {
            this.mMediaId = mediaId;
            return this;
        }

        public Builder setTitle(@Nullable CharSequence title) {
            this.mTitle = title;
            return this;
        }

        public Builder setSubtitle(@Nullable CharSequence subtitle) {
            this.mSubtitle = subtitle;
            return this;
        }

        public Builder setDescription(@Nullable CharSequence description) {
            this.mDescription = description;
            return this;
        }

        public Builder setIconBitmap(@Nullable Bitmap icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder setIconUri(@Nullable Uri iconUri) {
            this.mIconUri = iconUri;
            return this;
        }

        public Builder setExtras(@Nullable Bundle extras) {
            this.mExtras = extras;
            return this;
        }

        public Builder setMediaUri(@Nullable Uri mediaUri) {
            this.mMediaUri = mediaUri;
            return this;
        }

        public MediaDescriptionCompat build() {
            return new MediaDescriptionCompat(this.mMediaId, this.mTitle, this.mSubtitle, this.mDescription, this.mIcon, this.mIconUri, this.mExtras, this.mMediaUri);
        }
    }

    private MediaDescriptionCompat(String mediaId, CharSequence title, CharSequence subtitle, CharSequence description, Bitmap icon, Uri iconUri, Bundle extras, Uri mediaUri) {
        this.mMediaId = mediaId;
        this.mTitle = title;
        this.mSubtitle = subtitle;
        this.mDescription = description;
        this.mIcon = icon;
        this.mIconUri = iconUri;
        this.mExtras = extras;
        this.mMediaUri = mediaUri;
    }

    private MediaDescriptionCompat(Parcel in) {
        this.mMediaId = in.readString();
        this.mTitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mSubtitle = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mDescription = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        this.mIcon = (Bitmap) in.readParcelable(null);
        this.mIconUri = (Uri) in.readParcelable(null);
        this.mExtras = in.readBundle();
        this.mMediaUri = (Uri) in.readParcelable(null);
    }

    @Nullable
    public String getMediaId() {
        return this.mMediaId;
    }

    @Nullable
    public CharSequence getTitle() {
        return this.mTitle;
    }

    @Nullable
    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    @Nullable
    public CharSequence getDescription() {
        return this.mDescription;
    }

    @Nullable
    public Bitmap getIconBitmap() {
        return this.mIcon;
    }

    @Nullable
    public Uri getIconUri() {
        return this.mIconUri;
    }

    @Nullable
    public Bundle getExtras() {
        return this.mExtras;
    }

    @Nullable
    public Uri getMediaUri() {
        return this.mMediaUri;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (VERSION.SDK_INT < 21) {
            dest.writeString(this.mMediaId);
            TextUtils.writeToParcel(this.mTitle, dest, flags);
            TextUtils.writeToParcel(this.mSubtitle, dest, flags);
            TextUtils.writeToParcel(this.mDescription, dest, flags);
            dest.writeParcelable(this.mIcon, flags);
            dest.writeParcelable(this.mIconUri, flags);
            dest.writeBundle(this.mExtras);
            dest.writeParcelable(this.mMediaUri, flags);
            return;
        }
        MediaDescriptionCompatApi21.writeToParcel(getMediaDescription(), dest, flags);
    }

    public String toString() {
        return this.mTitle + ", " + this.mSubtitle + ", " + this.mDescription;
    }

    public Object getMediaDescription() {
        if (this.mDescriptionObj != null || VERSION.SDK_INT < 21) {
            return this.mDescriptionObj;
        }
        Object bob = Builder.newInstance();
        Builder.setMediaId(bob, this.mMediaId);
        Builder.setTitle(bob, this.mTitle);
        Builder.setSubtitle(bob, this.mSubtitle);
        Builder.setDescription(bob, this.mDescription);
        Builder.setIconBitmap(bob, this.mIcon);
        Builder.setIconUri(bob, this.mIconUri);
        Bundle extras = this.mExtras;
        if (VERSION.SDK_INT < 23 && this.mMediaUri != null) {
            if (extras == null) {
                extras = new Bundle();
                extras.putBoolean(DESCRIPTION_KEY_NULL_BUNDLE_FLAG, true);
            }
            extras.putParcelable(DESCRIPTION_KEY_MEDIA_URI, this.mMediaUri);
        }
        Builder.setExtras(bob, extras);
        if (VERSION.SDK_INT >= 23) {
            Builder.setMediaUri(bob, this.mMediaUri);
        }
        this.mDescriptionObj = Builder.build(bob);
        return this.mDescriptionObj;
    }

    public static MediaDescriptionCompat fromMediaDescription(Object descriptionObj) {
        if (descriptionObj == null || VERSION.SDK_INT < 21) {
            return null;
        }
        Uri mediaUri;
        Builder bob = new Builder();
        bob.setMediaId(MediaDescriptionCompatApi21.getMediaId(descriptionObj));
        bob.setTitle(MediaDescriptionCompatApi21.getTitle(descriptionObj));
        bob.setSubtitle(MediaDescriptionCompatApi21.getSubtitle(descriptionObj));
        bob.setDescription(MediaDescriptionCompatApi21.getDescription(descriptionObj));
        bob.setIconBitmap(MediaDescriptionCompatApi21.getIconBitmap(descriptionObj));
        bob.setIconUri(MediaDescriptionCompatApi21.getIconUri(descriptionObj));
        Bundle extras = MediaDescriptionCompatApi21.getExtras(descriptionObj);
        if (extras == null) {
            mediaUri = null;
        } else {
            mediaUri = (Uri) extras.getParcelable(DESCRIPTION_KEY_MEDIA_URI);
        }
        if (mediaUri != null) {
            if (extras.containsKey(DESCRIPTION_KEY_NULL_BUNDLE_FLAG) && extras.size() == 2) {
                extras = null;
            } else {
                extras.remove(DESCRIPTION_KEY_MEDIA_URI);
                extras.remove(DESCRIPTION_KEY_NULL_BUNDLE_FLAG);
            }
        }
        bob.setExtras(extras);
        if (mediaUri != null) {
            bob.setMediaUri(mediaUri);
        } else if (VERSION.SDK_INT >= 23) {
            bob.setMediaUri(MediaDescriptionCompatApi23.getMediaUri(descriptionObj));
        }
        MediaDescriptionCompat descriptionCompat = bob.build();
        descriptionCompat.mDescriptionObj = descriptionObj;
        return descriptionCompat;
    }
}
