package com.evenwell.powersaving.g3.aplifecycle;

import android.app.Application;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.utils.PSConst.FILENAME;
import java.io.File;

public class PSApplication extends Application {
    private static String TAG = "PSApplication";

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        String[] blackListFilePathCandidates = getResources().getStringArray(C0321R.array.black_list_file_path_candidates);
        String[] cfgFilePathCandidates = getResources().getStringArray(C0321R.array.cfg_file_path_candidates);
        String[] productCfgFilePathCandidates = getResources().getStringArray(C0321R.array.product_cfg_file_path_candidates);
        FILENAME.POWER_SAVING_DEFAULT_BLACK_LIST = findFilePathFromCandidates(blackListFilePathCandidates, FILENAME.POWER_SAVING_DEFAULT_BLACK_LIST);
        FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_CFG_FILE = findFilePathFromCandidates(cfgFilePathCandidates, FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_CFG_FILE);
        FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_PRODUCT_CFG_FILE = findFilePathFromCandidates(productCfgFilePathCandidates, FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_PRODUCT_CFG_FILE);
        Log.i(TAG, "PSConst.FILENAME.POWER_SAVING_DEFAULT_BLACK_LIST = " + FILENAME.POWER_SAVING_DEFAULT_BLACK_LIST);
        Log.i(TAG, "PSConst.FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_CFG_FILE = " + FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_CFG_FILE);
        Log.i(TAG, "PSConst.FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_PRODUCT_CFG_FILE = " + FILENAME.POWER_SAVING_DEFAULT_EXTERNAL_PRODUCT_CFG_FILE);
    }

    private String findFilePathFromCandidates(String[] candidates, String defaultValue) {
        String filePath = defaultValue;
        for (String candidate : candidates) {
            if (new File(candidate).exists()) {
                return candidate;
            }
        }
        return filePath;
    }
}
