package android.support.v4.app;

import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class FragmentTransaction {
    public static final int TRANSIT_ENTER_MASK = 4096;
    public static final int TRANSIT_EXIT_MASK = 8192;
    public static final int TRANSIT_FRAGMENT_CLOSE = 8194;
    public static final int TRANSIT_FRAGMENT_FADE = 4099;
    public static final int TRANSIT_FRAGMENT_OPEN = 4097;
    public static final int TRANSIT_NONE = 0;
    public static final int TRANSIT_UNSET = -1;

    @Retention(RetentionPolicy.SOURCE)
    private @interface Transit {
    }

    public abstract FragmentTransaction add(@IdRes int i, Fragment fragment);

    public abstract FragmentTransaction add(@IdRes int i, Fragment fragment, @Nullable String str);

    public abstract FragmentTransaction add(Fragment fragment, String str);

    public abstract FragmentTransaction addSharedElement(View view, String str);

    public abstract FragmentTransaction addToBackStack(@Nullable String str);

    public abstract FragmentTransaction attach(Fragment fragment);

    public abstract int commit();

    public abstract int commitAllowingStateLoss();

    public abstract FragmentTransaction detach(Fragment fragment);

    public abstract FragmentTransaction disallowAddToBackStack();

    public abstract FragmentTransaction hide(Fragment fragment);

    public abstract boolean isAddToBackStackAllowed();

    public abstract boolean isEmpty();

    public abstract FragmentTransaction remove(Fragment fragment);

    public abstract FragmentTransaction replace(@IdRes int i, Fragment fragment);

    public abstract FragmentTransaction replace(@IdRes int i, Fragment fragment, @Nullable String str);

    public abstract FragmentTransaction setBreadCrumbShortTitle(@StringRes int i);

    public abstract FragmentTransaction setBreadCrumbShortTitle(CharSequence charSequence);

    public abstract FragmentTransaction setBreadCrumbTitle(@StringRes int i);

    public abstract FragmentTransaction setBreadCrumbTitle(CharSequence charSequence);

    public abstract FragmentTransaction setCustomAnimations(@AnimRes int i, @AnimRes int i2);

    public abstract FragmentTransaction setCustomAnimations(@AnimRes int i, @AnimRes int i2, @AnimRes int i3, @AnimRes int i4);

    public abstract FragmentTransaction setTransition(int i);

    public abstract FragmentTransaction setTransitionStyle(@StyleRes int i);

    public abstract FragmentTransaction show(Fragment fragment);
}
