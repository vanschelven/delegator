/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.method;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

public class MethodComparator implements Comparator {
	public int compare(Object arg0, Object arg1) {
		Method lhs = (Method) arg0;
		Method rhs = (Method) arg1;
		String lhsSignature = lhs.getName() + Arrays.asList(lhs.getParameterTypes()).toString();
		String rhsSiganture = rhs.getName() + Arrays.asList(rhs.getParameterTypes()).toString();
		return lhsSignature.compareTo(rhsSiganture);
	}
}