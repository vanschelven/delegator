/*
 * Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com Copyright
 * (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl
 */
package org.cq2.delegator.method;

import java.lang.reflect.Method;
import java.util.Set;

import sun.reflect.ReflectionFactory;

public class MethodUtil {
    public static void addMethods(Class theClass, Set methodSet) {
        Class[] interfaces = theClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            addMethods(interfaces[i], methodSet);
        }
        Class superclass = theClass.getSuperclass();
        if (superclass != null)
            addMethods(superclass, methodSet);
        Method[] methods = theClass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            method.setAccessible(true);
            methodSet.remove(method);
            methodSet.add(method);
        }
    }

    //differs from class.getDeclaredMethod in the acceptance of less specific
    // types as well
    public static Method getDeclaredMethod(Class clazz, String name, Class[] parameterTypes, Class[] exceptionTypes) {
        Method method = searchMethods(clazz.getMethods(), name, parameterTypes, exceptionTypes);
        return method;
    }
    
    private static Method searchMethods(Method[] methods,
            String name, Class[] parameterTypes, Class[] exceptionTypes) {
        Method res = null;
        String internedName = name.intern();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName() == internedName
                    && parameterTypesMatch(parameterTypes, m
                            .getParameterTypes())
                    && exceptionTypesMatch(exceptionTypes, m.getExceptionTypes())
                    && (res == null || res.getReturnType().isAssignableFrom(
                            m.getReturnType())))
                res = m;
        }

        return (res == null ? res : getReflectionFactory().copyMethod(res));
    }

    private static boolean exceptionTypesMatch(Class[] desiredExceptionTypes, Class[] actualExceptionTypes) {
        if (desiredExceptionTypes == null) {
            return actualExceptionTypes == null || actualExceptionTypes.length == 0;
        }

        if (actualExceptionTypes == null) {
            return true;
        }

        for (int i = 0; i < actualExceptionTypes.length; i++) {
            boolean found = false;
            for (int j = 0; j < desiredExceptionTypes.length; j++) {
                if (desiredExceptionTypes[j].isAssignableFrom(actualExceptionTypes[i])) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    // Fetches the factory for reflective objects
    private static ReflectionFactory getReflectionFactory() {
        if (reflectionFactory == null) {
            reflectionFactory =  (ReflectionFactory)
                java.security.AccessController.doPrivileged
                    (new sun.reflect.ReflectionFactory.GetReflectionFactoryAction());
        }
        return reflectionFactory;
    }
    private static ReflectionFactory reflectionFactory;

    private static boolean parameterTypesMatch(Class[] desiredParameterTypes, Class[] actualParameterTypes) {
        if (desiredParameterTypes == null) {
            return actualParameterTypes == null || actualParameterTypes.length == 0;
        }

        if (actualParameterTypes == null) {
            return desiredParameterTypes.length == 0;
        }

        if (desiredParameterTypes.length != actualParameterTypes.length) {
            return false;
        }

        for (int i = 0; i < desiredParameterTypes.length; i++) {
            if (!isSomeSuperclass(actualParameterTypes[i], desiredParameterTypes[i])) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isSomeSuperclass(Class superClass, Class subClass) {
        if ((superClass).isAssignableFrom(subClass)) return true;
        if (subClass.isPrimitive()) return superClass.isAssignableFrom(getWrapperClass(subClass));
        return false;
    }

    private static Class getWrapperClass(Class c) {
        if (c.equals(Integer.TYPE)) {
            return Integer.class;
        } else if (c.equals(Long.TYPE)) {
            return Long.class;
        } else if (c.equals(Short.TYPE)) {
            return Short.class;
        } else if (c.equals(Character.TYPE)) {
            return Character.class;
        } else if (c.equals(Float.TYPE)) {
            return Float.class;
        } else if (c.equals(Double.TYPE)) {
            return Double.class;
        } else if (c.equals(Byte.TYPE)) {
            return Byte.class;
        } //if (c.equals(Boolean.TYPE)) {
            return Boolean.class;
    }

}