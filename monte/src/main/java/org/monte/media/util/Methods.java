

package org.monte.media.util;

import java.lang.reflect.*;



@SuppressWarnings("unchecked")
public class Methods {

    private Methods() {
    }


    public static Object invoke(Object obj, String methodName)
    throws NoSuchMethodException {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[0]);
            Object result = method.invoke(obj, new Object[0]);
            return result;
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }

    public static Object invoke(Object obj, String methodName, String stringParameter)
    throws NoSuchMethodException {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[] { String.class });
            Object result = method.invoke(obj, new Object[] { stringParameter });
            return result;
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }


    public static Object invokeStatic(Class clazz, String methodName)
    throws NoSuchMethodException {
        try {
            Method method =  clazz.getMethod(methodName,  new Class[0]);
            Object result = method.invoke(null, new Object[0]);
            return result;
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }

    public static Object invokeStatic(String clazz, String methodName)
    throws NoSuchMethodException {
        try {
            return invokeStatic(Class.forName(clazz), methodName);
        } catch (ClassNotFoundException e) {
            throw new NoSuchMethodException("class "+clazz+" not found");
        }
    }

    public static Object invokeStatic(Class clazz, String methodName, Class type, Object value)
    throws NoSuchMethodException {
        return invokeStatic(clazz,methodName,new Class[]{type},new Object[]{value});
    }

    public static Object invokeStatic(Class clazz, String methodName, Class[] types, Object[] values)
    throws NoSuchMethodException {
        try {
            Method method =  clazz.getMethod(methodName,  types);
            Object result = method.invoke(null, values);
            return result;
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }

    public static Object invokeStatic(String clazz, String methodName,
    Class[] types, Object[] values)
    throws NoSuchMethodException {
        try {
            return invokeStatic(Class.forName(clazz), methodName, types, values);
        } catch (ClassNotFoundException e) {
            throw new NoSuchMethodException("class "+clazz+" not found");
        }
    }

    public static Object invokeStatic(String clazz, String methodName,
    Class[] types, Object[] values, Object defaultValue) {
        try {
            return invokeStatic(Class.forName(clazz), methodName, types, values);
        } catch (ClassNotFoundException e) {
            return defaultValue;
        } catch (NoSuchMethodException e) {
            return defaultValue;
        }
    }


    public static int invokeGetter(Object obj, String methodName, int defaultValue) {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[0]);
            Object result = method.invoke(obj, new Object[0]);
            return ((Integer) result).intValue();
        } catch (NoSuchMethodException e) {
            return defaultValue;
        } catch (IllegalAccessException e) {
            return defaultValue;
        } catch (InvocationTargetException e) {
            return defaultValue;
        }
    }

    public static long invokeGetter(Object obj, String methodName, long defaultValue) {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[0]);
            Object result = method.invoke(obj, new Object[0]);
            return ((Long) result).longValue();
        } catch (NoSuchMethodException e) {
            return defaultValue;
        } catch (IllegalAccessException e) {
            return defaultValue;
        } catch (InvocationTargetException e) {
            return defaultValue;
        }
    }

    public static boolean invokeGetter(Object obj, String methodName, boolean defaultValue) {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[0]);
            Object result = method.invoke(obj, new Object[0]);
            return ((Boolean) result).booleanValue();
        } catch (NoSuchMethodException e) {
            return defaultValue;
        } catch (IllegalAccessException e) {
            return defaultValue;
        } catch (InvocationTargetException e) {
            return defaultValue;
        }
    }

    public static Object invokeGetter(Object obj, String methodName, Object defaultValue) {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[0]);
            Object result = method.invoke(obj, new Object[0]);
            return result;
        } catch (NoSuchMethodException e) {
            return defaultValue;
        } catch (IllegalAccessException e) {
            return defaultValue;
        } catch (InvocationTargetException e) {
            return defaultValue;
        }
    }

    public static boolean invokeStaticGetter(Class clazz, String methodName, boolean defaultValue) {
        try {
            Method method =  clazz.getMethod(methodName,  new Class[0]);
            Object result = method.invoke(null, new Object[0]);
            return ((Boolean) result).booleanValue();
        } catch (NoSuchMethodException e) {
            return defaultValue;
        } catch (IllegalAccessException e) {
            return defaultValue;
        } catch (InvocationTargetException e) {
            return defaultValue;
        }
    }

    public static Object invoke(Object obj, String methodName, boolean newValue)
    throws NoSuchMethodException {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[] { Boolean.TYPE} );
           return method.invoke(obj, new Object[] { newValue});
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }

    public static Object invoke(Object obj, String methodName, int newValue)
    throws NoSuchMethodException {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[] { Integer.TYPE} );
            return method.invoke(obj, new Object[] { newValue});
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }

    public static Object invoke(Object obj, String methodName, float newValue)
    throws NoSuchMethodException {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[] { Float.TYPE} );
            return method.invoke(obj, new Object[] { new Float(newValue)});
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }

    public static Object invoke(Object obj, String methodName, Class clazz, Object newValue)
    throws NoSuchMethodException {
        try {
            Method method =  obj.getClass().getMethod(methodName,  new Class[] { clazz } );
            return method.invoke(obj, new Object[] { newValue});
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            throw new InternalError(e.getMessage());
        }
    }

    public static Object invoke(Object obj, String methodName, Class[] clazz, Object... newValue)
    throws NoSuchMethodException {
        try {
            Method method =  obj.getClass().getMethod(methodName,  clazz );
            return method.invoke(obj, newValue);
        } catch (IllegalAccessException e) {
            throw new NoSuchMethodException(methodName+" is not accessible");
        } catch (InvocationTargetException e) {

            InternalError error = new InternalError(e.getMessage());
            error.initCause((e.getCause() != null) ? e.getCause() : e);
            throw error;
        }
    }

    public static void invokeIfExists(Object obj, String methodName) {
        try {
             invoke(obj, methodName);
        } catch (NoSuchMethodException e) {

        }
    }

    public static void invokeIfExists(Object obj, String methodName, float newValue) {
        try {
            invoke(obj, methodName, newValue);
        } catch (NoSuchMethodException e) {

        }
    }

    public static void invokeIfExists(Object obj, String methodName, boolean newValue) {
        try {
             invoke(obj, methodName, newValue);
        } catch (NoSuchMethodException e) {

        }
    }

    public static void invokeIfExists(Object obj, String methodName, Class clazz, Object newValue) {
        try {
             invoke(obj, methodName, clazz, newValue);
        } catch (NoSuchMethodException e) {

        }
    }


    public static void invokeIfExistsWithEnum(Object obj, String methodName, String enumClassName, String enumValueName) {
        try {
            Class enumClass = Class.forName(enumClassName);
            Object enumValue = invokeStatic("java.lang.Enum", "valueOf", new Class[] {Class.class, String.class},
                    new Object[] {enumClass, enumValueName}
            );
            invoke(obj, methodName, enumClass, enumValue);
        } catch (ClassNotFoundException e) {

            e.printStackTrace();
        } catch (NoSuchMethodException e) {

            e.printStackTrace();
        }
    }
}
