package com.evenwell.powersaving.g3.retrofit;

import android.content.Context;

public class CheckCpPost extends DevicePost {
    public String reason;

    public CheckCpPost(Context context, String category, String reason) {
        super(context);
        this.category = category;
        this.reason = reason;
    }

    public String toString() {
        return "category: " + this.category + ",app_name: " + this.app_name + ",fingerprint: " + this.fingerprint + ",version: " + this.version + ",device_project: " + this.device_project + ",device_model: " + this.device_model + ",device_skuid: " + this.device_skuid + ",device_id: " + this.device_id + ",device_version: " + this.device_version + ",device_sub_version: " + this.device_sub_version + ",reason: " + this.reason;
    }
}
