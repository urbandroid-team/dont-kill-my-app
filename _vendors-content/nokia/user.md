---
manufacturer:
- nokia
- hmd global

---

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
