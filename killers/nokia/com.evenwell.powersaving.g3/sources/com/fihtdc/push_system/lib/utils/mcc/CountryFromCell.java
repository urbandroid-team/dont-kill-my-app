package com.fihtdc.push_system.lib.utils.mcc;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.List;

public class CountryFromCell {
    private static final String TAG = "CountryFromCell";

    public static String getCountry(Context context) {
        String country = null;
        try {
            country = new MccTable().countryCodeForMcc(getCellMcc(context));
            Log.i(TAG, "Country code=" + country);
            if (country == null || country.isEmpty()) {
                Log.v(TAG, "Can not get country code from Cell info!!");
                return country;
            }
            Log.d(TAG, "We get the country code!!! " + country);
            return country;
        } catch (SecurityException e) {
            Log.i(TAG, "Get country code from cell error: " + e.getMessage());
        } catch (Exception e2) {
            Log.w(TAG, "Get country code from cell error: " + e2.getMessage());
        }
    }

    private static int getCellMcc(Context context) {
        int Result = -1;
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        List<CellInfo> cellList = tm.getAllCellInfo();
        while (cellList != null && !cellList.isEmpty()) {
            CellInfo info = (CellInfo) cellList.remove(0);
            Log.d(TAG, "getCellMcc info=" + info);
            if (tm.getPhoneType() == 1) {
                if (info instanceof CellInfoGsm) {
                    Result = ((CellInfoGsm) info).getCellIdentity().getMcc();
                } else if (info instanceof CellInfoWcdma) {
                    Result = ((CellInfoWcdma) info).getCellIdentity().getMcc();
                } else if (info instanceof CellInfoLte) {
                    Result = ((CellInfoLte) info).getCellIdentity().getMcc();
                }
                Log.i(TAG, "getCellMcc countryCode=" + Result);
            }
        }
        return Result;
    }
}
