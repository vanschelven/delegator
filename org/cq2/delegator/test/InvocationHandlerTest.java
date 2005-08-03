package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cq2.delegator.MethodRegister;
import org.cq2.delegator.MyInvocationHandler;

import junit.framework.TestCase;

public class InvocationHandlerTest extends TestCase implements
        MyInvocationHandler {

    protected void setUp() throws Exception {
        super.setUp();
        invokedMethod = null;
        invokeResult = null;
    }
    
    protected String invokedMethod;
    protected Object invokeResult;

    public Object invoke(Object proxy, int uniqueIdentifier, Object[] args) throws Throwable {
        invokedMethod = MethodRegister.getInstance().getMethod(uniqueIdentifier).name;
        return invokeResult;
    }

}