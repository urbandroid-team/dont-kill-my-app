---
name: Other vendors
position: 9999
explanation: |
  On Pixels and Nexuses you have the least change something goes wrong with background processing, but still there are
  several places to look at!

  Some use-cases are no more possible with the introduction of Doze mode in Android 6+ and you may need to opt the app out of battery optimizations to make it work properly.

  After Android 8 users or even the system (Adaptive battery) can decide to prevent your apps background processes from working and you may need to check the Background restrictions (or limits) option in your phone settings.

user_solution: |

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

  ## Generic advice for manufacturers which are not listed

    If your phone is not a Pixel or Nexus and your manufacturer is not listed elsewhere, please try to following generic advice.

    Look for any vendor-specific battery saver on your device and ideally uninstall if possible, disable if possible.

    If not, you are left with the option to root your device or uninstall it though **adb** (requires some expert skills though):

    `adb shell`

    `pm uninstall --user 0 com.useless.piece.of.trash`

    Look through the vendor-specific phone settings and search for anything related to battery optimization or background processing.
    If you find it try to disable it.

    Try the generic approach below as some vendors tent to hook more fuctionality into this than AOSP

  **Phone settings > Battery & power saving > Battery usage > Ignore optimizations > Turn on** to ignore battery optimization for your app.

    If nothing helps go systematically through the menus and look for any battery related settings. Try to turn off anything which leads to any battery saving.


---
