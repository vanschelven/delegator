/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import org.cq2.delegator.internal.ClassGenerator;

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
		return ClassGenerator.newProxyInstance(theInterface, self);
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