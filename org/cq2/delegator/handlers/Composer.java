/*
Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
*/
package org.cq2.delegator.handlers;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.handlers.Binder.Binding;
import org.cq2.delegator.util.MethodComparator;
import org.cq2.delegator.util.MethodFilter;
import org.cq2.delegator.util.Util;

import state.Self;

public class Composer implements InvocationHandler, Self {

	private final Binder binder = new SuperClassBinder(this);
	private final Map bindings = new TreeMap(new MethodComparator());

	public Composer(Object[] delegates, MethodFilter methodFilter) {
		createBindings(delegates, methodFilter);
	}

	/**
	 * @see InvocationHandler#invoke(Object, Method, Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Binding binding = (Binding) bindings.get(method);
		if (binding == null) {
			throw new NoSuchMethodError(method.toString());
		}
		return binding.invoke(args);
	}

	private void createBindings(Object[] delegates, MethodFilter methodFilter) {
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
		return (Method[]) methods.toArray(new Method[] {});
	}

	public Object cast(Class clas) {
		Iterator iter = bindings.values().iterator();
		while (iter.hasNext()) {
			Binding binding = (Binding) iter.next();
			if(binding.matches(clas)) {
				Object delegate = binding.getDelegate();
				assimilate(delegate);
				return delegate;
			}
		}
		throw new ClassCastException("does not implement: " + clas.getName());
	}

	public void assimilate(Object extension) {
		Field field = ProxyGenerator.getDelegateField(extension);
		try {
			field.set(extension, this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}