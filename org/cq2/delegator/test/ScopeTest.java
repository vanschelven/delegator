/*
 * Created on Jun 4, 2004
 */
package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.cq2.delegator.Delegator;
import org.cq2.delegator.Self;
import org.cq2.delegator.classgenerator.ProxyGenerator;

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

	public static abstract class B {
		abstract void method();
	}

	public void testAbstractNonPublicMethods() throws Exception {
		B b = (B) ProxyGenerator.newProxyInstance(B.class, this);
		Method declaredMethod = b.getClass().getDeclaredMethod("method", null);
		assertNotNull(declaredMethod);
		assertFalse(Modifier.isAbstract(declaredMethod.getModifiers()));
		//assertTrue(Modifier.isPublic(declaredMethod.getModifiers()));
		declaredMethod.setAccessible(true);
		declaredMethod.invoke(b, null);
		Method abstractMethod = B.class.getDeclaredMethod("method", null);
		assertTrue(Modifier.isAbstract(abstractMethod.getModifiers()));
		assertFalse(Modifier.isPrivate(abstractMethod.getModifiers()));
		assertFalse(Modifier.isPublic(abstractMethod.getModifiers()));
		b.method();
	}

	public static class B2 extends B {
		public void method() {}
	}

	public void testAbstract() {
		new B2().method();
	}
}
