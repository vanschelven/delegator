package org.cq2.delegator;

public interface MyInvocationHandler {
    
    public Object invoke(Object proxy, int index, String name,
            Class[] parameterTypes, Class[] exceptionTypes, int modifiers,
            Object[] args) throws Throwable;

}
