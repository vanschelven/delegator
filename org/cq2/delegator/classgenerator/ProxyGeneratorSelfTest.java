/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.classgenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Stack;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.cq2.delegator.MyInvocationHandler;
import org.cq2.delegator.Self;

public class ProxyGeneratorSelfTest extends TestCase implements MyInvocationHandler {
	private ProxyGeneratorSelfTestClass component;

	protected void setUp() throws Exception {
		component = (ProxyGeneratorSelfTestClass) ClassGenerator
				.newComponentInstance(ProxyGeneratorSelfTestClass.class, this);
	}

	public void testSuperObjectVoid() throws Exception {
		String methodName = "voidVoid";
		assertFalse(component.voidVoidCalled);
		Object result = callSuperMethod(methodName);
		assertTrue(component.voidVoidCalled);
		assertNull(result);
		try {
			((Stack) Self.self.get()).push(this);
			component.voidVoid();
			fail();
		}
		catch (AssertionFailedError e) {}
		finally {
			((Stack) Self.self.get()).pop();
		}
	}

	private Object callSuperMethod(String methodName) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		Method method = component.getClass()
				.getMethod(methodName + ClassGenerator.SUPERCALL_POSTFIX, new Class[]{});
		((Stack) Self.self.get()).push(this);
		Object result = method.invoke(component, new Object[]{});
		((Stack) Self.self.get()).pop();
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

    public Object invoke(Object proxy, int index, Object[] args) throws Throwable {
		fail("invoke must not be called");
		return null;
    }

}
