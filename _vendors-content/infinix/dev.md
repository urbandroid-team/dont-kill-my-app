---
manufacturer: 
    - infinix
---

Exclude desired app from going sleep entirely by using terminal (ROOT):

```bash
sudo dunmpsys deviceidle whitelist +com.termux
```
Via Android ADB:

```bash
adb shell dunmpsys deviceidle whitelist +com.termux
```

Under `com.termux` use full application name of your choice.
