/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.util;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;

public class Util {

	public static Set getMethods(Class theClass, MethodFilter filter) {
		Set methods = new TreeSet(new MethodComparator());
		addNonPrivateMethods(theClass, methods, filter);
		return methods;
	}

	private static void addNonPrivateMethods(Class theClass, Set methodSet, MethodFilter filter) {
		Method[] methods = theClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (filter.filter(method)) {
				method.setAccessible(true);
				methodSet.add(method);
			}
		}
		Class[] interfaces = theClass.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			addNonPrivateMethods(interfaces[i], methodSet, filter);
		}
		Class superclass = theClass.getSuperclass();
		if (superclass != null)
			addNonPrivateMethods(superclass, methodSet, filter);
	}
}
