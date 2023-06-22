---
manufacturer:
    - general
---

### Android 6+

Always check the following setting:

- on older devices:<br>
_Phone settings > Battery & power saving > Battery usage > Ignore optimizations > Turn on_ to ignore battery optimization for your app.

- on newer devices:<br>
_Settings > Apps > Your app > Battery > Optimize battery usage > All (from the top) > Your app_ (toggle to disable).

### Android 8+

Check if **Phone settings > Apps & Notifications > Your app > Background restrictions** or **Background limits** are not enabled for the app.

If all fails you can turn Doze mode off completely.

## Turn off doze on Android 6.0 and earlier

In **Settings > Developer options**. (If you don't know how to enable developer options, Google should help.)

### Turn off doze on Android 7+

Requires expert skills

`dumpsys deviceidle disable`

### If all fails

Look for any vendor-specific battery saver on your device and ideally uninstall if possible, disable if possible.


If not, you are left with the option to root your device or uninstall it though **adb** (requires some expert skills though):

`adb shell`

`pm uninstall --user 0 com.useless.piece.of.trash`


Look through the vendor-specific phone settings and search for anything related to battery optimization or background processing.
If you find it try to disable it.


Try the generic approach below as some vendors tend to hook more functionality into this than AOSP


**Phone settings > Battery & power saving > Battery usage > Ignore optimizations > Turn on** to ignore battery optimization for your app.
