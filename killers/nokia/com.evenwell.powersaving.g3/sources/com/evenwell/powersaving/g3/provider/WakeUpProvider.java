package com.evenwell.powersaving.g3.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.provider.BackDataDb.SaveData;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import java.util.List;

public class WakeUpProvider extends ContentProvider {
    private static final int ALLPACKAGE = 1;
    private static final int SERVICE_RESTART = 3;
    private static final int SINGLEPACKAGE = 2;
    static final String TAG = "[PowerSavingAppG3]WakeUpProvider";
    public static String URI = "com.evenwell.powersaving.g3.wakeupprovider";
    private static final UriMatcher sMatcher = new UriMatcher(-1);
    BackDataDb mDb;

    static {
        sMatcher.addURI(URI, PushMessageContract.MESSAGE_KEY_PACKAGE_NAME, 1);
        sMatcher.addURI(URI, "package/#", 2);
        sMatcher.addURI(URI, "service_restart", 3);
    }

    public int delete(Uri arg0, String arg1, String[] arg2) {
        switch (sMatcher.match(arg0)) {
            case 3:
                int delCount = this.mDb.deleteFromTimeStampToServiceRestartTable();
                if (delCount > 0) {
                    return delCount;
                }
                break;
        }
        return 0;
    }

    public String getType(Uri arg0) {
        return null;
    }

    public synchronized Uri insert(Uri uri, ContentValues values) {
        long id;
        Log.i(TAG, "[WakeUpProvider] insert()  ");
        List<String> list = BackgroundPolicyExecutor.getInstance(getContext()).getWhiteListApp();
        Log.i(TAG, "[WakeUpProvider] list () : " + list.toString());
        if (list.contains(values.getAsString("pkg_name"))) {
            Log.i(TAG, "[WakeUpProvider] need refresh " + values.getAsString("pkg_name"));
            values.put(SaveData.IS_FORBIDDEN, Boolean.valueOf(false));
        }
        id = this.mDb.addWakeUpInfo(values);
        if (id > 0) {
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    public boolean onCreate() {
        this.mDb = new BackDataDb(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (sMatcher.match(uri)) {
            case 1:
                return this.mDb.queryWakeUpInfo(projection, selection, selectionArgs, sortOrder);
            case 2:
                long id = ContentUris.parseId(uri);
                if (selection == null) {
                    selection = "_id=" + id;
                } else {
                    selection = selection + " and _id=" + id;
                }
                return this.mDb.queryWakeUpInfo(projection, selection, selectionArgs, sortOrder);
            case 3:
                return this.mDb.queryTimeStampToServiceRestartTable(projection, selection, selectionArgs, sortOrder);
            default:
                throw new IllegalArgumentException("Unknow Uri:" + uri);
        }
    }

    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        return 0;
    }
}
