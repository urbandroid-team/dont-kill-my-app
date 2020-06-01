package com.evenwell.powersaving.g3.retrofit;

import java.util.ArrayList;
import java.util.List;

public class UpdateResultPost {
    public String device_id;
    public String package_id;
    List<UpdateResultPostStatistics> statistics = new ArrayList();
    public String status;

    public String toLogString() {
        return "{package_id: " + this.package_id + ",device_id: " + this.device_id + ",status: " + this.status + "}";
    }
}
