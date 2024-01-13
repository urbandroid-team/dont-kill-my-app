---
manufacturer: 
    - sony

---

## STAMINA mode

### "Prefer battery time"

Newer Android versions on Sony devices have an extra "STAMINA level" setting under STAMINA mode. Avoid setting it to "Prefer battery time" if you need AOSP behaviour.

For older Android versions, never use STAMINA mode if you want your device to do something useful when not actively using it. Despite the official description, it does not affect only mobile data and WiFi, it also terminuates any background processes.
Stamine mode can be found (and disabled) at either *Battery* section or *Power management* section.

<div class="img-block">
  <figure>
    <img src="/assets/img/sony/sony_stamina.jpg">
    <figcaption>Older Androids.</figcaption>
  </figure>

  <figure>
    <img src="/assets/img/sony/sony_stamina2.png">
    <figcaption>Newer Androids.</figcaption>
  </figure>

</div>

## Adaptive battery

Adaptive battery was reported on Android 11, but it can be present on earlier versions too.

<div class="img-block">
  <figure>
    <img src="/assets/img/sony/sony_adaptive.png">
  </figure>

</div>



## Power-saving feature

The app you need to run in the background needs to be set as *Excepted* from Power-saving feature.

_System settings ​→ Apps & Notifications ​→ Advanced ​→ Special app access ​→ Power saving feature_

<div class="img-block">
  <figure>
    <img src="/assets/img/sony/sony_powersave.png">
    <figcaption>Switch Your app to <strong>Excepted</strong>.</figcaption>
  </figure>

</div>


## Battery optimisation

Try to make your app not battery optimized in *Phone settings > Battery > Three dots in the top right corner > Battery optimisation > Apps > your app*."
