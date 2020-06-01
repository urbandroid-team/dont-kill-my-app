package com.fihtdc.asyncservice;

import android.os.Bundle;

public class BaseRequestListener implements RequestListener {
    private static final String TAG = "BackupRestoreService/BaseRequestListener";

    public void onStart(Object task) {
    }

    public void onHandle(Object task) {
    }

    public void onComplete(Object task) {
    }

    public void onException(Object task, Throwable e) {
        LogUtils.logD(TAG, "onException()");
        if (e instanceof RuntimeException) {
            throw ((RuntimeException) e);
        }
        throw new RuntimeException(e);
    }

    public void updateProgress(int progress) {
    }

    public void updateProgress(int progress, Bundle progressInfo) {
    }
}
