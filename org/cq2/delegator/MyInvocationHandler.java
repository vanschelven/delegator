package org.cq2.delegator;

public interface MyInvocationHandler {
    
    public Object invoke(Object proxy, int uniqueIdentifier,
            Object[] args) throws Throwable;

}
