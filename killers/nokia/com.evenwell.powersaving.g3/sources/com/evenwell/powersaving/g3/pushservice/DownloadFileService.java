package com.evenwell.powersaving.g3.pushservice;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import java.io.File;

public class DownloadFileService extends IntentService {
    private static final String TAG = DownloadFileService.class.getSimpleName();
    private static File mSDCardRoot = Environment.getExternalStorageDirectory();

    public DownloadFileService(String name) {
        super("DownloadImageService");
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            intent.getExtras().getString("");
        }
    }
}
