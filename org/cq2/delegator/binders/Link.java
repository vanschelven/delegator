/*
Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
*/
package org.cq2.delegator.binders;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cq2.delegator.binders.Binder.Binding;

public class Link implements InvocationHandler {

	private static final Binder binder = new SimpleBinder();
	private Object delegate;
	private InvocationHandler next;

	public Link(Object delegate, InvocationHandler next) {
		this.delegate = delegate;
		this.next = next;
	}

	public Link(Object delegate) {
		this(delegate, noSuchMethodInvocationHandler());
	}

	public static InvocationHandler noSuchMethodInvocationHandler() {
		return new InvocationHandler() {
			public Object invoke(Object arg0, Method method, Object[] args) throws Throwable {
				throw new NoSuchMethodError(method.toString());
			}
		};
	}

	public Object invoke(Object self, Method method, Object[] args) throws Throwable {
		Binding binding = binder.bind(method, delegate);
		if (binding == null) {
			return next.invoke(self, method, args);
		}
		return binding.invoke(args);
	}
}