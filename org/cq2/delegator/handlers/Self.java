/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.cq2.delegator.Delegator;
import org.cq2.delegator.MethodFilterNonFinalNonPrivate;
import org.cq2.delegator.classgenerator.DObject;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.handlers.Binder.Binding;
import org.cq2.delegator.util.MethodComparator;
import org.cq2.delegator.util.MethodFilter;
import org.cq2.delegator.util.Util;

public class Self implements InvocationHandler, ISelf {
	private final static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	private final Binder binder = new SuperClassBinder(this);
	private final Map bindings = new TreeMap(new MethodComparator());
	private final static InvocationHandler nullHandler = new InvocationHandler() {
		public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
			throw new NullPointerException("Delegator: Object has no SELF pointer.");
		}
	};
	private List delegates;
	private Object caller; // TODO threadsafe!
	private MethodFilter methodFilter;

	private Self(MethodFilter methodFilter, DObject object) {
		this.delegates = new ArrayList();
		delegates.add(object);
		this.methodFilter = methodFilter;
		createBindings();
	}

	public Self() {
		this.delegates = new ArrayList();
		this.methodFilter = new MethodFilterNonFinalNonPrivate();
		createBindings();
	}

	public Self(Class firstComponentClass) {
		this.delegates = new ArrayList();
		this.methodFilter = new MethodFilterNonFinalNonPrivate();
		delegates.add(newDObject(firstComponentClass, null));
		createBindings();
	}

	/**
	 * @see InvocationHandler#invoke(Object, Method, Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		this.caller = proxy;
		Binding binding = (Binding) bindings.get(method);
		if (binding == null) {
			throw new NoSuchMethodError(method.toString());
		}
		return binding.invoke(args);
	}

	private void createBindings() {
		bindings.clear();
		addBinding("cast", new Class[]{Class.class});
		addBinding("add", new Class[]{ISelf.class});
		addBinding("become", new Class[]{Class.class});
		addBinding("self", new Class[]{});
		Method[] methods = collectMethods();
		for (int ifNr = 0; ifNr < methods.length; ifNr++) {
			Method method = methods[ifNr];
			for (Iterator iter = delegates.iterator(); iter.hasNext();) {
				Binding binding = binder.bind(method, iter.next());
				if (binding != null) {
					bindings.put(method, binding);
					break;
				}
			}
		}
	}

	private void addBinding(String name, Class[] argTypes) {
		Method cast;
		try {
			cast = getClass().getMethod(name, argTypes);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		bindings.put(cast, binder.bind(cast, this));
	}

	private Method[] collectMethods() {
		Set methods = new TreeSet(new MethodComparator());
		for (Iterator iter = delegates.iterator(); iter.hasNext();) {
			Util.addMethods(iter.next().getClass(), methods, methodFilter);
		}
		return (Method[]) methods.toArray(new Method[]{});
	}

	public Object cast(Class clas) {
		return Delegator.proxyFor(clas, this);
	}

	// You don't wanna know why this method is here
	public Object cast(InvocationHandler self, Class clas) {
		return cast(clas);
	}

	public void become(Class clas) {
		ISelf delegate = new Self(clas);
		delegates.set(delegates.indexOf(caller), delegate.component(0));
		createBindings();
	}

	public void become(InvocationHandler handler, Class clas) {
		become(clas);
	}

	public void add(ISelf object) {
		Self c = (Self) object;
		for (Iterator components = c.delegates.iterator(); components.hasNext();)
			delegates.add(components.next());
		createBindings();
	}

	public Object component(int component) {
		return delegates.get(component);
	}

	public void add(Class clas) {
		delegates.add(newDObject(clas, null));
		createBindings();
	}

	public void add(Class componentType, Object[] ctorArgs) {
		delegates.add(newDObject(componentType, ctorArgs));
		createBindings();
	}

	private DObject newDObject(Class clas, Object[] ctorArgs) {
		return ProxyGenerator.newProxyInstance(classLoader, clas, nullHandler, methodFilter,
				ctorArgs);
	}

	public Self extend(Class class1) {
		Self newSelf = new Self(methodFilter, newDObject(class1, null));
		newSelf.add(this);
		return newSelf;
	}

	public Self self() {
		return this;
	}

	public Self self(InvocationHandler handler) {
		return this;
	}
}