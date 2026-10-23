---
manufacturer: 
    - infinix
---

Next thing usually is being done by setting `Unrestricted` battery mode for an app. But, if 

with ROOT (terminal):

```bash
sudo dunmpsys deviceidle whitelist
```

or via Android ADB:

```bash
adb shell dumpsys deviceidle whitelist
```

doesn't shows your package name (`com.termux`, for example), you could try to exclude desired app from doze management by using terminal (ROOT):

```bash
sudo dunmpsys deviceidle whitelist +com.termux
```

Via Android ADB:

```bash
adb shell dumpsys deviceidle whitelist +com.termux
```

Once again, use full application name of your choice in place of `com.termux`.
