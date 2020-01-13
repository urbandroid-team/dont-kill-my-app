---
name: OnePlus
manufacturer:
  - oneplus
award: 5
position: 3
redirect_from: /vendors/oneplus.html
explanation: "OnePlus and their OxygenOS Android modification is known for maximizing the stock Android experience. This may be true on the UX front but the exact opposite is valid for background process limits.


When releasing their 1+5 and 1+6 phones, OnePlus introduced one of the most severe background limits on the market to date, dwarfing even those performed by Xiaomi or Huawei. Not only did users need to enable extra settings to make their apps work properly, but those settings even got reset with firmware updates. So apps break again and users are required to re-enable those settings on a regular basis.
"

user_solution: "

### Battery optimization

- Turn off **System Settings > Apps > Gear Icon > Special Access > Battery Optimization**.
<br>
<small>**WARNING:** Recently OnePlus phones started reverting this setting randomly for random apps. So if you set it to be *not optimized*, the next day it may be back to *optimized*. 
<br>
To avoid the system to automatically revert the *not optimized* setting, you must also lock the app into the 'Recent App' list. (solution described [here](https://forum.xda-developers.com/showpost.php?p=78588761&postcount=7))
<br>
Start the app you want to protect. Go to *Recent apps* (App switcher). Toggle the *Lock* button on the upper right corner of the app.
<br>
This will avoid the app to be killed in background and the *Battery optimisation* setting to be reverted.
<br>
This is however not 100%. You may have to check system settings every once in a while. See [a bug report filed to OnePlus](https://forums.oneplus.com/threads/in-battery-optimisation-apps-are-getting-automatically-switched-from-not-optimised-to-optimised.849162/).</small>


- Turn off **System settings > Battery > Battery optimization**, switch to 'All apps' in top right menu **> Your app > Don’t optimize**
<br>
<small>**NOTE:** Some of our users indicated that you need to disable Doze mode in Developer options in 1+3 and earlier.</small>


### App Auto-Launch

App Auto-Launch (on some OnePlus phones) essentially prevents apps from working in the background. Please disable it for your app.

<div class=\"img-block\">
  <img src=\"/assets/img/ss_oneplus_1.jpg\">
</div>


### Enhanced / Advanced optimization

- OnePlus 6 and further: 
<br>
**System settings > Battery > Battery optimization > (three dots) > Advanced optimization**. 
<br>
You'll see two options there. Both are enabled by default:

1. Deep optimization
<br>
This is the main app killer. 
If you need any apps to run in background, disable it.

2. Sleep standby optimization
<br>
OnePlus tries to learn when you are usually asleep, and in those times it will then disable the phone's network connections. 
This setting will prevent push notifications from being delivered.






- OnePlus below 6: 
<br>
Turn off **System settings > Battery > Battery optimization > (three dots) > Enhanced optimization**.
<br>
<small>**NOTE:** This should help with the problem where you lose Bluetooth connection to your smartwatch / fitness tracker (e.g. for sleep tracking).</small>

<div class=\"img-block\">
  <img src=\"/assets/img/ss_oneplus6_setting_sleepstandby.jpg\">
</div>

### Recent apps clearing behaviour

Normally when you swipe an app away, it won't close. Android handles that well on its own. On OnePlus this may however work in a different way. Recent app clear behaviour manager might be set up in a way that swiping the app to close will kill it.

<div class=\"img-block\">
  <img src=\"/assets/img/ss_oneplus_2a.jpg\">
  <img src=\"/assets/img/ss_oneplus_2b.jpg\">
</div>

"

developer_solution: "No known solution on the developer end"
links: "
- [Gadgethacks: Disable This Setting if Notifications Are Delayed on Your OnePlus](https://oneplus.gadgethacks.com/how-to/disable-setting-if-notifications-are-delayed-your-oneplus-0192639/)"
---
