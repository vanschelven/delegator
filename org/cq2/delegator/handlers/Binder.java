/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract class Binder {

	public interface Binding {
		Object invoke(Object[] args) throws Throwable;
		boolean matches(Class claz);
		Object getDelegate();
	}

	private class BindingImpl implements Binding {
		private final Method method;
		private final Object delegate;
		private BindingImpl(Method method, Object delegate) {
			this.method = method;
			this.delegate = delegate;
		}

		public Object invoke(Object[] args) throws Throwable {
			try {
//				System.out.println("invoking " + method);
				return method.invoke(delegate, mapArgs(args));
			} catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}

		public boolean matches(Class claz) {
			return claz.equals(delegate.getClass().getSuperclass());
		}

		public Object getDelegate() {
			return delegate;
		}
	}

	Binding bind(Method method, Object delegate) {
		Method delegateMethod = findMethod(method, delegate);
		if (delegateMethod == null) {
			return null;
		}
		return new BindingImpl(delegateMethod, delegate);
	}

	private Method findMethod(Method method, Object delegate) {
		Method delegateMethod = null;
		try {
			delegateMethod = mapMethod(method, delegate);
			delegateMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			return null;
		}
		if (!delegateMethod.getReturnType().equals(method.getReturnType())) {
			return null;
		}
		if (!getExceptions(delegateMethod).equals(getExceptions(method))) {
			return null;
		}
		return delegateMethod;
	}

	private String getExceptions(Method method) {
		String exceptions = method.toString();
		return exceptions.substring(exceptions.indexOf(')'));
	}

	protected abstract Method mapMethod(Method method, Object delegate)
		throws NoSuchMethodException;

	protected abstract Object[] mapArgs(Object[] args);
}