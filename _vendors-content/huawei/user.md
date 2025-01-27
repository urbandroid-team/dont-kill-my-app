https://github.com/95706911/Axel.git---
manufacturer:
    - huawei

---


### App Launch on some EMUI 8, 9 and 10 devices (Huawei P20, Huawei P20 Lite, Huawei Mate 10...)

* *Phone settings > Battery > App launch* and then set your app to “Manage manually” and make sure everything is turned on.

<div class="img-block">
  <figure>
    <img src="/assets/img/huawei/ss_huawei_app_launch_1.png">
    <figcaption>1. *Phone settings > Battery > App launch*. This feature may or may not be available for all devices or labeled differently.</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/huawei/ss_huawei_app_launch_3.png">
    <figcaption>2. Turn off “Manage all automatically”</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/huawei/ss_huawei_app_launch_4.png">
    <figcaption>3. Make sure to ENABLE! all toggles.</figcaption>
  </figure>

</div>

Also for reliable background processes you may need to uninstall PowerGenie as described below.


### Startup manager

Startup manager seems to be a new kid on the block, preventing apps run automatically after the phone starts up.

* Go to Settings > All > Startup manager.

<div class="img-block">
  <figure>
    <img src="/assets/img/huawei/startup.jpg">
    <figcaption>1. *Phone settings > All > Startup manager* and allow the app.</figcaption>
  </figure>
</div>

### EMUI 9+ devices

#### Classic battery optimization

Open Settings, and search for and access Battery optimization. Touch the little inverted triangle next to Don't allow, touch All apps, locate and touch your app, and select Don't allow.

#### PowerGenie

Huawei is extremely inventive in breaking apps on their devices. In addition to all the non-standard power management measures described below, they introduced a new task killer app build right into EMUI 9 on Android Pie.


It is called <b>PowerGenie</b> and it kills all apps that are not on its whitelist. You cannot add custom apps on their pre-defined whitelist. This means there is no other way to fix proper app functionality on Huawei than uninstalling PowerGenie.



Unfortunately this is a system app and can only be fully uninstalled using ADB (Android Debug Bridge) Source: [XDA](https://forum.xda-developers.com/mate-20-pro/themes/remove-powergenie-to-allow-background-t3890409).


You need to:


1. [install ADB](https://www.xda-developers.com/install-adb-windows-macos-linux/) on your computer


2. Connect your phone with a data cable


3. Enable [Developer options](https://developer.android.com/studio/debug/dev-options.html)


4. Enable USB debugging within Developer options on your device


5. Run the following commands on your computer:

`adb shell pm uninstall -k --user 0 com.huawei.powergenie`

`adb shell pm uninstall -k --user 0 com.huawei.android.hwaps`

If apps keep getting killed try running `adb shell am stopservice hwPfwService`.

We did not yet have this confirmed but it is possible you can alternatively just disable PowerGenie in *Phone settings > Apps*. This setting would need to be re-applied every time you reboot your device.


<div class="caution-box">
Please still follow the steps below - Huawei phones usually have multiple powersaving mechanisms.
<br><br>
Also, you may not have PowerGenie on your device, but your apps may still get killed by another mechanism.
</div>

### EMUI 5.X and 8.X

#### Classic battery optimization

Open Settings, and search for and access Ignore battery optimization. Touch the little inverted triangle next to Allow, touch All apps, locate and touch the app, and select Allow.

### EMUI 6+ devices (and some EMUI 5 devices)

* *Phone settings > Advanced Settings > Battery manager > Power plan* set to *Performance*

* *Phone Settings > Advanced Settings > Battery Manager > Protected apps* – set your app as *Protected*

* *Phone Settings > Apps > Your app > Battery > Power-intensive prompt* [uncheck] and *Keep running after screen off* [check]

* *Phone settings > Apps > Advanced (At the bottom) > Ignore optimisations >* Press *Allowed > All apps >* Find your app on the list and set to *Allow*


### Huawei P9 Plus

* *Phone settings > Apps > Settings > Special access > Ignore battery optimisation >* select allow for your app.


### Huawei Honor 9 Lite, Huawei Mate 9 Pro

* *Phone settings > Battery > Launch* and then set your app to “Manage manually” and make sure everything is turned on.

On EMUI 4 there is no way out, sorry, but you can ask developers of your apps to implement the workaround described in <a href="#developer-solution-section">Developer section</a>
