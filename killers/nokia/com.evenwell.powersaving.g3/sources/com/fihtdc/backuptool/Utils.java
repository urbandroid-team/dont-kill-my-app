package com.fihtdc.backuptool;

import android.os.Environment;
import android.os.StatFs;
import com.fihtdc.asyncservice.LogUtils;
import net2.lingala.zip4j.util.InternalZipConstants;

public class Utils {
    public static boolean isEnoughSpace(long fileSize) {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long sdAvailSize = ((long) stat.getAvailableBlocks()) * ((long) stat.getBlockSize());
        LogUtils.logD("SD SCARD AVALIABLE SPACE", "-->" + sdAvailSize);
        if (sdAvailSize > fileSize) {
            LogUtils.logD("SD SCARD SPACE", "Enough Space: " + fileSize + InternalZipConstants.ZIP_FILE_SEPARATOR + sdAvailSize);
            return true;
        }
        LogUtils.logD("SD SCARD SPACE", "Not Enough Space: " + fileSize + InternalZipConstants.ZIP_FILE_SEPARATOR + sdAvailSize);
        return false;
    }
}
