---
manufacturer: 
    - oppo

---

No way is known to prevent the ColorOS `o-kill` described above. What you can do is detect it and recover from it.

**Detect it:** on Android 11+, `ActivityManager.getHistoricalProcessExitReasons()` returns the kill as `REASON_OTHER` (13), with `getDescription()` like `o-kill(40)`. Testers can read the same data with adb:

```
adb shell dumpsys activity exit-info <your.package>
adb shell dumpsys package <your.package> | grep -o 'stopped=[a-z]*'
```

The second command shows whether the app was force-stopped (`stopped=true`). In that case `dumpsys activity exit-info` names the caller, e.g. `stop <package> due to from pid ... (com.oplus.battery)`.

**Recover from it:** an `o-kill` is not a force-stop, so scheduled jobs are kept. In our test, a periodic JobScheduler job (15 minutes, the minimum) restarted the killed work 15 minutes after the kill. Note that the job belonged to a separate companion package (Termux:API), not to the killed app itself. If `com.oplus.battery` force-stops the package that owns the job, the job is removed.
