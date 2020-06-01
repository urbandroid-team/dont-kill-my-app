package android.support.v4.media;

import android.media.Rating;

class RatingCompatApi21 {
    RatingCompatApi21() {
    }

    public static Object newUnratedRating(int ratingStyle) {
        return Rating.newUnratedRating(ratingStyle);
    }

    public static Object newHeartRating(boolean hasHeart) {
        return Rating.newHeartRating(hasHeart);
    }

    public static Object newThumbRating(boolean thumbIsUp) {
        return Rating.newThumbRating(thumbIsUp);
    }

    public static Object newStarRating(int starRatingStyle, float starRating) {
        return Rating.newStarRating(starRatingStyle, starRating);
    }

    public static Object newPercentageRating(float percent) {
        return Rating.newPercentageRating(percent);
    }

    public static boolean isRated(Object ratingObj) {
        return ((Rating) ratingObj).isRated();
    }

    public static int getRatingStyle(Object ratingObj) {
        return ((Rating) ratingObj).getRatingStyle();
    }

    public static boolean hasHeart(Object ratingObj) {
        return ((Rating) ratingObj).hasHeart();
    }

    public static boolean isThumbUp(Object ratingObj) {
        return ((Rating) ratingObj).isThumbUp();
    }

    public static float getStarRating(Object ratingObj) {
        return ((Rating) ratingObj).getStarRating();
    }

    public static float getPercentRating(Object ratingObj) {
        return ((Rating) ratingObj).getPercentRating();
    }
}
