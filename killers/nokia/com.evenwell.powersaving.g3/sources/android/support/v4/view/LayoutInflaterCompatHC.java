package android.support.v4.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.LayoutInflater.Factory2;
import android.view.View;
import java.lang.reflect.Field;

class LayoutInflaterCompatHC {
    private static final String TAG = "LayoutInflaterCompatHC";
    private static boolean sCheckedField;
    private static Field sLayoutInflaterFactory2Field;

    static class FactoryWrapperHC extends FactoryWrapper implements Factory2 {
        FactoryWrapperHC(LayoutInflaterFactory delegateFactory) {
            super(delegateFactory);
        }

        public View onCreateView(View parent, String name, Context context, AttributeSet attributeSet) {
            return this.mDelegateFactory.onCreateView(parent, name, context, attributeSet);
        }
    }

    LayoutInflaterCompatHC() {
    }

    static void setFactory(LayoutInflater inflater, LayoutInflaterFactory factory) {
        Factory2 factory2 = factory != null ? new FactoryWrapperHC(factory) : null;
        inflater.setFactory2(factory2);
        Factory f = inflater.getFactory();
        if (f instanceof Factory2) {
            forceSetFactory2(inflater, (Factory2) f);
        } else {
            forceSetFactory2(inflater, factory2);
        }
    }

    static void forceSetFactory2(LayoutInflater inflater, Factory2 factory) {
        if (!sCheckedField) {
            try {
                sLayoutInflaterFactory2Field = LayoutInflater.class.getDeclaredField("mFactory2");
                sLayoutInflaterFactory2Field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "forceSetFactory2 Could not find field 'mFactory2' on class " + LayoutInflater.class.getName() + "; inflation may have unexpected results.", e);
            }
            sCheckedField = true;
        }
        if (sLayoutInflaterFactory2Field != null) {
            try {
                sLayoutInflaterFactory2Field.set(inflater, factory);
            } catch (IllegalAccessException e2) {
                Log.e(TAG, "forceSetFactory2 could not set the Factory2 on LayoutInflater " + inflater + "; inflation may have unexpected results.", e2);
            }
        }
    }
}
