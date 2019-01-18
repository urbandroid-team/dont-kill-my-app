package com.evenwell.powersaving.g3.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class ProcessMonitorProvider extends ContentProvider {
    private static final boolean DBG = true;
    private static final int FORCESTOP_LIST = 2;
    private static final int PROCESS_MONITOR = 1;
    private static final String TAG = "[PowerSavingAppG3]ProcessMonitorProvider";
    public static String URI = "com.evenwell.powersaving.g3.ProcessMonitorProvider";
    private static final UriMatcher sMatcher = new UriMatcher(-1);
    private ProcessMonitorDB mDb;

    static {
        sMatcher.addURI(URI, "ProcessMonitor", 1);
        sMatcher.addURI(URI, "ProcessWasForcestopped", 2);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int delCount;
        switch (sMatcher.match(uri)) {
            case 1:
                delCount = this.mDb.deleteAllFromProcessMonitor();
                if (delCount > 0) {
                    return delCount;
                }
                break;
            case 2:
                break;
        }
        delCount = this.mDb.deleteFromForceStopAppList();
        if (delCount > 0) {
            return delCount;
        }
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public boolean onCreate() {
        this.mDb = new ProcessMonitorDB(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sMatcher.match(uri)) {
            case 1:
                return this.mDb.queryPackageFromProcessMonitor(projection, selection, selectionArgs, sortOrder);
            case 2:
                return this.mDb.queryPackagesWereForceStop(projection, selection, selectionArgs, sortOrder);
            default:
                throw new IllegalArgumentException("Unknow Uri:" + uri);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
