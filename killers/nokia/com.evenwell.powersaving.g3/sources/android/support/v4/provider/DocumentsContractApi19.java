package android.support.v4.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;

class DocumentsContractApi19 {
    private static final String TAG = "DocumentFile";

    public static boolean exists(android.content.Context r10, android.net.Uri r11) {
        /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
	at jadx.api.JadxDecompiler$$Lambda$8/951880373.run(Unknown Source)
*/
        /*
        r8 = 1;
        r9 = 0;
        r0 = r10.getContentResolver();
        r6 = 0;
        r1 = 1;
        r2 = new java.lang.String[r1];	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r1 = 0;	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r3 = "document_id";	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r2[r1] = r3;	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r3 = 0;	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r4 = 0;	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r5 = 0;	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r1 = r11;	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r1 = r6.getCount();	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        if (r1 <= 0) goto L_0x0022;
    L_0x001d:
        r1 = r8;
    L_0x001e:
        closeQuietly(r6);
    L_0x0021:
        return r1;
    L_0x0022:
        r1 = r9;
        goto L_0x001e;
    L_0x0024:
        r7 = move-exception;
        r1 = "DocumentFile";	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r2.<init>();	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r3 = "Failed query: ";	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r2 = r2.append(r7);	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        android.util.Log.w(r1, r2);	 Catch:{ Exception -> 0x0024, all -> 0x0042 }
        closeQuietly(r6);
        r1 = r9;
        goto L_0x0021;
    L_0x0042:
        r1 = move-exception;
        closeQuietly(r6);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.provider.DocumentsContractApi19.exists(android.content.Context, android.net.Uri):boolean");
    }

    DocumentsContractApi19() {
    }

    public static boolean isDocumentUri(Context context, Uri self) {
        return DocumentsContract.isDocumentUri(context, self);
    }

    public static String getName(Context context, Uri self) {
        return queryForString(context, self, "_display_name", null);
    }

    private static String getRawType(Context context, Uri self) {
        return queryForString(context, self, "mime_type", null);
    }

    public static String getType(Context context, Uri self) {
        String rawType = getRawType(context, self);
        if ("vnd.android.document/directory".equals(rawType)) {
            return null;
        }
        return rawType;
    }

    public static boolean isDirectory(Context context, Uri self) {
        return "vnd.android.document/directory".equals(getRawType(context, self));
    }

    public static boolean isFile(Context context, Uri self) {
        String type = getRawType(context, self);
        if ("vnd.android.document/directory".equals(type) || TextUtils.isEmpty(type)) {
            return false;
        }
        return true;
    }

    public static long lastModified(Context context, Uri self) {
        return queryForLong(context, self, "last_modified", 0);
    }

    public static long length(Context context, Uri self) {
        return queryForLong(context, self, "_size", 0);
    }

    public static boolean canRead(Context context, Uri self) {
        if (context.checkCallingOrSelfUriPermission(self, 1) == 0 && !TextUtils.isEmpty(getRawType(context, self))) {
            return true;
        }
        return false;
    }

    public static boolean canWrite(Context context, Uri self) {
        if (context.checkCallingOrSelfUriPermission(self, 2) != 0) {
            return false;
        }
        String type = getRawType(context, self);
        int flags = queryForInt(context, self, "flags", 0);
        if (TextUtils.isEmpty(type)) {
            return false;
        }
        if ((flags & 4) != 0) {
            return true;
        }
        if ("vnd.android.document/directory".equals(type) && (flags & 8) != 0) {
            return true;
        }
        if (TextUtils.isEmpty(type) || (flags & 2) == 0) {
            return false;
        }
        return true;
    }

    public static boolean delete(Context context, Uri self) {
        return DocumentsContract.deleteDocument(context.getContentResolver(), self);
    }

    private static String queryForString(Context context, Uri self, String column, String defaultValue) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(self, new String[]{column}, null, null, null);
            if (!c.moveToFirst() || c.isNull(0)) {
                closeQuietly(c);
                return defaultValue;
            }
            defaultValue = c.getString(0);
            return defaultValue;
        } catch (Exception e) {
            Log.w(TAG, "Failed query: " + e);
        } finally {
            closeQuietly(c);
        }
    }

    private static int queryForInt(Context context, Uri self, String column, int defaultValue) {
        return (int) queryForLong(context, self, column, (long) defaultValue);
    }

    private static long queryForLong(Context context, Uri self, String column, long defaultValue) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(self, new String[]{column}, null, null, null);
            if (!c.moveToFirst() || c.isNull(0)) {
                closeQuietly(c);
                return defaultValue;
            }
            defaultValue = c.getLong(0);
            return defaultValue;
        } catch (Exception e) {
            Log.w(TAG, "Failed query: " + e);
        } finally {
            closeQuietly(c);
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e) {
            }
        }
    }
}
