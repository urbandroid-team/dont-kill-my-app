---
manufacturer: 
    - google
    - stock_android

---


## Android 14

If you see background processing issues, overall it is a good idea to make your app _not battery optimized_ to ensure it gets the freedom it needs to perform in the background.


For that:

1. Go to **Settings > Apps > _Your app_ > App battery usage**

2. Ensure that the toggle switch next to **Allow background usage** is enabled.

3. Tap on the words **Allow background usage**. This opens a "hidden" menu.

4. Choose **Unrestricted**.


## Android P

There's a special option in **Settings > Apps > Your app > Advanced > Battery > Background restrictions**. If users accidentally enable this option, it will break their apps. And users do enable that option!

## Pie and pre-Pie

If you see background processing issues, overall it is a good idea to make your app _not battery optimized_ to ensure it gets the freedom it needs to perform in the background.


For that:

1. Go to **Settings > Apps > Your app > Advanced > Battery > Battery optimization**

2. Change view to **All apps**

3. Search for your app

4. Choose **Not optimized**


## Android O

Make sure **Settings > Apps > Your app > Advanced > Battery > Background limitations** is not enabled. If the app is not yet optimized for Oreo API level it will break their background processing.


## If all fails


If all fails you can turn doze mode completely off in **Settings > Developer options**. (If you don't know how to enable developer options, a web search-engine should help.)
