/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.classgenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class ProxyGeneratorSelfTest extends TestCase implements InvocationHandler {
	private ProxyGeneratorSelfTestClass proxy;

	protected void setUp() throws Exception {
		proxy =
			(ProxyGeneratorSelfTestClass) ProxyGenerator.newProxyInstance(
				ClassLoader.getSystemClassLoader(),
				ProxyGeneratorSelfTestClass.class,
				null);
	}

	public void testSuperObjectVoid() throws Exception {
		String methodName = "voidVoid";
		assertFalse(proxy.voidVoidCalled);
		Object result = callSuperMethod(methodName);
		assertTrue(proxy.voidVoidCalled);
		assertNull(result);
		try {
			proxy.voidVoid();
			fail();
		} catch (AssertionFailedError e) {}
	}

	private Object callSuperMethod(String methodName)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method method =
			proxy.getClass().getMethod(methodName, new Class[] { InvocationHandler.class });
		Object result = method.invoke(proxy, new Object[] { this });
		return result;
	}

	public void testSuperhashCode() throws Exception {
		assertEquals(new Integer(87654), callSuperMethod("hashCode"));
	}
	public void testSuperlongVoid() throws Exception {
		assertEquals(new Long(98765678), callSuperMethod("longVoid"));
	}
	public void testSuperfloatVoid() throws Exception {
		assertEquals(new Float(123.987F), callSuperMethod("floatVoid"));
	}
	public void testSuperdoubleVoid() throws Exception {
		assertEquals(new Double(765.890), callSuperMethod("doubleVoid"));
	}
	public void testSuperbyteVoid() throws Exception {
		assertEquals(new Byte((byte) 87), callSuperMethod("byteVoid"));
	}
	public void testSupershortVoid() throws Exception {
		assertEquals(new Short((short) 789), callSuperMethod("shortVoid"));
	}
	public void testSupercharVoid() throws Exception {
		assertEquals(new Character('B'), callSuperMethod("charVoid"));
	}
	public void testSuperstringVoid() throws Exception {
		assertEquals("Are you there?", callSuperMethod("stringVoid"));
	}

	// TODO test rest of methods in TestClass
	// TODO test Object methods

	public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
		fail("invoke must not be called");
		return null;
	}
}
