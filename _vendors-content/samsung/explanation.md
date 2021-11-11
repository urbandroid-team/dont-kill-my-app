---
manufacturer: samsung

---

<div class='caution-box'>
Despite Android team promise to enforce OEMs to be transparent about non-standard app killing, in Android 11 Samsung has introduced a new severe (default ON) restriction. Apps can no longer hold wake lock in foreground services. This breaks many use-cases, for instance health apps are now unable to gather sensoric data for their users.
See details <a href="https://issuetracker.google.com/issues/179644471">here</a> and read below for workaround.<br>
<br>

UPDATE 04/2021: Is Samsung now killing even foreground services? Could this be real, or is it a chimera?<br>
Even disabling the system battery restrictions does not save the app from being killed. Let's find out, if it is a bug or a feature... <a href="https://github.com/urbandroid-team/dont-kill-my-app/issues/307#issuecomment-827649020">Here you can read more details</a><br>
<br>

</div>

We record significantly increased number of app killing on Samsung's since Android Pie flavor. The hints show adaptive battery being much more eager than in stock Android.<br>After 3 days any unused app will not be able to start from background (e.g. alarms will not work anymore). Imagine, you won't use your alarm clock for the weekend plus 1 day and bang! no alarms anymore and you miss work! We strongly suggest to turn off <strong>Adaptive battery</strong> and <strong>Put apps to sleep</strong> options per instructions below.
<br><br>
Important: The latest feedback suggests even when you remove an app from the restricted list, Samsung may re-add them later after a firmware update or when it thinks it is using too much resources!


Yes, Samsung - a dominant vendor in the Android market - is now using one of the nastiest battery saving techniques in the industry. They kill background processes and render alarm clocks and other apps which rely on background processing useless. See below for workarounds.
<br>
<br>
NOTE: It is very hard to keep up with all the changes in the system settings layout and their modifications across all the combinations of phones and Android versions. If you find a different layout, or different name, let us know.
