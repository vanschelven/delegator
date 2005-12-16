package org.cq2.delegator.internal;

public interface MyInvocationHandler {
    
    public Object invoke(Object proxy, int uniqueIdentifier,
            Object[] args) throws Throwable;

}
