package com.evenwell.powersaving.g3.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.ArraySet;
import android.util.Log;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.background.CheckDBService;
import com.evenwell.powersaving.g3.utils.PSConst.SYMBOLS;
import com.evenwell.powersaving.g3.utils.PSUtils;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class BackDataDb {
    private static final String CREATE_ALARM_IN_DOZE = "CREATE TABLE IF NOT EXISTS alarm_in_doze(_id integer primary key autoincrement,time varchar,pkg_name varchar,light_doze_status varchar,deep_doze_status varchar,wakeup_alarms integer,non_wakeup_alarms integer,tag varchar)";
    private static final String CREATE_DISAUTO = "CREATE TABLE IF NOT EXISTS disauto_app(_id integer primary key autoincrement,pkg_name varchar unique,dis_boot integer)";
    private static final String CREATE_FORCESTOP_PROCESS_LIST = "CREATE TABLE IF NOT EXISTS forcestop_process_list(_id integer primary key autoincrement,pkg_name varchar,time varchar)";
    private static final String CREATE_MOTION_TABLE = "CREATE TABLE IF NOT EXISTS motion_time_table(_id integer primary key autoincrement,trigger_time varchar)";
    private static final String CREATE_PROT = "CREATE TABLE IF NOT EXISTS prot_app(_id integer primary key autoincrement,pkg_name varchar unique,is_add integer,is_delete integer)";
    private static final String CREATE_RESTART_SERVICE_TIMESTAMP_TABLE = "CREATE TABLE IF NOT EXISTS service_restart_time_table(_id integer primary key autoincrement,service_name varchar,time varchar)";
    private static final String CREATE_STOP_SYNC_ADAPTER_INFO = "CREATE TABLE IF NOT EXISTS stop_sync_apapter_info(_id integer primary key autoincrement,set_stop varchar,sync_adapter_type_info varchar,time varchar)";
    private static final String CREATE_WAKEUP = "CREATE TABLE IF NOT EXISTS wake_up(_id integer primary key autoincrement,pkg_name varchar,call_pkg_name varchar,class_name varchar,call_type varchar,call_num integer default (0),call_time long,is_forbidden integer default (1),forbid_num integer default (0))";
    private static final String CREATE_WHITELIST = "CREATE TABLE IF NOT EXISTS white_list(_id integer primary key autoincrement,pkg_name varchar unique)";
    public static final String FILE_DIS_BOOT_COMPLETE = "disautoboot.xml";
    private static final String SQLITE_NAME = "background_clean.db";
    private static final int SQLITE_VERSION = 13;
    public static final String TAG = "BackDataDb";
    public static final String TB_NAME_ALARM_IN_DOZE = "alarm_in_doze";
    public static final String TB_NAME_ALARM_IN_LIGHT_DOZE = "alarm_in_light_doze";
    public static final String TB_NAME_CONFIG = "pkg_config";
    public static final String TB_NAME_DISAUTO = "disauto_app";
    public static final String TB_NAME_FORCESTOP_PROCESS_LIST = "forcestop_process_list";
    public static final String TB_NAME_KILLED = "clean_app";
    public static final String TB_NAME_MOTION = "motion_time_table";
    public static final String TB_NAME_PROT = "prot_app";
    public static final String TB_NAME_STOP_SYNC_ADAPTER_INFO = "stop_sync_apapter_info";
    public static final String TB_NAME_USER = "pkg_user";
    public static final String TB_NAME_WAKEUP = "wake_up";
    public static final String TB_NAME_WHITELIST = "white_list";
    public static final String TB_SERVICE_RESTART = "service_restart_time_table";
    private DatabaseHelper dbHelper;
    private Context mContext;
    private SQLiteDatabase mDB;
    private DateFormat mdf;
    private int rowCountThreshold = 1500;

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private boolean isCN;
        private Context mContext;

        public DatabaseHelper(Context context) {
            super(context, BackDataDb.SQLITE_NAME, null, 13);
            this.isCN = context.getResources().getBoolean(C0321R.bool.region_cn);
            this.mContext = context;
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(BackDataDb.CREATE_DISAUTO);
            db.execSQL(BackDataDb.CREATE_WAKEUP);
            db.execSQL(BackDataDb.CREATE_PROT);
            db.execSQL(BackDataDb.CREATE_WHITELIST);
            createDefaultWhiteList(db);
            db.execSQL(BackDataDb.CREATE_FORCESTOP_PROCESS_LIST);
            db.execSQL(BackDataDb.CREATE_STOP_SYNC_ADAPTER_INFO);
            db.execSQL(BackDataDb.CREATE_ALARM_IN_DOZE);
            db.execSQL(BackDataDb.CREATE_MOTION_TABLE);
            db.execSQL(BackDataDb.CREATE_RESTART_SERVICE_TIMESTAMP_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int version = oldVersion;
            Log.i(BackDataDb.TAG, "oldVersion = " + oldVersion);
            Log.i(BackDataDb.TAG, "newVersion = " + newVersion);
            db.execSQL("DROP TABLE IF EXISTS clean_app");
            db.execSQL("DROP TABLE IF EXISTS pkg_config");
            if (version < 5) {
                version = 5;
            }
            if (version < 6) {
                if (!this.isCN) {
                    db.execSQL("DROP TABLE IF EXISTS disauto_app");
                    db.execSQL("DROP TABLE IF EXISTS wake_up");
                    db.execSQL(BackDataDb.CREATE_DISAUTO);
                    db.execSQL(BackDataDb.CREATE_WAKEUP);
                }
                version++;
            }
            if (version < 7) {
                version++;
            }
            if (version < 8) {
                Log.i(BackDataDb.TAG, "isTableExists(db, TB_NAME_WHITELIST) = " + isTableExists(db, BackDataDb.TB_NAME_WHITELIST));
                if (!isTableExists(db, BackDataDb.TB_NAME_WHITELIST)) {
                    db.execSQL(BackDataDb.CREATE_WHITELIST);
                    createDefaultWhiteList(db);
                }
                version++;
            }
            if (version < 9) {
                Log.i(BackDataDb.TAG, "isTableExists(db, TB_NAME_FORCESTOP_PROCESS_LIST) = " + isTableExists(db, "forcestop_process_list"));
                if (!isTableExists(db, "forcestop_process_list")) {
                    db.execSQL(BackDataDb.CREATE_FORCESTOP_PROCESS_LIST);
                }
                Log.i(BackDataDb.TAG, "isTableExists(db, TB_NAME_STOP_SYNC_ADAPTER_INFO) = " + isTableExists(db, BackDataDb.TB_NAME_STOP_SYNC_ADAPTER_INFO));
                if (!isTableExists(db, BackDataDb.TB_NAME_STOP_SYNC_ADAPTER_INFO)) {
                    db.execSQL(BackDataDb.CREATE_STOP_SYNC_ADAPTER_INFO);
                }
                version++;
            }
            if (version < 10) {
                version++;
            }
            if (version < 11) {
                db.execSQL("DROP TABLE IF EXISTS alarm_in_light_doze");
                db.execSQL(BackDataDb.CREATE_ALARM_IN_DOZE);
                version++;
            }
            if (version < 12) {
                db.execSQL("DROP TABLE IF EXISTS motion_time_table");
                db.execSQL(BackDataDb.CREATE_MOTION_TABLE);
                version++;
            }
            if (version < 13) {
                db.execSQL("DROP TABLE IF EXISTS service_restart_time_table");
                db.execSQL(BackDataDb.CREATE_RESTART_SERVICE_TIMESTAMP_TABLE);
                version++;
            }
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            this.mContext.deleteDatabase(BackDataDb.SQLITE_NAME);
            Log.d(BackDataDb.TAG, "onDowngrade");
            db.execSQL(BackDataDb.CREATE_DISAUTO);
            db.execSQL(BackDataDb.CREATE_WAKEUP);
            db.execSQL(BackDataDb.CREATE_PROT);
            db.execSQL(BackDataDb.CREATE_WHITELIST);
            createDefaultWhiteList(db);
            db.execSQL(BackDataDb.CREATE_FORCESTOP_PROCESS_LIST);
            db.execSQL(BackDataDb.CREATE_STOP_SYNC_ADAPTER_INFO);
            db.execSQL(BackDataDb.CREATE_ALARM_IN_DOZE);
            db.execSQL(BackDataDb.CREATE_MOTION_TABLE);
            db.execSQL(BackDataDb.CREATE_RESTART_SERVICE_TIMESTAMP_TABLE);
            Intent intent = new Intent(this.mContext, CheckDBService.class);
            intent.setAction(CheckDBService.FORCE_REFRESH);
            this.mContext.startService(intent);
            super.onDowngrade(db, oldVersion, newVersion);
        }

        private void createDefaultWhiteList(SQLiteDatabase db) {
            if (!this.isCN) {
                List<String> apps = PSUtils.getAllApps(this.mContext);
                StringBuilder builder = new StringBuilder();
                for (String app : apps) {
                    builder.append("(\"" + app + "\"),");
                }
                db.execSQL("INSERT INTO white_list ( pkg_name ) VALUES " + builder.substring(0, builder.length() - 1) + SYMBOLS.SEMICOLON);
            }
        }

        private boolean isTableExists(SQLiteDatabase db, String tableName) {
            boolean isExist = false;
            Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    isExist = true;
                }
                cursor.close();
            }
            return isExist;
        }
    }

    public static class SaveData {
        public static final String CALL_NUMBER = "call_num";
        public static final String CALL_PKG_NAME = "call_pkg_name";
        public static final String CALL_TYPE = "call_type";
        public static final String CLASS_NAME = "class_name";
        public static final String DEEP_DOZE_STATUS = "deep_doze_status";
        public static final String DL_CK = "dl_ck";
        public static final String FORBID_NUM = "forbid_num";
        public static final String GPS_CK = "gps_ck";
        public static final String ID = "_id";
        public static final String IS_DELETE = "is_delete";
        public static final String IS_FORBIDDEN = "is_forbidden";
        public static final String IS_USER_ADD = "is_add";
        public static final String LAST_CALL_TIME = "call_time";
        public static final String LIGHT_DOZE_STATUS = "light_doze_status";
        public static final String MUST_DIS_BOOT = "dis_boot";
        public static final String NON_WAKEUP_ALARMS = "non_wakeup_alarms";
        public static final String PKG_NAME = "pkg_name";
        public static final String PL_CK = "pl_ck";
        public static final String SERVICE_NAME = "service_name";
        public static final String SET_STOP = "set_stop";
        public static final String SYNC_ADAPTER_TYPE_INFO = "sync_adapter_type_info";
        public static final String TAG = "tag";
        public static final String TIME = "time";
        public static final String TRIGGER_TIME = "trigger_time";
        public static final String WAKEUP_ALARMS = "wakeup_alarms";
        public boolean isCheckDown;
        public boolean isCheckGps;
        public boolean isCheckPlay;
        public boolean isDelete;
        public String pkgName;
    }

    public BackDataDb(Context context) {
        this.mContext = context;
        this.dbHelper = new DatabaseHelper(context);
        try {
            this.mDB = this.dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mdf = new SimpleDateFormat("yyyyMMddHHmmss");
    }

    public void close() {
        this.mDB.close();
        this.dbHelper.close();
    }

    public List<String> getAllProtectedPkgs() {
        List<String> dataList;
        synchronized (this.dbHelper) {
            Cursor cursor = this.mDB.query(TB_NAME_PROT, new String[]{"pkg_name"}, "is_delete=0", null, null, null, "_id DESC");
            dataList = new ArrayList();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    dataList.add(cursor.getString(cursor.getColumnIndex("pkg_name")));
                }
                cursor.close();
            }
        }
        return dataList;
    }

    public int getUserSetProtectedCnt() {
        int cnt = 0;
        synchronized (this.dbHelper) {
            Cursor cursor = this.mDB.query(TB_NAME_PROT, new String[]{"pkg_name"}, "is_delete=0 and is_add=1", null, null, null, null);
            if (cursor != null) {
                cnt = cursor.getCount();
                cursor.close();
            }
        }
        return cnt;
    }

    public List<String> getUserSetProtectedPkgs() {
        List<String> pkgList;
        synchronized (this.dbHelper) {
            Cursor cursor = this.mDB.query(TB_NAME_PROT, new String[]{"pkg_name"}, "is_delete=0 and is_add=1", null, null, null, null);
            pkgList = new ArrayList();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    pkgList.add(cursor.getString(cursor.getColumnIndex("pkg_name")));
                }
                cursor.close();
            }
        }
        return pkgList;
    }

    public int getUserSetKilledCnt() {
        int cnt = 0;
        synchronized (this.dbHelper) {
            Cursor cursor = this.mDB.query(TB_NAME_PROT, new String[]{"pkg_name"}, "is_delete=1", null, null, null, null);
            if (cursor != null) {
                cnt = cursor.getCount();
                cursor.close();
            }
        }
        return cnt;
    }

    public List<String> getUserSetKilledPkgs() {
        List<String> pkgList;
        synchronized (this.dbHelper) {
            pkgList = new ArrayList();
            Cursor cursor = this.mDB.query(TB_NAME_PROT, new String[]{"pkg_name"}, "is_delete=1", null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    pkgList.add(cursor.getString(cursor.getColumnIndex("pkg_name")));
                }
                cursor.close();
            }
        }
        return pkgList;
    }

    public long savePkgAsProtected(String pkgName, boolean isAuto) {
        long id;
        int i = 0;
        synchronized (this.dbHelper) {
            ContentValues values = new ContentValues();
            values.put("pkg_name", pkgName);
            String str = SaveData.IS_USER_ADD;
            if (!isAuto) {
                i = 1;
            }
            values.put(str, Integer.valueOf(i));
            values.put(SaveData.IS_DELETE, Integer.valueOf(0));
            id = this.mDB.insertWithOnConflict(TB_NAME_PROT, "pkg_name", values, 5);
            Log.d(TAG, "savePkgAsProtected: " + pkgName);
        }
        return id;
    }

    public void saveProPkgsBaseOnConfig(List<String> pkgList) {
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            for (int i = 0; i < pkgList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("pkg_name", (String) pkgList.get(i));
                values.put(SaveData.IS_USER_ADD, Integer.valueOf(0));
                values.put(SaveData.IS_DELETE, Integer.valueOf(0));
                this.mDB.insert(TB_NAME_PROT, null, values);
                Log.d(TAG, "saveProPkgsBaseOnConfig: " + ((String) pkgList.get(i)));
            }
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
    }

    public void deletePkgFromProtected(String pkgName) {
        synchronized (this.dbHelper) {
            ContentValues values = new ContentValues();
            values.put(SaveData.IS_DELETE, Integer.valueOf(1));
            this.mDB.update(TB_NAME_PROT, values, "pkg_name=?", new String[]{pkgName});
            Log.d(TAG, "deletePackage: " + pkgName);
        }
    }

    public void removePkg(String pkgName) {
        synchronized (this.dbHelper) {
            int remove = 3;
            boolean isExit = false;
            Cursor cursor = this.mDB.query(TB_NAME_PROT, new String[]{"pkg_name", SaveData.IS_DELETE}, "pkg_name='" + pkgName + "'", null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    isExit = true;
                    if (cursor.getInt(cursor.getColumnIndex(SaveData.IS_DELETE)) == 0) {
                        remove = 2;
                    } else {
                        remove = 3;
                    }
                }
                cursor.close();
            }
            if (isExit) {
                ContentValues values = new ContentValues();
                values.put(SaveData.IS_DELETE, Integer.valueOf(remove));
                this.mDB.update(TB_NAME_PROT, values, "pkg_name=?", new String[]{pkgName});
                Log.d(TAG, "removePkg: " + pkgName);
            }
        }
    }

    public int addPkg(String pkgName) {
        int i;
        synchronized (this.dbHelper) {
            Cursor cursor = this.mDB.query(TB_NAME_PROT, new String[]{"pkg_name", SaveData.IS_DELETE}, "pkg_name='" + pkgName + "'", null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ContentValues values = new ContentValues();
                    int protect = cursor.getInt(cursor.getColumnIndex(SaveData.IS_DELETE));
                    if (protect != 2) {
                        if (protect == 3) {
                            values.put(SaveData.IS_DELETE, Integer.valueOf(1));
                            this.mDB.update(TB_NAME_PROT, values, "pkg_name=?", new String[]{pkgName});
                            Log.d(TAG, "addPkg: " + pkgName + " as non protect");
                            i = 1;
                            break;
                        }
                    }
                    values.put(SaveData.IS_DELETE, Integer.valueOf(0));
                    this.mDB.update(TB_NAME_PROT, values, "pkg_name=?", new String[]{pkgName});
                    Log.d(TAG, "addPkg: " + pkgName + " as protect");
                    i = 0;
                    break;
                }
                cursor.close();
            }
            i = -1;
        }
        return i;
    }

    public long addToDisAutoStart(String pkgName) {
        long id;
        List<String> widgetPkg = PowerSavingUtils.getWidgetPackageName(PowerSavingUtils.getDefaultLauncher(this.mContext.getPackageManager()));
        synchronized (this.dbHelper) {
            ContentValues values = new ContentValues();
            values.put("pkg_name", pkgName);
            values.put(SaveData.MUST_DIS_BOOT, Boolean.valueOf(!widgetPkg.contains(pkgName)));
            id = this.mDB.insertWithOnConflict("disauto_app", "pkg_name", values, 5);
            Log.d(TAG, "addToDisAutoStart: " + pkgName);
            this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/package/" + id), null);
        }
        return id;
    }

    public void deleteFromDisAutoStart(String pkgName) {
        synchronized (this.dbHelper) {
            Log.d(TAG, "deleteFromDisAutoStart: " + pkgName);
            long id = (long) this.mDB.delete("disauto_app", "pkg_name=?", new String[]{pkgName});
            this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/package/" + pkgName), null);
        }
    }

    public void addToDisAutoStart(List<String> pkgList) {
        List<String> widgetPkg = PowerSavingUtils.getWidgetPackageName(PowerSavingUtils.getDefaultLauncher(this.mContext.getPackageManager()));
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            for (int i = 0; i < pkgList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("pkg_name", (String) pkgList.get(i));
                values.put(SaveData.MUST_DIS_BOOT, Boolean.valueOf(!widgetPkg.contains(pkgList.get(i))));
                this.mDB.insertWithOnConflict("disauto_app", "pkg_name", values, 5);
                Log.d(TAG, "addToDisAutoStart: " + ((String) pkgList.get(i)));
            }
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/package"), null);
    }

    public void deleteFromDisAutoStart(List<String> pkgList) {
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            for (int i = 0; i < pkgList.size(); i++) {
                this.mDB.delete("disauto_app", "pkg_name=?", new String[]{(String) pkgList.get(i)});
            }
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/package"), null);
    }

    public List<String> getAllDisAutoStartPkg() {
        List<String> pkgList;
        synchronized (this.dbHelper) {
            pkgList = new ArrayList();
            Cursor cursor = this.mDB.query("disauto_app", new String[]{"pkg_name"}, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    pkgList.add(cursor.getString(cursor.getColumnIndex("pkg_name")));
                }
                cursor.close();
            }
        }
        return pkgList;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hasDisAutoStartPkg(java.lang.String r13) {
        /*
        r12 = this;
        r9 = 1;
        r10 = 0;
        r11 = r12.dbHelper;
        monitor-enter(r11);
        r0 = r12.mDB;	 Catch:{ all -> 0x0031 }
        r1 = "disauto_app";
        r2 = 1;
        r2 = new java.lang.String[r2];	 Catch:{ all -> 0x0031 }
        r3 = 0;
        r4 = "pkg_name";
        r2[r3] = r4;	 Catch:{ all -> 0x0031 }
        r3 = "pkg_name=?";
        r4 = 1;
        r4 = new java.lang.String[r4];	 Catch:{ all -> 0x0031 }
        r5 = 0;
        r4[r5] = r13;	 Catch:{ all -> 0x0031 }
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = r0.query(r1, r2, r3, r4, r5, r6, r7);	 Catch:{ all -> 0x0031 }
        if (r8 == 0) goto L_0x002e;
    L_0x0022:
        r0 = r8.moveToFirst();	 Catch:{ all -> 0x0031 }
        if (r0 == 0) goto L_0x002b;
    L_0x0028:
        monitor-exit(r11);	 Catch:{ all -> 0x0031 }
        r0 = r9;
    L_0x002a:
        return r0;
    L_0x002b:
        r8.close();	 Catch:{ all -> 0x0031 }
    L_0x002e:
        monitor-exit(r11);	 Catch:{ all -> 0x0031 }
        r0 = r10;
        goto L_0x002a;
    L_0x0031:
        r0 = move-exception;
        monitor-exit(r11);	 Catch:{ all -> 0x0031 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.evenwell.powersaving.g3.provider.BackDataDb.hasDisAutoStartPkg(java.lang.String):boolean");
    }

    public Cursor queryDisAutoStartPkg(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.dbHelper) {
            query = this.mDB.query("disauto_app", projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public Cursor queryWhiteList(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.dbHelper) {
            query = this.mDB.query(TB_NAME_WHITELIST, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public void addAppToWhiteList(String pkgName) {
        synchronized (this.dbHelper) {
            ContentValues values = new ContentValues();
            values.put("pkg_name", pkgName);
            long id = this.mDB.insertWithOnConflict(TB_NAME_WHITELIST, "pkg_name", values, 5);
            Log.d(TAG, "addAppToWhiteList: " + pkgName);
            this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/whitelist/" + pkgName), null);
        }
    }

    public void removeAppFromWhiteList(String pkgName) {
        synchronized (this.dbHelper) {
            Log.d(TAG, "removeAppFromWhiteList: " + pkgName);
            long id = (long) this.mDB.delete(TB_NAME_WHITELIST, "pkg_name=?", new String[]{pkgName});
            this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/whitelist/" + pkgName), null);
        }
    }

    public void addAppToWhiteList(List<String> pkgList) {
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            for (int i = 0; i < pkgList.size(); i++) {
                ContentValues values = new ContentValues();
                values.put("pkg_name", (String) pkgList.get(i));
                this.mDB.insertWithOnConflict(TB_NAME_WHITELIST, "pkg_name", values, 5);
                Log.d(TAG, "addToDisAutoStart: " + ((String) pkgList.get(i)));
            }
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/whitelist"), null);
    }

    public void removeAppFromWhiteList(List<String> pkgList) {
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            for (int i = 0; i < pkgList.size(); i++) {
                this.mDB.delete(TB_NAME_WHITELIST, "pkg_name=?", new String[]{(String) pkgList.get(i)});
            }
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + DisAutoStartProvider.URI + "/whitelist"), null);
    }

    public Set<String> getAllWhiteListPkg() {
        Set<String> pkgList;
        synchronized (this.dbHelper) {
            pkgList = new ArraySet();
            Cursor cursor = this.mDB.query(TB_NAME_WHITELIST, new String[]{"pkg_name"}, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    pkgList.add(cursor.getString(cursor.getColumnIndex("pkg_name")));
                }
                cursor.close();
            }
        }
        return pkgList;
    }

    public long addWakeUpInfo(ContentValues values) {
        long id;
        Log.d(TAG, "addWakeUpInfo:  " + values.getAsString("pkg_name") + "  call package:  " + values.getAsString(SaveData.CALL_PKG_NAME));
        boolean i = false;
        if (values.containsKey(SaveData.IS_FORBIDDEN)) {
            i = values.getAsBoolean(SaveData.IS_FORBIDDEN).booleanValue();
        } else {
            Log.i(TAG, "addWakeUpInfo no IS_FORBIDDEN key ");
        }
        Log.i(TAG, "addWakeUpInfo addWakeUpInfo IS_FORBIDDEN : " + i);
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            Cursor cursor = this.mDB.query("wake_up", new String[]{"_id", "pkg_name", SaveData.CALL_PKG_NAME, SaveData.CALL_NUMBER, SaveData.IS_FORBIDDEN, SaveData.FORBID_NUM}, "pkg_name='" + values.getAsString("pkg_name") + "' and " + SaveData.CALL_PKG_NAME + "='" + values.getAsString(SaveData.CALL_PKG_NAME) + "'", null, null, null, null);
            if (cursor == null || !cursor.moveToNext()) {
                Log.i(TAG, "cursor == null ,isForbidden : ");
                values.put(SaveData.CALL_NUMBER, Integer.valueOf(1));
                values.put(SaveData.FORBID_NUM, Integer.valueOf(1));
                id = this.mDB.insert("wake_up", null, values);
            } else {
                Log.i(TAG, "cursor != null  ");
                id = cursor.getLong(cursor.getColumnIndex("_id"));
                int num = cursor.getInt(cursor.getColumnIndex(SaveData.CALL_NUMBER)) + 1;
                boolean isForbidden = cursor.getInt(cursor.getColumnIndex(SaveData.IS_FORBIDDEN)) == 1;
                int forbid_num = cursor.getInt(cursor.getColumnIndex(SaveData.FORBID_NUM));
                if (isForbidden) {
                    forbid_num++;
                }
                ContentValues newValues = new ContentValues();
                newValues.put(SaveData.CALL_NUMBER, Integer.valueOf(num));
                newValues.put(SaveData.FORBID_NUM, Integer.valueOf(forbid_num));
                newValues.put(SaveData.LAST_CALL_TIME, values.getAsLong(SaveData.LAST_CALL_TIME));
                newValues.put(SaveData.IS_FORBIDDEN, Boolean.valueOf(i));
                this.mDB.update("wake_up", newValues, "_id=" + id, null);
                cursor.close();
            }
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + WakeUpProvider.URI + "/package/" + id), null);
        return id;
    }

    public Cursor queryWakeUpInfo(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.dbHelper) {
            query = this.mDB.query("wake_up", projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public List<WakePathInfo> queryWakeUpInfo() {
        List<WakePathInfo> wakeInfoList = new ArrayList();
        synchronized (this.dbHelper) {
            Cursor cursor = this.mDB.query("wake_up", new String[]{"_id", "pkg_name", SaveData.CALL_PKG_NAME, SaveData.CALL_NUMBER, SaveData.LAST_CALL_TIME, SaveData.IS_FORBIDDEN, SaveData.FORBID_NUM}, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    try {
                        boolean z;
                        WakePathInfo info = new WakePathInfo();
                        info.id = cursor.getLong(cursor.getColumnIndex("_id"));
                        info.mPackageName = cursor.getString(cursor.getColumnIndex("pkg_name"));
                        info.mCallPackageName = cursor.getString(cursor.getColumnIndex(SaveData.CALL_PKG_NAME));
                        info.mWakeTime = cursor.getInt(cursor.getColumnIndex(SaveData.CALL_NUMBER));
                        info.mLastWakeTime = cursor.getLong(cursor.getColumnIndex(SaveData.LAST_CALL_TIME));
                        if (cursor.getInt(cursor.getColumnIndex(SaveData.IS_FORBIDDEN)) == 1) {
                            z = true;
                        } else {
                            z = false;
                        }
                        info.isForbidden = z;
                        info.mForbiddenNum = cursor.getInt(cursor.getColumnIndex(SaveData.FORBID_NUM));
                        wakeInfoList.add(info);
                    } catch (Exception e) {
                        Log.d(TAG, "queryWakeUpInfo Exception e : " + e);
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }
        }
        return wakeInfoList;
    }

    public void setForbidStatu(long id, boolean isForbidden) {
        Log.d(TAG, "setForbidStatu id : " + id + ",isForbidden : " + isForbidden);
        ContentValues newValues = new ContentValues();
        newValues.put(SaveData.IS_FORBIDDEN, Boolean.valueOf(isForbidden));
        this.mDB.update("wake_up", newValues, "_id=" + id, null);
        this.mContext.getContentResolver().notifyChange(Uri.parse("content://" + WakeUpProvider.URI + "/package/" + id), null);
    }

    public long insertProcessWasForceStopped(String pkg) {
        long id;
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            ContentValues value = new ContentValues();
            value.put("pkg_name", pkg);
            value.put("time", this.mdf.format(Calendar.getInstance().getTime()));
            id = this.mDB.insert("forcestop_process_list", null, value);
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        return id;
    }

    public Cursor queryProcessWasForceStopped(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.dbHelper) {
            query = this.mDB.query("forcestop_process_list", projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public int deleteFromForceStopAppList() {
        int id;
        synchronized (this.dbHelper) {
            id = this.mDB.delete("forcestop_process_list", "", null);
        }
        return id;
    }

    public long insertStopSyncAdapterInfo(String setStop, String syncAdapterTypeInfo) {
        long id;
        synchronized (this.dbHelper) {
            this.mDB.beginTransaction();
            ContentValues value = new ContentValues();
            value.put(SaveData.SET_STOP, setStop);
            value.put(SaveData.SYNC_ADAPTER_TYPE_INFO, syncAdapterTypeInfo);
            value.put("time", this.mdf.format(Calendar.getInstance().getTime()));
            id = this.mDB.insert(TB_NAME_STOP_SYNC_ADAPTER_INFO, null, value);
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        return id;
    }

    public Cursor queryStopSyncAdapterInfo(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.dbHelper) {
            query = this.mDB.query(TB_NAME_STOP_SYNC_ADAPTER_INFO, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public int deleteStopSyncAdapterInfo() {
        int id;
        synchronized (this.dbHelper) {
            id = this.mDB.delete(TB_NAME_STOP_SYNC_ADAPTER_INFO, "", null);
        }
        return id;
    }

    public long insertTimeToMotionTable() {
        long id;
        synchronized (this.dbHelper) {
            if (DatabaseUtils.queryNumEntries(this.mDB, TB_NAME_MOTION) > ((long) this.rowCountThreshold)) {
                this.mDB.delete(TB_NAME_MOTION, "", null);
                Log.d(TAG, "motion_time_table row count > " + this.rowCountThreshold + " , clear it");
            }
            this.mDB.beginTransaction();
            ContentValues value = new ContentValues();
            value.put(SaveData.TRIGGER_TIME, this.mdf.format(Calendar.getInstance().getTime()));
            id = this.mDB.insert(TB_NAME_MOTION, null, value);
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        return id;
    }

    public Cursor queryMotionTable(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.dbHelper) {
            query = this.mDB.query(TB_NAME_MOTION, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public int deleteFromMotionTable() {
        int id;
        synchronized (this.dbHelper) {
            id = this.mDB.delete(TB_NAME_MOTION, "", null);
        }
        return id;
    }

    public long insertTimeStampToServiceRestartTable(String serviceName) {
        long id;
        synchronized (this.dbHelper) {
            if (DatabaseUtils.queryNumEntries(this.mDB, TB_SERVICE_RESTART) > ((long) this.rowCountThreshold)) {
                this.mDB.delete(TB_SERVICE_RESTART, "", null);
                Log.d(TAG, "service_restart_time_table row count > " + this.rowCountThreshold + " , clear it");
            }
            this.mDB.beginTransaction();
            ContentValues value = new ContentValues();
            value.put(SaveData.SERVICE_NAME, serviceName);
            value.put("time", this.mdf.format(Calendar.getInstance().getTime()));
            id = this.mDB.insert(TB_SERVICE_RESTART, null, value);
            this.mDB.setTransactionSuccessful();
            this.mDB.endTransaction();
        }
        return id;
    }

    public Cursor queryTimeStampToServiceRestartTable(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.dbHelper) {
            query = this.mDB.query(TB_SERVICE_RESTART, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public int deleteFromTimeStampToServiceRestartTable() {
        int id;
        synchronized (this.dbHelper) {
            id = this.mDB.delete(TB_SERVICE_RESTART, "", null);
        }
        return id;
    }
}
