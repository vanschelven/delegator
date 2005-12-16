/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.cq2.delegator.binders.Link;
import org.cq2.delegator.classgenerator.ClassGenerator;
import org.cq2.delegator.internal.InvocationHandlerWrapper;

/**
 * @author ejgroene
 */
public class Delegator {

	/*	private final static InvocationHandler nullHandler = new InvocationHandler() {
	 public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
	 throw new NullPointerException("Delegator: Object has no SELF pointer.");
	 }
	 };
	 */
	public static Object proxyFor(Class theInterface, Self self) {
	    if (theInterface.isInterface()) {
			return Proxy.newProxyInstance(ClassGenerator.getClassLoader(), new Class[]{theInterface, ISelf.class}, new InvocationHandlerWrapper(self));
		}
		return ClassGenerator.newProxyInstance(theInterface, self);
	}

	public static Object forInterface(Class theInterface, Object delegate) {
		return forInterface(theInterface, new Object[]{delegate});
	}

	public static Object forInterface(Class theInterface, Object[] delegates) {
		InvocationHandler newDynImpl = Link.noSuchMethodInvocationHandler();
		for (int i = delegates.length - 1; i >= 0; i--)
			newDynImpl = new Link(delegates[i], newDynImpl);
		ClassLoader classLoader = ClassGenerator.getClassLoader();
        return Proxy.newProxyInstance(classLoader, new Class[]{theInterface},
				newDynImpl);
	}

	public static Object extend(Class subclass, Class[] superclasses) {
		ISelf extension = new Self(subclass);
		for (int i = 0; i < superclasses.length; i++) {
			extension.add(superclasses[i]);
		}
		return extension.cast(subclass);
	}
	
	public static Object extend(Class subclass, Class superclass) {
	    return extend(subclass, new Class[]{superclass});
	}
	
	public static Object wrap(Class clazz) {
	    return extend(clazz, new Class[]{});
	}

	public static ClassLoader configureClassLoader(ClassLoader loader) {
		return ClassGenerator.configureClassLoader(loader);
	}
}