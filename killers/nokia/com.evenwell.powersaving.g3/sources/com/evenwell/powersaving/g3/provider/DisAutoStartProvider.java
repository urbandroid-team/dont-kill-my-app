package com.evenwell.powersaving.g3.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.background.BackgroundCleanUtil;
import com.evenwell.powersaving.g3.background.CheckDBService;
import com.evenwell.powersaving.g3.exception.BMS;
import com.evenwell.powersaving.g3.exception.BackgroundPolicyExecutor;
import com.evenwell.powersaving.g3.utils.PSConst.SERVICE_START.PSSPREF;
import com.fihtdc.push_system.lib.common.PushMessageContract;
import java.util.List;

public class DisAutoStartProvider extends ContentProvider {
    private static final int ALLPACKAGE = 1;
    private static final int ALLPACKAGE_EXCLUDE_WIDGETAPP = 5;
    private static final int BOOTPACKAGE = 3;
    private static final boolean DBG = true;
    private static final int SINGLEPACKAGE = 2;
    private static final String TAG = "[PowerSavingAppG3]DisAutoStartProvider";
    public static final String TB_NAME_CONFIG = "pkg_config";
    public static final String TB_NAME_DISAUTO = "disauto_app";
    public static final String TB_NAME_KILLED = "clean_app";
    public static final String TB_NAME_WAKEUP = "wake_up";
    public static String URI = "com.evenwell.powersaving.g3.disautoprovider";
    private static final int WHITELISTPACKAGE = 4;
    public static boolean isCheckDB = false;
    private static final UriMatcher sMatcher = new UriMatcher(-1);
    BackDataDb mDb;

    static {
        sMatcher.addURI(URI, PushMessageContract.MESSAGE_KEY_PACKAGE_NAME, 1);
        sMatcher.addURI(URI, "package/#", 2);
        sMatcher.addURI(URI, "boot", 3);
        sMatcher.addURI(URI, "whitelist", 4);
        sMatcher.addURI(URI, "package_exclude_widgetapp", 5);
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public boolean onCreate() {
        Context ctx = getContext();
        Log.i(TAG, "onCreate,isBootCompleteReceive : " + PowerSavingUtils.GetPreferencesStatus(ctx, PSSPREF.IS_BOOT_COMPLETE));
        this.mDb = new BackDataDb(ctx);
        ctx.startService(new Intent(ctx, CheckDBService.class));
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor matrixCursor;
        int i;
        String pkgName;
        Integer[] numArr;
        int i2;
        switch (sMatcher.match(uri)) {
            case 1:
                return this.mDb.queryDisAutoStartPkg(projection, selection, selectionArgs, sortOrder);
            case 2:
                long id = ContentUris.parseId(uri);
                if (selection == null) {
                    selection = "_id=" + id;
                } else {
                    selection = selection + " and _id=" + id;
                }
                return this.mDb.queryDisAutoStartPkg(projection, selection, selectionArgs, sortOrder);
            case 3:
                List<String> boots;
                if (BackgroundPolicyExecutor.getInstance(getContext()).isCNModel()) {
                    boots = BackgroundPolicyExecutor.getInstance(getContext()).getWhiteListApp(4);
                } else {
                    boots = BackgroundPolicyExecutor.getInstance(getContext()).getWhiteListApp(1);
                }
                if (boots == null) {
                    return null;
                }
                matrixCursor = new MatrixCursor(new String[]{"_id", "pkgName"});
                for (i = 0; i < boots.size(); i++) {
                    matrixCursor.addRow(new Object[]{Integer.valueOf(i + 1), boots.get(i)});
                }
                return matrixCursor;
            case 4:
                if (!BMS.getInstance(getContext()).getBMSValue()) {
                    return null;
                }
                Cursor cursor = this.mDb.queryWhiteList(projection, selection, selectionArgs, sortOrder);
                if (cursor == null) {
                    return null;
                }
                matrixCursor = new MatrixCursor(new String[]{"_id", "pkg_name"});
                i = 1;
                while (cursor.moveToNext()) {
                    pkgName = cursor.getString(cursor.getColumnIndex("pkg_name"));
                    if (!PowerSavingUtils.isAppHideOnBamUi(getContext(), pkgName)) {
                        numArr = new Object[2];
                        i2 = i + 1;
                        numArr[0] = Integer.valueOf(i);
                        numArr[1] = pkgName;
                        matrixCursor.addRow(numArr);
                        i = i2;
                    }
                }
                cursor.close();
                return matrixCursor;
            case 5:
                Cursor cAll = this.mDb.queryDisAutoStartPkg(projection, selection, selectionArgs, sortOrder);
                if (cAll == null) {
                    return null;
                }
                matrixCursor = new MatrixCursor(new String[]{"_id", "pkg_name"});
                List<String> widgetPkgs = BackgroundCleanUtil.getWidgetPackageName(BackgroundCleanUtil.getDefaultLauncher(getContext().getPackageManager()));
                i = 1;
                while (cAll.moveToNext()) {
                    pkgName = cAll.getString(cAll.getColumnIndex("pkg_name"));
                    if (!widgetPkgs.contains(pkgName)) {
                        numArr = new Object[2];
                        i2 = i + 1;
                        numArr[0] = Integer.valueOf(i);
                        numArr[1] = pkgName;
                        matrixCursor.addRow(numArr);
                        i = i2;
                    }
                }
                cAll.close();
                return matrixCursor;
            default:
                throw new IllegalArgumentException("Unknow Uri:" + uri);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
