---
layout: page
title: API
permalink: /apidoc/
machine_name: api
---

**Don't kill my app** provides a JSON API at [https://dontkillmyapp.com/api/v1/output.json](https://dontkillmyapp.com/api/v1/output.json) for developers to use on their websites or in their apps.

If you use the API, please let us know via email at jiri.richter@urbandroid.org and give credit to dontkillmyapp.com.

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