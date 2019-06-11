package com.evenwell.powersaving.g3.component;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.util.CollectionUtils;
import com.evenwell.powersaving.g3.utils.PackageManagerUtils;
import java.util.List;

public class UpdateComponentService extends IntentService {
    public static final String ACTION_NO_RESTRICTED = "action_no_restricted";
    public static final String ACTION_RESTRICTED = "action_restricted";
    private static final boolean DBG = false;
    public static final String KEY_PACKAGE_NAME = "key_package_name";
    private static final String TAG = "UpdateComponentService";
    public static final String TYPE_ACTIVITY = "activity";
    public static final String TYPE_PROVIDER = "provider";
    public static final String TYPE_RECEIVER = "receiver";
    public static final String TYPE_SERVICE = "service";

    public UpdateComponentService() {
        super(TAG);
    }

    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            updateComponent(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void updateComponent(@Nullable Intent intent) {
        boolean enabled;
        String action = intent.getAction();
        if (ACTION_RESTRICTED.equals(action)) {
            enabled = false;
        } else if (ACTION_NO_RESTRICTED.equals(action)) {
            enabled = true;
        } else {
            Log.i(TAG, "unknown action=" + action);
            return;
        }
        String pkgName = intent.getStringExtra(KEY_PACKAGE_NAME);
        List<RestrictedComponent> restrictedComponents = RestrictedUtils.getRestrictedComponent(this);
        if (TextUtils.isEmpty(pkgName)) {
            Log.i(TAG, "pkgName is empty");
            return;
        }
        for (RestrictedComponent restrictedComponent : restrictedComponents) {
            if (TextUtils.isEmpty(restrictedComponent.pkgName) || TextUtils.equals(restrictedComponent.pkgName, pkgName)) {
                if (TextUtils.isEmpty(restrictedComponent.type)) {
                    setActivitiesState(pkgName, restrictedComponent.component, enabled);
                    setServiceState(pkgName, restrictedComponent.component, enabled);
                    setReceiverState(pkgName, restrictedComponent.component, enabled);
                    setProvidersState(pkgName, restrictedComponent.component, enabled);
                } else if ("activity".equals(restrictedComponent.type)) {
                    setActivitiesState(pkgName, restrictedComponent.component, enabled);
                } else if ("service".equals(restrictedComponent.type)) {
                    setServiceState(pkgName, restrictedComponent.component, enabled);
                } else if (TYPE_RECEIVER.equals(restrictedComponent.type)) {
                    setReceiverState(pkgName, restrictedComponent.component, enabled);
                } else if (TYPE_PROVIDER.equals(restrictedComponent.type)) {
                    setProvidersState(pkgName, restrictedComponent.component, enabled);
                } else {
                    Log.i(TAG, "unknown type=" + restrictedComponent.type);
                }
            }
        }
    }

    private void setServiceState(String pkgName, String componentName, boolean enabled) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(pkgName, componentName);
        intent.setComponent(component);
        if (CollectionUtils.size(PackageManagerUtils.queryIntentServices(this, intent)) > 0) {
            PackageManagerUtils.setComponentState(this, component, enabled);
        }
    }

    private void setReceiverState(String pkgName, String componentName, boolean enabled) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(pkgName, componentName);
        intent.setComponent(component);
        if (CollectionUtils.size(PackageManagerUtils.queryBroadcastReceivers(this, intent)) > 0) {
            PackageManagerUtils.setComponentState(this, component, enabled);
        }
    }

    private void setActivitiesState(String pkgName, String componentName, boolean enabled) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(pkgName, componentName);
        intent.setComponent(component);
        if (CollectionUtils.size(PackageManagerUtils.queryIntentActivities(this, intent)) > 0) {
            PackageManagerUtils.setComponentState(this, component, enabled);
        }
    }

    private void setProvidersState(String pkgName, String componentName, boolean enabled) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName(pkgName, componentName);
        intent.setComponent(component);
        if (CollectionUtils.size(PackageManagerUtils.queryIntentContentProviders(this, intent)) > 0) {
            PackageManagerUtils.setComponentState(this, component, enabled);
        }
    }
}
