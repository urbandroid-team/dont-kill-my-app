---
layout: page
title: API
permalink: apidoc
machine_name: api
---

**Don't kill my app** provides a JSON API at [https://dontkillmyapp.com/api/v1/output.json](https://dontkillmyapp.com/api/v1/output.json) for developers to use on their websites or in their apps.

If you use the API, please let us know via email at jiri.richter@urbandroid.org and give credit to dontkillmyapp.com.

### API v2 docs

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

Android example (Java):
````
new AsyncTask<Void, Void, String>() {
    @Override
    protected String doInBackground(Void... voids) {
        try {
            return ((JSONObject) new JSONTokener(
                InputStreamUtil.read(new URL("https://dontkillmyapp.com/api/v2/"+Build.MANUFACTURER.toLowerCase().replaceAll(" ", "-")+".json").openStream())).nextValue()
              ).getString("user_solution").replaceAll("\\[[Yy]our app\\]", context.getString(R.string.app_name));
        } catch (Exception e) {
            // This vendor is not in the DontKillMyApp list
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            WebView wv = new WebView(context);
            wv.loadData(result, "text/html; charset=utf-8", "UTF-8");
            wv.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });

            new AlertDialog.Builder(context)
                    .setTitle("How to make my app work")
                    .setView(wv).setPositiveButton(android.R.string.ok, null).show();
        }
    }
}.execute();

````



### API v1 docs

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





