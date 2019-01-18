package com.evenwell.powersaving.g3.exception;

import android.util.Log;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.REGISTER_DEVICE_KEY;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HighPower {
    private static final String TAG = "HighPower";
    private HighPowerList mHighPowerList;

    public static class HighPowerList {
        public HashMap<String, List<String>> backgroundHashMap = new HashMap();
        public HashMap<String, List<String>> foregroundHashMap = new HashMap();
        public List<String> version = new ArrayList();
    }

    public HighPower(File file) {
        this.mHighPowerList = readHighPowerList(file);
        Log.i(TAG, "mHighPowerList.version : " + this.mHighPowerList.version.toString() + ",mHighPowerList.BackgroundHashMap : " + this.mHighPowerList.backgroundHashMap.toString() + ",mHighPowerList.ForegroundHashMap : " + this.mHighPowerList.foregroundHashMap.toString());
    }

    private HighPowerList readHighPowerList(File file) {
        Exception e;
        Throwable th;
        HighPowerList highPowerList = new HighPowerList();
        if (file.exists()) {
            Log.i(TAG, "readHighPowerList file: " + file.getPath());
            BufferedReader reader = null;
            InputStream inputStream = null;
            try {
                InputStream inputStream2 = new FileInputStream(file);
                try {
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2));
                    try {
                        StringBuilder sb = new StringBuilder();
                        while (true) {
                            String line = reader2.readLine();
                            if (line == null) {
                                break;
                            }
                            sb.append(line);
                        }
                        Log.i(TAG, "readBlackList file: " + sb.toString());
                        JSONObject json = new JSONObject(sb.toString());
                        JSONArray jsonArrayBackground = null;
                        JSONArray jsonArrayForeground = null;
                        try {
                            jsonArrayBackground = json.getJSONArray("Background");
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        try {
                            jsonArrayBackground = json.getJSONArray("background");
                        } catch (JSONException ex2) {
                            ex2.printStackTrace();
                        }
                        try {
                            jsonArrayForeground = json.getJSONArray("Foreground");
                        } catch (JSONException ex22) {
                            ex22.printStackTrace();
                        }
                        try {
                            jsonArrayForeground = json.getJSONArray("foreground");
                        } catch (JSONException ex222) {
                            ex222.printStackTrace();
                        }
                        JSONArray jsonArrayVersion = json.getJSONArray(REGISTER_DEVICE_KEY.version);
                        if (jsonArrayBackground == null) {
                            jsonArrayBackground = new JSONArray();
                        }
                        if (jsonArrayForeground == null) {
                            jsonArrayForeground = new JSONArray();
                        }
                        readJsonArrayToMap(jsonArrayForeground, highPowerList.foregroundHashMap);
                        readJsonArrayToMap(jsonArrayBackground, highPowerList.backgroundHashMap);
                        for (int index = 0; index < jsonArrayVersion.length(); index++) {
                            highPowerList.version.add(jsonArrayVersion.getString(index));
                        }
                        PSUtils.closeSilently(inputStream2);
                        PSUtils.closeSilently(reader2);
                        inputStream = inputStream2;
                        reader = reader2;
                    } catch (Exception e2) {
                        e = e2;
                        inputStream = inputStream2;
                        reader = reader2;
                        try {
                            Log.e(TAG, "readBlackList Happen exception", e);
                            PSUtils.closeSilently(inputStream);
                            PSUtils.closeSilently(reader);
                            return highPowerList;
                        } catch (Throwable th2) {
                            th = th2;
                            PSUtils.closeSilently(inputStream);
                            PSUtils.closeSilently(reader);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        inputStream = inputStream2;
                        reader = reader2;
                        PSUtils.closeSilently(inputStream);
                        PSUtils.closeSilently(reader);
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    inputStream = inputStream2;
                    Log.e(TAG, "readBlackList Happen exception", e);
                    PSUtils.closeSilently(inputStream);
                    PSUtils.closeSilently(reader);
                    return highPowerList;
                } catch (Throwable th4) {
                    th = th4;
                    inputStream = inputStream2;
                    PSUtils.closeSilently(inputStream);
                    PSUtils.closeSilently(reader);
                    throw th;
                }
            } catch (Exception e4) {
                e = e4;
                Log.e(TAG, "readBlackList Happen exception", e);
                PSUtils.closeSilently(inputStream);
                PSUtils.closeSilently(reader);
                return highPowerList;
            }
        }
        Log.i(TAG, "high power list," + file.getPath() + " does not exist!");
        return highPowerList;
    }

    private void readJsonArrayToMap(JSONArray jsonArray, HashMap<String, List<String>> hashMap) {
        try {
            String symbol = "@";
            for (int index = 0; index < jsonArray.length(); index++) {
                String[] array = jsonArray.getString(index).split(symbol);
                String packageName = array[0].trim();
                List<String> versionList = (List) hashMap.get(packageName);
                if (versionList == null) {
                    versionList = new ArrayList();
                }
                String version = "";
                if (array.length == 2) {
                    version = array[1].trim();
                    versionList.add(version);
                }
                versionList.add(version);
                hashMap.put(packageName, versionList);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean APInHighPowerList(String packageName, String packageVersion) {
        return APInHighPowerList(this.mHighPowerList.backgroundHashMap, packageName, packageVersion) || APInHighPowerList(this.mHighPowerList.foregroundHashMap, packageName, packageVersion);
    }

    private boolean APInHighPowerList(HashMap<String, List<String>> hashMap, String packageName, String packageVersion) {
        if (hashMap.containsKey(packageName)) {
            List<String> appVersions = (List) hashMap.get(packageName);
            if (appVersions != null && appVersions.contains(packageVersion)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNewerVersion(HighPower rhs) {
        String symbolToSplitMainVerion = "_";
        String symbolToSplitSubVerion = "\\.";
        try {
            String[] lhsVersion = ((String) this.mHighPowerList.version.get(0)).split(symbolToSplitMainVerion)[0].split(symbolToSplitSubVerion);
            int lhsYear = Integer.valueOf(lhsVersion[0].substring(1, lhsVersion[0].length())).intValue();
            int lhsDay = Integer.valueOf(lhsVersion[1]).intValue();
            int lhsSerialNo = Integer.valueOf(lhsVersion[2]).intValue();
            String[] rhsVersion = ((String) rhs.mHighPowerList.version.get(0)).split(symbolToSplitMainVerion)[0].split(symbolToSplitSubVerion);
            int rhsYear = Integer.valueOf(rhsVersion[0].substring(1, rhsVersion[0].length())).intValue();
            int rhsDay = Integer.valueOf(rhsVersion[1]).intValue();
            int rhsSerialNo = Integer.valueOf(rhsVersion[2]).intValue();
            Log.d(TAG, "lhsYear = " + lhsYear + ",lhsDay = " + lhsDay + ",lhsSerialNo = " + lhsSerialNo + ",rhsYear = " + rhsYear + ",rhsDay = " + rhsDay + ",rhsSerialNo = " + rhsSerialNo);
            if (lhsYear < rhsYear || lhsDay < rhsDay || lhsSerialNo < rhsSerialNo) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }
}
