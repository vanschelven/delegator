/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.method;

import java.lang.reflect.Method;
import java.util.Set;

public class MethodUtil {
	public static void addMethods(Class theClass, Set methodSet) {
		Class[] interfaces = theClass.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			addMethods(interfaces[i], methodSet);
		}
		Class superclass = theClass.getSuperclass();
		if (superclass != null)
			addMethods(superclass, methodSet);
		Method[] methods = theClass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			method.setAccessible(true);
			methodSet.remove(method);
			methodSet.add(method);
		}
	}
}
