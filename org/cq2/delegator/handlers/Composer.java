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
import org.cq2.delegator.classgenerator.DObject;
import org.cq2.delegator.handlers.Binder.Binding;
import org.cq2.delegator.util.MethodComparator;
import org.cq2.delegator.util.MethodFilter;
import org.cq2.delegator.util.Util;

public class Composer implements InvocationHandler, Self {
	private final Binder binder = new SuperClassBinder(this);
	private final Map bindings = new TreeMap(new MethodComparator());
	private List delegates;
	private Object caller; // TODO threadsafe!
	private MethodFilter methodFilter;

	public Composer(MethodFilter methodFilter, DObject object) {
		this.delegates = new ArrayList();
		delegates.add(object);
		this.methodFilter = methodFilter;
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
		addBinding("cast", Class.class);
		addBinding("add", Self.class);
		addBinding("become", Class.class);
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

	private void addBinding(String name, Class argType) {
		Method cast;
		try {
			cast = getClass().getMethod(name, new Class[] {argType});
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		bindings.put(cast, binder.bind(cast,this));
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
		Self delegate = Delegator.create(clas, new Object[0]);
		delegates.set(delegates.indexOf(caller), delegate.getComponent(0));
		createBindings();
	}

	public void become(InvocationHandler handler, Class clas) {
		become(clas);
	}

	public void add(Self object) {
		Composer c = (Composer) object;
		for (Iterator components = c.delegates.iterator(); components.hasNext();)
			delegates.add(components.next());
		createBindings();
	}

	public Object getComponent(int nr) {
		return delegates.get(nr);
	}
}