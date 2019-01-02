---
name: Stock Android
layout: vendor

explanation: "
Yes, even stock android may kill your app when doing a task valuable to the user. 
"


user_solution: "
## Android P

There a special option in **Settings > Apps > Your app > Advanced > Battery > Background restrictions**. If users accidentally enable this option it will break their apps. And users do enable that option!

## Pie and pre-Pie

Overall it is a good idea to make your app not battery optimized to ensure it gets the freedom it need in performing in the background. 
For that go to **Settings > Apps > Your app > Advanced > Battery > Battery optimization** change view to **All apps** search for your app and choose **not optimized**  

Make sure **Settings > Apps > Your app > Advanced > Battery > Background limitations** is not enabled. If the app is not yet optimized for Ore API level it will break their background processing.
"


developer_solution: "You can ask the user to make your app not battery optimized. See [https://developer.android.com/training/monitoring-device-state/doze-standby]"

---