---
制造商：
-一加

---

###应用程序锁定

将应用锁定在最近的应用中，可以避免应用在后台被杀，*电池优化*要恢复的设置。
<溴>

然而，这并不是100%。您可能必须每隔一段时间检查系统设置。看见[向OnePlus提交错误报告](https://forums.oneplus.com/threads/in-battery-optimisation-apps-are-getting-automatically-switched-from-not-optimised-to-optimised.849162/).


1.启动您要保护的应用程序。去*近期应用*-向上滑动并按住以打开最近的应用程序(应用程序切换器)。

2.长按窗口上的任何位置，然后轻敲*锁*应用程序右上角的按钮。

3.已锁定的应用程序将具有已关闭的挂锁图标。


<div班级="img-block">
  <IMGsrc="/assets/img/oneplus_locking.jpg">
</div>

###电池优化

-关闭**系统设置-应用程序-Gear图标-特殊访问-电池优化**.
<溴>
<小的>**警告：**最近，OnePlus手机开始为随机应用程序随机恢复此设置。因此，如果您将其设置为*未优化*，第二天可能会回到*优化的*.
<溴>
以避免系统自动恢复*未优化*设置，您还必须将应用锁定到“最近使用的应用”列表中，请参阅上面的解决方案或[在这里](https://forum.xda-developers.com/showpost.php?p=78588761&postcount=7).
<溴>


-关闭**系统设置>电池>电池优化**，切换到右上角菜单中的“所有应用程序”**>您的应用>不优化**
<溴>
<小的>**笔记：**我们的一些用户指出，在1+3和更早版本中，您需要在Developer选项中禁用Doze模式。</小的>


###应用程序自动启动

应用程序自动启动(在部分OnePlus手机上)实际上会阻止应用程序在后台工作。请为您的应用程序禁用它。

<div班级="img-block">
  <IMGsrc="/assets/img/ss_oneplus_1.jpg">
</div>


### Enhanced / Advanced optimization

- OnePlus 6 and further: 
<br>
**System settings > Battery > Battery optimization > (three dots) > Advanced optimization**. 
<br>
You'll see two options there. Both are enabled by default:

1. Deep optimization or Adaptive Battery
<br>
This is the main app killer. 
If you need any apps to run in background, disable it.

2. Sleep standby optimization
<br>
OnePlus tries to learn when you are usually asleep, and in those times it will then disable the phone's network connections. 
This setting will prevent push notifications from being delivered.






- OnePlus below 6: 
<br>
Turn off **System settings > Battery > Battery optimization > (three dots) > Enhanced optimization**.
<br>
<small>**NOTE:** This should help with the problem where you lose Bluetooth connection to your smartwatch / fitness tracker (e.g. for sleep tracking).</small>

<div class="img-block">
  <img src="/assets/img/ss_oneplus6_setting_sleepstandby.jpg">
</div>

### Recent apps clearing behaviour

Normally when you swipe an app away, it won't close. Android handles that well on its own. On OnePlus this may however work in a different way. Recent app clear behaviour manager might be set up in a way that swiping the app to close will kill it.

<div class="img-block">
  <img src="/assets/img/ss_oneplus_2a.jpg">
  <img src="/assets/img/ss_oneplus_2b.jpg">
</div>
