---
manufacturer: 
    - google
    - stock_android

---

 On Pixels and Nexuses you have the least chance something goes wrong with background processing, but still there are
  several places to look at!


  Some use-cases are no longer possible or paradoxically more battery consuming (e.g. gathering sensor data through sensor batching,see [Solution for developers](#dev-solution)) with the introduction of [Doze mode](https://developer.android.com/training/monitoring-device-state/doze-standby) in Android 6+ and you may need to opt the app out of battery optimizations to make it work properly.


Also a serious Doze mode bug in Android 6.0 that prevented foreground services from doing their intended job (see [Solution for devs](#dev-solution) for workaround), but luckily this was later fixed in 7.0.


  After Android 8, users or even the system (Adaptive battery in Android 9) can decide to prevent your app's background processes from working and you may need to check the Background restrictions (or limits) option in your phone settings.
