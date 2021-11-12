---
manufacturer: 
    - google
    - stock_android

---


## Android P Background restriction


For apps whose core functionality is background processing this option basically means 'Break the app core functionality' and this is not always obvious for users. You would be surprised how many support cases we look back on having spent hours debugging, only to find users enabled this. In our opinion this option is unnecessary or even evil. If users don't want the app to do its job, they can still force close or uninstall. Maybe this is a matter of naming as most users don't know what are background processes, but I guess whatever you do in terms of explanation it is so hard to understand (even for experienced users) what are all the consequences of this that there always will be people enabling it unintentionally.


## Doze mode


Some background processing scenarios are not possible anymore with Doze mode. For example low battery sensor logging
using sensor batching is no longer possible, due to the 9 minute limit on consecutive alarms. The only workaround is keeping a partial wake lock all the time, which means dramatically more battery is consumed for the same job.


Or, a nasty hack to schedule a user-visible alarm though the setAlarmClock() method which can trigger more often.


Tips:

* You can ask the user to make your app _not battery optimized_. See [https://developer.android.com/training/monitoring-device-state/doze-standby](https://developer.android.com/training/monitoring-device-state/doze-standby)


## Android 6.0


A serious bug in Doze mode in Android 6.0 which we (authors of this site) reported to Google (Dianne Hackborn) during the 6.0 BETA does not allow foreground services to keep a wake lock every time an activity or a broadcast receiver kicks in, see [https://plus.google.com/+AndroidDevelopers/posts/94jCkmG4jff](https://web.archive.org/web/20181030095832/https://plus.google.com/+AndroidDevelopers/posts/94jCkmG4jff) and search for Petr Nalevka and Dianne Hackborn.



A workaround is to keep your foreground service in a separate process without any other Android components (read Activities, Receivers, Services…) in that process. This workaround is needed for all Android 6.0 devices but not needed on later devices where this is already fixed.
