---
name: Nokia
manufacturer:
  - nokia
  - hmd global
redirect_from:
  - /vendors/nokia.html
award: 4
position: 3
explanation: "

It seems HMD Global finally found the killer app, but unfortunately it is killing other apps!


There are confirmed reports of three different app killing mechanisms:


* *com.evenwell.powersaving.g3* on Android Pie for **most Nokia phones**

* *com.evenwell.emm* on Android Go (Oreo?) for **Nokia 1**

* *DuraSpeed* on Android Pie (build 00WW_3_180) for the US **Nokia 3.1** (TA-1049)


### Most Nokia phones (Power saver AKA com.evenwell.powersaving.g3)

Note: In Feb/March 2019, apparently on a few models distributed in Europe and US, the Evenwell Power Saver has been reworked to not kill the apps as aggressively, which largely resolves all issues for those models.


Nokia on Android O and P kills any background process including sleep tracking (or any other sport tracking) after 20 minutes if the screen is off. Also when killed all alarms are stopped which renders for example any alarm clock apps useless.


We have investigated this issue in details. We did even purchase a Nokia 6.1 to be able to reproduce the issue. The problem only occurs on Nokia devices with Android Pie. Nokia started to bundle a toxic app (package: com.evenwell.powersaving.g3 or com.evenwell.emm, name: Power saver) with their devices by some 3rd party company Evenwell. This app kills apps in the most brutal way we have seen so far among Android vendors.


Whitelisting apps from battery optimizations does not help! Evenwell kills even whitelisted apps.


What this non-standard app does is every process gets killed after 20 minutes regardless it is actually supposed to be running and doing a useful job for the user. Also alarms are not triggered. The aim is apparently to save your battery by rendering tracking apps and other apps that use background processing useless.


Moreover even 3rd party user visible alarms (alarm clock alarms) are not triggering properly on Nokia as foreground services cannot be started from background on Nokia. This is a serious issue unparalleled to any other vendor. We did not yet find a workaround for this :(. 3rd party alarms clock / calendars etc... won't be realiable on Nokia.


You can read more on this issue here:
[https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted](https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted)


For fun investigative read about Evenwell, check out [Who is Nokia?](https://medium.com/@roundedeverett/who-is-nokia-cb24ecbc52a9)


### Nokia 1 (com.evenwell.emm)

On Nokia 1 there is an alternative package that works very similar to what the com.evenwell.powersaving.g3 package is doing on the higher end models.


### Nokia 3.1 (DuraSpeed)

HMD Global included Mediatek's *DuraSpeed* task killer as a system service. Since DuraSpeed is not packaged as an app, it cannot simply be uninstalled, but it does have a secret settings switch that will enable or disable the service.


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


Disable the *com.evenwell.powersaving.g3* package via the following adb commands:


`adb shell`<br>
`pm disable-user com.evenwell.powersaving.g3`


### Nokia 1 (Android Go)

Disable the *com.evenwell.emm* package via the following adb commands:


`adb shell`<br>
`pm disable-user com.evenwell.emm`


### Nokia 3.1

DuraSpeed is not packaged as an app, it cannot simply be uninstalled, but it does have a secret settings switch that will enable or disable the service. The flag is not exposed in the Settings app; it can only be manipulated through adb.


`adb shell settings put global setting.duraspeed.enabled 0`


Toggling it will produce immediate logcat feedback.


`04-15 21:13:57.544  1063  1089 D DuraSpeed/DuraSpeedService: onChange, checked: false`


Background apps and notifications should be now running without any restrictions - even after a factory reset and enabling all of HMD's evenwell apps (including com.evenwell.powersaving.g3).



"

developer_solution: "The only workaround we found so far is to keep the screen on all time your process runs. Yes, this is very battery consuming. As usually, vendors trying to safe your battery cause much bigger battery drain on this kind of workarounds. An alternative to this is to turn the screen on only less than every 20 minutes.


Another serious issue which we did not find a workaround for is that Nokia does not allow to start service using `startForegroundService()` when the process is not on background. We cannot reproduce it in few minutes after the process gets to background, but after ~hours there is the following message in the log:


`ActivityManager: Background start not allowed: service Intent { act=com.myapp.ALARM_ALERT flg=0x4 pkg=com.myapp (has extras) } to com.myapp/.MyService from pid=-1 uid=666 pkg=com.myapp startFg?=true`


This renders any alarm clocks, calendars, schedulers, automation tasks or any other processing at specified time useless.

"

---
