adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh--
manufacturer: 
    - motorola

---


## Improve battery while inactive

A new option in some of the Motorola phones, enhanced by unknown AI algorithms. It is added by the *Battery care* app and does seem to kill the apps even if you disable the option in _Settings -> Battery -> Improve battery while inactive_. The only fix is to uninstall the com.motorola.batterycare package using adb: adb shell pm uninstall -k --user 0 com.motorola.batterycare.

<div class="img-block">
  <figure>
    <img src="/assets/img/motorola/moto_AI_battery.png">
    <figcaption>Improve battery while inactive</figcaption>
  </figure>

</div>


## Allow background usage

Another new option on newer Android version on Motorolas, hidden from the plain sight. In setting for each app, you can find *App battery usage* toggle. But it did not stop there, you need to tap the option with the toggle to get to the hidden menu with *Unrestricted battery usage*.

1. Go to your phone's settings -> Apps -> Find your app.
2. Open Apps battery usage.
3. Allow Background battery usage.
4. Tap the option with the toggle.
5. Set the battery usage as 'Unrestricted'.

<div class="img-block">
  <figure>
    <img src="/assets/img/motorola/moto_ai_per-app_1.png">
    <figcaption>Allow battery usage.</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/motorola/moto_ai_per-app_2.png">
    <figcaption>Tap the option Allow battery usage.</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/motorola/moto_ai_per-app_3.png">
    <figcaption>Set as 'unrestricted'.</figcaption>
  </figure>

</div>



## Adaptive battery

The Adaptive battery might be quite aggresive on some phones. It is designed for "infrequently used apps", but from the feedback it looks like the definition of infrequently used apps is quite wide.

1. Go to your phone's settings.
2. Open the Battery section.
3. Disable the Adaptive battery option.


## Background activity restrictions

There can be a restriction on background activity enabled for each app. 

1. Go to your phone's settings.
2. Scroll down and tap on 'Apps & notifications'.
3. Tap on the your app.
4. Tap on 'Advanced'.
5. Tap on 'Battery'.
6. Tap on 'Background restriction' or 'Background limits'.
7. If it says 'Background activity restricted', tap on it and then tap 'Remove'.

## Managing background apps

This option does not seem to be found in the layout, but rather with the search tool...

1. Go to phone preferences and type "managing background apps" in the search bar. This is kind of an app where you can select apps that you want to run in the background.
2. Switch the toggle to the active position to allow the app in the background.

<div class="img-block">
  <figure>
    <img src="/assets/img/motorola/moto_background_manager.jpg">
    <figcaption>Allow the app to run in the background.</figcaption>
  </figure>

</div>
