package com.evenwell.powersaving.g3.p000e.doze;

import android.util.Log;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/* renamed from: com.evenwell.powersaving.g3.e.doze.DozeStatus */
public class DozeStatus {
    public static final int LIGHT_STATE_ACTIVE = 0;
    public static final int LIGHT_STATE_IDLE = 4;
    public static final int LIGHT_STATE_IDLE_MAINTENANCE = 6;
    public static final int LIGHT_STATE_INACTIVE = 1;
    public static final int LIGHT_STATE_OVERRIDE = 7;
    public static final int LIGHT_STATE_PRE_IDLE = 3;
    public static final int LIGHT_STATE_WAITING_FOR_NETWORK = 5;
    public static final int STATE_ACTIVE = 0;
    public static final int STATE_IDLE = 5;
    public static final int STATE_IDLE_MAINTENANCE = 6;
    public static final int STATE_IDLE_PENDING = 2;
    public static final int STATE_INACTIVE = 1;
    public static final int STATE_LOCATING = 4;
    public static final int STATE_SENSING = 3;
    private static final String TAG = "DozeUtils";

    public static String stateToString(int state) {
        switch (state) {
            case 0:
                return "ACTIVE";
            case 1:
                return "INACTIVE";
            case 2:
                return "IDLE_PENDING";
            case 3:
                return "SENSING";
            case 4:
                return "LOCATING";
            case 5:
                return "IDLE";
            case 6:
                return "IDLE_MAINTENANCE";
            default:
                return Integer.toString(state);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int stringToState(java.lang.String r6) {
        /*
        r3 = 3;
        r2 = 2;
        r1 = 1;
        r0 = 0;
        r4 = -1;
        r5 = r6.hashCode();
        switch(r5) {
            case -1611296881: goto L_0x003a;
            case -1596614453: goto L_0x0030;
            case -917315572: goto L_0x0026;
            case -164102392: goto L_0x004e;
            case 2242516: goto L_0x0044;
            case 807292011: goto L_0x001c;
            case 1925346054: goto L_0x0012;
            default: goto L_0x000c;
        };
    L_0x000c:
        r5 = r4;
    L_0x000d:
        switch(r5) {
            case 0: goto L_0x0011;
            case 1: goto L_0x0058;
            case 2: goto L_0x005a;
            case 3: goto L_0x005c;
            case 4: goto L_0x005e;
            case 5: goto L_0x0060;
            case 6: goto L_0x0062;
            default: goto L_0x0010;
        };
    L_0x0010:
        r0 = r4;
    L_0x0011:
        return r0;
    L_0x0012:
        r5 = "ACTIVE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x001a:
        r5 = r0;
        goto L_0x000d;
    L_0x001c:
        r5 = "INACTIVE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0024:
        r5 = r1;
        goto L_0x000d;
    L_0x0026:
        r5 = "IDLE_PENDING";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x002e:
        r5 = r2;
        goto L_0x000d;
    L_0x0030:
        r5 = "SENSING";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0038:
        r5 = r3;
        goto L_0x000d;
    L_0x003a:
        r5 = "LOCATING";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0042:
        r5 = 4;
        goto L_0x000d;
    L_0x0044:
        r5 = "IDLE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x004c:
        r5 = 5;
        goto L_0x000d;
    L_0x004e:
        r5 = "IDLE_MAINTENANCE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0056:
        r5 = 6;
        goto L_0x000d;
    L_0x0058:
        r0 = r1;
        goto L_0x0011;
    L_0x005a:
        r0 = r2;
        goto L_0x0011;
    L_0x005c:
        r0 = r3;
        goto L_0x0011;
    L_0x005e:
        r0 = 4;
        goto L_0x0011;
    L_0x0060:
        r0 = 5;
        goto L_0x0011;
    L_0x0062:
        r0 = 6;
        goto L_0x0011;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.evenwell.powersaving.g3.e.doze.DozeStatus.stringToState(java.lang.String):int");
    }

    public static String lightStateToString(int state) {
        switch (state) {
            case 0:
                return "ACTIVE";
            case 1:
                return "INACTIVE";
            case 3:
                return "PRE_IDLE";
            case 4:
                return "IDLE";
            case 5:
                return "WAITING_FOR_NETWORK";
            case 6:
                return "IDLE_MAINTENANCE";
            case 7:
                return "OVERRIDE";
            default:
                return Integer.toString(state);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int stringToLightState(java.lang.String r6) {
        /*
        r3 = 4;
        r2 = 3;
        r1 = 1;
        r0 = 0;
        r4 = -1;
        r5 = r6.hashCode();
        switch(r5) {
            case -482847728: goto L_0x0026;
            case -164102392: goto L_0x0044;
            case 2242516: goto L_0x0030;
            case 16877926: goto L_0x003a;
            case 807292011: goto L_0x001c;
            case 1312623564: goto L_0x004e;
            case 1925346054: goto L_0x0012;
            default: goto L_0x000c;
        };
    L_0x000c:
        r5 = r4;
    L_0x000d:
        switch(r5) {
            case 0: goto L_0x0011;
            case 1: goto L_0x0058;
            case 2: goto L_0x005a;
            case 3: goto L_0x005c;
            case 4: goto L_0x005e;
            case 5: goto L_0x0060;
            case 6: goto L_0x0062;
            default: goto L_0x0010;
        };
    L_0x0010:
        r0 = r4;
    L_0x0011:
        return r0;
    L_0x0012:
        r5 = "ACTIVE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x001a:
        r5 = r0;
        goto L_0x000d;
    L_0x001c:
        r5 = "INACTIVE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0024:
        r5 = r1;
        goto L_0x000d;
    L_0x0026:
        r5 = "PRE_IDLE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x002e:
        r5 = 2;
        goto L_0x000d;
    L_0x0030:
        r5 = "IDLE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0038:
        r5 = r2;
        goto L_0x000d;
    L_0x003a:
        r5 = "WAITING_FOR_NETWORK";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0042:
        r5 = r3;
        goto L_0x000d;
    L_0x0044:
        r5 = "IDLE_MAINTENANCE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x004c:
        r5 = 5;
        goto L_0x000d;
    L_0x004e:
        r5 = "OVERRIDE";
        r5 = r6.equals(r5);
        if (r5 == 0) goto L_0x000c;
    L_0x0056:
        r5 = 6;
        goto L_0x000d;
    L_0x0058:
        r0 = r1;
        goto L_0x0011;
    L_0x005a:
        r0 = r2;
        goto L_0x0011;
    L_0x005c:
        r0 = r3;
        goto L_0x0011;
    L_0x005e:
        r0 = 5;
        goto L_0x0011;
    L_0x0060:
        r0 = 6;
        goto L_0x0011;
    L_0x0062:
        r0 = 7;
        goto L_0x0011;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.evenwell.powersaving.g3.e.doze.DozeStatus.stringToLightState(java.lang.String):int");
    }

    public int getDeepDozeStatus() {
        String cmd = "dumpsys deviceidle get deep";
        return DozeStatus.stringToState(getDozeStatus("dumpsys deviceidle get deep"));
    }

    public int getLightDozeStatus() {
        String cmd = "dumpsys deviceidle get light";
        return DozeStatus.stringToLightState(getDozeStatus("dumpsys deviceidle get light"));
    }

    private String getDozeStatus(String cmd) {
        Exception e;
        Throwable th;
        String line = "";
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(cmd).getInputStream()));
            try {
                line = reader2.readLine();
                PSUtils.closeSilently(reader2);
                reader = reader2;
            } catch (Exception e2) {
                e = e2;
                reader = reader2;
                try {
                    Log.e(TAG, "Happen exception", e);
                    PSUtils.closeSilently(reader);
                    return line;
                } catch (Throwable th2) {
                    th = th2;
                    PSUtils.closeSilently(reader);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                PSUtils.closeSilently(reader);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "Happen exception", e);
            PSUtils.closeSilently(reader);
            return line;
        }
        return line;
    }
}
