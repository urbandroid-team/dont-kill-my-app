package com.evenwell.powersaving.g3.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class MotionTimeTableProvider extends ContentProvider {
    private static final int ALL = 1;
    private static final boolean DBG = true;
    private static final String TAG = "[PowerSavingAppG3]MotionTimeTableProvider";
    public static String URI = "com.evenwell.powersaving.g3.MotionTimeTableProvider";
    private static final UriMatcher sMatcher = new UriMatcher(-1);
    BackDataDb mDb;

    static {
        sMatcher.addURI(URI, "time", 1);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (sMatcher.match(uri)) {
            case 1:
                int delCount = this.mDb.deleteFromMotionTable();
                if (delCount > 0) {
                    return delCount;
                }
                break;
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
        this.mDb = new BackDataDb(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sMatcher.match(uri)) {
            case 1:
                return this.mDb.queryMotionTable(projection, selection, selectionArgs, sortOrder);
            default:
                throw new IllegalArgumentException("Unknow Uri:" + uri);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
