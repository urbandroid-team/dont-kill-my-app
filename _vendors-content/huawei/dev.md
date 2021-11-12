---
manufacturer: 
    - huawei

---


### EMUI 4

On EMUI 4 Huawei implemented an evil service called `HwPFWService`. In your `adb logcat` you can see message like:


```

[ 05-25 18:10:17.167 4230:16683 D/PFW.HwPFWAppWakeLockPolicy ]
getUidWakeLock uid: 10185 wakelock >= 10 mins


[ 05-25 18:10:17.249 4230:16683 W/PFW.HwPFWAppWakeLockPolicy ]
uid: 10185 wakelock > 60 mins


[ 05-25 18:10:17.249 4230:16683 D/PFW.HwPFWAppWakeLockPolicy ]
force stop abnormal wakelock app uid: 10185

```

The good news is that in order to not kill itself or others, Huawei/Honor's services, before killing an app, hwPfwService looks at the tag of the wakelock and if the tag is one the hard-coded whitelisted tags, it does not kill the app.
The whitelisted wakelock tags are: "AudioMix", "AudioIn", "AudioDup", "AudioDirectOut", "AudioOffload" and "LocationManagerService".


Here is how you can workaround this in code:


```

String tag = "com.my_app:LOCK";


if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && Build.MANUFACTURER.equals("Huawei")) {
    tag = "LocationManagerService";
}


PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(1, tag);
wakeLock.acquire();

```
