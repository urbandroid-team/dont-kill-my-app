---
name: Stock Android
layout: vendor
permalink: stock_android
explanation: "
Yes, even stock Android may kill your app when doing a task valuable to the user.
"


user_solution: "

## Android P

There a special option in **Settings > Apps > Your app > Advanced > Battery > Background restrictions**. If users accidentally enable this option it will break their apps. And users do enable that option!

## Pie and pre-Pie

Overall it is a good idea to make your app not battery optimized to ensure it gets the freedom it needs to perform in the background.


For that:

1. Go to **Settings > Apps > Your app > Advanced > Battery > Battery optimization**

2. Change view to **All apps**

3. Search for your app

4. Choose **Not optimized**


Make sure **Settings > Apps > Your app > Advanced > Battery > Background limitations** is not enabled. If the app is not yet optimized for Oreo API level it will break their background processing.
"

developer_solution: "You can ask the user to make your app not battery optimized. See [https://developer.android.com/training/monitoring-device-state/doze-standby](https://developer.android.com/training/monitoring-device-state/doze-standby)"

---