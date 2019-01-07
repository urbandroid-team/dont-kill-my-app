---
name: OnePlus
layout: vendor
award: 4
position: 2
permalink: oneplus
redirect_from: /vendors/oneplus.html
explanation: "OnePlus and their Oxygen OS Android modification is known as maximizing the stock Android experience. This may be true on the UX front but the exact opposite is valid for background process limits.


When releasing their 1+5 and 1+6 phones, OnePlus introduced one of the most severe background limits on the market to date, dwarfing even those performed by Xiaomi or Huawei. Not only did users need to enable extra settings to make their apps work properly, but those settings even get reset with firmware update so that apps break again and users are required to re-enable those settings on a regular basis.
"

user_solution: "Turn off **System Settings > Apps > Gear Icon > Special Access > Battery Optimization**.

> WARNING: Recently OnePlus phones started reverting this setting randomly for random apps. So if you set it to be **not optimized**, the next day it may be back to **optimized**. There is no workaround and you may have to check system settings every once in a while.<br>See [a bug report filed to OnePlus](https://forums.oneplus.com/threads/in-battery-optimisation-apps-are-getting-automatically-switched-from-not-optimised-to-optimised.849162/).

To avoid the system to automatically revert the **not optimized** setting, you must also lock the app into the 'Recent App' list. (solution described [here](https://forum.xda-developers.com/showpost.php?p=78588761&postcount=7))

Start the app you want to 'Protect'. Press the phone **Recent app** button. Toggle the **Lock** button on the upper right corner of the app.

This will avoid the app to be killed in background and the **Battery optimisation** setting to be reverted.

On some OnePlus phones there is also a thing called App Auto-Launch which essentially prevents apps from working in the background. Please disable it for your app.

![OnePlus Settings](/assets/img/ss_oneplus_1.jpg?raw=true **OnePlus Settings**)

Also try:

**Phone settings > Battery > Battery optimization** and switch to the All apps list **(Top menu) > Your app > Don’t optimize**

> NOTE: Some of our users indicated that you need to disable Doze mode in Developer options in 1+3 and earlier.

Also disable **Settings > Battery > Battery optimization > (three dots) > Enhanced optimization**. This option may also be called **Advanced optimisation**.
"

developer_solution: "No known solution on the developer end"

---
