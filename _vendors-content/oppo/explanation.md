---
manufacturer: 
    - oppo

---

We have currently only information for Oppo F1S, but on other models the situation may well be similar.

## ColorOS 16 (Android 16): the `o-kill` killer

Tested on an OPPO PLG110 with Android 16 (build PLG110_16.0.10.501).

Besides the standard Android mechanisms, ColorOS has its own process killer. It kills apps even when all of these exemptions are in place:

* "Allow background activity" and "Auto launch" on
* verified with adb: app on the battery optimization whitelist, standby bucket EXEMPTED, `RUN_ANY_IN_BACKGROUND` allowed
* verified with adb: phantom process killer disabled

It even kills apps **while they run a foreground service** with a visible notification (process importance 125).

Android records these kills, so they can be told apart from other causes. The exit reason is `OTHER KILLS BY SYSTEM` (13), and the description has the form `o-kill(N)`. Codes seen so far: 35, 40 and 72. Their meaning is not documented.

In addition, the system battery app `com.oplus.battery` force-stops apps it considers unused. We saw this for a rarely opened helper app (Termux:Boot). A force-stopped app no longer receives `BOOT_COMPLETED` or other broadcasts until the user opens it again, so its boot actions silently stop working.
