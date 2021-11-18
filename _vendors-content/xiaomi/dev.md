---
manufacturer: 
    - xiaomi

---


### MIUI 11 or lower

There seems to be a way for developers to check if autostart is enabled on MIUI devices below MIUI version 12 (tested on MIUI 11 and 10):

```java
    public enum State {
        ENABLED, DISABLED, NO_INFO, UNKNOWN
    }

    // the class name for MIUI autostart
    private static final String CLAZZ = "android.miui.AppOpsUtils";

    private static final int ENABLED = 0;
    private static final int DISABLED = 1;

    private State getAutoStartState(Activity activity) throws Exception {
        Class<?> clazz;
        try {
            clazz = Class.forName(CLAZZ);
        } catch (ClassNotFoundException ignored) {
            // we don't know if its enabled, class
            // is not found, no info
            return State.NO_INFO;
        }
        final Method method = getMethod(clazz);
        if (method == null) {
            // exception raised while search the method,
            // or it doesn't exist
            return State.NO_INFO;
        }
        // the method is a public method, It's still
        // better to do this
        method.setAccessible(true);

        // the target object is null, because the
        // method is static
        final Object result = method.invoke(null, activity,
                activity.getPackageName());

        // the result should be an Int
        if (!(result instanceof Integer))
            throw new Exception();

        final int _int = (int) result;

        if (_int == ENABLED)
            return State.ENABLED;
        else if (_int == DISABLED)
            return State.DISABLED;
        return State.UNKNOWN;
    }

    private Method getMethod(Class<?> clazz) {
        try {
            return clazz.getDeclaredMethod("getApplicationAutoStart",
                    Context.class, String.class);
        } catch (Exception ignored) {
            // this should not happen, probably
            // MIUI version is updated, lets give a last try
            return null;
        }
    }

```

#### Usage

```java
public void checkMIUIAutoStart(Activity activity) throws Exception {
        if (getAutoStartState(activity) == State.DISABLED) {
            Toast.makeText(activity, "Auto-start is disabled.", Toast.LENGTH_SHORT).show();
        }
    }
```

In case the class is not found or some exception is raised, we could ignore that.
Also, it would be good to check the device name accordingly before invoking the method, in case if the device is not an MIUI device.
