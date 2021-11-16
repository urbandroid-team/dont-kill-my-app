---
manufacturer: 
    - xiaomi

---


### MIUI 10 or lower

There seems to be a way for developers to check if autostart is enabled on MIUI devices below MIUI version 12 (tested on MIUI 11 and 10):

```java

    // the class name for MIUI autostart
    private static final String CLAZZ = "android.miui.AppOpsUtils";

    private static final int ENABLED = 0;

    private Object isAutoStartEnabled(Activity activity, String packageName) throws Exception {
        Class<?> clazz;
        try {
            clazz = Class.forName(CLAZZ);
        } catch (ClassNotFoundException ignored) {
            // we don't know if its enabled, class
            // is not found
            return false;
        }
        Method method = getMethod(clazz);
        if (method == null) {
            return false;
        }
        method.setAccessible(true);

        // the target object is null, because the
        // method is static
        Object result = method.invoke(null, activity, packageName);
        if (!(result instanceof Integer))
            throw new Exception();
        return (int) result == ENABLED;
    }

    private Method getMethod(Class<?> clazz) {
        try {
            return clazz.getDeclaredMethod("getApplicationAutoStart",
                    Context.class, String.class);
        } catch (Exception ignored) {
            // this should not happen, probably
            // MIUI version is updated
            return null;
        }
    }

```
In case the class is not found or some exception is raised, we could ignore that.
Also, it would be good to check the device name accordingly before invoking the method, in case if the device is not an MIUI device.
