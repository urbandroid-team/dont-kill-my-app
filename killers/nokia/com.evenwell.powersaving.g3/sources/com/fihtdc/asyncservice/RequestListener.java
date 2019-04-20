package com.fihtdc.asyncservice;

import android.os.Bundle;

public interface RequestListener {
    void onComplete(Object obj);

    void onException(Object obj, Throwable th);

    void onHandle(Object obj);

    void onStart(Object obj);

    void updateProgress(int i);

    void updateProgress(int i, Bundle bundle);
}
