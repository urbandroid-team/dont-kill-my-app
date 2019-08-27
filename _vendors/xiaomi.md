Mi A2 Lite
name: Xiaomi
subtitle: except Android One
manufacturer:
  - xiaomi
position: 5
award: 4
redirect_from: /vendors/xiaomi.html
explanation: "
Traditionally Xiaomi and their Android customization called MIUI belongs to the most troubled on the market with respect to non-standard background process limitations and non-standard permissions.
There are no APIs and no documentation for those extensions. In default settings background processing simply does not work right and apps using them will break.


> NOTE: Android One devices by Xiaomi work much better than MIUI-based devices. So if you liek Xioam we definitely recommend to look for their Android One offering.
"

user_solution: '

### MIUI 10


To let your app run successfully in the background, make sure your settings look like the following (here for example is Sleep as Android):


<div class="img-block">
  <img src="/assets/img/ss_xiaomi_1a.png">
  <img src="/assets/img/ss_xiaomi_1b.png">
  <img src="/assets/img/ss_xiaomi_1c.png">
</div>


### Power management


Please enable:yes


* *Settings > Advanced Settings > Battery manager > Power plan* is set to Performance

* *Device Settings > Advanced Settings > Battery Manager > Protected apps* – your app needs to be Protected

* *Device Settings > Apps > your app > Battery > Power-intensive prompt* and *Keep running after screen off*

* *Settings > Additional Settings > Battery & Performance > Manage apps’ battery usage* and here:

1. Switch Power Saving Modes to Off

2. Choose the next options: *Saving Power in The Background > Choose apps > select your app > Background Settings > No restrictions*


### App battery saver


*Security > Battery > App Battery Saver > your app > No restriction*


### Autostart

(according to [Xiaomi](https://in.c.mi.com/thread-253478-1-0.html):


Open *Security app > Permissions > Auto-start*


Enable Autostart for desired apps.

<div class="img-block">
  <img src="/assets/img/ss_xiaomi_as_1.png">
  <img src="/assets/img/ss_xiaomi_as_2.png">
    <div class="img-block">
     <figure>
          <img src="/assets/img/ss_xiaomi_as_3.png">
       <figcaption>Search for Your app and tap to enable</figcaption>
     </figure>
</div>


### App pinning

When you open recent apps tray, drag your app downwards – it will be locked. So even if you clear recent apps it will not clear from the background. Drag downwards again to clear your app from the background.

'

developer_solution: "At the moment we don't know of any solution on dev end."
---
