package com.evenwell.powersaving.g3.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.provider.BackDataDb.DatabaseHelper;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WhiteListProvider extends ContentProvider {
    private static final int ALARM_IN_DOZE = 7;
    private static final boolean DBG = true;
    private static final int DISAUTO_WHITE_LIST = 8;
    private static final int HIDE_IN_WHITE_LIST = 5;
    private static final String TAG = "[PowerSavingAppG3]WhiteListProvider";
    public static final String URI = "com.evenwell.powersaving.g3.whitelistprovider";
    private static final int WHITELISTPACKAGE = 4;
    private static final UriMatcher sMatcher = new UriMatcher(-1);
    private List<String> hideNonSystemAppList;
    BackDataDb mDb;
    private DatabaseHelper mHelper = null;

    static {
        sMatcher.addURI(URI, "whitelist", 4);
        sMatcher.addURI(URI, "hideWhiteList", 5);
        sMatcher.addURI(URI, BackDataDb.TB_NAME_ALARM_IN_DOZE, 7);
        sMatcher.addURI(URI, "disautoWhiteList", 8);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.mHelper.getWritableDatabase();
        int delCount = 0;
        switch (sMatcher.match(uri)) {
            case 7:
                delCount = db.delete(BackDataDb.TB_NAME_ALARM_IN_DOZE, selection, selectionArgs);
                break;
        }
        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        long id = -1;
        SQLiteDatabase db = this.mHelper.getWritableDatabase();
        switch (sMatcher.match(uri)) {
            case 7:
                id = db.insert(BackDataDb.TB_NAME_ALARM_IN_DOZE, null, values);
                break;
        }
        return getUriForId(id, uri);
    }

    public int bulkInsert(Uri uri, ContentValues[] values) {
        int insertCount = 0;
        SQLiteDatabase db = this.mHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            switch (sMatcher.match(uri)) {
                case 7:
                    for (ContentValues value : values) {
                        if (db.insert(BackDataDb.TB_NAME_ALARM_IN_DOZE, null, value) > 0) {
                            insertCount++;
                        }
                    }
                    break;
            }
            db.setTransactionSuccessful();
            db.endTransaction();
            if (insertCount > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return insertCount;
        } catch (Throwable th) {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    public boolean onCreate() {
        this.mDb = new BackDataDb(getContext());
        this.mHelper = new DatabaseHelper(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Iterator it;
        switch (sMatcher.match(uri)) {
            case 4:
                this.hideNonSystemAppList = BackgroundPolicyExecutor.getInstance(getContext()).getHideNonSystemAppList();
                List<String> whiteListApp = BackgroundPolicyExecutor.getInstance(getContext()).getWhiteListApp();
                MatrixCursor matrixCursor = new MatrixCursor(new String[]{"pkg_name"});
                for (String pkg : whiteListApp) {
                    if (!this.hideNonSystemAppList.contains(pkg)) {
                        matrixCursor.addRow(new Object[]{(String) it.next()});
                    }
                }
                return matrixCursor;
            case 5:
                List<String> ret = new ArrayList();
                ret = PSUtils.getSystemApps(getContext());
                MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{"pkgName"});
                if (BackgroundPolicyExecutor.getInstance(getContext()).isCNModel()) {
                    List<String> systemAppisNeedToShow = BackgroundPolicyExecutor.getInstance(getContext()).getSystemAppisNeedToShow();
                    for (String pkg2 : ret) {
                        if (!systemAppisNeedToShow.contains(pkg2)) {
                            matrixCursor2.addRow(new Object[]{(String) it.next()});
                        }
                    }
                } else {
                    it = ret.iterator();
                    while (it.hasNext()) {
                        matrixCursor2.addRow(new Object[]{(String) it.next()});
                    }
                }
                return matrixCursor2;
            case 7:
                SQLiteDatabase db = this.mHelper.getReadableDatabase();
                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
                builder.setTables(BackDataDb.TB_NAME_ALARM_IN_DOZE);
                Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case 8:
                List<String> allAppList = PowerSavingUtils.getAllApList(getContext());
                List<String> disautoList = BackgroundPolicyExecutor.getInstance(getContext()).getDisAutoAppList();
                List<String> systemApp = PSUtils.getSystemApps(getContext());
                this.hideNonSystemAppList = BackgroundPolicyExecutor.getInstance(getContext()).getHideNonSystemAppList();
                MatrixCursor disautoWhiteListCursor = new MatrixCursor(new String[]{"pkg_name"});
                for (String pkg22 : allAppList) {
                    if (!(disautoList.contains(pkg22) || this.hideNonSystemAppList.contains(pkg22))) {
                        disautoWhiteListCursor.addRow(new Object[]{pkg22});
                    }
                }
                return disautoWhiteListCursor;
            default:
                throw new IllegalArgumentException("Unknow Uri:" + uri);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.mHelper.getWritableDatabase();
        int updateCount = 0;
        switch (sMatcher.match(uri)) {
            case 7:
                updateCount = db.update(BackDataDb.TB_NAME_ALARM_IN_DOZE, values, selection, selectionArgs);
                break;
        }
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
