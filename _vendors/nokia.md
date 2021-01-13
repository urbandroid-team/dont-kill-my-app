---
name: Nokia
subtitle: including Android One
manufacturer:
  - nokia
  - hmd global
redirect_from:
  - /vendors/nokia.html
award: 2
position: 11
explanation: "

<div class='caution-box'>
  8/2019 update
  <br><br>
  <b>Good news</b>: HMD Global <a href=\"https://community.phones.nokia.com/discussion/51246/tapping-into-android-pies-adaptive-battery-for-optimum-battery-performance\">claims to disable Evenwell powersaving apps</a> on all devices running Android Pie or newer.
  <br>
  <b>NOT SO GOOD NEWS</b>: DuraSpeed remains.
</div>

HMD Global/Nokia was the main reason this website came to exist. They had the most aggressive app killers preinstalled on their phones.


There were three different app killing mechanisms:


* *com.evenwell.powersaving.g3* on Android Pie for most Nokia phones - **this one has been disabled since 8/2019 on devices running Pie or greater**

* *com.evenwell.emm* on Android Go (Oreo?) for Nokia 1 - **probably still in the wild since HMD only disabled Evenwell apps for Pie or greater**

* *DuraSpeed* on Android Pie (build 00WW_3_180) for the US Nokia 3.1 (TA-1049, TA-1063) and Nokia 5.1 - **this one is still in the wild**


### Most Nokia phones (Power saver AKA com.evenwell.powersaving.g3)

<div class='caution-box'>
  The Evenwell Power saver *(com.evenwell.powersaving.g3)* has been disabled by HMD Global for devices running Pie or greater as of 8/2019.
</div>

The text below has been left here to preserve the detail and history of events.


~~Note: In Feb/March 2019, apparently on a few models distributed in Europe and US, the Evenwell Power Saver has been reworked to not kill the apps as aggressively, which largely resolves all issues for those models.~~


~~Nokia on Android O and P kills any background process including sleep tracking (or any other sport tracking) after 20 minutes if the screen is off. Also when killed all alarms are stopped which renders for example any alarm clock apps useless.~~


~~We have investigated this issue in details. We did even purchase a Nokia 6.1 to be able to reproduce the issue. The problem only occurs on Nokia devices with Android Pie. Nokia started to bundle a toxic app (package: com.evenwell.powersaving.g3 or com.evenwell.emm, name: Power saver) with their devices by some 3rd party company Evenwell. This app kills apps in the most brutal way we have seen so far among Android vendors.~~


~~Whitelisting apps from battery optimizations does not help! Evenwell kills even whitelisted apps.~~


~~What this non-standard app does is every process gets killed after 20 minutes regardless it is actually supposed to be running and doing a useful job for the user. Also alarms are not triggered. The aim is apparently to save your battery by rendering tracking apps and other apps that use background processing useless.~~


~~Moreover even third-party user visible alarms (alarm clock alarms) are not triggering properly on Nokia as foreground services cannot be started from background on Nokia. This is a serious issue unparalleled to any other vendor. We did not yet find a workaround for this :(. 3rd party alarms clock / calendars etc… won't be realiable on Nokia.~~


~~You can read more on this issue here:
[https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted](https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted)~~


For fun investigative read about Evenwell, check out [Who is Nokia?](https://medium.com/@roundedeverett/who-is-nokia-cb24ecbc52a9)


### Nokia 1 (com.evenwell.emm)

On Nokia 1 there is an alternative package that works very similar to what the com.evenwell.powersaving.g3 package is doing on the higher end models.


### Nokia 3.1 and 5.1 (DuraSpeed)

On Mediatek-based devices, HMD has baked in [DuraSpeed](https://www.appbrain.com/app/duraspeed/com.mediatek.duraspeed) as a system service. There is no user-facing control, or whitelist; this Mediatek-developed task killer terminates all background apps without prejudice.


DuraSpeed can be disabled through the global settings store, but this is a protected area of Android that can only be manipulated through adb, or an app that has been granted the `WRITE_SECURE_SETTINGS` permission (which must also be done with ADB). Additionally, the setting does not survive a reboot. Users can fix their devices themselves using an automation app (see \"Solution for users\"), or apps can request the `WRITE_SECURE_SETTINGS` permission and then cycle the flag on startup to kill DuraSpeed. Syncthing-Fork is one app that has [taken this approach](https://github.com/Catfriend1/syncthing-android/wiki/Nokia-HMD-phone-preparations).


Unfortunately, there are [some](https://forum.xda-developers.com/showpost.php?s=1f4fbd7602c2739781c1c5346bb06e36&p=80157506&postcount=7) [reports](https://github.com/urbandroid-team/dont-kill-my-app/issues/57#issuecomment-534246709) that even this fix does not work.


"
user_solution: "

### Most Nokia phones (Power saver AKA com.evenwell.powersaving.g3)

To fix this issue, please do the following:

* Go to **Phone settings > Apps > See all apps**.

* Tap on the **right top corner menu > Show system**.

* Find **Power saver** app in the list, select it and **Force close**. It will remain stopped for a while, but will restart itself eventually.


From now on, background apps should work normally and use the standard Android battery optimizations.


Still 3rd party alarm clocks or any task scheduling of foreground tasks at a particular time won't work. ~~We do not have any solution for this at the moment~~ UPDATE: in our preliminary tests it seems that force stopping or uninstalling the **Power saver** app also fixes alarms and starting of foreground services, until the Power saver restarts.


Alternative solution for tech-savvy users:

### Most Nokia models

<div class='caution-box'>
  The Evenwell Power saver *(com.evenwell.powersaving.g3)* has been disabled by HMD Global for devices running Pie or greater as of 8/2019.
</div>


Disable the *com.evenwell.powersaving.g3* package via the following adb commands:


`adb shell`<br>
`pm disable-user com.evenwell.powersaving.g3`


### Nokia 1 (Android Go)

Disable the *com.evenwell.emm* package via the following adb commands:


`adb shell`<br>
`pm disable-user com.evenwell.emm`


### Nokia 3.1 and 5.1

Regrettably, HMD did not include any sort of Settings switch to control DuraSpeed's operation. And since the task killer is a system service and not an app, it cannot simply be uninstalled. Fortunately, DuraSpeed does have a hidden kill switch: It watches the `setting.duraspeed.enabled` setting and will stop itself when the flag is set to any value that does not equal `1`. Once DuraSpeed stops itself, the phone is cured and all background apps will function normally. However, this workaround does not stick across reboots, so the flag has to be cycled at every boot using an automation app like [MacroDroid](https://play.google.com/store/apps/details?id=com.arlosoft.macrodroid).


First, use adb to grant MacroDroid (or your choice of automation app) the ability to write to the global settings store:


```
adb shell pm grant com.arlosoft.macrodroid android.permission.WRITE_SECURE_SETTINGS
```


Then create a task, triggered at **Device Boot**, that performs the following:


1. System Setting: type **Global**, name **setting.duraspeed.enabled**, value **2**

2. System Setting: type **System**, name **setting.duraspeed.enabled**, value **2**

3. System Setting: type **Global**, name **setting.duraspeed.enabled**, value **0**

4. System Setting: type **System**, name **setting.duraspeed.enabled**, value **0**


NOTE: You need both 'Global' and 'System' type settings (the screenshots below show only Global - you get the idea).

<div class='img-block'>
  <figure>
     <img src='/assets/img/nokia/duraspeed_macrodroid_kyrasantae.png'>
     <figcaption>MacroDroid example task</figcaption>
  </figure>
  <figure>
     <img src='/assets/img/nokia/duraspeed_tasker_yoryan.jpg'>
     <figcaption>Tasker example task</figcaption>
  </figure>
</div>


Run this task and verify there are no errors. If all is well, then DuraSpeed will be immediately disabled, and it will also be disabled on reboot.

"

developer_solution: "The only workaround we found so far is to keep the screen on all time your process runs. Yes, this is very battery consuming. As usually, vendors trying to safe your battery cause much bigger battery drain on this kind of workarounds. An alternative to this is to turn the screen on only less than every 20 minutes.


Another serious issue which we did not find a workaround for is that Nokia does not allow to start service using `startForegroundService()` when the process is not on background. We cannot reproduce it in few minutes after the process gets to background, but after ~hours there is the following message in the log:


`ActivityManager: Background start not allowed: service Intent { act=com.myapp.ALARM_ALERT flg=0x4 pkg=com.myapp (has extras) } to com.myapp/.MyService from pid=-1 uid=666 pkg=com.myapp startFg?=true`


This renders any alarm clocks, calendars, schedulers, automation tasks or any other processing at specified time useless.

"

---
