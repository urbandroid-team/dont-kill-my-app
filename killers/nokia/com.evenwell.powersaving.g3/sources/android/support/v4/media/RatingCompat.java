package android.support.v4.media;

import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class RatingCompat implements Parcelable {
    public static final Creator<RatingCompat> CREATOR = new C02131();
    public static final int RATING_3_STARS = 3;
    public static final int RATING_4_STARS = 4;
    public static final int RATING_5_STARS = 5;
    public static final int RATING_HEART = 1;
    public static final int RATING_NONE = 0;
    private static final float RATING_NOT_RATED = -1.0f;
    public static final int RATING_PERCENTAGE = 6;
    public static final int RATING_THUMB_UP_DOWN = 2;
    private static final String TAG = "Rating";
    private Object mRatingObj;
    private final int mRatingStyle;
    private final float mRatingValue;

    /* renamed from: android.support.v4.media.RatingCompat$1 */
    static class C02131 implements Creator<RatingCompat> {
        C02131() {
        }

        public RatingCompat createFromParcel(Parcel p) {
            return new RatingCompat(p.readInt(), p.readFloat());
        }

        public RatingCompat[] newArray(int size) {
            return new RatingCompat[size];
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface StarStyle {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Style {
    }

    private RatingCompat(int ratingStyle, float rating) {
        this.mRatingStyle = ratingStyle;
        this.mRatingValue = rating;
    }

    public String toString() {
        return "Rating:style=" + this.mRatingStyle + " rating=" + (this.mRatingValue < 0.0f ? "unrated" : String.valueOf(this.mRatingValue));
    }

    public int describeContents() {
        return this.mRatingStyle;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRatingStyle);
        dest.writeFloat(this.mRatingValue);
    }

    public static RatingCompat newUnratedRating(int ratingStyle) {
        switch (ratingStyle) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return new RatingCompat(ratingStyle, RATING_NOT_RATED);
            default:
                return null;
        }
    }

    public static RatingCompat newHeartRating(boolean hasHeart) {
        return new RatingCompat(1, hasHeart ? 1.0f : 0.0f);
    }

    public static RatingCompat newThumbRating(boolean thumbIsUp) {
        return new RatingCompat(2, thumbIsUp ? 1.0f : 0.0f);
    }

    public static RatingCompat newStarRating(int starRatingStyle, float starRating) {
        float maxRating;
        switch (starRatingStyle) {
            case 3:
                maxRating = 3.0f;
                break;
            case 4:
                maxRating = 4.0f;
                break;
            case 5:
                maxRating = 5.0f;
                break;
            default:
                Log.e(TAG, "Invalid rating style (" + starRatingStyle + ") for a star rating");
                return null;
        }
        if (starRating >= 0.0f && starRating <= maxRating) {
            return new RatingCompat(starRatingStyle, starRating);
        }
        Log.e(TAG, "Trying to set out of range star-based rating");
        return null;
    }

    public static RatingCompat newPercentageRating(float percent) {
        if (percent >= 0.0f && percent <= 100.0f) {
            return new RatingCompat(6, percent);
        }
        Log.e(TAG, "Invalid percentage-based rating value");
        return null;
    }

    public boolean isRated() {
        return this.mRatingValue >= 0.0f;
    }

    public int getRatingStyle() {
        return this.mRatingStyle;
    }

    public boolean hasHeart() {
        boolean z = true;
        if (this.mRatingStyle != 1) {
            return false;
        }
        if (this.mRatingValue != 1.0f) {
            z = false;
        }
        return z;
    }

    public boolean isThumbUp() {
        if (this.mRatingStyle == 2 && this.mRatingValue == 1.0f) {
            return true;
        }
        return false;
    }

    public float getStarRating() {
        switch (this.mRatingStyle) {
            case 3:
            case 4:
            case 5:
                if (isRated()) {
                    return this.mRatingValue;
                }
                break;
        }
        return RATING_NOT_RATED;
    }

    public float getPercentRating() {
        if (this.mRatingStyle == 6 && isRated()) {
            return this.mRatingValue;
        }
        return RATING_NOT_RATED;
    }

    public static RatingCompat fromRating(Object ratingObj) {
        RatingCompat ratingCompat = null;
        if (ratingObj != null && VERSION.SDK_INT >= 21) {
            int ratingStyle = RatingCompatApi21.getRatingStyle(ratingObj);
            if (RatingCompatApi21.isRated(ratingObj)) {
                switch (ratingStyle) {
                    case 1:
                        ratingCompat = newHeartRating(RatingCompatApi21.hasHeart(ratingObj));
                        break;
                    case 2:
                        ratingCompat = newThumbRating(RatingCompatApi21.isThumbUp(ratingObj));
                        break;
                    case 3:
                    case 4:
                    case 5:
                        ratingCompat = newStarRating(ratingStyle, RatingCompatApi21.getStarRating(ratingObj));
                        break;
                    case 6:
                        ratingCompat = newPercentageRating(RatingCompatApi21.getPercentRating(ratingObj));
                        break;
                    default:
                        break;
                }
            }
            ratingCompat = newUnratedRating(ratingStyle);
            ratingCompat.mRatingObj = ratingObj;
        }
        return ratingCompat;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.Object getRating() {
        /*
        r2 = this;
        r0 = r2.mRatingObj;
        if (r0 != 0) goto L_0x000a;
    L_0x0004:
        r0 = android.os.Build.VERSION.SDK_INT;
        r1 = 21;
        if (r0 >= r1) goto L_0x000d;
    L_0x000a:
        r0 = r2.mRatingObj;
    L_0x000c:
        return r0;
    L_0x000d:
        r0 = r2.isRated();
        if (r0 == 0) goto L_0x004a;
    L_0x0013:
        r0 = r2.mRatingStyle;
        switch(r0) {
            case 1: goto L_0x001a;
            case 2: goto L_0x0027;
            case 3: goto L_0x0032;
            case 4: goto L_0x0032;
            case 5: goto L_0x0032;
            case 6: goto L_0x003f;
            default: goto L_0x0018;
        };
    L_0x0018:
        r0 = 0;
        goto L_0x000c;
    L_0x001a:
        r0 = r2.hasHeart();
        r0 = android.support.v4.media.RatingCompatApi21.newHeartRating(r0);
        r2.mRatingObj = r0;
    L_0x0024:
        r0 = r2.mRatingObj;
        goto L_0x000c;
    L_0x0027:
        r0 = r2.isThumbUp();
        r0 = android.support.v4.media.RatingCompatApi21.newThumbRating(r0);
        r2.mRatingObj = r0;
        goto L_0x0024;
    L_0x0032:
        r0 = r2.mRatingStyle;
        r1 = r2.getStarRating();
        r0 = android.support.v4.media.RatingCompatApi21.newStarRating(r0, r1);
        r2.mRatingObj = r0;
        goto L_0x0024;
    L_0x003f:
        r0 = r2.getPercentRating();
        r0 = android.support.v4.media.RatingCompatApi21.newPercentageRating(r0);
        r2.mRatingObj = r0;
        goto L_0x0018;
    L_0x004a:
        r0 = r2.mRatingStyle;
        r0 = android.support.v4.media.RatingCompatApi21.newUnratedRating(r0);
        r2.mRatingObj = r0;
        goto L_0x0024;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.media.RatingCompat.getRating():java.lang.Object");
    }
}
