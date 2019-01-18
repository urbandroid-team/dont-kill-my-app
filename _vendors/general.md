---
name: Other vendors
explanation: "
Whatever device you are using, don't blame the developers as the first thing when something goes wrong.
First check your phone settings whether some background processing is not restricted on your device.


See below for general solutions that apply for various vendors.
"
user_solution: "

Look for any vendor-specific battery saver on your device and ideally uninstall if possible, disable if possible.


If not, you are left if the option to root your device and uninstall it though **adb** (requires some expert skills though):


`adb shell`


`pm uninstall --user 0 com.useless.piece.of.trash`


Look though the vendor-specific phone settings and search for anything related to battery optimization or background processing.
If you find it try to disable it.

## Android 6+

Always check the following setting
**Phone settings > Battery & power saving > Battery usage > Ignore optimizations > Turn on** to ignore battery optimization for your app.

## Android 8+

Check if **Phone settings > Apps & Notifications > Your app > Background restrictions** or **Background limits** are not enabled for the app.


If all fails you can turn Doze mode off completely.

## Turn off doze on Android 6.0 and earlier


In **Settings > Developer options**. (If you don't know how to enable developer options, Google should help.)


## Turn off doze on Android 7+


Requires expert skills


`dumpsys deviceidle disable`


"


---
