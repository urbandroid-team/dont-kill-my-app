---
layout: page
title: API
permalink: apidoc
machine_name: api
menu: true
---

### Friend projects (big thanks!)

<div class="img-block" style="float:right">
  <img src="/assets/img/ss_doki.png">
</div>

#### [Doki](https://github.com/DoubleDotLabs/doki)

Doki by Double Dot Labs is an UI Library helping you with displaying information from DKMA. It provides a nice UI easily embeddable into your app if you do not want to mess with the APIs below.

#### [AutoStarter](https://github.com/judemanutd/AutoStarter)

AutoStart by Jude Fernandes is a library that helps bring up the autostart permission manager for different OEMs so that users can allows apps to start automatically. AOSP does not use this permission, but some OEMs (usually highly rated in our list) require it. This permission is essential for alarm clocks, calendars health tracking apps or anything which needs to work permanently or regularly on a device.

### URL API Parameters


#### App name

Use your app's real name in all guides and references


`https://dontkillmyapp.com?app=[MyAppName]`


##### Example


`https://dontkillmyapp.com?app=Sleep%20as%20Android`


#### Crap score icon

Choose negative score icon of your taste


`https://dontkillmyapp.com?[Icon_Number]`


##### Example

`https://dontkillmyapp.com?2`

Special case: Add `?0` parameter to prevent showing any score icon.


#### Combine parameters

`https://dontkillmyapp.com?2&app=Sleep` or `https://dontkillmyapp.com?app=Sleep&2` or `https://dontkillmyapp.com?app=Sleep&?2`


### JSON API


**Don't kill my app** provides a JSON API for developers to use on their websites or in their apps.


Please let us know when you use the API via e-mail at jiri.richter@urbandroid.org and give credit to dontkillmyapp.com.


### JSON API v2 docs


URL: https://dontkillmyapp.com/api/v2/[vendor].json


example: [https://dontkillmyapp.com/api/v2/nokia.json](https://dontkillmyapp.com/api/v2/nokia.json)


API v2 provides one JSON URL per vendor.


scheme:
````
{
  "name": "Human-readable vendor name",
  "manufacturer": ["name","alias1","alias2"],
  "url": "/relative-url-to-vendor",
  "award": number or null,
  "position": number or null,
  "explanation": "JSON-escaped HTML",
  "user_solution": "JSON-escaped HTML",
  "developer_solution": "JSON-escaped HTML"
}
````

Android example (Kotlin):
````
// Use this method in your ViewModel
fun getDKMAData() {

    viewModelScope.launch(Dispatchers.IO) {

        val result = try {
            val manufacturer = Build.MANUFACTURER.toLowerCase(Locale.ROOT).replace(" ", "-")
            val url = URL("https://dontkillmyapp.com/api/v2/$manufacturer.json")
            val json = JSONTokener(url.readText()).nextValue() as JSONObject?
            json?.getString("user_solution")?.replace(Regex("\\[[Yy]our app\\]"), yourAppName)
        } catch (e: Exception) {
            // Vendor not present in the DontKillMyApp list
            null
        }

        withContext(Dispatchers.Main) {
            when (result) {
                null -> TODO("Handle lack of result")
                else -> TODO("Pass back result to your UI here")
            }
        }

    }
}

// Use this method in your UI, Activity or Fragment
fun showData(result: String) {
    AlertDialog.Builder(context)
        .setTitle("How to make my app work")
        .setView(webview.loadDKMAData(result))
        .setPositiveButton(android.R.string.ok, null)
        .show()
}

fun WebView.loadDKMAData(result: String) {
    loadData(result, "text/html; charset=utf-8", "UTF-8")
    webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            loadUrl(url)
            return true
        }
    }
}
````



### JSON API v1 docs

URL: [https://dontkillmyapp.com/api/v1/output.json](https://dontkillmyapp.com/api/v1/output.json)

scheme:
````
{ "vendors" :
  [
    {
      "name": "Human-readable vendor name",
      "url": "/relative-url-to-vendor",
      "award": number or null,
      "explanation": "JSON-escaped HTML",
      "user_solution": "JSON-escaped HTML",
      "developer_solution": "JSON-escaped HTML"
    },
    {
      ...
    },
    {
      ...
    }
  ]
}
````

### Crap score badges


We encourage anyone to include DontKillMyApp badges to their sites or device reviews.


#### Example


````<a href="https://dontkillmyapp.com/nokia?2"><img width="306px" src="https://dontkillmyapp.com/badge/nokia2.svg"></a>````


<a href="https://dontkillmyapp.com/nokia?2"><img width="306px" src="https://dontkillmyapp.com/badge/nokia2.svg"></a>



````<a href="https://dontkillmyapp.com/xiaomi?3"><img width="306px" src="https://dontkillmyapp.com/badge/xiaomi3.svg"></a>````


<a href="https://dontkillmyapp.com/xiaomi?3"><img width="306px" src="https://dontkillmyapp.com/badge/xiaomi3.svg"></a>



#### URL format


Image source


````https://dontkillmyapp.com/badge/VENDOR+ICON_NUMBER.svg````

Link


````https://dontkillmyapp.com/VENDOR?ICON_NUMBER````





