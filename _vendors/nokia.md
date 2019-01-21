---
name: Nokia
manufacturer:
  - nokia
  - hmd global
redirect_from:
  - /vendors/nokia.html
award: 5
position: 1
explanation: "

It seems HMD Global finally found the killer app, but unfortunately it is killing other apps!


Nokia on Android O and P kills any background process including sleep tracking (or any other sport tracking) after 20 minutes if the screen is off. Also when killed all alarms are stopped which renders for example any alarm clock apps useless.


We have investigated this issue in details. We did even purchase a Nokia 6.1 to be able to reproduce the issue. The problem only occurs on Nokia devices with Android Pie. Nokia started to bundle a toxic app (package: com.evenwell.powersaving.g3 or com.evenwell.emm, name: Battery protection) with their devices by some 3rd party company Evenwell. This app kills apps in the most brutal way we have seen so far among Android vendors.


Whitelisting apps from battery optimizations does not help! Evenwell kills even whitelisted apps.


What this non-standard app does is every process gets killed after 20 minutes regardless it is actually supposed to be running and doing a useful job for the user. Also alarms are not triggered. The aim is apparently to save your battery by rendering tracking apps and other apps that use background processing useless.

Moreover event 3rd party user visible alarms (alarm clock alarms) are not triggering properly on Nokia as foreground services cannost be started from background on Nokia. This is a serious issue unraralleled to any other vendor. We did not yet find a workaround for this :(. 3rd party alarms clock / calendars etc... won't be realiable on Nokia.

You can read more on this issue here:
[https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted](https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted)
"
user_solution: "
To fix this issue, please do the following:

* Go to **Phone settings > Apps > See all apps**.

* Tap on the **right top corner menu > Show system**.

* Find **Battery protection** app in the list, select it and **Force close**. It will remain stopped until the next restart.

From now on, background apps should work normally and use the standard Android battery optimizations.

Still 3rd party alarm clock will be broken and we do not have any solution for this at the moment. Also scheduling tasks in the background for a particular time won't work.

Alternative solution for tech-savvy users:



### Nokia 1 (Android Go) Rooted

Uninstall the *com.evenwell.emm* package via the following adb commands:


`adb shell`<br>
`pm uninstall --user 0 com.evenwell.emm`


### Other Nokia models rooted


Uninstall the *com.evenwell.powersaving.g3* package via the following adb commands:


`adb shell`<br>
`pm uninstall --user 0 com.evenwell.powersaving.g3`
"

developer_solution: "The only workaround we found so far is to keep the screen on all time your process runs. Yes, this is very battery consuming. As usually, vendors trying to safe your battery cause much bigger battery drain on this kind of workarounds. An alternative to this is to turn the screen on only less than every 20 minutes.


Another serious issue which we did not find a workaround for is that Nokia does not allow to start service using `startForegroundService()` when the process is not on background. We cannot reproduce it in few minutes after the process gets to background, but after ~hours there is the following message in the log:


`ActivityManager: *Background start not allowed*: service Intent { act=com.myapp.ALARM_ALERT flg=0x4 pkg=com.myapp (has extras) } to com.myapp/.MyService from pid=-1 uid=666 pkg=com.myapp *startFg?=true*`


This reders any alarm clocks, calendars, schedulers, automation tasks or any other processing at specified time useless.


If anybody is interested to take a look at how the Nokia app killer (com.evenwell.powersaving.g3) works internally, take a look at [the decompiled APK](https://github.com/urbandroid-team/dont-kill-my-app/tree/master/killers/nokia/com.evenwell.powersaving.g3).
"

---
