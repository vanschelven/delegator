/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

public class DelegatorTest extends TestCase implements InvocationHandler {

	public DelegatorTest(String arg0) {
		super(arg0);
	}

	public void setUp() {
		invokedMethod = null;
		invokeResult = null;
	}

	private String invokedMethod;
	private Object invokeResult;

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		invokedMethod = method.getName();
		return invokeResult;
	}

	public void testUseClassLoader() throws ClassNotFoundException {
		ClassLoader classLoader = new ClassLoader() {};
		Delegator factory = new Delegator(classLoader);
		Object proxyForInterface = factory.proxyFor(Iterator.class, this);
		assertEquals(classLoader, proxyForInterface.getClass().getClassLoader());
		Object proxyForClass = factory.proxyFor(HashMap.class, this);
		assertEquals(classLoader, proxyForClass.getClass().getClassLoader().getParent());
	}

	public void testDelegateInterface() {
		Map map = (Map) new Delegator().proxyFor(Map.class, this);
		assertNotNull(map);
		map.clear();
		assertEquals("clear", invokedMethod);
	}

	public void testDelegateSubclass() {
		Map map = (Map) new Delegator().proxyFor(HashMap.class, this);
		assertNotNull(map);
		invokeResult = new Boolean(true);
		assertEquals(true, map.isEmpty());
		invokeResult = new Boolean(false);
		assertEquals(false, map.isEmpty());
		assertEquals("isEmpty", invokedMethod);
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
		// Next line: test cglib
		// Object mixin = Mixin.create(new Object[] { new A2(), new A1()});
		Object mixin = new Delegator().createExtension(A1.class, A2.class);
		assertEquals("A1", ((A1) mixin).f2());
		Object result = ((A1) mixin).f1();
		assertEquals("A1", result);
	}
}