package com.evenwell.powersaving.g3.pushservice;

public enum PackageCategory {
    WHITE_LIST("ps_white_list"),
    BLACK_LIST("ps_black_list");
    
    private String mValue;

    private PackageCategory(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return this.mValue;
    }
}
