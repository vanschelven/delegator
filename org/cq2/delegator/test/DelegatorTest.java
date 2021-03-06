/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.test;

import java.util.AbstractList;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.Self;
import org.cq2.delegator.internal.MethodRegister;
import org.cq2.delegator.internal.MyInvocationHandler;

public class DelegatorTest extends TestCase {
	public DelegatorTest(String arg0) {
		super(arg0);
	}

	public static abstract class A1 {
		public abstract String f1();

		public String f2() {
			return "A1";
		}
	}
	public static class A2 {
		public String f1() {
			return f2();
		}

		public String f2() {
			return "A2";
		}
	}

	public void testSelfMixin() {
		Self a2 = new Self();
		a2.addSharableComponent(A2.class);
		Self mixin = a2.extend(A1.class);
		//Object mixin = Delegator.extend(A1.class, new Class[] {A2.class});
		A1 a1 = (A1) mixin.cast(A1.class);
		assertEquals("A1", a1.f2());
		Object result = a1.f1();
		assertEquals("A1", result);
	}

	public void testListProblemFromRobWestgeest() {
		Object obj = Delegator.extend(Object.class, new Class[]{AbstractList.class});
		assertNotNull(obj);
		obj = Delegator.extend(Object.class, new Class[]{ArrayList.class});
		assertNotNull(obj);
	}

	public static interface AnInterface {
		void hello();
	}

}