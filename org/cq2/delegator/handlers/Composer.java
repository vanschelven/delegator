/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.handlers.Binder.Binding;
import org.cq2.delegator.util.MethodComparator;
import org.cq2.delegator.util.MethodFilter;
import org.cq2.delegator.util.Util;

public class Composer implements InvocationHandler, Self {
	private final Binder binder = new SuperClassBinder(this);
	private final Map bindings = new TreeMap(new MethodComparator());
	private Object[] delegates;
	private Object caller; // TODO threadsafe!
	private MethodFilter methodFilter;

	public static Self compose(Object[] prototypes, MethodFilter methodFilter) {
		for (int i = 0; i < prototypes.length; i++) {
			if (prototypes[i] == null)
				throw new NullPointerException("Prototype #" + i + " is null.");
			if (!ProxyGenerator.isProxy(prototypes[i]))
				throw new IllegalArgumentException("Prototype #" + i
						+ " is not delegation capable. "
						+ "Create prototypes using Delegator.create().");
		}
		return new Composer(prototypes, methodFilter);
	}

	private Composer(Object[] delegates, MethodFilter methodFilter) {
		List list = new ArrayList(Arrays.asList(delegates));
		list.add(this);
		this.delegates = list.toArray();
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
		Method[] methods = collectMethods(delegates, methodFilter);
		for (int ifNr = 0; ifNr < methods.length; ifNr++) {
			Method method = methods[ifNr];
			for (int delNr = 0; delNr < delegates.length; delNr++) {
				Binding binding = binder.bind(method, delegates[delNr]);
				if (binding != null) {
					bindings.put(method, binding);
					break;
				}
			}
		}
	}

	private static Method[] collectMethods(Object[] delegates, MethodFilter methodFilter) {
		Set methods = Util.getMethods(delegates[0].getClass(), methodFilter);
		for (int i = 1; i < delegates.length; i++) {
			methods.addAll(Util.getMethods(delegates[i].getClass(), methodFilter));
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

	public void assimilate(Object extension) {
		Field field = ProxyGenerator.getDelegateField(extension);
		try {
			field.set(extension, this);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void become(Class clas) {
		Object delegate = Delegator.instanceOf(clas);
		assimilate(delegate);
		int i = 0;
		while (delegates[i] != caller)
			i++;
		delegates[i] = delegate;
		createBindings();
	}

	public void become(InvocationHandler handler, Class clas) {
		become(clas);
	}
}