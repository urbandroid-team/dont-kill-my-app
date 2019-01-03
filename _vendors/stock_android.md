---
name: Stock Android
layout: vendor
award: 1
position: 9
permalink: stock_android
redirect_from: /vendors/stock_android.html
explanation: "
Yes, even stock Android may kill your app when doing a task valuable to the user.


Moreover, in Android 6.0 Google did dramatically restrict background processing with their doze mode [https://developer.android.com/training/monitoring-device-state/doze-standby](https://developer.android.com/training/monitoring-device-state/doze-standby).


This attempt to unify various battery saving features across the Android ecosystem felt flat. Not only did 
doze mode not prevent various vendors to implement their own battery saving. But doze mode in 6.0 was buggy 
and did made some background processing scenarios impossible on Android (see Solution for developers). 


A serious bug in doze mode in Android 6.0 even prevented foreground services to do their job (see Solution for developers for workaround), but luckily this was fixed in 7.0. 

"


user_solution: "

## Android P

There a special option in **Settings > Apps > Your app > Advanced > Battery > Background restrictions**. If users accidentally enable this option it will break their apps. And users do enable that option!

## Pie and pre-Pie

Overall it is a good idea to make your app not battery optimized to ensure it gets the freedom it needs to perform in the background.


For that:

1. Go to **Settings > Apps > Your app > Advanced > Battery > Battery optimization**

2. Change view to **All apps**

3. Search for your app

4. Choose **Not optimized**

Make sure **Settings > Apps > Your app > Advanced > Battery > Background limitations** is not enabled. If the app is not yet optimized for Oreo API level it will break their background processing.

if all fails you can turn doze mode completely off in **Settings > Developer options**. (Don't know how to enable developer options, Google it!) 
"

developer_solution: "
Some background processing scenarios are no more possible with Doze mode. For example low battery sensor logging
using sensor batching is no more possible due to the 9 minute limit on consecutive alarms. The only workaround is keeping a partial wake lock all the time which means daramatically more battery is consumed for the same job.
Or a nasty hack to schedule a user visible alarm though the setAlarmClock() method which can trigger more often.

Tips:  
* You can ask the user to make your app not battery optimized. See [https://developer.android.com/training/monitoring-device-state/doze-standby](https://developer.android.com/training/monitoring-device-state/doze-standby)

## Android 6.0
A serious bug in doze mode in Android 6.0 which we did report during the 6.0 BETA does not allow foreground services to keep a wake lock every time an activity or a content provider kicks in, see [https://plus.google.com/u/0/+AndroidDevelopers/posts/94jCkmG4jff](https://plus.google.com/u/0/+AndroidDevelopers/posts/94jCkmG4jff) and search for Petr Nalevka and Dianne Hackborn.
A workaround is to keep your foreground service in a separate process without any other Android components (read Activities, Receivers, Services..) in that process.       




"

---