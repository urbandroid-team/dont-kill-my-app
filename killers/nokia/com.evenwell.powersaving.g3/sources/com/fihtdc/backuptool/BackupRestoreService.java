package com.fihtdc.backuptool;

import android.os.Bundle;
import com.fihtdc.asyncservice.RequestService;

public abstract class BackupRestoreService extends RequestService {
    protected static final String TAG = "BackupRestoreService/BackupRestoreService";

    public abstract Bundle backup(Bundle bundle);

    public abstract Bundle cancel(Bundle bundle);

    public abstract Bundle checkPermission(Bundle bundle);

    public abstract Bundle isBackedUp(Bundle bundle);

    public abstract Bundle restore(Bundle bundle);

    protected void handleRequest(Bundle task) {
        if (BackupTool.containsZipPassword(task)) {
            ZipUtils.setPassword(BackupTool.getZipPassword(task));
        }
        super.handleRequest(task);
        if (BackupTool.containsZipPassword(task)) {
            ZipUtils.removePassword();
        }
    }
}
