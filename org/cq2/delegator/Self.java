/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cq2.delegator.binders.Binder;
import org.cq2.delegator.binders.SuperClassBinder;
import org.cq2.delegator.binders.Binder.Binding;
import org.cq2.delegator.classgenerator.Component;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.method.MethodComparator;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.MethodFilterNonFinalNonPrivate;
import org.cq2.delegator.method.MethodUtil;

public class Self implements InvocationHandler, ISelf {
	private final static MethodFilter methodFilter = new MethodFilterNonFinalNonPrivate();
	private final static InvocationHandler nullHandler = new InvocationHandler() {
		public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
			throw new NullPointerException("Delegator: Object has no SELF pointer.");
		}
	};
	private List delegates;
	private transient Map bindings;
	private transient Object caller; // TODO threadsafe!
	private final transient Binder binder = new SuperClassBinder(this);

	private Self(MethodFilter methodFilter, Component object) {
		this.delegates = new ArrayList();
		delegates.add(object);
	}

	public Self() {
		this.delegates = new ArrayList();
	}

	public Self(Class firstComponentClass) {
		this.delegates = new ArrayList();
		delegates.add(newComponent(firstComponentClass));
	}

	/**
	 * @see InvocationHandler#invoke(Object, Method, Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		this.caller = proxy;
		Binding binding = (Binding) getBindings().get(method);
		if (binding == null) {
			throw new NoSuchMethodError(method.toString());
		}
		return binding.invoke(args);
	}

	private Map getBindings() {
		if (bindings == null) {
			createBindings();
		}
		return bindings;
	}

	private void createBindings() {
		if (bindings == null) {
			bindings = new TreeMap(new MethodComparator());
		}
		else {
			bindings.clear();
		}
		addBinding("cast", new Class[]{Class.class});
		addBinding("add", new Class[]{ISelf.class});
		addBinding("add", new Class[]{Class.class});
		addBinding("become", new Class[]{Class.class});
		addBinding("self", new Class[]{});
		addBinding("toString", new Class[]{});
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
		// hashCode and equals cannot be overridden, because it is not possible to
		// make equals symetric, as per the Object.equals() spec.
		addBinding("hashCode", new Class[]{});
		addBinding("equals", new Class[]{Object.class});
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
			MethodUtil.addMethods(iter.next().getClass().getSuperclass(), methods,
					new MethodFilterNonFinalNonPrivate() {
						public boolean filter(Method method) {
							if (method.getDeclaringClass().equals(Object.class)) {
								//System.out.println("Ignoring " + method);
								return false;
							}
							else {
								//System.out.println("Adding " + method);
								return super.filter(method);
							}
						}
					});
		}
		return (Method[]) methods.toArray(new Method[]{});
	}

	// For each method that is added using addBinding, there is a second version
	// with an additional arg "InvocationHandler self", because the binder will
	// add this arg before calling the method.
	public Object cast(Class clas) {
		return Delegator.proxyFor(clas, this);
	}

	public Object cast(InvocationHandler self, Class clas) {
		return cast(clas);
	}

	public void become(Class clas) {
		Object newComponent = newComponent(clas); 
		for (ListIterator iter = delegates.listIterator(); iter.hasNext();) {
			if (iter.next() == caller)
				iter.set(newComponent);
		}
		createBindings();
	}

	public void become(InvocationHandler handler, Class clas) {
		become(clas);
	}

	public void add(Self object) {
		for (Iterator components = object.delegates.iterator(); components.hasNext();)
			delegates.add(components.next());
		createBindings();
	}

	public Object component(int component) {
		return delegates.get(component);
	}

	public void add(Class clas) {
		delegates.add(newComponent(clas));
		createBindings();
	}

	public void add(InvocationHandler self, Class clas) {
		add(clas);
	}

	public void add(Class componentType, Object[] ctorArgs) {
		delegates.add(newComponent(componentType));
		createBindings();
	}

	private Component newComponent(Class clas) {
		return ProxyGenerator.newComponentInstance(Delegator.injector, clas, methodFilter, nullHandler);
	}

	public Self extend(Class class1) {
		Self newSelf = new Self(methodFilter, newComponent(class1));
		newSelf.add(this);
		return newSelf;
	}

	public Self self() {
		return this;
	}

	public Self self(InvocationHandler handler) {
		return this;
	}

	public String toString(InvocationHandler h) {
		return toString();
	}

	public int hashCode(InvocationHandler self) {
		return hashCode();
	}

	public boolean equals(Object arg0) {
		return arg0 instanceof Self ? super.equals(arg0) : arg0 != null ? arg0.equals(this) : false;
	}

	public boolean equals(InvocationHandler self, Object arg0) {
		return equals(arg0);
	}
}