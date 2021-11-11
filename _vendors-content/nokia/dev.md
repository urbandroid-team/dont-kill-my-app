---
manufacturer: 
    - nokia
    - hmd global

---

The only workaround we found so far is to keep the screen on all time your process runs. Yes, this is very battery consuming. As usually, vendors trying to safe your battery cause much bigger battery drain on this kind of workarounds. An alternative to this is to turn the screen on only less than every 20 minutes.


Another serious issue which we did not find a workaround for is that Nokia does not allow to start service using `startForegroundService()` when the process is not on background. We cannot reproduce it in few minutes after the process gets to background, but after ~hours there is the following message in the log:


`ActivityManager: Background start not allowed: service Intent { act=com.myapp.ALARM_ALERT flg=0x4 pkg=com.myapp (has extras) } to com.myapp/.MyService from pid=-1 uid=666 pkg=com.myapp startFg?=true`


This renders any alarm clocks, calendars, schedulers, automation tasks or any other processing at specified time useless.
