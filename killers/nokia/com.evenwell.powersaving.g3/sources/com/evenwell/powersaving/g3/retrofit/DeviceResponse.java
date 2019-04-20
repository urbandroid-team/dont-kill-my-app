package com.evenwell.powersaving.g3.retrofit;

import java.util.ArrayList;
import java.util.List;

public class DeviceResponse {
    public String app_name;
    public String category;
    List<CheckCpResponseComponent> components = new ArrayList();
    public String fingerprint;
    public String latest_version;
    public String package_id;
    public String process;

    public String toLogString() {
        return "{package_id: " + this.package_id + ",app_name: " + this.app_name + ",category: " + this.category + ",fingerprint : " + this.fingerprint + ",latest_version : " + this.latest_version + ",process : " + this.process + ",components : " + this.components + "}";
    }
}
