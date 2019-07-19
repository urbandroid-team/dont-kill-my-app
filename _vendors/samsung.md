J4+
name: Samsung
manufacturer:
  - samsung
award: 3
position: 7
redirect_from: /vendors/samsung.html
explanation: "
Yes, even Samsung - a dominant vendor in the Android market - is using nasty battery saving technique which may kill background processes and render alarm clocks useless. See below for workarounds.
"

user_solution: '

## Galaxy S9 / S10

Battery optimization is *turned on by default*. At some unclear moments (maybe on app update, OS update?), the settings also do revert back to the defaults, forcing you to turn them off again and again.

Below are walkthrough screenshots for Galaxy S10. Please let us know your feedback about whether it works for S9 (or others) as well. Thanks!

### Sleeping apps

<div class="img-block">
  <figure>
    <img src="/assets/img/samsung/s10_1.jpg">
    <figcaption>1. Start <strong>Device care</strong><br>from phone settings</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/samsung/s10_2.jpg">
    <figcaption>2. Tap Battery</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/samsung/s10_3.jpg">
    <figcaption>3. Tap the 3-dot menu > Settings</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/samsung/s10_5.jpg">
    <figcaption>4. Disable all toggles<br>(except Notifications)</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/samsung/s10_6.jpg">
    <figcaption>5. Tap Sleeping apps</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/samsung/s10_8.jpg">
    <figcaption>6. Wake up all the apps<br>using the trashcan icon</figcaption>
  </figure>

</div>

## Galaxy S8 (j6ltedx) and later

With the introduction of their flagship Galaxy S8 (and with some earlier experiments), Samsung has introduced a flawed attempt at prolonging battery life called **App power monitor**.


For your apps to work correctly, please whitelist them in **App power monitor**.


How to do it:


Open the **Settings > Device maintenance > Battery** and at the bottom you’ll see a list of your most frequently used apps. You can manage apps individually or in a group by selecting them then tapping the big **Save power** button. Apps that are sleeping will appear in the **Sleeping apps** list at the bottom (tap it to expand the list). Scrolling further — all the way to the very bottom — and you’ll find **Unmonitored apps**. These are apps that you specifically want to exclude (white list) from **App power monitor** evil reach.


When inside the **Unmonitored apps** menu, you can tap the 3-dot menu to add or delete apps from the list. Rather than bothering with any of that, you can just turn off the **App power monitor** feature completely as it has little-to-no impact on battery life and only serves to handicap the normal functioning of your Galaxy phone.


It’s excessive and in some cases downright misleading, using scare tactics to keep you reliant on Samsung’s software when other Android devices get by just fine without it.

## On other Samsung phones, the path may look like this:

**Phone settings > Applications > select three dot menu (top right corner) > Special Access > Optimize Battery usage >** Find your app on the list and make sure that it is not selected.

> NOTE:  If you enable Edge Lighting for your app, then the app will not be able to wake up your screen. To allow your app to wake up your screen, please remove it from the Edge Lighting applications list.

'

developer_solution: "No known solution on developer end"

---
