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
import org.cq2.delegator.util.MethodFilter;

import state.Self;


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
		Self self = compose(new Object[] {extension, prototype});
		self.assimilate(extension);
		return extension;
	}

	public static Self extend(Class subclass, Class[] superclasses) {
		Delegator delegator = new Delegator();
		
		Self extension = delegator.create(subclass);
		Object[] prototypes = new Object[superclasses.length +1 ];
		prototypes[0] = extension;
		
		for (int i = 0; i < superclasses.length; i++) {
			Object prototype = delegator.create(superclasses[i]);
			prototypes[i+1] = prototype;
		}
		
		Self self = delegator.compose(prototypes);
		self.assimilate(extension);
		return self;
}

	private Self compose(Object[] prototypes) {
		return  new Composer(prototypes, methodFilter);
		
	}

	private Self create(Class clas) {
		return (Self) ProxyGenerator.newProxyInstance(classLoader, clas, null);
	
	}
}
