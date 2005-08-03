package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import junit.framework.TestCase;

public class InvocationHandlerTest extends TestCase implements
        InvocationHandler {

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        invokedMethod = method.getName();
        return invokeResult;
    }

    protected void setUp() throws Exception {
        super.setUp();
        invokedMethod = null;
        invokeResult = null;
    }
    
    protected String invokedMethod;
    protected Object invokeResult;

}