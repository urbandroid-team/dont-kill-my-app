  {% assign sorted_vendors = site.vendors | sort: "award" | reverse %}
  {% for vendor in sorted_vendors limit: page.limit %}
    {
      "name": {{ vendor.name | jsonify }},
      "manufacturer": [{% for v in vendor.manufacturer %}"{{ v }}"{% unless forloop.last %},{% endunless %}{% endfor %}],
      "url": {{ vendor.url | jsonify }},
      "award": {{ vendor.award | jsonify }},
      "position": {{ vendor.position | jsonify }},
      "explanation": {{ vendor.explanation | markdownify | replace: 'src="/assets/img/',
          'src="https://dontkillmyapp.com/assets/img/' | jsonify }},
      "user_solution": {{ vendor.user_solution | markdownify | replace: 'src="/assets/img/',
          'src="https://dontkillmyapp.com/assets/img/' | jsonify }},
      "developer_solution": {{ vendor.developer_solution | markdownify | replace: 'src="/assets/img/',
          'src="https://dontkillmyapp.com/assets/img/' | jsonify }}
    }
  {% unless forloop.last %},{% endunless %}
  {% endfor %}