/*
Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
*/
package org.cq2.delegator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.handlers.Composer;
import org.cq2.delegator.handlers.Link;
import org.cq2.delegator.util.MethodFilter;

/**
 * @author ejgroene
 */
public class Delegator {

	private ClassLoader classLoader;
	
	final MethodFilter methodFilter = new MethodFilter() {
		public boolean filter(Method method) {
			return !Modifier.isFinal(method.getModifiers())
				&& !Modifier.isPrivate(method.getModifiers());
		}
	};

	public Delegator() {
		classLoader = ClassLoader.getSystemClassLoader();
	}

	public Delegator(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public Object proxyFor(Class theInterface, InvocationHandler handler) {
		if (theInterface.isInterface()) {
			return Proxy.newProxyInstance(classLoader, new Class[] { theInterface }, handler);
		} else {
			return ProxyGenerator.newProxyInstance(classLoader, theInterface, handler);
		}
	}

	public static Object forInterface(Class theInterface, Object delegate) {
		InvocationHandler newDynImpl = new Link(delegate);
		return Proxy.newProxyInstance(
			Composer.class.getClassLoader(),
			new Class[] { theInterface },
			newDynImpl);
	}

	public Object createExtension(Class extClass, Class protoClass) {
		Object prototype = ProxyGenerator.newProxyInstance(classLoader, protoClass, null);
		Object extension = ProxyGenerator.newProxyInstance(classLoader, extClass, null);
		InvocationHandler self = new Composer(new Object[] { extension, prototype }, methodFilter);
		setHandler(extension, self);
		return extension;
	}

	private static void setHandler(Object prototype, InvocationHandler extHandler) {
		Field field = ProxyGenerator.getDelegateField(prototype);
		try {
			field.set(prototype, extHandler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
