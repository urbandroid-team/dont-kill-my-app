---
name: Huawei
subtitle: except Nexus 6P
manufacturer:
  - huawei
redirect_from: /vendors/huawei.html
award: 5
position: 1
explanation: "

<div class='caution-box'>
UPDATE: It seems on EMUI 9+ (Android P+) Huawei introduced a new task killer app called PowerGenie which kills everything not whitelisted form Huawei and does not give users any configuration options. See below how to uninstall it.
</div>


Traditionally Huawei and their Android customization called EMUI belongs to the most troubled on the market with respect to non-standard background process limitations.

There are no APIs and no documentation for those extensions. In default settings background processing simply does not work right and apps working in background will break.


In some of the EMUI versions (we know about EMUI 4 at and we have some reports about EMUI 5 and now the latest EMUI 9) no user accessible settings can prevent the system to break background processing longer than 60 minutes. This is done by an evil custom service called HwPFWService (and in EMUI 9 this is called PowerGenie) developed and bundled with EMUI by Huawei.
"

user_solution: '

### EMUI 9+ devices


Huawei is extremely inventive in breaking apps on there devices. It seems on Android Pie, in addition to all the non-standard power management measures described below
they introduced a new task killer app build right into EMUI 9. It is called PowerGenie and you cannot add custom apps on their pre-defined whitelist. This means there is 
no other way to fix proper app functionality on Huawei than installing PowerGenie. 



Unfortunately this is a system app and can only be fully uninstalled using ADB (Android Debug Bridge) Source: [XDA](https://forum.xda-developers.com/mate-20-pro/themes/remove-powergenie-to-allow-background-t3890409).


You need to:
1. [install ABD](https://www.xda-developers.com/install-adb-windows-macos-linux/) on your computed
2. Connect your phone with a data cable
3. Enable [Developer options](https://developer.android.com/studio/debug/dev-options.html)
4. Enable USB debugging within Developer options\
5. Run the following commands:

`adb shell pm uninstall --user 0 com.huawei.powergenie`


Please still follow the steps below and Huawei does not have a single power saving mechanism but rather multiple.  


We did not yet had thsi confirmed but it is possible you can alternatively just disable PowerGenie in *Phone settings > Apps* but this setting needs to be re-applied every time you reboot your device.

 

### EMUI 6+ devices (and some EMUI 5 devices)

* *Phone settings > Advanced Settings > Battery manager > Power plan* is set to *Performance*

* *Phone Settings > Advanced Settings > Battery Manager > Protected apps* – check for your app as *Protected*

* *Phone Settings > Apps > Your app > Battery > Power-intensive* prompt [x] and *Keep running after screen off [x]*

* *Phone settings > Apps > Advanced (At the bottom) > Ignore optimisations >* Press *Allowed > All apps >* Find your app on the list and set to *Allow*


### Huawei P9 Plus

* *Phone settings > Apps > Settings > Special access > Ignore battery optimisation >* select allow for your app.


### Huawei P20

* *Phone settings > Battery > App launch* and then set your app to “Manage manually” and make sure everything is turned on.

+ For realible background processes you need to uninstall PowerGenie as described above.

### Huawei Honor 9 Lite, Huawei Mate 9 Pro

* *Phone settings > Battery > Launch* and then set your app to “Manage manually” and make sure everything is turned on.

On EMUI 4 there is no way out, sorry, but you can ask developers of your apps to implement the workaround described in <a href="#developer-solution-section">Developer section</a>

'

developer_solution: "

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

The good news is that in order to not kill itself or others Huawei/Honor's services, before killing an app, hwPfwService looks at the tag of the wakelock and if the tag is one the hard-coded whitelisted tags, it does not kill the app.
The whitelisted wakelock tags are: \"AudioMix\", \"AudioIn\", \"AudioDup\", \"AudioDirectOut\", \"AudioOffload\" and \"LocationManagerService\".


Here is how you can workaround this in code:


```

String tag = \"com.my_app:LOCK\";


if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && Build.MANUFACTURER.equals(\"Huawei\")) {
    tag = \"LocationManagerService\";
}


PowerManager.WakeLock wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(1, tag);
wakeLock.acquire();

```


"

---
