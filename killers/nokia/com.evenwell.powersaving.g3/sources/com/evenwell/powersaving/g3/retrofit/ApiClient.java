package com.evenwell.powersaving.g3.retrofit;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;
import com.evenwell.powersaving.g3.C0321R;
import com.evenwell.powersaving.g3.PowerSavingUtils;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.HEADER_VALUE;
import com.evenwell.powersaving.g3.pushservice.PushServiceUtils.RESPONSE_CODE;
import com.evenwell.powersaving.g3.utils.PSConst.TAG;
import com.evenwell.powersaving.g3.utils.PSUtils;
import com.evenwell.powersaving.g3.utils.ProjectInfo;
import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.MediaType;
import okhttp3.OkHttpClient.Builder;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static String AccessKeyId = HEADER_VALUE.AccessKeyId;
    public static String BASE_URL = "";
    private static String DOWNLOAD_FILE_FAIL = "download_file_error";
    private static String TAG = TAG.PSLOG;
    public static String mAccessKeySecret = "f96fab2095c7918e05c966c7a8ef4275";
    public static int mRetryCount = 0;
    public static int mRetryInterval = 180000;
    public static int mTotalRetryCount = 5;
    private static Retrofit retrofit = null;
    private Context mContext;
    private DeviceResponseAPI mDeviceResponseAPI;
    private DevicePostAPI mRegisterDeviceRequestAPI;
    private boolean success = false;

    private boolean downloadFile(java.lang.String r23, java.lang.String r24) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Not initialized variable reg: 11, insn: 0x002c: RETURN  (r11 boolean), block:B:21:0x002c, method: com.evenwell.powersaving.g3.retrofit.ApiClient.downloadFile(java.lang.String, java.lang.String):boolean
	at jadx.core.dex.visitors.ssa.SSATransform.renameVar(SSATransform.java:168)
	at jadx.core.dex.visitors.ssa.SSATransform.renameVar(SSATransform.java:197)
	at jadx.core.dex.visitors.ssa.SSATransform.renameVar(SSATransform.java:197)
	at jadx.core.dex.visitors.ssa.SSATransform.renameVariables(SSATransform.java:132)
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
        r22 = this;
        r10 = 1;
        r16 = 0;
        r3 = new okhttp3.OkHttpClient;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r3.<init>();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20 = new okhttp3.Request$Builder;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20.<init>();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r0 = r20;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r1 = r23;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20 = r0.url(r1);	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r14 = r20.build();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20 = r3.newCall(r14);	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r15 = r20.execute();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20 = r15.isSuccessful();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        if (r20 != 0) goto L_0x002d;
    L_0x0027:
        r10 = 0;
        com.evenwell.powersaving.g3.utils.PSUtils.closeSilently(r16);
        r11 = r10;
    L_0x002c:
        return r11;
    L_0x002d:
        r2 = r15.body();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r4 = r2.contentLength();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r17 = r2.source();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r9 = new java.io.File;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r0 = r24;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r9.<init>(r0);	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20 = okio.Okio.sink(r9);	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r16 = okio.Okio.buffer(r20);	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r18 = 0;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r12 = 0;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r6 = 1024; // 0x400 float:1.435E-42 double:5.06E-321;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
    L_0x004e:
        r20 = r16.buffer();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r0 = r17;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r1 = r20;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r12 = r0.read(r1, r6);	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20 = -1;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r20 = (r12 > r20 ? 1 : (r12 == r20 ? 0 : -1));	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        if (r20 != 0) goto L_0x004e;	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
    L_0x0060:
        r16.writeAll(r17);	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        r16.flush();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        com.evenwell.powersaving.g3.utils.PSUtils.closeSilently(r16);
    L_0x0069:
        r11 = r10;
        goto L_0x002c;
    L_0x006b:
        r8 = move-exception;
        r10 = 0;
        r8.printStackTrace();	 Catch:{ Exception -> 0x006b, all -> 0x0074 }
        com.evenwell.powersaving.g3.utils.PSUtils.closeSilently(r16);
        goto L_0x0069;
    L_0x0074:
        r20 = move-exception;
        com.evenwell.powersaving.g3.utils.PSUtils.closeSilently(r16);
        throw r20;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.evenwell.powersaving.g3.retrofit.ApiClient.downloadFile(java.lang.String, java.lang.String):boolean");
    }

    public ApiClient(Context context) {
        this.mContext = context;
        Log.d(TAG, "[ApiClient]: ApiClient() getServerConfig : " + ProjectInfo.getServerConfig());
        if (ProjectInfo.getServerConfig() == null) {
            BASE_URL = this.mContext.getResources().getString(C0321R.string.server_url);
        } else {
            BASE_URL = ProjectInfo.getServerConfig();
        }
        Log.d(TAG, "[ApiClient]: ApiClient() BASE_URL : " + BASE_URL);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(Level.BODY);
        Builder httpClient = new Builder();
        httpClient.addInterceptor(logging);
        retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).client(httpClient.build()).build();
        this.mRegisterDeviceRequestAPI = (DevicePostAPI) retrofit.create(DevicePostAPI.class);
    }

    private int getRandomNum() {
        return new Random().nextInt(10000) + 0;
    }

    private String getSignatureString(int randomNum, long now) {
        String newLine = "\\n";
        return HEADER_VALUE.Version + newLine + HEADER_VALUE.AccessKeyId + newLine + HEADER_VALUE.SignatureMethod + newLine + String.valueOf(now) + newLine + "1.0" + newLine + String.valueOf(randomNum);
    }

    public void RegisterDevice(RegisterDevicePost mDevicePost) {
        int i1 = getRandomNum();
        long now = System.currentTimeMillis();
        String mSignatureHeader = createSignatureHeader(getSignatureString(i1, now));
        String custom = new Gson().toJson(mDevicePost);
        Log.d(TAG, "[ApiClient]: RegisterDevice : " + custom);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), custom);
        this.mRegisterDeviceRequestAPI = (DevicePostAPI) retrofit.create(DevicePostAPI.class);
        try {
            Response<ResponseBody> rawResponse = this.mRegisterDeviceRequestAPI.postRegisterDevice(HEADER_VALUE.Version, HEADER_VALUE.AccessKeyId, HEADER_VALUE.SignatureMethod, String.valueOf(now), "1.0", String.valueOf(i1), mSignatureHeader, body).execute();
            Log.d(TAG, "RegisterDevice onResponse : " + rawResponse.headers());
            Log.d(TAG, "[ApiClient]: CheckCP : ret = " + getResult(rawResponse.code()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean CheckCP(CheckCpPost mCheckCpPost) {
        int i1 = getRandomNum();
        long now = System.currentTimeMillis();
        String mSignatureHeader = createSignatureHeader(getSignatureString(i1, now));
        String custom = new Gson().toJson(mCheckCpPost);
        Log.d(TAG, "[ApiClient]: CheckCP : " + custom);
        DeviceResponse res = null;
        String ret = "";
        try {
            Response<ResponseBody> rawResponse = this.mRegisterDeviceRequestAPI.postCheckCP(HEADER_VALUE.Version, AccessKeyId, HEADER_VALUE.SignatureMethod, String.valueOf(now), "1.0", String.valueOf(i1), mSignatureHeader, RequestBody.create(MediaType.parse("application/json; charset=utf-8"), custom)).execute();
            Log.d(TAG, "[ApiClient] CheckCP() head " + rawResponse.headers());
            Log.d(TAG, "[ApiClient] CheckCP() body " + rawResponse.body());
            ret = getResult(rawResponse.code());
            res = getCheckCPResponse(((ResponseBody) rawResponse.body()).string());
            Log.d(TAG, "[ApiClient]: CheckCP : ret = " + ret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean isSuccess = false;
        if (res != null) {
            UpdateResultPost updateResultPost = getUpdateResultPost(res, mCheckCpPost);
            if (updateResultPost.status == null) {
                updateResultPost.status = ret;
                isSuccess = true;
            }
            UpdateResult(updateResultPost);
        }
        return isSuccess;
    }

    public DeviceResponse getCheckCPResponse(String strBody) {
        Exception e;
        Throwable th;
        DeviceResponse res = new DeviceResponse();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(strBody.getBytes(StandardCharsets.UTF_8))));
            try {
                res = (DeviceResponse) new Gson().fromJson(reader2, DeviceResponse.class);
                PSUtils.closeSilently(reader2);
                reader = reader2;
            } catch (Exception e2) {
                e = e2;
                reader = reader2;
                try {
                    e.printStackTrace();
                    PSUtils.closeSilently(reader);
                    return res;
                } catch (Throwable th2) {
                    th = th2;
                    PSUtils.closeSilently(reader);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                reader = reader2;
                PSUtils.closeSilently(reader);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            PSUtils.closeSilently(reader);
            return res;
        }
        return res;
    }

    public void UpdateResult(UpdateResultPost updateResultPost) {
        int i1 = getRandomNum();
        long now = System.currentTimeMillis();
        String mSignatureHeader = createSignatureHeader(getSignatureString(i1, now));
        String custom = new Gson().toJson(updateResultPost);
        Log.d(TAG, "[ApiClient]:UpdateResult : " + custom);
        try {
            Response<ResponseBody> rawResponse = this.mRegisterDeviceRequestAPI.postUpdateResult(HEADER_VALUE.Version, "testPower", HEADER_VALUE.SignatureMethod, String.valueOf(now), "1.0", String.valueOf(i1), mSignatureHeader, RequestBody.create(MediaType.parse("application/json; charset=utf-8"), custom)).execute();
            Log.d(TAG, "[ApiClient] UpdateResult() onResponse: " + rawResponse.body());
            Log.d(TAG, "[ApiClient]: UpdateResult : ret = " + getResult(rawResponse.code()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createSignatureHeader(String text) {
        String Signature = null;
        try {
            Signature = hmacSha1(mAccessKeySecret, text).trim();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        }
        return Signature;
    }

    private String hmacSha1(String base, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        String type = "HmacSHA1";
        SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
        Mac mac = Mac.getInstance(type);
        mac.init(secret);
        return Base64.encodeToString(mac.doFinal(base.getBytes()), 0);
    }

    public String getResult(int retCode) {
        String ret = "unKnown";
        if (retCode == RESPONSE_CODE.success) {
            return "ok";
        }
        if (retCode == 403) {
            return "access_denied ";
        }
        if (retCode == RESPONSE_CODE.invalid_request) {
            return "invalid_request ";
        }
        if (retCode == RESPONSE_CODE.signature_not_found) {
            return "signature_not_found ";
        }
        if (retCode == RESPONSE_CODE.package_not_found) {
            return "package_not_found ";
        }
        if (retCode == RESPONSE_CODE.internal_erorr) {
            return "internal_erorr ";
        }
        return ret;
    }

    public UpdateResultPost getUpdateResultPost(DeviceResponse res, CheckCpPost checkCpPost) {
        UpdateResultPost updateResultPost = new UpdateResultPost();
        try {
            updateResultPost.package_id = res.package_id;
            updateResultPost.device_id = PowerSavingUtils.GetPreferencesStatusString(this.mContext, "device_id");
            String storePath = this.mContext.getFilesDir().toString();
            updateResultPost.package_id = res.package_id;
            updateResultPost.device_id = PowerSavingUtils.GetPreferencesStatusString(this.mContext, "device_id");
            UpdateResultPostStatistics mUpdateResultPostStatistics = new UpdateResultPostStatistics();
            mUpdateResultPostStatistics.check_response_time = "1.0";
            updateResultPost.statistics.add(mUpdateResultPostStatistics);
            for (CheckCpResponseComponent objCheckCpResponseComponent : res.components) {
                String urlFileName = URLUtil.guessFileName(objCheckCpResponseComponent.download_url, null, null);
                String destPath = storePath + File.separator + urlFileName;
                boolean isFileDownloadSuccess = downloadFile(objCheckCpResponseComponent.download_url, destPath);
                String checkSum = calculateMD5(destPath);
                Log.d(TAG, "[ApiClient]: " + destPath + " md5 " + checkSum);
                if (!isFileDownloadSuccess || !checkSum.equalsIgnoreCase(objCheckCpResponseComponent.checksum)) {
                    Log.d(TAG, "[ApiClient]: download file fail or md5 check fail");
                    updateResultPost.status = DOWNLOAD_FILE_FAIL;
                    new File(destPath).delete();
                    break;
                }
                boolean isSuccess = unpackZip(storePath + File.separator, urlFileName);
                new File(destPath).delete();
                File file = new File(storePath + File.separator + objCheckCpResponseComponent.filename);
                if (isSuccess) {
                    file.renameTo(new File(storePath + File.separator + checkCpPost.category));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return updateResultPost;
    }

    private boolean unpackZip(String path, String zipname) {
        Exception e;
        Throwable th;
        ZipInputStream zis = null;
        FileOutputStream fout = null;
        InputStream is = null;
        try {
            InputStream is2 = new FileInputStream(path + zipname);
            try {
                ZipInputStream zis2 = new ZipInputStream(new BufferedInputStream(is2));
                try {
                    byte[] buffer = new byte[1024];
                    FileOutputStream fout2 = null;
                    while (true) {
                        try {
                            ZipEntry mZipEntry = zis2.getNextEntry();
                            if (mZipEntry != null) {
                                String filename = mZipEntry.getName();
                                if (mZipEntry.isDirectory()) {
                                    new File(path + filename).mkdirs();
                                } else {
                                    fout = new FileOutputStream(path + filename);
                                    while (true) {
                                        int count = zis2.read(buffer);
                                        if (count == -1) {
                                            break;
                                        }
                                        fout.write(buffer, 0, count);
                                    }
                                    zis2.closeEntry();
                                    fout.flush();
                                    fout2 = fout;
                                }
                            } else {
                                PSUtils.closeSilently(zis2);
                                PSUtils.closeSilently(fout2);
                                PSUtils.closeSilently(is2);
                                is = is2;
                                fout = fout2;
                                zis = zis2;
                                return true;
                            }
                        } catch (Exception e2) {
                            e = e2;
                            is = is2;
                            fout = fout2;
                            zis = zis2;
                        } catch (Throwable th2) {
                            th = th2;
                            is = is2;
                            fout = fout2;
                            zis = zis2;
                        }
                    }
                } catch (Exception e3) {
                    e = e3;
                    is = is2;
                    zis = zis2;
                } catch (Throwable th3) {
                    th = th3;
                    is = is2;
                    zis = zis2;
                }
            } catch (Exception e4) {
                e = e4;
                is = is2;
                try {
                    e.printStackTrace();
                    PSUtils.closeSilently(zis);
                    PSUtils.closeSilently(fout);
                    PSUtils.closeSilently(is);
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    PSUtils.closeSilently(zis);
                    PSUtils.closeSilently(fout);
                    PSUtils.closeSilently(is);
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                is = is2;
                PSUtils.closeSilently(zis);
                PSUtils.closeSilently(fout);
                PSUtils.closeSilently(is);
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            e.printStackTrace();
            PSUtils.closeSilently(zis);
            PSUtils.closeSilently(fout);
            PSUtils.closeSilently(is);
            return false;
        }
    }

    private String calculateMD5(String filePath) {
        String output;
        Exception e;
        Throwable th;
        InputStream inputStream = null;
        try {
            File file = new File(filePath);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            InputStream is = new FileInputStream(file);
            try {
                byte[] buffer = new byte[2048];
                while (true) {
                    int read = is.read(buffer);
                    if (read <= 0) {
                        break;
                    }
                    digest.update(buffer, 0, read);
                }
                output = String.format("%32s", new Object[]{new BigInteger(1, digest.digest()).toString(16)}).replace(' ', '0');
                PSUtils.closeSilently(is);
                inputStream = is;
            } catch (Exception e2) {
                e = e2;
                inputStream = is;
                try {
                    e.printStackTrace();
                    output = "";
                    PSUtils.closeSilently(inputStream);
                    return output;
                } catch (Throwable th2) {
                    th = th2;
                    PSUtils.closeSilently(inputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStream = is;
                PSUtils.closeSilently(inputStream);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            output = "";
            PSUtils.closeSilently(inputStream);
            return output;
        }
        return output;
    }
}
