---
manufacturer: 
    - huawei

---


<div class='caution-box'>
<strong>UPDATE</strong>: On some phones with EMUI 9+ (Android P+) Huawei introduced a new task killer app called PowerGenie which kills everything not whitelisted by Huawei and does not give users any configuration options. See below how to uninstall it.
<br>
We have mixed reviews on Huawei - the PowerGenie app is present on some EMUI 9+ systems, while on others it isn't.
</div>


Traditionally Huawei and their Android customization called EMUI belongs to the most troubled on the market with respect to non-standard background process limitations.

There are no APIs and no documentation for those extensions. On default settings, background processing simply does not work right and apps working in background will break.


In some of the EMUI versions (we know about EMUI 4 at and we have some reports about EMUI 5 and now the latest EMUI 9) no user accessible settings can prevent the system to break background processing longer than 60 minutes. This is done by an evil custom service called HwPFWService (and in EMUI 9 this is called PowerGenie) developed and bundled with EMUI by Huawei.
