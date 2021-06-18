---
name: Samsung
subtitle: Especially after Android P update
manufacturer:
  - samsung
award: 5
position: 1
redirect_from: /vendors/samsung.html
explanation: "
<div class='caution-box'>
UPDATE 04/2021: Is Samsung now killing even foreground services? Could this be real, or is it a chimera?<br>
[Here you can read more details](https://github.com/urbandroid-team/dont-kill-my-app/issues/307#issuecomment-827649020) <br>
<br>
Even disabling the system battery restrictions does not save the app from being killed. Let's find out, if it is a bug or a feature...

</div>

<div class='caution-box'>
UPDATE 2021: Despite Android team promise to enforce OEMs to be transparent about non-standard app killing, in Android 11 Samsung has introduced a new severe (default ON) restriction. Apps can no longer hold wake lock in foreground services. This breaks many use-cases, for instance health apps are now unable to gather sensoric data for their users.
See details <a href=\"https://issuetracker.google.com/issues/179644471\">here</a> and read below for workarounds.
</div>


We record significantly increased number of app killing on Samsung's Android Pie flavor. The hints show adaptive battery being much more eager than in stock Android.<br>After 3 days any unused app will not be able to start from background (e.g. alarms will not work anymore). Imagine, you won't use your alarm clock for a the weekend +1 day and bang! no alarms any more and you miss work! We strongly suggest to turn off <strong>Adaptive battery</strong> and <strong>Put apps to sleep</strong> options per instructions below.
<br><br>
Important: The latest feedback suggests even when those options get disabled, Samsung may re-enable them later after a firmware update!


Yes, Samsung - a dominant vendor in the Android market - is now using one of the nastiest battery saving techniques in the industry. They kill background processes and render alarm clocks and other apps which rely on background processing useless. See below for workarounds.
"

user_solution: "

1. [ Summary ](#what-optimization-apps-does-samsung-have) <br>

2. [ Android 11 ](#android-11) <br>

3. [ Galaxy S10 ](#galaxy-s10) <br>

4. [ Galaxy S9 ](#galaxy-s9) <br>

5. [ Galaxy S8 and later ](#galaxy-s8-and-later) <br>

6. [ Other Samsung phones ](#other-samsung-phones) <br>


## What optimization apps does Samsung have?

- **Android 11**: Battery optimization, Auto-optimize, Adaptive battery, Background restrictions, lists of Sleeping apps, Unused apps, Deep sleeping apps, Never sleeping apps

- **Android Pie and higher**: Device care, lists of Sleeping apps, Unused apps, Auto-disable unused apps

- **Android Oreo or Nougat**: Device maintenance

- **Android Marshmallow or below**: Smart manager


## Android 11

On Android 11 Samsung will prevent apps work in background by default unless you exclude apps from battery optimizations. This is a severe divergence from standard Android process management policies. To keep your apps working properly make sure you enable:<br>
_Settings > Apps > Your App > Battery > Battery optimization > All apps > Your app > Don't optimize_.<br>
Yes, this is a long way to go! Devs cannot ask for it automatically as they risk being kicked out from Play Store due to policy violations.

<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/samsung1.png\">
    <figcaption>Settings -> Apps, then select Your app</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/samsung3.png\">
    <figcaption>Your app -> Battery</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/samsung4.png\">
    <figcaption>Battery -> Battery optimization</figcaption>
  </figure>

</div>

<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/samsung5.png\">
    <figcaption>Settings -> Switch to All apps listing</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/samsung6.png\">
    <figcaption>Find Your app<br> switch off the battery optimization</figcaption>
  </figure>

</div>

## Galaxy S10

Battery optimization is *turned on by default*. At some unclear moments (maybe on app update, OS update?), the settings also do revert back to the defaults, forcing you to turn them off again and again.

### Sleeping apps

Sleeping apps menu is the sniper's nest for Samsung's app killing policies. Make sure to follow the instructions very carefully to prevent the apps from being killed.


Checklist:

* List of apps in *System settings > Device care > Battery* > (⁝) *menu > Settings*:

1. Disable **Put unused apps to sleep**

2. Disable **Auto-disable unused apps**

3. Remove your app from the list of Sleeping apps

4. Disable **background restrictions** for your app


<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/s10_1.jpg\">
    <figcaption>1. Start <strong>Device care</strong><br>from phone settings</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_2.jpg\">
    <figcaption>2. Tap Battery</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_3.jpg\">
    <figcaption>3. Tap the 3-dot menu > Settings</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_5.jpg\">
    <figcaption>4. Disable all toggles<br>(except Notifications)</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_6.jpg\">
    <figcaption>5. Tap \"Sleeping apps\"</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_8.jpg\">
    <figcaption>6. Wake up all the apps<br>using the trashcan icon</figcaption>
  </figure>

</div>

<div class=\"caution-box\">Warning: Make sure <strong>Put unused apps to sleep</strong> and <strong>Auto-disable unused apps</strong> is disabled. Otherwise, Samsung will put your apps back to sleep after a few days (3 by default) even if you have woken them up manually!</div>

On some phones, the same lists are placed in *Battery > Background usage limits*.

<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/S10_sleeping_10.jpg\">
    <figcaption>1. Open Battery > Background usage limits. </figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/S10_sleeping_2.png\">
    <figcaption>2. Check the lists.</figcaption>
  </figure>

</div>


### New options after update on Android 11

Apart from the various lists of apps, Android 11 on S10 has some new restrictions added:

* **Optimize battery usage** in *Settings > Apps* > (⁝) *menu > Special Access > Optimize battery usage*

* **Adaptive Battery** in *Battery > More battery settings*

* **Adaptive power saving** in *Battery* > (⁝) *menu > Automation*

* **Auto-optimize daily** in *Battery* > (⁝) *menu > Automation*

* **Auto start at set times** in *Battery* > (⁝) *menu > Automation*


#### Optimize battery usage

<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/s10_obu_1.png\">
    <figcaption>1. Open Apps section.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_obu_2.png\">
    <figcaption>2. Tap on the (⁝) menu.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_obu_3.png\">
    <figcaption>3. Choose Special Access.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_obu_4.png\">
    <figcaption>4. Open Optimiza battery usage.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_obu_5.png\">
    <figcaption>5. Expand the list to All apps.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_obu_6.png\">
    <figcaption>6. Toggle the apps.</figcaption>
  </figure>

</div>


#### Adaptive battery

<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/s10_ab_1.jpg\">
    <figcaption>1. Open Battery > (⁝) menu.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_ab_2.png\">
    <figcaption>2. Choose Automation.</figcaption>
  </figure>

</div>

#### Automation

<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/s10_battery_1.png\">
    <figcaption>1. Open Battery > (⁝) menu.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_battery_2.png\">
    <figcaption>2. Choose Automation.</figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s10_battery_3.png\">
    <figcaption>3. Adjust.</figcaption>
  </figure>

</div>



## Galaxy S9

Battery optimizations are *turned on by default*. It is possible the disabled restrictions might get revert after OS update or reboot.


Checklist:

* Disable **Put unused apps to sleep**

* Remove your app from the list of **Sleeping apps** - list of apps not allowed to run on the background

* Remove your app from the list of **Deep sleeping apps** - list of apps that can only work when you open them

* Add your app to the list of **Apps that won't be put to sleep**

* Disable **Auto-optimization**

<div class=\"img-block\">
  <figure>
    <img src=\"/assets/img/samsung/s9_1.png\">
    <figcaption>1. Start <strong>Device care</strong><br>from phone settings. </figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s9_2.png\">
    <figcaption>2. Tap <strong>Battery</strong>. </figcaption>
  </figure>

   <figure>
       <img src=\"/assets/img/samsung/s9_3.png\">
       <figcaption>3. Open <strong>App power management</strong>. </figcaption>
     </figure>

   <figure>
       <img src=\"/assets/img/samsung/s9_4.png\">
       <figcaption>4. Disable the option <br><strong>Put unused apps to Sleep </strong>. </figcaption>
     </figure>

   <figure>
       <img src=\"/assets/img/samsung/s9_5.png\">
       <figcaption>5. Remove your app from<br> the lists in <strong>Sleeping apps</strong><br>and <strong>Deep sleeping apps</strong>. </figcaption>
     </figure>

   <figure>
       <img src=\"/assets/img/samsung/s9_6.png\">
       <figcaption>6. Add you app to the list<br> in <strong>Apps that won't be put to sleep</strong>. </figcaption>
     </figure>

  <figure>
    <img src=\"/assets/img/samsung/s9_7.png\">
    <figcaption>7. Go back to <strong>Device care</strong><br> and tap the 3-dot menu. </figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s9_8.png\">
    <figcaption>8. Tap on <strong>Advanced</strong>. </figcaption>
  </figure>

  <figure>
    <img src=\"/assets/img/samsung/s9_9.png\">
    <figcaption>9. Disable <strong>Auto-optimization</strong>. </figcaption>
  </figure>

</div>

<div class=\"caution-box\">Warning: Make sure <strong>Put unused apps to sleep</strong> is disabled. Otherwise, Samsung will put your apps back to sleep after a few days (3 by default) even if you have woken them up manually!</div>

## Galaxy S8 and later

With the introduction of their flagship Galaxy S8 (and with some earlier experiments), Samsung has introduced a flawed attempt at prolonging battery life called **App power monitor**.


For your apps to work correctly, please whitelist them in **App power monitor**.


How to do it:


Open the *Settings > Device maintenance > Battery*, and at the bottom you’ll see a list of your most frequently used apps. You can manage apps individually or in a group by selecting them then tapping the big **Save power** button. Apps that are sleeping will appear in the **Sleeping apps** list at the bottom (tap it to expand the list). Scrolling further — all the way to the very bottom — and you’ll find **Unmonitored apps**. These are apps that you specifically want to exclude (whitelist) from **App power monitor** evil reach.


When inside the **Unmonitored apps** menu, you can tap the 3-dot menu to add or delete apps from the list. Rather than bothering with any of that, you can just turn off the **App power monitor** feature completely as it has little-to-no impact on battery life and only serves to handicap the normal functioning of your Galaxy device.


It’s excessive and in some cases downright misleading, using scare tactics to keep you reliant on Samsung’s software when other Android devices get by just fine without it.

## Other Samsung phones

On other Samsung phones, the path may look like this:<br>

*Phone settings > Applications > select three dot menu (top right corner) > Special Access > Optimize Battery usage >* Find your app on the list and make sure that it is not selected.

> Note: If you enable \"Edge Lighting\" for your app, the app will not be able to wake up your screen. To allow your app to wake up your screen, please remove it from the Edge Lighting applications list.

"

developer_solution: "No known solution on dev end"

---
