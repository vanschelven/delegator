/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.cq2.delegator.classgenerator.DObject;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.handlers.Composer;
import org.cq2.delegator.handlers.Link;
import org.cq2.delegator.handlers.Self;
import org.cq2.delegator.util.MethodFilter;

/**
 * @author ejgroene
 */
public class Delegator {
	private final static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	private final static MethodFilter methodFilter = new MethodFilter() {
		public boolean filter(Method method) {
			return !Modifier.isFinal(method.getModifiers())
					&& !Modifier.isPrivate(method.getModifiers());
		}
	};
	private final static InvocationHandler nullHandler = new InvocationHandler() {
		public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
			throw new NullPointerException("Delegator: Object has no SELF pointer.");
		}
	};

	public static Object proxyFor(Class theInterface, InvocationHandler handler) {
		if (theInterface.isInterface()) {
			return Proxy.newProxyInstance(classLoader, new Class[]{theInterface}, handler);
		}
		else {
			return ProxyGenerator
					.newProxyInstance(classLoader, theInterface, handler, methodFilter);
		}
	}

	public static Object forInterface(Class theInterface, Object delegate) {
		InvocationHandler newDynImpl = new Link(delegate);
		return Proxy.newProxyInstance(Composer.class.getClassLoader(), new Class[]{theInterface},
				newDynImpl);
	}

	public static Object extend(Class subclass, Class[] superclasses) {
		Self extension = Delegator.create(subclass, new Object[0]);
		for (int i = 0; i < superclasses.length; i++) {
			extension.add(Delegator.create(superclasses[i], new Object[0]));
		}
		return extension.cast(subclass);
	}

	public static Self create(Class clas, Object[] ctorArgs) {
		DObject object = ProxyGenerator.newProxyInstance(classLoader, clas, nullHandler, methodFilter);
		return new Composer(methodFilter, object);
	}

	public static MethodFilter defaultMethodFilter() {
		return methodFilter;
	}

	public static Self newObject() {
		return new Composer(methodFilter, null);
	}
}
