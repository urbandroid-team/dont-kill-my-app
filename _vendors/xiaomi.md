---
name: Xiaomi
layout: vendor
award: 3
explanation: "
Traditionally Xiaomi and their Android customization called MIUI belongs to the most troubled on the market with respect to non-standard background process limitations and non-standard permissions. 
There are no APIs and no documentation for those extensions. In default settings background processing simply does not work right and apps using them will break.
"

user_solution: "

On MIUI 10 (Oreo) we have confirmed that the following settings make sleep tracking work correctly:

![OnePlus Settings](/assets/img/ss_xiaomi_1.jpg?raw=true "MIUI 10 Settings")

Other things to try, e.g. if you run an older version of MIUI:

## Battery management 

Please enable:
**Settings > Advanced Settings > Battery manager > Power plan** is set to **Performance**
**Device Settings > Advanced Settings > Battery Manager > Protected apps – Your app** needs to be **Protected**
**Device Settings > Apps > Sleep as Android > Battery > Power-intensive prompt [x]** and **Keep running after screen off [x]**

## Battery management Option 2
**Settings > Additional Settings > Battery & Performance > Manage apps** battery usage
and here:
* Switch Power Saving Modes to Off
* Choose the next options: **Saving Power** in the **Background > Choose apps > select ‘Your app’ > Background Settings > No restrictions**

## App battery saver
** Security > Battery > App Battery Saver > Your app name > No restriction**
then enable Your App to autostart in the security center

## Autostart
(according to [https://in.c.mi.com/thread-253478-1-0.html]):
Open **MIUI Security app > Permissions > auto-start**
Enable **Autostart** for your apps.

## App pinning
It really helps sleep tracking if you pin Sleep as Android. How to do it?
When you open recent apps tray, just drag Sleep as Android downwards – it will be locked. So even if you clear recent apps it will not clear from the background. Drag downwards again to clear Sleep from the background.
If all fails, please try to set the app as device administrator (Sleep as Android > Setting > CAPTCHA >Prevent escaping CAPTCHA). This may also prevent the system from killing sleep tracking.
"

developer_solution: "At the moment we don't know of any solution on dev end."
---

Xioami