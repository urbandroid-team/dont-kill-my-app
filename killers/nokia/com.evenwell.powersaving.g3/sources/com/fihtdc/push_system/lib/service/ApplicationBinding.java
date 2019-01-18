package com.fihtdc.push_system.lib.service;

import java.util.LinkedHashMap;

public class ApplicationBinding {
    public LinkedHashMap<String, String> extraInfo = new LinkedHashMap();
    public String packageName;

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ApplicationBinding)) {
            return false;
        }
        ApplicationBinding binding = (ApplicationBinding) obj;
        if (this.packageName == null || !this.packageName.equals(binding.packageName) || binding.extraInfo.size() != this.extraInfo.size()) {
            return false;
        }
        for (String key : this.extraInfo.keySet()) {
            Object obj1 = this.extraInfo.get(key);
            Object obj2 = binding.extraInfo.get(key);
            if (obj1 != obj2) {
                if (obj1 == null) {
                    return false;
                }
                if (!obj1.equals(obj2)) {
                    return false;
                }
            }
        }
        return true;
    }
}
