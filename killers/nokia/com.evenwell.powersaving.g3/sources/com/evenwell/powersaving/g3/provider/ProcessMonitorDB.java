package com.evenwell.powersaving.g3.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class ProcessMonitorDB {
    private static final String TAG = "[PowerSavingAppG3]ProcessMonitorDB";
    public static int rowCountThreshold = 1500;
    private Context mContext;
    private SQLiteDatabase mDb;
    private DBHelper mdbHelper;
    private DateFormat mdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public class DBHelper extends SQLiteOpenHelper {
        private static final String CREATE_TABLE_FORCESTOP_PROCESS_LIST = "CREATE TABLE IF NOT EXISTS forcestop_process_list(_id integer primary key autoincrement,pkg_name varchar,time varchar)";
        private static final String CREATE_TABLE_PROCESS_MONITOR = "CREATE TABLE IF NOT EXISTS process_monitor(_id integer primary key autoincrement,caller_name varchar,callee_name varchar,hosting_type varchar,intent_action varchar,intent_info varchar,screen_on varchar,time varchar)";
        public static final String DB_NAME = "process_monitor.db";
        private static final int DB_VERSION = 1;

        public class TB_PROCESS_FORCESTOP {
            public static final String TABLE_NAME = "forcestop_process_list";

            public class FIELD {
                public static final String PKG_NAME = "pkg_name";
                public static final String TIME = "time";
            }
        }

        public class TB_PROCESS_MONITOR {
            public static final String TABLE_NAME = "process_monitor";

            public class FIELD {
                public static final String CALLEE_NAME = "callee_name";
                public static final String CALLER_NAME = "caller_name";
                public static final String HOSTING_TYPE = "hosting_type";
                public static final String INTENT_ACTION = "intent_action";
                public static final String INTENT_INFO = "intent_info";
                public static final String SCREEN_ON = "screen_on";
                public static final String TIME = "time";
            }
        }

        public DBHelper(com.evenwell.powersaving.g3.provider.ProcessMonitorDB r4, android.content.Context r5) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:120)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/951880373.run(Unknown Source)
*/
            /*
            r3 = this;
            com.evenwell.powersaving.g3.provider.ProcessMonitorDB.this = r4;
            r0 = "process_monitor.db";
            r1 = 0;
            r2 = 1;
            r3.<init>(r5, r0, r1, r2);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.evenwell.powersaving.g3.provider.ProcessMonitorDB.DBHelper.<init>(com.evenwell.powersaving.g3.provider.ProcessMonitorDB, android.content.Context):void");
        }

        public void onCreate(android.database.sqlite.SQLiteDatabase r3) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:120)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/951880373.run(Unknown Source)
*/
            /*
            r2 = this;
            r0 = "[PowerSavingAppG3]ProcessMonitorDB";
            r1 = "onCreate Create Table";
            android.util.Log.i(r0, r1);
            r0 = "CREATE TABLE IF NOT EXISTS forcestop_process_list(_id integer primary key autoincrement,pkg_name varchar,time varchar)";
            r3.execSQL(r0);
            r0 = "CREATE TABLE IF NOT EXISTS process_monitor(_id integer primary key autoincrement,caller_name varchar,callee_name varchar,hosting_type varchar,intent_action varchar,intent_info varchar,screen_on varchar,time varchar)";
            r3.execSQL(r0);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.evenwell.powersaving.g3.provider.ProcessMonitorDB.DBHelper.onCreate(android.database.sqlite.SQLiteDatabase):void");
        }

        public void onUpgrade(android.database.sqlite.SQLiteDatabase r2, int r3, int r4) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: SSA rename variables already executed
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:120)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:52)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:56)
	at jadx.core.ProcessClass.process(ProcessClass.java:39)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/951880373.run(Unknown Source)
*/
            /*
            r1 = this;
            r0 = "DROP TABLE IF EXISTS process_monitor";
            r2.execSQL(r0);
            r0 = "CREATE TABLE IF NOT EXISTS forcestop_process_list(_id integer primary key autoincrement,pkg_name varchar,time varchar)";
            r2.execSQL(r0);
            r0 = "DROP TABLE IF EXISTS process_monitor";
            r2.execSQL(r0);
            r0 = "CREATE TABLE IF NOT EXISTS process_monitor(_id integer primary key autoincrement,caller_name varchar,callee_name varchar,hosting_type varchar,intent_action varchar,intent_info varchar,screen_on varchar,time varchar)";
            r2.execSQL(r0);
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.evenwell.powersaving.g3.provider.ProcessMonitorDB.DBHelper.onUpgrade(android.database.sqlite.SQLiteDatabase, int, int):void");
        }
    }

    public ProcessMonitorDB(Context context) {
        this.mContext = context;
        try {
            this.mdbHelper = new DBHelper(this.mContext);
            this.mDb = this.mdbHelper.getWritableDatabase();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public long insertToProcessMonitor(String calleePkg, String callerPkg, String hostingType, String intentAction, String broadcastName, boolean isScreenOn) {
        long rowId;
        synchronized (this.mdbHelper) {
            if (DatabaseUtils.queryNumEntries(this.mDb, TB_PROCESS_MONITOR.TABLE_NAME) > ((long) rowCountThreshold)) {
                this.mDb.delete(TB_PROCESS_MONITOR.TABLE_NAME, "", null);
                Log.d(TAG, "process_monitor row count > " + rowCountThreshold + " , clear it");
            }
            ContentValues value = new ContentValues();
            value.put(FIELD.CALLER_NAME, calleePkg);
            value.put(FIELD.CALLEE_NAME, callerPkg);
            value.put(FIELD.HOSTING_TYPE, hostingType);
            value.put(FIELD.INTENT_ACTION, intentAction);
            value.put(FIELD.INTENT_INFO, broadcastName);
            value.put(FIELD.SCREEN_ON, Boolean.toString(isScreenOn));
            this.mdf.setTimeZone(TimeZone.getDefault());
            value.put("time", this.mdf.format(Calendar.getInstance(TimeZone.getDefault()).getTime()));
            this.mDb.beginTransaction();
            rowId = this.mDb.insertWithOnConflict(TB_PROCESS_MONITOR.TABLE_NAME, FIELD.CALLER_NAME, value, 5);
            this.mDb.setTransactionSuccessful();
            this.mDb.endTransaction();
        }
        return rowId;
    }

    public int deleteAllFromProcessMonitor() {
        int id;
        synchronized (this.mdbHelper) {
            id = this.mDb.delete(TB_PROCESS_MONITOR.TABLE_NAME, "", null);
        }
        return id;
    }

    public long insertProcessWasForceStopped(String pkg) {
        long id;
        synchronized (this.mdbHelper) {
            if (DatabaseUtils.queryNumEntries(this.mDb, "forcestop_process_list") > ((long) rowCountThreshold)) {
                this.mDb.delete("forcestop_process_list", "", null);
                Log.d(TAG, "forcestop_process_list row count > " + rowCountThreshold + " , clear it");
            }
            this.mDb.beginTransaction();
            ContentValues value = new ContentValues();
            value.put("pkg_name", pkg);
            this.mdf.setTimeZone(TimeZone.getDefault());
            value.put("time", this.mdf.format(Calendar.getInstance(TimeZone.getDefault()).getTime()));
            id = this.mDb.insert("forcestop_process_list", null, value);
            this.mDb.setTransactionSuccessful();
            this.mDb.endTransaction();
        }
        return id;
    }

    public Cursor queryPackageFromProcessMonitor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.mdbHelper) {
            query = this.mDb.query(TB_PROCESS_MONITOR.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public Cursor queryPackagesWereForceStop(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor query;
        synchronized (this.mdbHelper) {
            query = this.mDb.query("forcestop_process_list", projection, selection, selectionArgs, null, null, sortOrder);
        }
        return query;
    }

    public int deleteFromForceStopAppList() {
        int id;
        synchronized (this.mdbHelper) {
            id = this.mDb.delete("forcestop_process_list", "", null);
        }
        return id;
    }

    public long queryRowCountFromForceStopAppList() {
        long count;
        synchronized (this.mdbHelper) {
            count = DatabaseUtils.queryNumEntries(this.mDb, "forcestop_process_list");
        }
        return count;
    }

    public long queryRowCountFromProcessMonitor() {
        long count;
        synchronized (this.mdbHelper) {
            count = DatabaseUtils.queryNumEntries(this.mDb, TB_PROCESS_MONITOR.TABLE_NAME);
        }
        return count;
    }

    public void close() {
        this.mDb.close();
        this.mdbHelper.close();
    }
}
