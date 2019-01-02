---
name: Nokia
layout: vendor
award: 1
explanation: "Nokia on Android P kills any background process including sleep tracking (or any other sport tracking) after 30 minutes if the screen is off. Also all alarms are stopped which renders for example any alarm clock app useless.


We have investigated this issue in details. We did even purchase a Nokia 6.1 to be able to reproduce the issue. The problem only occurs on Nokia devices with Android Pie. Nokia started to bundle a toxic app (package: com.evenwell.powersaving.g3, name: Battery protection) with their devices by some Asian company Evenwell. This app kills apps in the most brutal way we have seen so far among Android vendors.


What this non-standard app does is every process gets killed after 30 minutes regardless it is actually supposed to be running and doing a useful job for the user. Also alarms are not triggered. The aim is apparently to save your battery by rendering tracking apps and other apps that use background processing useless.


You can read more on this issue here:
[https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted](https://community.phones.nokia.com/discussion/3428/background-service-killed-even-when-whitelisted)
"
user_solution: "
To fix this issue, please do the following:

* Go to *Phone settings > Apps > See all apps*.

* Tap on the *right top corner menu > Show system*.

* Find *Battery protection* app in the list, select it and *Force close*. It will remain stopped until the next restart.

From now on, background apps should work normally.


Alternative solution for tech-savvy users:


Uninstall the *com.evenwell.powersaving.g3* package via the following adb commands:


`adb shell`<br>
`pm uninstall --user 0 com.evenwell.powersaving.g3`
"
developer_solution: "The only workaround we found so far is to keep the screen on all the time your process runs. Yes, this is very battery consuming. As usual, vendors trying to save your battery cause much bigger battery drain with this kind of workarounds. An alternative to this is to turn the screen on every (less than) 30 minutes."

---