/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;


public class UtilTest extends TestCase {

	final MethodFilter methodFilter = new MethodFilter() {
		public boolean filter(Method method) {
			return !Modifier.isFinal(method.getModifiers())
				&& !Modifier.isPrivate(method.getModifiers());
		}
	};

	public void testClass() {
		class Class1 {
			public void f1() {}
		}
		Set set = Util.getMethods(Class1.class, methodFilter);
		String string = set.toString();
		assertTrue(string.matches(".*clone().*"));
		assertTrue(string.matches(".*equals().*"));
		assertTrue(string.matches(".*f1().*"));
		assertTrue(string.matches(".*finalize().*"));
		assertTrue(string.matches(".*hashCode().*"));
		assertTrue(string.matches(".*toString().*"));
		assertEquals(6, set.size());
	}

	interface I1 {
		void f();
	}
	interface I2 {
		void f();
	}
	public static abstract class C1 implements I1, I2 {}

	public void testAbstractSuperSuperMethod() {
		Set methods = Util.getMethods(C1.class, methodFilter);
		assertEquals(6, methods.size());
		printMethods(methods);
	}

	private void printMethods(Set set) {
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			System.out.println("\t" + iter.next().toString() + ";");
		}
	}
}
