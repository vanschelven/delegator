/*
 * Created on Jan 22, 2004
 */
package org.cq2.delegator.binders;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cq2.delegator.method.MethodComparator;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.MethodUtil;

public class DynamicBinder extends Binder {

	static class DynamicBinding implements Binding {
		final Method mainMethod;
		private final Object delegate;
		public DynamicBinding(Method method, Object delegate) {
			this.mainMethod = method;
			this.delegate = delegate;
		}
		public Object invoke(final Object[] args) throws Throwable {
			Set candidateMethods = new TreeSet(new MethodComparator());			
			MethodUtil.addMethods(delegate.getClass(), candidateMethods, new MethodFilter() {
				public boolean filter(Method method) {
					return method.getName().equals(mainMethod.getName())
						&& mainMethod.getParameterTypes()[0].isAssignableFrom(
							method.getParameterTypes()[0])
						&& method.getParameterTypes()[0].isAssignableFrom(args[0].getClass());
				}
			});
			List methods = new ArrayList(candidateMethods);
			Collections.sort(methods, new Comparator() {
				public int compare(Object arg0, Object arg1) {
					Method lhs = (Method) arg0;
					Method rhs = (Method) arg1;
					Class lhsType = lhs.getParameterTypes()[0];
					Class rhsType = rhs.getParameterTypes()[0];
					if (lhsType.equals(rhsType))
						return 0;
					if (lhsType.isAssignableFrom(rhsType))
						return 1;
					return -1;
				}
			});
			Method targetMethod = (Method) methods.get(0);
			return targetMethod.invoke(delegate, args);
		}

	}

	public Binding bind(Method method, Object delegate) {
		return new DynamicBinding(method, delegate);
	}

	protected Method mapMethod(Method method, Object delegate) throws NoSuchMethodException {
		throw new UnsupportedOperationException();
	}

	protected Object[] mapArgs(Object[] args) {
		throw new UnsupportedOperationException();
	}
}
