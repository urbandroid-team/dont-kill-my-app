---
制造商：
-华为

---


###在部分EMUI8、9和10设备上发布应用程序(华为P20、华为P20Lite、华为Mate10...)

* *手机设置>电池>应用启动*然后将应用程序设置为“手动管理”，并确保所有功能都已打开。

<div班级="img-block">
  <数字>
    <IMGsrc="/assets/img/huawei/ss_huawei_app_launch_1.PNG">
    <figcaption>1.*手机设置>电池>应用程序启动*。此功能可能对所有设备可用，也可能不适用于所有设备或标签不同。</figcaption>
  </数字>

  <数字>
    <IMGsrc="/assets/img/huawei/ss_huawei_app_launch_3.PNG">
    <figcaption>2、关闭“全部自动管理”</figcaption>
  </数字>

  <数字>
    <IMGsrc="/assets/img/huawei/ss_huawei_app_launch_4.PNG">
    <figcaption>3.确保启用！所有切换。</figcaption>
  </数字>

</div>

同样，为了可靠的后台进程，您可能需要卸载PowerGenie，如下所述。


###启动管理器

启动管理器似乎是一个新的孩子在块，防止应用程序自动运行后，手机启动。

*进入【设置】>【全部】>【启动管理器】。

<div班级="img-block">
  <数字>
    <IMGsrc="/assets/img/huawei/startup.jpg">
    <figcaption>1、*手机设置->全部->启动管理器*，允许应用。</figcaption>
  </数字>
</div>

###EMUI9+设备

####经典电池优化

打开设置，搜索并访问电池优化。触摸“不允许”旁边的小倒三角，触摸“所有应用程序”，定位并触摸您的应用程序，然后选择“不允许”。

####PowerGenie

华为在破解其设备上的应用程序方面极具创造力。除了下文介绍的所有非标准电源管理措施外，他们还在Android Pie上的EMUI9中引入了一款新的任务杀手应用程序。


It is called <b>PowerGenie</b> and it kills all apps that are not on its whitelist. You cannot add custom apps on their pre-defined whitelist. This means there is no other way to fix proper app functionality on Huawei than uninstalling PowerGenie.



Unfortunately this is a system app and can only be fully uninstalled using ADB (Android Debug Bridge) Source: [XDA](https://forum.xda-developers.com/mate-20-pro/themes/remove-powergenie-to-allow-background-t3890409).


You need to:


1. [install ADB](https://www.xda-developers.com/install-adb-windows-macos-linux/) on your computer


2. Connect your phone with a data cable


3. Enable [Developer options](https://developer.android.com/studio/debug/dev-options.html)


4. Enable USB debugging within Developer options on your device


5. Run the following commands on your computer:

`adb shell pm uninstall -k --user 0 com.huawei.powergenie`

`adb shell pm uninstall -k --user 0 com.huawei.android.hwaps`

If apps keep getting killed try running `adb shell am stopservice hwPfwService`.

We did not yet have this confirmed but it is possible you can alternatively just disable PowerGenie in *Phone settings > Apps*. This setting would need to be re-applied every time you reboot your device.


<div class="caution-box">
Please still follow the steps below - Huawei phones usually have multiple powersaving mechanisms.
<br><br>
Also, you may not have PowerGenie on your device, but your apps may still get killed by another mechanism.
</div>

### EMUI 5.X and 8.X

#### Classic battery optimization

Open Settings, and search for and access Ignore battery optimization. Touch the little inverted triangle next to Allow, touch All apps, locate and touch the app, and select Allow.

### EMUI 6+ devices (and some EMUI 5 devices)

* *Phone settings > Advanced Settings > Battery manager > Power plan* set to *Performance*

* *Phone Settings > Advanced Settings > Battery Manager > Protected apps* – set your app as *Protected*

* *Phone Settings > Apps > Your app > Battery > Power-intensive prompt* [uncheck] and *Keep running after screen off* [check]

* *Phone settings > Apps > Advanced (At the bottom) > Ignore optimisations >* Press *Allowed > All apps >* Find your app on the list and set to *Allow*


### Huawei P9 Plus

* *Phone settings > Apps > Settings > Special access > Ignore battery optimisation >* select allow for your app.


### Huawei Honor 9 Lite, Huawei Mate 9 Pro

* *Phone settings > Battery > Launch* and then set your app to “Manage manually” and make sure everything is turned on.

On EMUI 4 there is no way out, sorry, but you can ask developers of your apps to implement the workaround described in <a href="#developer-solution-section">Developer section</a>
