package com.evenwell.powersaving.g3.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class PowerSavingProvider extends ContentProvider {
    public static final String AUTHORITY = "com.evenwell.powersaving.g3";
    public static final Uri AUTHORITY_URI = Uri.parse("content://com.evenwell.powersaving.g3");
    public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, SETTINGS_TABLE_NAME);
    private static final boolean DBG = true;
    public static final String SETTINGS_TABLE_NAME = "settings";
    private static final String TAG = "PowerSavingProvider";
    private SQLiteDatabase db;
    private SettingsDatabaseHelper mSettingsDatabaseHelper;

    public interface SettingsColumns {
        public static final String NAME = "name";
        public static final String VALUE = "value";
        public static final String _ID = "_id";
    }

    static final class SettingsDatabaseHelper extends SQLiteOpenHelper {
        static final String DATABASE_NAME = "power_saving_settings.db";
        static final int DATABASE_VERSION = 1;

        public SettingsDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, 1);
        }

        public void onCreate(SQLiteDatabase db) {
            Log.i(PowerSavingProvider.TAG, "onCreate tables.");
            db.execSQL("CREATE TABLE settings(_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,value TEXT NOT NULL);");
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(PowerSavingProvider.TAG, "onUpgrade oldVersion: " + oldVersion + ", newVersion: " + newVersion);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(PowerSavingProvider.TAG, "onDowngrade oldVersion: " + oldVersion + ", newVersion: " + newVersion);
        }
    }

    public boolean onCreate() {
        Log.i(TAG, "onCreate()");
        Context context = getContext();
        if (this.mSettingsDatabaseHelper == null) {
            Log.i(TAG, "onCreate() - Generate database helper object.");
            this.mSettingsDatabaseHelper = new SettingsDatabaseHelper(context);
        } else {
            Log.i(TAG, "onCreate() - Use previous database helper object.");
        }
        this.db = this.mSettingsDatabaseHelper.getWritableDatabase();
        if (this.db == null) {
            return false;
        }
        return true;
    }

    public Uri insert(Uri uri, ContentValues values) {
        Context context = getContext();
        if (this.db == null) {
            Log.i(TAG, "insert() db is null & need to re-get it");
            if (this.mSettingsDatabaseHelper == null) {
                Log.i(TAG, "insert() database helper is null & need to re-get it");
                this.mSettingsDatabaseHelper = new SettingsDatabaseHelper(context);
            }
            this.db = this.mSettingsDatabaseHelper.getWritableDatabase();
        }
        long rowID = this.db.insert(SETTINGS_TABLE_NAME, null, values);
        if (rowID >= 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            Log.i(TAG, "insert() _uri:" + _uri.toString());
            return _uri;
        }
        Log.i(TAG, "Failed to add a record into " + uri.toString());
        throw new SQLException("Failed to add a record into " + uri);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i(TAG, "delete()");
        throw new UnsupportedOperationException();
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Context context = getContext();
        if (this.db == null) {
            Log.i(TAG, "query() db is null & need to re-get it");
            if (this.mSettingsDatabaseHelper == null) {
                Log.i(TAG, "query() database helper is null & need to re-get it");
                this.mSettingsDatabaseHelper = new SettingsDatabaseHelper(context);
            }
            this.db = this.mSettingsDatabaseHelper.getWritableDatabase();
        }
        int count = this.db.update(SETTINGS_TABLE_NAME, values, selection, selectionArgs);
        Log.i(TAG, "update uri " + uri.toString() + " count:" + count);
        if (count > 0) {
            context.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Context context = getContext();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(SETTINGS_TABLE_NAME);
        if (this.db == null) {
            Log.i(TAG, "query() db is null & need to re-get it");
            if (this.mSettingsDatabaseHelper == null) {
                Log.i(TAG, "query() database helper is null & need to re-get it");
                this.mSettingsDatabaseHelper = new SettingsDatabaseHelper(context);
            }
            this.db = this.mSettingsDatabaseHelper.getWritableDatabase();
        }
        Cursor cursor = qb.query(this.db, projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }
        return cursor;
    }

    public String getType(Uri uri) {
        Log.i(TAG, "getType()");
        return "";
    }

    public static String getAuthority(Context context) {
        Log.i(TAG, "getAuthority()");
        return context.getPackageName() + ".provider";
    }

    public static Uri getUriFor(String name) {
        return Uri.withAppendedPath(CONTENT_URI, name);
    }

    public static Uri getUriFor(Uri uri, String name) {
        return Uri.withAppendedPath(uri, name);
    }
}
