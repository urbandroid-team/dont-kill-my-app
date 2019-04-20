package com.fihtdc.push_system.lib.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.fihtdc.push_system.lib.common.PushUtil;
import com.fihtdc.push_system.lib.utils.mcc.CountryFromCell;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonConfig {
    public static final String DEFAULT_PUSH_SERVER_CN = "https://cn-aps.c2dms.com";
    public static final String DEFAULT_PUSH_SERVER_DEBUG = "http://aps-lab.c2dms.com";
    public static final String DEFAULT_PUSH_SERVER_WORLDWIDE = "https://aps.c2dms.com";
    public static final String PRODUCT_CONFIG_PATH = "/system/etc/EvenwellCloud.config";
    private static final String TAG = "FP819.CommonConfig";

    public static final String getDefaultPushServerAddr(Context context) {
        String serverAddr = DEFAULT_PUSH_SERVER_WORLDWIDE;
        JSONObject config = readConfig();
        if (config != null) {
            try {
                if (config.has("DefaultPushServer") && !TextUtils.isEmpty(config.getString("DefaultPushServer"))) {
                    return config.getString("DefaultPushServer");
                }
                if (config.has("ProduceLocale") && "cn".equalsIgnoreCase(config.getString("ProduceLocale"))) {
                    serverAddr = DEFAULT_PUSH_SERVER_CN;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if ("cn".equals(CountryFromCell.getCountry(context))) {
                serverAddr = DEFAULT_PUSH_SERVER_CN;
            }
        }
        return serverAddr;
    }

    public static final JSONObject readConfig() {
        Throwable th;
        File file = new File(PRODUCT_CONFIG_PATH);
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(file));
            try {
                for (String line = br2.readLine(); line != null; line = br2.readLine()) {
                    sb.append(line);
                }
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (sb.length() == 0) {
                    br = br2;
                    return null;
                }
                try {
                    br = br2;
                    return new JSONObject(sb.toString());
                } catch (JSONException e2) {
                    e2.printStackTrace();
                    br = br2;
                    return null;
                }
            } catch (Exception e3) {
                br = br2;
                try {
                    Log.d(TAG, "readConfig(): cannot read config file, use default. ");
                    if (br != null) {
                        return null;
                    }
                    try {
                        br.close();
                        return null;
                    } catch (IOException e4) {
                        e4.printStackTrace();
                        return null;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e42) {
                            e42.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                br = br2;
                if (br != null) {
                    br.close();
                }
                throw th;
            }
        } catch (Exception e5) {
            Log.d(TAG, "readConfig(): cannot read config file, use default. ");
            if (br != null) {
                return null;
            }
            br.close();
            return null;
        }
    }

    public static String getUserId(Context context) {
        return PushUtil.getDeviceId(context);
    }

    public static String getPassword(Context context) {
        String strPassword = getUserId(context);
        try {
            byte[] aryRegID = strPassword.getBytes("US-ASCII");
            for (int i = 0; i < aryRegID.length; i++) {
                aryRegID[i] = (byte) ((aryRegID[i] >> 2) | (aryRegID[i] << 6));
            }
            strPassword = Base64.encodeToString(aryRegID, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strPassword;
    }
}
