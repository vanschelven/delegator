/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuperClassBinder extends Binder {

	private InvocationHandler self;

	public SuperClassBinder(InvocationHandler self) {
		this.self = self;
	}

	protected Method mapMethod(Method method, Object delegate) throws NoSuchMethodException {
		List argTypeList = new ArrayList();
		argTypeList.add(InvocationHandler.class);
		argTypeList.addAll(Arrays.asList(method.getParameterTypes()));
		Method delegateMethod =
			delegate.getClass().getMethod(
				method.getName(),
				(Class[]) argTypeList.toArray(new Class[] {}));
		return delegateMethod;
	}

	protected Object[] mapArgs(Object[] args) {
		List argList = new ArrayList();
		argList.add(self);
		if(args != null)
			argList.addAll(Arrays.asList(args));
		return argList.toArray();
	}
}
