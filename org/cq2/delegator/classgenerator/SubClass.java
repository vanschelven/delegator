/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 *
 * Created on Mar 29, 2004
 */
package org.cq2.delegator.classgenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.cq2.delegator.handlers.Invoker;

class DDObject {
	InvocationHandler self;
}
public class SubClass extends Invoker {
	public Object invoke(Method ga, Object[] args, Object target, InvocationHandler newSelf) {
		((DDObject) target).self = newSelf;
		try {
			return ga.invoke(target.getClass().getName(), args);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
