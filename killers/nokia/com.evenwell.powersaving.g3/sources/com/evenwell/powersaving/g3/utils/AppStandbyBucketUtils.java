package com.evenwell.powersaving.g3.utils;

import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import net2.lingala.zip4j.util.InternalZipConstants;

public class AppStandbyBucketUtils {
    private static final String TAG = "AppStandbyBucketUtils";

    public static String bucketToString(int bucket) {
        switch (bucket) {
            case 5:
                return "Exempted";
            case 10:
                return "Active";
            case 20:
                return "Working_set";
            case 30:
                return "Frequent";
            case MotionEventCompat.AXIS_GENERIC_9 /*40*/:
                return "Rare";
            case InternalZipConstants.FOLDER_MODE_HIDDEN_ARCHIVE /*50*/:
                return "Never";
            default:
                return String.valueOf(bucket);
        }
    }

    public static boolean setAppStandbyBucketIfLarger(Context context, String packageName, int bucket) {
        int oldBucket = getAppStandbyBucket(context, packageName);
        if (bucket > oldBucket) {
            setAppStandbyBucket(context, packageName, bucket);
            Log.i(TAG, packageName + " change to " + bucketToString(bucket) + " from " + bucketToString(oldBucket));
            return true;
        }
        Log.i(TAG, packageName + " bucket is " + bucketToString(oldBucket) + ", don't need to change it.");
        return false;
    }

    public static void setAppStandbyBucket(Context context, String packageName, int bucket) {
        ((UsageStatsManager) context.getSystemService("usagestats")).setAppStandbyBucket(packageName, bucket);
    }

    public static int getAppStandbyBucket(Context context, String packageName) {
        return ((UsageStatsManager) context.getSystemService("usagestats")).getAppStandbyBucket(packageName);
    }
}
