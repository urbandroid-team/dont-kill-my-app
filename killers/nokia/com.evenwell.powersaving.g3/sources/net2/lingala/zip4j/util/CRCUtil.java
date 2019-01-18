package net2.lingala.zip4j.util;

import net2.lingala.zip4j.exception.ZipException;

public class CRCUtil {
    private static final int BUF_SIZE = 16384;

    public static long computeFileCRC(String inputFile) throws ZipException {
        return computeFileCRC(inputFile, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long computeFileCRC(java.lang.String r8, net2.lingala.zip4j.progress.ProgressMonitor r9) throws net2.lingala.zip4j.exception.ZipException {
        /*
        r6 = net2.lingala.zip4j.util.Zip4jUtil.isStringNotNullAndNotEmpty(r8);
        if (r6 != 0) goto L_0x000e;
    L_0x0006:
        r6 = new net2.lingala.zip4j.exception.ZipException;
        r7 = "input file is null or empty, cannot calculate CRC for the file";
        r6.<init>(r7);
        throw r6;
    L_0x000e:
        r3 = 0;
        net2.lingala.zip4j.util.Zip4jUtil.checkFileReadAccess(r8);	 Catch:{ IOException -> 0x0069, Exception -> 0x0077 }
        r4 = new java.io.FileInputStream;	 Catch:{ IOException -> 0x0069, Exception -> 0x0077 }
        r6 = new java.io.File;	 Catch:{ IOException -> 0x0069, Exception -> 0x0077 }
        r6.<init>(r8);	 Catch:{ IOException -> 0x0069, Exception -> 0x0077 }
        r4.<init>(r6);	 Catch:{ IOException -> 0x0069, Exception -> 0x0077 }
        r6 = 16384; // 0x4000 float:2.2959E-41 double:8.0948E-320;
        r0 = new byte[r6];	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        r5 = -2;
        r1 = new java.util.zip.CRC32;	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        r1.<init>();	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
    L_0x0026:
        r5 = r4.read(r0);	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        r6 = -1;
        if (r5 == r6) goto L_0x0056;
    L_0x002d:
        r6 = 0;
        r1.update(r0, r6, r5);	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        if (r9 == 0) goto L_0x0026;
    L_0x0033:
        r6 = (long) r5;	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        r9.updateWorkCompleted(r6);	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        r6 = r9.isCancelAllTasks();	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        if (r6 == 0) goto L_0x0026;
    L_0x003d:
        r6 = 3;
        r9.setResult(r6);	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        r6 = 0;
        r9.setState(r6);	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        r6 = 0;
        if (r4 == 0) goto L_0x004c;
    L_0x0049:
        r4.close();	 Catch:{ IOException -> 0x004d }
    L_0x004c:
        return r6;
    L_0x004d:
        r2 = move-exception;
        r6 = new net2.lingala.zip4j.exception.ZipException;
        r7 = "error while closing the file after calculating crc";
        r6.<init>(r7);
        throw r6;
    L_0x0056:
        r6 = r1.getValue();	 Catch:{ IOException -> 0x008d, Exception -> 0x008a, all -> 0x0087 }
        if (r4 == 0) goto L_0x004c;
    L_0x005c:
        r4.close();	 Catch:{ IOException -> 0x0060 }
        goto L_0x004c;
    L_0x0060:
        r2 = move-exception;
        r6 = new net2.lingala.zip4j.exception.ZipException;
        r7 = "error while closing the file after calculating crc";
        r6.<init>(r7);
        throw r6;
    L_0x0069:
        r2 = move-exception;
    L_0x006a:
        r6 = new net2.lingala.zip4j.exception.ZipException;	 Catch:{ all -> 0x0070 }
        r6.<init>(r2);	 Catch:{ all -> 0x0070 }
        throw r6;	 Catch:{ all -> 0x0070 }
    L_0x0070:
        r6 = move-exception;
    L_0x0071:
        if (r3 == 0) goto L_0x0076;
    L_0x0073:
        r3.close();	 Catch:{ IOException -> 0x007e }
    L_0x0076:
        throw r6;
    L_0x0077:
        r2 = move-exception;
    L_0x0078:
        r6 = new net2.lingala.zip4j.exception.ZipException;	 Catch:{ all -> 0x0070 }
        r6.<init>(r2);	 Catch:{ all -> 0x0070 }
        throw r6;	 Catch:{ all -> 0x0070 }
    L_0x007e:
        r2 = move-exception;
        r6 = new net2.lingala.zip4j.exception.ZipException;
        r7 = "error while closing the file after calculating crc";
        r6.<init>(r7);
        throw r6;
    L_0x0087:
        r6 = move-exception;
        r3 = r4;
        goto L_0x0071;
    L_0x008a:
        r2 = move-exception;
        r3 = r4;
        goto L_0x0078;
    L_0x008d:
        r2 = move-exception;
        r3 = r4;
        goto L_0x006a;
        */
        throw new UnsupportedOperationException("Method not decompiled: net2.lingala.zip4j.util.CRCUtil.computeFileCRC(java.lang.String, net2.lingala.zip4j.progress.ProgressMonitor):long");
    }
}
