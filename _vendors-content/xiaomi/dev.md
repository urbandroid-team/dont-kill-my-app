---
manufacturer: 
    - xiaomi

---


### Check Autostart permission (MIUI)

There is a way to check if the Autostart has been enabled,
<br>
[https://github.com/XomaDev/MIUI-autostart](https://github.com/XomaDev/MIUI-autostart)


This has been tested on devices:

    - MIUI 10 (firebase)
    - MIUI 11 (physical device 11.0.9)
    - MIUI 12 (physical device 12.5)
    - MIUI 13 (untested, but works)
    - MIUI 14 (physical device 14.0.2)

### Gradle

Add the JitPack repository to your build file

```
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add the dependency

```
dependencies {
    implementation 'com.github.XomaDev:MIUI-autostart:v1.2'
}
```

### Kotlin

```kotlin
if (Utils.isOnMiui()) {
  val enabled: Boolean = Autostart.isAutoStartEnabled(context)
}
```

### Java

```java
if (Utils.INSTANCE.isOnMiui()) {
  boolean enabled = Autostart.INSTANCE.isAutoStartEnabled(context);
}
```
