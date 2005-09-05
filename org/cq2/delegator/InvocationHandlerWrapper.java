package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvocationHandlerWrapper implements InvocationHandler {

    private final MyInvocationHandler forwardee;

    public InvocationHandlerWrapper(MyInvocationHandler forwardee) {
        this.forwardee = forwardee;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return forwardee.invoke(proxy, MethodRegister.getInstance().getIdentifier(new MiniMethod(method)), args);
    }

}
