---
manufacturer: 
    - xiaomi

---


### Check Autostart permission

There is a way to check the state of Autostart permission on MIUI devices programtically.
Devices (MIUI 10, 11, 12) are working for it.

https://github.com/XomaDev/MIUI-autostart


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
