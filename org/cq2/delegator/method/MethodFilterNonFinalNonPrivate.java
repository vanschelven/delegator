/*
 * Created on Apr 26, 2004
 */
package org.cq2.delegator.method;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class MethodFilterNonFinalNonPrivate implements MethodFilter {
	public boolean filter(Method method) {
		return !Modifier.isFinal(method.getModifiers())
				&& !Modifier.isPrivate(method.getModifiers());
	}
}