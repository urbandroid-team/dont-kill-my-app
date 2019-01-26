# Android vendors, don't kill my app!

### [dontkillmyapp.com](https://dontkillmyapp.com)

## What is this about?

Smartphones are getting more and more powerful, but the battery capacity is lagging behind. Vendors are always trying to squeeze some battery saving features into the firmware with each new Android release.

But some go so far that they break useful apps just to get a little more juice out of your device. This even gets so absurd that with some vendors (e.g. Nokia, Xiaomi, OnePlus or Huawei) our smart phones are becoming **dumbphones** again.

Dumbphones are unable to do any useful tasks for you in the background unless you actively use your device at the time. This affects most of the apps which are not just another browser window. Most affected are alarm clocks, health trackers, automation apps or simply anything which needs to do some job for you at a particular moment when you don't use your phone.

With Android 6 (Marshmallow), Google has introduced Doze mode to the base Android, in an attempt to unify battery saving across the various Android phones.

Unfortunately, vendors (e.g. Xiaomi, Huawei, OnePlus or even Samsung..) did not seem to catch that ball and they all have their own battery savers, usually very poorly written, saving battery only superficially with side effects.

Naturally users blame developers for their apps failing to deliver. But the truth is developers do the maximum they can. Always investigating new device specific hacks to keep their (your!) apps working. But in many cases they simply fall short as vendors have full control over processes on your phone.

This is the true aim of this site. **To help set things right whenever possible.** Communicate these issues with users and provide them with hacks, workarounds and guides to keep their apps working and making their lives easier.

## API

The website provides a JSON API at https://dontkillmyapp.com/api/v1/output.json for developers to use on their websites or in their apps.

If you use the API, please let us know via email at jiri.richter@urbandroid.org and give credit to dontkillmyapp.com.

### API v1 docs

URL: https://dontkillmyapp.com/api/v1/output.json

API v1 outputs information on all vendors in one big JSON. If you want one JSON URL per vendor, see API v2.

scheme:
````
{ "vendors" :
  [
    {
      "name": "Human-readable vendor name",
      "manufacturer": ["name","alias1","alias2"],
      "url": "/relative-url-to-vendor",
      "award": number or null,
      "position": number or null,
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

## Contribution

Pull requests are very welcome, as well as discussion using Github issues.

### Add a new vendor / edit existing vendor:

In _vendor folder, add or edit a xxxx.md file.

Template:

```
---
name: Nokia
layout: vendor
permalink: nokia
explanation: '<html or markdown here>'
user_solution: '<html or markdown here>'
developer_solution: '<html or markdown here>'
---

Y U say stock Android!?
```

### Award a vendor
Add
```
award: (int between 1 and 5)
```
variable to the vendor.md file you wish to award.

## Who started this project?

Ultimately, every indie Android developer is at least partly affected by this issue.

We at Urbandroid Team are affected heavily with our Sleep as Android app and we gathered so much information about hacks and workarounds that we felt the need to share the information. We started by contacting individual indie developers with offers to exchange information, which led to the idea of a more effective approach in the form of an open-source website.
Thank u
