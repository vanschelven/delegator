package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InvocationHandlerWrapper implements InvocationHandler {

    private final Self self;

    public InvocationHandlerWrapper(Self self) {
        this.self = self;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(method);
        return self.composedClass.getReflectMethod(identifier).invoke(method, args); //TODO stack enzo??!
    }

}
