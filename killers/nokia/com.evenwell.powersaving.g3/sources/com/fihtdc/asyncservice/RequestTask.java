package com.fihtdc.asyncservice;

import android.os.Bundle;
import android.os.Messenger;

public class RequestTask {
    public static final int SERVICE_ID_BUNDLE = 1;
    private Throwable exception;
    private int mServiceId = 100;
    private String methodName;
    private int progress;
    private Bundle progressInfo;
    private Messenger replier;
    private RequestListener requestListener;
    private Object requestParams;
    private Object requestResults;

    public RequestTask(String methodName, RequestParams requestParams, RequestListener requestListener) {
        this.methodName = methodName;
        this.requestParams = requestParams;
        this.requestListener = requestListener;
    }

    public RequestTask(String methodName, Bundle requestParams, RequestListener requestListener) {
        this.methodName = methodName;
        this.requestParams = requestParams;
        this.requestListener = requestListener;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object getRequestParams() {
        return this.requestParams;
    }

    public void setRequestParams(Object requestParams) {
        this.requestParams = requestParams;
    }

    public Object getRequestResults() {
        return this.requestResults;
    }

    public void setRequestResults(Object requestResults) {
        this.requestResults = requestResults;
    }

    public Throwable getException() {
        return this.exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public RequestListener getRequestListener() {
        return this.requestListener;
    }

    public void setRequestListener(RequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public Messenger getReplier() {
        return this.replier;
    }

    public void setReplier(Messenger replier) {
        this.replier = replier;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Bundle getProgressInfo() {
        return this.progressInfo;
    }

    public void setProgressInfo(Bundle progressInfo) {
        this.progressInfo = progressInfo;
    }

    public int getServiceId() {
        return this.mServiceId;
    }

    public void setServiceId(int serviceId) {
        this.mServiceId = serviceId;
    }
}
