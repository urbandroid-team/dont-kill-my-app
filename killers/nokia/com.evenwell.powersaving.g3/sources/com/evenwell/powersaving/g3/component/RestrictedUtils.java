package com.evenwell.powersaving.g3.component;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.NOTIFICATION.EXTRA_KEY;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RestrictedUtils {
    public static List<RestrictedComponent> getRestrictedComponent(@NonNull Context context) {
        List<RestrictedComponent> restrictedComponents = new ArrayList();
        String[] items = context.getResources().getStringArray(C0321R.array.restricted_sdk_component);
        KeyValueListParser parser = new KeyValueListParser(',');
        for (String item : items) {
            parser.setString(item);
            String type = parser.getString(EXTRA_KEY.TYPE, "");
            String component = parser.getString("component", "");
            String pkgName = parser.getString(PushMessageContract.MESSAGE_KEY_PACKAGE_NAME, "");
            if (!TextUtils.isEmpty(component)) {
                RestrictedComponent restrictedComponent = new RestrictedComponent();
                restrictedComponent.type = type;
                restrictedComponent.component = component;
                restrictedComponent.pkgName = pkgName;
                restrictedComponents.add(restrictedComponent);
            }
        }
        return restrictedComponents;
    }

    public static void restricted(@NonNull Context context, @NonNull String pkgName, boolean restricted) {
        String action = restricted ? UpdateComponentService.ACTION_RESTRICTED : UpdateComponentService.ACTION_NO_RESTRICTED;
        Intent updateComponentService = new Intent(context, UpdateComponentService.class);
        updateComponentService.setAction(action);
        updateComponentService.putExtra(UpdateComponentService.KEY_PACKAGE_NAME, pkgName);
        context.startServiceAsUser(updateComponentService, UserHandle.CURRENT);
    }

    public static List<String> getRestrictedByType(@NonNull Context context, @NonNull String type) {
        List<String> components = new ArrayList();
        for (RestrictedComponent restrictedComponent : getRestrictedComponent(context)) {
            if (type.equals(restrictedComponent.type)) {
                components.add(restrictedComponent.component);
            }
        }
        return components;
    }

    public static Set<String> getRestrictedComponents(@NonNull Context context) {
        Set<String> components = new ArraySet();
        for (RestrictedComponent restrictedComponent : getRestrictedComponent(context)) {
            if (!TextUtils.isEmpty(restrictedComponent.component)) {
                components.add(restrictedComponent.component);
            }
        }
        return components;
    }
}
