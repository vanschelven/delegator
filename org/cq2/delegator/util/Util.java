/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.util;

import java.lang.reflect.Method;
import java.util.Set;

public class Util {

	public static void addMethods(Class theClass, Set methodSet, MethodFilter filter) {
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
			addMethods(interfaces[i], methodSet, filter);
		}
		Class superclass = theClass.getSuperclass();
		if (superclass != null)
			addMethods(superclass, methodSet, filter);
	}
}
