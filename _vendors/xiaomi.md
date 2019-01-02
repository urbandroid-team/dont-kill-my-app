---
name: Xiaomi
layout: vendor
explanation: 'Xiaomi are one of the most troubled phones on the market. Chinese vendors tend to ignore Android best practices and implement very bizarre custom modifications which make background tasks nearly impossible to run.'
user_solution: '

### MIUI 10

To let [your app] run successfully in the background, make sure your settings look like the following (here for example is Sleep as Android):
<img src="/assets/img/miui-guide.png">


### Power management

Please enable:

* *Settings > Advanced Settings > Battery manager > Power plan* is set to Performance

* *Device Settings > Advanced Settings > Battery Manager > Protected apps* – [your app] needs to be Protected

* *Device Settings > Apps > [your app] > Battery > Power-intensive prompt* and *Keep running after screen off*

* *Settings > Additional Settings > Battery & Performance > Manage apps’ battery usage* and here:

1. Switch Power Saving Modes to Off

2. Choose the next options: *Saving Power in The Background > Choose apps > select [your app] > Background Settings > No restrictions*


### App battery saver

*Security > Battery > App Battery Saver > [your app] > No restriction*

### Autostart

(according to [Xiaomi](https://in.c.mi.com/thread-253478-1-0.html):

Open *Security app > Permissions > Auto-start*

Enable Autostart for desired apps.

### App pinning

When you open recent apps tray, drag [your app] downwards – it will be locked. So even if you clear recent apps it will not clear from the background. Drag downwards again to clear [your app] from the background.

'
developer_solution: ''
---
