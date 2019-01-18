package com.evenwell.powersaving.g3.retrofit;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface DevicePostAPI {
    @POST("AtlService/CheckCp")
    Call<ResponseBody> postCheckCP(@Header("Version") String str, @Header("AccessKeyId") String str2, @Header("SignatureMethod") String str3, @Header("Timestamp") String str4, @Header("SignatureVersion") String str5, @Header("SignatureNonce") String str6, @Header("Signature") String str7, @Body RequestBody requestBody);

    @POST("PushService/RegisterDevice")
    Call<ResponseBody> postRegisterDevice(@Header("Version") String str, @Header("AccessKeyId") String str2, @Header("SignatureMethod") String str3, @Header("Timestamp") String str4, @Header("SignatureVersion") String str5, @Header("SignatureNonce") String str6, @Header("Signature") String str7, @Body RequestBody requestBody);

    @POST("AtlService/UpdateResult")
    Call<ResponseBody> postUpdateResult(@Header("Version") String str, @Header("AccessKeyId") String str2, @Header("SignatureMethod") String str3, @Header("Timestamp") String str4, @Header("SignatureVersion") String str5, @Header("SignatureNonce") String str6, @Header("Signature") String str7, @Body RequestBody requestBody);
}
