/*
 * Created on Jun 4, 2004
 */
package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.Self;

public class ScopeTest extends TestCase implements InvocationHandler {
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

	public void testDelegateSubclass() {
		Map map = (Map) Delegator.proxyFor(HashMap.class, this);
		assertNotNull(map);
		invokeResult = Boolean.TRUE;
		assertEquals(true, map.isEmpty());
		invokeResult = Boolean.FALSE;
		assertEquals(false, map.isEmpty());
		assertEquals("isEmpty", invokedMethod);
	}

	public void testDelegateSubclassPackageScopeMethod() {
		Self self = new Self(MySuperClass.class);
		self.add(MySubclass.class);
		MySubclass subclass = (MySubclass) self.cast(MySubclass.class);
		assertEquals(3, subclass.myPackageScopeMethod());
	}

	public void testDelegateSubclassProtectedMethod() {
		Self self = new Self(MySuperClass.class);
		self.add(MySubclass.class);
		MySubclass subclass = (MySubclass) self.cast(MySubclass.class);
		int result = subclass.getProtected();
		assertEquals(5, result);
	}
}
