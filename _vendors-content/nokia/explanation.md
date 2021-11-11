---
manufacturer:
- nokia
- hmd global

---

<div class='caution-box'>
  8/2019 update
  <br><br>
  <b>Good news</b>: HMD Global <a href="https://community.phones.nokia.com/discussion/51246/tapping-into-android-pies-adaptive-battery-for-optimum-battery-performance">claims to disable Evenwell powersaving apps</a> on all devices running Android Pie or newer.
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


DuraSpeed can be disabled through the global settings store, but this is a protected area of Android that can only be manipulated through adb, or an app that has been granted the `WRITE_SECURE_SETTINGS` permission (which must also be done with ADB). Additionally, the setting does not survive a reboot. Users can fix their devices themselves using an automation app (see "Solution for users"), or apps can request the `WRITE_SECURE_SETTINGS` permission and then cycle the flag on startup to kill DuraSpeed. Syncthing-Fork is one app that has [taken this approach](https://github.com/Catfriend1/syncthing-android/wiki/Nokia-HMD-phone-preparations).


Unfortunately, there are [some](https://forum.xda-developers.com/showpost.php?s=1f4fbd7602c2739781c1c5346bb06e36&p=80157506&postcount=7) [reports](https://github.com/urbandroid-team/dont-kill-my-app/issues/57#issuecomment-534246709) that even this fix does not work.

