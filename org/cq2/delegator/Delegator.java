/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.handlers.Composer;
import org.cq2.delegator.handlers.Link;
import org.cq2.delegator.handlers.Self;
import org.cq2.delegator.util.MethodFilter;

/**
 * @author ejgroene
 */
public class Delegator {

    private static ClassLoader classLoader;
    static {
        classLoader = ClassLoader.getSystemClassLoader();
    }

    final static MethodFilter methodFilter = new MethodFilter() {
        public boolean filter(Method method) {
            return !Modifier.isFinal(method.getModifiers())
                    && !Modifier.isPrivate(method.getModifiers());
        }
    };

    private static InvocationHandler nullHandler = new InvocationHandler() {
        public Object invoke(Object arg0, Method arg1, Object[] arg2)
                throws Throwable {
            throw new NullPointerException(
                    "Delegator: Object has no SELF pointer.");
        }
    };

    public Delegator() {
        classLoader = ClassLoader.getSystemClassLoader();
    }

    public static Object proxyFor(Class theInterface, InvocationHandler handler) {
        if (theInterface.isInterface()) {
            return Proxy.newProxyInstance(classLoader,
                    new Class[] { theInterface}, handler);
        } else {
            return ProxyGenerator.newProxyInstance(classLoader, theInterface,
                    handler, methodFilter);
        }
    }

    public static Object forInterface(Class theInterface, Object delegate) {
        InvocationHandler newDynImpl = new Link(delegate);
        return Proxy.newProxyInstance(Composer.class.getClassLoader(),
                new Class[] { theInterface}, newDynImpl);
    }

    public static Object createExtension(Class extClass, Class protoClass) {
        Object prototype = ProxyGenerator.newProxyInstance(classLoader,
                protoClass, nullHandler, methodFilter);
        Object extension = ProxyGenerator.newProxyInstance(classLoader,
                extClass, nullHandler, methodFilter);
        return Composer.compose(new Object[] { extension, prototype},
                methodFilter).cast(extClass);
    }

    public static Self extend(Class subclass, Class[] superclasses) {
        Self extension = (Self) Delegator.instanceOf(subclass);
        Object[] prototypes = new Object[superclasses.length + 1];
        prototypes[0] = extension;
        for (int i = 0; i < superclasses.length; i++) {
            Object prototype = Delegator.instanceOf(superclasses[i]);
            prototypes[i + 1] = prototype;
        }
        return Composer.compose(prototypes, methodFilter);
    }

    public static Object instanceOf(Class clas) {
        return ProxyGenerator.newProxyInstance(classLoader, clas, nullHandler, methodFilter);
    }

    public static Self compose(Object obj1, Object obj2, Object obj3) {
        return Composer.compose(new Object[] { obj1, obj2, obj3}, methodFilter);
    }

    public static MethodFilter defaultMethodFilter() {
        return methodFilter;
    }
}
