/*
Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
*/
package org.cq2.delegator.handlers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author ejgroene
 */
public class LinkTest extends TestCase {

	private boolean clearCalled;
	private Object ikNieBegrijpNieResult;
	private int ikNieBegrijpNieB;
	private String ikNieBegrijpNieA;
	private boolean ikNieBegrijpNieCalled;

	public LinkTest(String name) throws SecurityException, NoSuchMethodException {
		super(name);
	}

	protected void setUp() throws Exception {
		clearCalled = false;
		ikNieBegrijpNieResult = null;
		ikNieBegrijpNieCalled = false;
		ikNieBegrijpNieA = null;
		ikNieBegrijpNieB = 0;
	}

	public Object ikNieBegrijpNie(String a, int b) {
		ikNieBegrijpNieCalled = true;
		ikNieBegrijpNieA = a;
		ikNieBegrijpNieB = b;
		return ikNieBegrijpNieResult;
	}

	public void testCreateSimpleDelegator() throws Throwable {
		InvocationHandler handler = new Link(this);
		assertNotNull(handler);
		Method method =
			getClass().getMethod("ikNieBegrijpNie", new Class[] { String.class, Integer.TYPE });
		ikNieBegrijpNieResult = "mannetje";
		Object result = handler.invoke(null, method, new Object[] { "Aap", new Integer(2)});
		assertTrue(ikNieBegrijpNieCalled);
		assertEquals("Aap", ikNieBegrijpNieA);
		assertEquals(2, ikNieBegrijpNieB);
		assertEquals(ikNieBegrijpNieResult, result);
	}

	public void clear() {
		clearCalled = true;
	}

	public void testDelegateToNext() throws Throwable {
		Map map = new HashMap();
		InvocationHandler handler = new Link(this, new Link(map));
		assertNotNull(handler);
		Method putMethod = Map.class.getMethod("put", new Class[] { Object.class, Object.class });
		handler.invoke(null, putMethod, new Object[] { "key", "value" });
		assertEquals("value", map.get("key"));
		Method clearMethod = Map.class.getMethod("clear", null);
		handler.invoke(null, clearMethod, null);
		assertTrue(!map.isEmpty());
		assertTrue(clearCalled);
	}

	public void testException() throws Throwable {
		try {
			new HashMap().putAll(null);
			fail();
		} catch (NullPointerException npe) {}
		Map map = new HashMap();
		InvocationHandler handler = new Link(this, new Link(map));
		Method putAllMethod = Map.class.getMethod("putAll", new Class[] { Map.class });
		try {
			handler.invoke(null, putAllMethod, new Object[] { null });
			fail();
		} catch (NullPointerException npe) {}
	}
}
