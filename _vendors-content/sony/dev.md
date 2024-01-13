---
manufacturer: 
    - sony

---

There is no workaround to prevent background process optimizations in *Stamina mode*, but at least apps can detect that Stamina mode is enabled with the following command:


```java
if (Build.MANUFACTURER.equals("sony") && android.provider.Settings.Secure.getInt(context.getContentResolver(), "somc.stamina_mode", 0) > 0) {
    // show warning
}
```

The problem is this will only tell if Stamina is enabled, but not if it is currently applied, but we can assume it is when not charged and battery is under X%. (TBS)
