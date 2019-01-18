package com.evenwell.powersaving.g3.retrofit;

import android.content.Context;
import android.telephony.TelephonyManager;

public class RegisterDevicePost extends DevicePost {
    public String device_imei;
    public String regular_polling_interval;

    public RegisterDevicePost(Context context, String category, String regular_polling_interval) {
        super(context);
        this.category = category;
        this.device_imei = getIMEI(context);
        this.regular_polling_interval = regular_polling_interval;
    }

    public String toString() {
        return "category: " + this.category + ",app_name: " + this.app_name + ",fingerprint: " + this.fingerprint + ",version: " + this.version + ",device_project: " + this.device_project + ",device_model: " + this.device_model + ",device_skuid: " + this.device_skuid + ",device_id: " + this.device_id + ",device_version: " + this.device_version + ",device_sub_version: " + this.device_sub_version + ",device_imei: " + this.device_imei + ",regular_polling_interval: " + this.regular_polling_interval;
    }

    public static String getIMEI(Context context) {
        return ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
    }
}
