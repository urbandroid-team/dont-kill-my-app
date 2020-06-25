---
name: Huawei
subtitle: except Nexus 6P
manufacturer:
  - huawei
redirect_from: /vendors/huawei.html
award: 5
position: 2
explanation: "

<div class='caution-box'>
<strong>UPDATE</strong>: On some phones with EMUI 9+ (Android P+) Huawei introduced a new task killer app called PowerGenie which kills everything not whitelisted by Huawei and does not give users any configuration options. See below how to uninstall it.
<br>
We have mixed reviews on Huawei - the PowerGenie app is present on some EMUI 9+ systems, while on others it isn't.
</div>


Traditionally Huawei and their Android customization called EMUI belongs to the most troubled on the market with respect to non-standard background process limitations.

There are no APIs and no documentation for those extensions. On default settings, background processing simply does not work right and apps working in background will break.


In some of the EMUI versions (we know about EMUI 4 at and we have some reports about EMUI 5 and now the latest EMUI 9) no user accessible settings can prevent the system to break background processing longer than 60 minutes. This is done by an evil custom service called HwPFWService (and in EMUI 9 this is called PowerGenie) developed and bundled with EMUI by Huawei.
"

user_solution: "

### EMUI 9+ devices


Huawei is extremely inventive in breaking apps on their devices. In addition to all the non-standard power management measures described below, they introduced a new task killer app build right into EMUI 9 on Android Pie.


It is called <b>PowerGenie</b> and it kills all apps that are not on its whitelist. You cannot add custom apps on their pre-defined whitelist. This means there is no other way to fix proper app functionality on Huawei than uninstalling PowerGenie.



Unfortunately this is a system app and can only be fully uninstalled using ADB (Android Debug Bridge) Source: [XDA](https://forum.xda-developers.com/mate-20-pro/themes/remove-powergenie-to-allow-background-t3890409).


You need to:


1. [install ADB](https://www.xda-developers.com/install-adb-windows-macos-linux/) on your computer


2. Connect your phone with a data cable


3. Enable [Developer options](https://developer.android.com/studio/debug/dev-options.html)


4. Enable USB debugging within Developer options on your phone


5. Run the following commands on your computer:


`adb shell pm uninstall --user 0 com.huawei.powergenie`


We did not yet have this confirmed but it is possible you can alternatively just disable PowerGenie in *Phone settings > Apps*. This setting would need to be re-applied every time you reboot your device.


<div class=\"caution-box\">
Please still follow the steps below - Huawei phones usually have multiple powersaving mechanisms.
<br><br>
Also, you may not have PowerGenie on your phone, but your apps may still get killed by another mechanism.
</div>


### EMUI 6+ devices (and some EMUI 5 devices)

* *Phone settings > Advanced Settings > Battery manager > Power plan* set to *Performance*

* *Phone Settings > Advanced Settings > Battery Manager > Protected apps* – set your app as *Protected*

* *Phone Settings > Apps > Your app > Battery > Power-intensive prompt* [uncheck] and *Keep running after screen off* [check]

* *Phone settings > Apps > Advanced (At the bottom) > Ignore optimisations >* Press *Allowed > All apps >* Find your app on the list and set to *Allow*


### Huawei P9 Plus

* *Phone settings > Apps > Settings > Special access > Ignore battery optimisation >* select allow for your app.


### Huawei P20, Huawei P20 Lite, Huawei Mate 10

* *Phone settings > Battery > App launch* and then set your app to “Manage manually” and make sure everything is turned on.

Also for reliable background processes you may need to uninstall PowerGenie as described above.



### Huawei Honor 9 Lite, Huawei Mate 9 Pro

* *Phone settings > Battery > Launch* and then set your app to “Manage manually” and make sure everything is turned on.

On EMUI 4 there is no way out, sorry, but you can ask developers of your apps to implement the workaround described in <a href=\"#developer-solution-section\">Developer section</a>

"

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
