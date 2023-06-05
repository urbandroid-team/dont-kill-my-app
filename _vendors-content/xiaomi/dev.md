---
manufacturer: 
    - xiaomi

---


### Check Autostart permission

There is a way to check if the Autostart has been enabled,
<br>
[https://github.com/XomaDev/MIUI-autostart](https://github.com/XomaDev/MIUI-autostart)


This has been tested on devices:

    - MIUI 10 (firebase)
    - MIUI 11 (physical device 11.0.9)
    - MIUI 12 (physical device 12.5)
    - MIUI 13 (untested, but work)
    - MIUI 14 (physical device 14.0.2)


### Usage

```java
// make sure device is MIUI device, else an 
// exception will be thrown at initialization
Autostart autostart = new Autostart(applicationContext);

State state = autostart.getAutoStartState();

if (state == State.DISABLED) {
    // now we are sure that autostart is disabled
    // ask user to enable it manually in the settings app    
} else if (state == State.ENABLED) {
    // now we are also sure that autostart is enabled
}
```
