deril1/dont-kill-my-app
name: Oppo
manufacturer:
  - oppo
award: 3
position: 9
redirect_from: /vendors/samsung.html
explanation: "
We have currently only information for Oppo F1S, but on other models the situation may well be similar.
"

user_solution: "

## Oppo F1S

Background services are being killed (including accessibility services, which then need re-enabling) every time you turn the screen off. So far, a workaround for this is:


* Pin your app to the recent apps screen.

* Enable your app in the app list inside the security app's \"startup manager\" and \"floating app list\" (com.coloros.safecenter / com.coloros.safecenter.permission.Permission).

* Turn off battery optimizations.

* Give the service a persistent notification to remain in the foreground.


All four of those need to be done before the app would function.

Here are links to some other resources verifying that some of the above steps work on other Oppo devices:

* [XDA developers](https://forum.xda-developers.com/android/general/coloros-5-0-how-to-allow-apps-running-t3847738)

* [XDA developers](https://forum.xda-developers.com/find-X/help/killing-apps-screen-off-arghh-t3818105)

* [Oppo customer service portal](https://oppo-au.custhelp.com/app/answers/detail/a_id/1313/~/how-to-lock-applications-in-the-background%3F)

* [Quora](https://www.quora.com/How-do-you-add-apps-into-Whitelist-in-OPPO-F1s-phone)



"

developer_solution: "No known solution on the dev end"

---
