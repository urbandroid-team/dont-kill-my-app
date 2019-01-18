package com.fihtdc.push_system.lib.app;

import android.os.Bundle;

interface IFihPushReceiveService {
    Bundle getPushInfos();
    void newPushMessage(in Bundle datas);
    Bundle getApplicationInfo();
}