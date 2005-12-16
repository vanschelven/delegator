/*
 * Created on Jun 10, 2004
 */
package org.cq2.delegator.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.cq2.delegator.method.MethodFilter;

public class ComponentMethodFilter implements MethodFilter {
	public boolean filter(Method method) {
		return !Modifier.isFinal(method.getModifiers())
				&& !Modifier.isPrivate(method.getModifiers())
				&& !Modifier.isStatic(method.getModifiers())
				&& !Object.class.equals(method.getDeclaringClass())
				&& !method.getName().equals("finalize");
	}
}
