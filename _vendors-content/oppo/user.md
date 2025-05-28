---
manufacturer: 
    - oppo

---

## Oppo Reno 

Background services are being killed (including accessibility services, which then need re-enabling) every time you turn the screen off. So far, a workaround for this is:


* Pin your app to the recent apps screen.

* Enable your app in the app list inside the security app's "startup manager" and "floating app list" (com.coloros.safecenter / com.coloros.safecenter.permission.Permission).

* Turn off battery optimizations.

* Give the service a persistent notification to remain in the foreground.

All four of those need to be done before the app would function.

<div class="img-block">
  <figure>
    <img src="/assets/img/oppo/oppo_autolaunch1.jpg">
    <figcaption>Open App management.</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/oppo/oppo_autolaunch2.jpg">
    <figcaption>Toggle to allow Your app.</figcaption>
  </figure>

</div>

<div class="img-block">
  <figure>
    <img src="/assets/img/oppo/oppo_background1.jpg">
    <figcaption>App info -> Battery usage</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/oppo/oppo_background2.jpg">
    <figcaption>Choose Run in background.</figcaption>
  </figure>

</div>

Here are links to some other resources verifying that some of the above steps work on other Oppo devices:

* [XDA developers](https://forum.xda-developers.com/android/general/coloros-5-0-how-to-allow-apps-running-t3847738)

* [XDA developers](https://forum.xda-developers.com/find-X/help/killing-apps-screen-off-arghh-t3818105)

* [Oppo customer service portal](https://oppo-au.custhelp.com/app/answers/detail/a_id/1313/~/how-to-lock-applications-in-the-background%3F)

* [Quora](https://www.quora.com/How-do-you-add-apps-into-Whitelist-in-OPPO-F1s-phone)

## Allow Auto Start-up

On Color OS 6, you need to enable automatic start from the background in the App's info page.

<div class="img-block">
  <figure>
    <img src="/assets/img/oppo/oppo_autostart.jpg">
    <figcaption>App info -> Allow Auto Start-up</figcaption>
  </figure>

</div>

## Power Saver modes

You can choose one of three power-saving options under each app and allow the app to run in the background. If something like smart restriction is enabled and if the app is not cleared from the tasks, it should be running background processes but if the app is closed, the background services are paused and are resumed immediately when the app is opened again.

<div class="img-block">
  <figure>
    <img src="/assets/img/oppo/oppo_power_saver_1.jpg">
    <figcaption>App info ->Power Saver</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/oppo/oppo_power_saver_2.jpg">
    <figcaption>Choices for the Power saver modes.</figcaption>
  </figure>

</div>
