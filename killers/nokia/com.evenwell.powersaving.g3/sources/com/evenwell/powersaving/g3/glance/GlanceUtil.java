package com.evenwell.powersaving.g3.glance;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings.Secure;
import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSConst.SWITCHER;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

public class GlanceUtil {
    private static final boolean DBG = true;
    public static final String DOZE_ENABLED = "doze_enabled";
    private static final String GLANCE_OPTION_PATH = "/proc/AllHWList/LCM0/glance";
    private static final String GLANCE_PACKAGE_NAME_1 = "com.evenwell.glance";
    private static final String GLANCE_PACKAGE_NAME_2 = "com.evenwell.glance.Screensaver";
    private static final String SELTP_PATH = "/proc/AllHWList/LCM0/setlp";
    private static String TAG = TAG.PSLOG;
    private static String sCurrentGlanceSettings = "-1";

    private static boolean writeCommand(String path, String value) {
        Exception e;
        PrintWriter outStream = null;
        try {
            if (new File(path).exists()) {
                PrintWriter outStream2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
                try {
                    outStream2.println(value);
                    Log.i("Glance", "writeCommand:flush path=" + path + " val=" + value);
                    if (outStream2 != null) {
                        outStream2.close();
                    }
                    outStream = outStream2;
                } catch (Exception e2) {
                    e = e2;
                    outStream = outStream2;
                    try {
                        e.printStackTrace();
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (Throwable th) {
                        if (outStream != null) {
                            outStream.close();
                        }
                        return false;
                    }
                    return false;
                } catch (Throwable th2) {
                    outStream = outStream2;
                    if (outStream != null) {
                        outStream.close();
                    }
                    return false;
                }
            } else if (outStream != null) {
                outStream.close();
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            if (outStream != null) {
                outStream.close();
            }
            return false;
        }
        return false;
    }

    private static boolean notifyGlanceSettingsToKernel(boolean enabled) {
        String newValue = enabled ? "1" : SYMBOLS.ZERO;
        if (sCurrentGlanceSettings.equals(newValue)) {
            return true;
        }
        Log.i(TAG, "write Glance option file node = " + newValue);
        sCurrentGlanceSettings = newValue;
        return writeCommand(GLANCE_OPTION_PATH, newValue);
    }

    public static void setGlanceModeEnable(Context context, boolean enable) {
        ContentResolver cr = context.getContentResolver();
        if (enable) {
            Secure.putInt(cr, DOZE_ENABLED, 1);
        } else {
            Secure.putInt(cr, DOZE_ENABLED, 0);
        }
        notifyGlanceSettingsToKernel(enable);
    }

    public static void setGlanceModeEnable(Context context, String value) {
        if (checkIfGlanceIsInstalled(context.getPackageManager())) {
            Log.i(TAG, "support Glance Mode");
            boolean z = false;
            if (!value.equals(SWITCHER.KEEP)) {
                if (value.equals(SWITCHER.ON)) {
                    z = true;
                }
                if (value.equals(SWITCHER.OFF)) {
                    z = false;
                }
                setGlanceModeEnable(context, z);
                return;
            }
            return;
        }
        Log.i(TAG, "Don't support Glance Mode");
    }

    public static boolean getGlanceModeEnable(Context context) {
        if (Secure.getInt(context.getContentResolver(), DOZE_ENABLED, -1) == 1) {
            return true;
        }
        return false;
    }

    private static boolean checkIfGlanceIsInstalled(PackageManager pm) {
        List<ApplicationInfo> applist = pm.getInstalledApplications(0);
        for (int i = 0; i < applist.size(); i++) {
            ApplicationInfo info = (ApplicationInfo) applist.get(i);
            if (info.packageName.equals(GLANCE_PACKAGE_NAME_1) || info.packageName.equals(GLANCE_PACKAGE_NAME_2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSupportGlanceMode(PackageManager pm) {
        return checkIfGlanceIsInstalled(pm);
    }
}
