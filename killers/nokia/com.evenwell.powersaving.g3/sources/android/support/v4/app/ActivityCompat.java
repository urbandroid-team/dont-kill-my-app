package android.support.v4.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat21.SharedElementCallback21;
import android.support.v4.content.ContextCompat;
import android.view.View;
import java.util.List;
import java.util.Map;

public class ActivityCompat extends ContextCompat {

    public interface OnRequestPermissionsResultCallback {
        void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr);
    }

    private static class SharedElementCallback21Impl extends SharedElementCallback21 {
        private SharedElementCallback mCallback;

        public SharedElementCallback21Impl(SharedElementCallback callback) {
            this.mCallback = callback;
        }

        public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
            this.mCallback.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
        }

        public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
            this.mCallback.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
        }

        public void onRejectSharedElements(List<View> rejectedSharedElements) {
            this.mCallback.onRejectSharedElements(rejectedSharedElements);
        }

        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            this.mCallback.onMapSharedElements(names, sharedElements);
        }

        public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
            return this.mCallback.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds);
        }

        public View onCreateSnapshotView(Context context, Parcelable snapshot) {
            return this.mCallback.onCreateSnapshotView(context, snapshot);
        }
    }

    public static boolean invalidateOptionsMenu(Activity activity) {
        if (VERSION.SDK_INT < 11) {
            return false;
        }
        ActivityCompatHoneycomb.invalidateOptionsMenu(activity);
        return true;
    }

    public static void startActivity(Activity activity, Intent intent, @Nullable Bundle options) {
        if (VERSION.SDK_INT >= 16) {
            ActivityCompatJB.startActivity(activity, intent, options);
        } else {
            activity.startActivity(intent);
        }
    }

    public static void startActivityForResult(Activity activity, Intent intent, int requestCode, @Nullable Bundle options) {
        if (VERSION.SDK_INT >= 16) {
            ActivityCompatJB.startActivityForResult(activity, intent, requestCode, options);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public static void finishAffinity(Activity activity) {
        if (VERSION.SDK_INT >= 16) {
            ActivityCompatJB.finishAffinity(activity);
        } else {
            activity.finish();
        }
    }

    public static void finishAfterTransition(Activity activity) {
        if (VERSION.SDK_INT >= 21) {
            ActivityCompat21.finishAfterTransition(activity);
        } else {
            activity.finish();
        }
    }

    public Uri getReferrer(Activity activity) {
        if (VERSION.SDK_INT >= 22) {
            return ActivityCompat22.getReferrer(activity);
        }
        Intent intent = activity.getIntent();
        Uri referrer = (Uri) intent.getParcelableExtra("android.intent.extra.REFERRER");
        if (referrer != null) {
            return referrer;
        }
        String referrerName = intent.getStringExtra("android.intent.extra.REFERRER_NAME");
        if (referrerName != null) {
            return Uri.parse(referrerName);
        }
        return null;
    }

    public static void setEnterSharedElementCallback(Activity activity, SharedElementCallback callback) {
        if (VERSION.SDK_INT >= 21) {
            ActivityCompat21.setEnterSharedElementCallback(activity, createCallback(callback));
        }
    }

    public static void setExitSharedElementCallback(Activity activity, SharedElementCallback callback) {
        if (VERSION.SDK_INT >= 21) {
            ActivityCompat21.setExitSharedElementCallback(activity, createCallback(callback));
        }
    }

    public static void postponeEnterTransition(Activity activity) {
        if (VERSION.SDK_INT >= 21) {
            ActivityCompat21.postponeEnterTransition(activity);
        }
    }

    public static void startPostponedEnterTransition(Activity activity) {
        if (VERSION.SDK_INT >= 21) {
            ActivityCompat21.startPostponedEnterTransition(activity);
        }
    }

    public static void requestPermissions(@NonNull final Activity activity, @NonNull final String[] permissions, final int requestCode) {
        if (VERSION.SDK_INT >= 23) {
            ActivityCompatApi23.requestPermissions(activity, permissions, requestCode);
        } else if (activity instanceof OnRequestPermissionsResultCallback) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    int[] grantResults = new int[permissions.length];
                    PackageManager packageManager = activity.getPackageManager();
                    String packageName = activity.getPackageName();
                    int permissionCount = permissions.length;
                    for (int i = 0; i < permissionCount; i++) {
                        grantResults[i] = packageManager.checkPermission(permissions[i], packageName);
                    }
                    ((OnRequestPermissionsResultCallback) activity).onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            });
        }
    }

    public static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity, @NonNull String permission) {
        if (VERSION.SDK_INT >= 23) {
            return ActivityCompatApi23.shouldShowRequestPermissionRationale(activity, permission);
        }
        return false;
    }

    private static SharedElementCallback21 createCallback(SharedElementCallback callback) {
        if (callback != null) {
            return new SharedElementCallback21Impl(callback);
        }
        return null;
    }
}
