package com.fihtdc.asyncservice;

import android.util.Log;

public class LogUtils {
    private static final boolean DDBG = true;
    private static final boolean EDBG = true;
    private static final boolean IDBG = false;
    public static final String LOG_TAG = "BackupRestoreService";
    private static final boolean VDBG = false;
    private static final boolean WDBG = true;

    private LogUtils() {
    }

    public static void logV(String tag, String msg) {
    }

    public static void logI(String tag, String msg) {
    }

    public static void logD(String tag, String msg) {
        Log.d(tag, msg);
    }

    public static void logW(String tag, String msg) {
        Log.w(tag, msg);
    }

    public static void logW(String tag, String msg, Throwable tr) {
        Log.w(tag, msg, tr);
    }

    public static void logE(String tag, String msg) {
        Log.e(tag, msg);
    }
}
