/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

public class MethodComparator implements Comparator {
	public int compare(Object arg0, Object arg1) {
		Method lhs = (Method) arg0;
		Method rhs = (Method) arg1;
		int result = lhs.getName().compareTo(rhs.getName());
		if (result == 0) {
			Class[] lhsArgTypes = lhs.getParameterTypes();
			Class[] rhsArgTypes = rhs.getParameterTypes();
			return Arrays.equals(lhsArgTypes, rhsArgTypes) ? 0 : -1;
		}
		return result;
	}
}