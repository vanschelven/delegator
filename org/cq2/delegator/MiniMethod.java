package org.cq2.delegator;

public class MiniMethod {

    final String name;
    final Class[] parameterTypes;
    final Class[] exceptionTypes;
    final int modifiers;

    public MiniMethod(String name, Class[] parameterTypes, Class[] exceptionTypes, int modifiers) {
        this.name = name;
        this.parameterTypes = parameterTypes;
        this.exceptionTypes = exceptionTypes;
        this.modifiers = modifiers;
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