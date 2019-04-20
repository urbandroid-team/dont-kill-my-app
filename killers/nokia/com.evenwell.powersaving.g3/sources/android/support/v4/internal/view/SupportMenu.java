package android.support.v4.internal.view;

import android.view.Menu;

public interface SupportMenu extends Menu {
    public static final int CATEGORY_MASK = -65536;
    public static final int CATEGORY_SHIFT = 16;
    public static final int USER_MASK = 65535;
    public static final int USER_SHIFT = 0;
}
