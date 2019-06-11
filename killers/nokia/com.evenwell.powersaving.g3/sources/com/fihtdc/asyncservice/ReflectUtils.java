package com.fihtdc.asyncservice;

import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {
    public static final String TAG = "BackupRestoreService/ReflectUtils";

    public static Object getFieldValue(Object obj, String fieldName) {
        return getFieldValue(obj.getClass(), obj, fieldName);
    }

    public static Object getFieldValue(Class<?> clazz, Object obj, String fieldName) {
        Object ret = null;
        try {
            ret = getFieldValueOrThrow(clazz, obj, fieldName);
        } catch (Exception e) {
            Log.w(TAG, "Exception occured when get field value", e);
        }
        return ret;
    }

    public static Object getFieldValueOrThrow(Object obj, String fieldName) {
        return getFieldValueOrThrow(obj.getClass(), obj, fieldName);
    }

    public static Object getFieldValueOrThrow(Class<?> clazz, Object obj, String fieldName) {
        Object ret = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            if (field != null) {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                ret = field.get(obj);
            }
            return ret;
        } catch (Exception e) {
            Log.w(TAG, "Exception occured when get field value", e);
            throw new RuntimeException("Exception occured when get field value", e);
        }
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        return invokeMethod(obj.getClass(), obj, methodName, parameterTypes, args);
    }

    public static Object invokeMethod(Class<?> clazz, Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        Object ret = null;
        try {
            ret = invokeMethodOrThrow(clazz, obj, methodName, parameterTypes, args);
        } catch (Exception e) {
            Log.w(TAG, "Exception occured when invoke method", e);
        }
        return ret;
    }

    public static Object invokeMethodOrThrow(Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        return invokeMethodOrThrow(obj.getClass(), obj, methodName, parameterTypes, args);
    }

    public static Object invokeMethodOrThrow(Class<?> clazz, Object obj, String methodName, Class<?>[] parameterTypes, Object[] args) {
        Object ret = null;
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            if (method != null) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                ret = method.invoke(obj, args);
            }
            return ret;
        } catch (Exception e) {
            Log.w(TAG, "Exception occured when invoke method", e);
            throw new RuntimeException("Exception occured when invoke method", e);
        }
    }
}
