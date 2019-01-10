package com.fihtdc.push_system.lib.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {
    public static String timeToString(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
    }
}
