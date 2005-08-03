package org.cq2.delegator;

import java.lang.reflect.Method;

public class MiniMethod {

    public final String name;
    public final Class[] parameterTypes;
    final Class[] exceptionTypes;
    final int modifiers;

    public MiniMethod(String name, Class[] parameterTypes, Class[] exceptionTypes, int modifiers) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.exceptionTypes = exceptionTypes;
        this.modifiers = modifiers;
    }
    
    public MiniMethod(Method method) {
        name = method.getName();
        parameterTypes = method.getParameterTypes();
        exceptionTypes = method.getExceptionTypes();
        modifiers = method.getModifiers();
    }
    
    public int hashCode() {
        return name.hashCode();
    }
    
    public boolean equals(Object obj) {
        MiniMethod other = (MiniMethod) obj;
        if (!name.equals(other.name)) return false;
        if (!ArraysAreEqual(parameterTypes, other.parameterTypes)) return false; 
        if (!ArraysAreEqual(exceptionTypes, other.exceptionTypes)) return false; 
        return (modifiers == other.modifiers);
    }

    private boolean ArraysAreEqual(Object[] array1, Object[] array2) {
        if (array1.length != array2.length) return false;
        for (int i = 0; i < array1.length; i++) {
            if (!array1[i].equals(array2[i])) return false;
        }
        return true;
    }
}