/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.TestCase;

public class DelegatorTest extends TestCase implements InvocationHandler {
	public DelegatorTest(String arg0) {
		super(arg0);
	}

	private String invokedMethod;

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		invokedMethod = method.getName();
		return null;
	}

	public void testDelegateInterface() {
		Map map = (Map) Delegator.proxyFor(Map.class, this);
		assertNotNull(map);
		map.clear();
		assertEquals("clear", invokedMethod);
	}

	public void clear() {
		invokedMethod = "clear";
	}

	public void testDelegateInterfaceWithImplementationArray() {
		Map map = (Map) Delegator.forInterface(Map.class, new Object[]{new Object(), "", this});
		assertNotNull(map);
		map.clear();
		assertEquals("clear", invokedMethod);
	}

	public void testDelegateInterfaceWithImplementationArray2() {
		Map realMap = new HashMap();
		realMap.put("something", "that is cleared");
		Map map = (Map) Delegator.forInterface(Map.class, new Object[]{new Object(), "", realMap,
				this});
		assertNotNull(map);
		map.clear();
		assertNull(invokedMethod);
		assertTrue(realMap.isEmpty());
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
		Self a2 = new Self(A2.class);
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

	public void testListProblemFromRobWestgeest2() {
		try {
			Delegator.extend(Object.class, new Class[]{AnInterface.class});
			fail();
		}
		catch (RuntimeException e) {
			assertTrue(e.getCause()instanceof IllegalArgumentException);
			assertEquals("Interfaces are not supported, use java.lang.reflect.Proxy.", e.getCause().getMessage());
		}
	}
	
	public class InnerClass {
		private void privateMethod() {
		}
	}
	
	public void testPrivateMethodsCanBeAccessed() {
		new InnerClass().privateMethod();
	}
	
	public static abstract class ExtendedVector {
		
		public static Vector createVector() {
			ISelf result = new Self(ExtendedVector.class);
			result.add(Vector.class);
			return (Vector) result.cast(Vector.class);
		}
		
		public static ExtendedVector createExtendedVector() {
			return (ExtendedVector) Delegator.extend(ExtendedVector.class, new Class[]{Vector.class});
		}
		
		private boolean add(Object o) {
			//note: if ExtendedVector would really extend Vector this wouldn't compile
			reached = true;
			return false;
		}
		
	}
	
	private static boolean reached;

	public void testPrivateMethodsCannotBeReachedIfSeenAsVector() {
		Vector vector = ExtendedVector.createVector();
		reached = false;
		vector.add("");
		assertFalse(reached);
	}
	
	public void testPrivateMethodsCanStillBeReached() {
		ExtendedVector extendedVector = ExtendedVector.createExtendedVector();
		reached = false;
		extendedVector.add("");
		assertTrue(reached);
	}


	
}