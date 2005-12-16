package org.cq2.delegator.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cq2.delegator.Self;

public class InvocationHandlerWrapper implements InvocationHandler {

    private final Self self;

    public InvocationHandlerWrapper(Self self) {
        this.self = self;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int identifier = ForwardingMethodRegister.getInstance().getMethodIdentifier(method);
        Method reflectMethod = self.composedClass.getReflectMethod(identifier);
        return reflectMethod.invoke(self.components[self.composedClass.getComponentIndex(identifier)], args);
    }

}
