/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.binders;

import java.lang.reflect.Method;

public class SimpleBinder extends Binder {

	protected Method mapMethod(Method method, Object delegate) throws NoSuchMethodException {
		return delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
	}

	protected Object[] mapArgs(Object[] args) {
		return args;
	}

}
