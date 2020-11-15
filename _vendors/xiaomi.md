---
name: Xiaomi note 9s curtana
subtitle: except Android One
manufacturer:
  - xiaomi
position: 4
award: 4
redirect_from: /vendors/xiaomi.html
explanation: "
Traditionally, Xiaomi and their Android customization called _MIUI_ belongs in the most troubled group on the market with respect to non-standard background process limitations and non-standard permissions.
There are no APIs and no documentation for those extensions. In default settings, background processing simply does not work right and apps using them will break.


> **Note**: Android One devices by Xiaomi work much better than MIUI-based devices. So, if you like Xiaomi, we definitely recommend looking for their Android One offering.
"

user_solution: '

### MIUI 11

To let your app run in the background, make sure settings for your app look like the following:

<div class="img-block">
  <img src="/assets/img/xiaomi/ss_miui11_batterysaversettings1.png">
  <img src="/assets/img/xiaomi/ss_miui11_batterysaversettings2.png">
</div>

### MIUI 10


To let your app run in the background, make sure your settings look like the following (here for example is Sleep as Android):


<div class="img-block">
  <img src="/assets/img/ss_xiaomi_1a.png">
  <img src="/assets/img/ss_xiaomi_1b.png">
  <img src="/assets/img/ss_xiaomi_1c.png">
</div>


### Power management


Please enable:

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


Enable \"Autostart\" for desired apps.

<div class="img-block">
  <img src="/assets/img/ss_xiaomi_as_1.png">
  <img src="/assets/img/ss_xiaomi_as_2.png">
    <div class="img-block">
     <figure>
          <img src="/assets/img/ss_xiaomi_as_3.png">
       <figcaption>Search for Your app and tap to enable</figcaption>
     </figure>
    </div>
</div>    

### App pinning

When you open the recent apps tray, drag your app downwards (it will be locked). So, even if you clear recent apps, it will not clear them from the background. Drag downwards again to clear your app from the background.

'

developer_solution: "

So far, no workarounds on the dev side are known.

"
---
