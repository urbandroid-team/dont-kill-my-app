package android.support.v4.media;

import android.media.browse.MediaBrowser.MediaItem;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

class ParceledListSliceAdapterApi21 {
    private static Constructor sConstructor;

    ParceledListSliceAdapterApi21() {
    }

    static {
        ReflectiveOperationException e;
        try {
            sConstructor = Class.forName("android.content.pm.ParceledListSlice").getConstructor(new Class[]{List.class});
            return;
        } catch (ClassNotFoundException e2) {
            e = e2;
        } catch (NoSuchMethodException e3) {
            e = e3;
        }
        e.printStackTrace();
    }

    static Object newInstance(List<MediaItem> itemList) {
        ReflectiveOperationException e;
        Object result = null;
        try {
            result = sConstructor.newInstance(new Object[]{itemList});
        } catch (InstantiationException e2) {
            e = e2;
            e.printStackTrace();
            return result;
        } catch (IllegalAccessException e3) {
            e = e3;
            e.printStackTrace();
            return result;
        } catch (InvocationTargetException e4) {
            e = e4;
            e.printStackTrace();
            return result;
        }
        return result;
    }
}
