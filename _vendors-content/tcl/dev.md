---
manufacturer:
    - tcl
    - tct

---

### Detecting TCL / TCT devices

Note that `Build.MANUFACTURER` on TCL devices can return either `"TCL"` (newer models) or `"TCT"` (older models). Make sure to check for both:

```java
String manufacturer = Build.MANUFACTURER.toLowerCase();
boolean isTcl = manufacturer.contains("tcl") || manufacturer.contains("tct") || manufacturer.contains("alcatel");
```

### Opening the Smart Manager app optimization screen

The Smart Manager app (`com.tct.onetouchbooster`) contains an App Optimization screen where users can enable auto-start for your app. You can launch it directly using an intent:

```java
Intent intent = new Intent();
intent.setComponent(new ComponentName(
    "com.tct.onetouchbooster",
    "com.tct.smartmanager.appoptimise.ui.ApopSettingActivity"
));
try {
    startActivity(intent);
} catch (ActivityNotFoundException e) {
    // Smart Manager not available on this device/firmware, fail silently
}
```
