---
manufacturer: 
    - sony

---

For newer Android versions, check for Battery Saver mode, which for Sony devices is rebranded as STAMINA mode. There is no way to detect if "Prefer battery time" is set, but if your app's notifications don't show up or SurfaceFlinger is at a lower framerate than usual, it's probably on. Alarms are not killed either way, but users can (and some will) deprive your app of the permission to set these alarms in the first place.

For older Android versions, there is no workaround to prevent background process optimizations in STAMINA mode, but apps can detect whether it's enabled with the following code:

```java
if (Build.MANUFACTURER.equals("sony") && android.provider.Settings.Secure.getInt(context.getContentResolver(), "somc.stamina_mode", 0) > 0) {
    // show warning
}
```

The problem is this will only tell if Stamina is enabled, but not if it is currently applied, but we can assume it is when not charged and battery is under X%. (TBS)
