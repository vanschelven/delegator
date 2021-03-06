/*
 * Created on Jun 3, 2004
 */
package org.cq2.delegator.internal;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;

import junit.framework.TestCase;

import org.cq2.delegator.Component;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.Self;

public class ClassGeneratorTest extends TestCase {

	public void testCreateProxyForJDKClass() throws Exception {
		Object p = ClassGenerator.newProxyInstance(HashMap.class, null);
		assertTrue(ClassGenerator.isProxy(p));
		assertFalse(ClassGenerator.isComponent(p));
		assertTrue(p instanceof Proxy);
		assertFalse(p instanceof Component);
		assertEquals("proxy$java.util.HashMap", p.getClass().getName());
		try {
			p.getClass().getMethod("size", new Class[]{InvocationHandler.class});
			fail();
		}
		catch (NoSuchMethodException e) {}
	}

	public void testCreateProxyForOwnClass() throws Exception {
		Object p = ClassGenerator.newProxyInstance(ClassGeneratorTest.class, null);
		assertEquals(ClassGeneratorTest.class.getName() + "$proxy", p.getClass().getName());
	}

	public static class A {}

	public void testCreateProxyClass() throws Exception {
		ClassLoader loader = ClassGenerator.configureClassLoader(ClassLoader.getSystemClassLoader());
		Class c = loader
				.loadClass("org.cq2.delegator.internal.ClassGeneratorTest$A$component");
		assertTrue(Component.class.isAssignableFrom(c));
		assertTrue(A.class.isAssignableFrom(c));
	}

	public void testCreateComponent() throws Exception {
		Object c = ClassGenerator.newComponentInstance(HashMap.class, new Self());
		assertTrue(ClassGenerator.isComponent(c));
		assertFalse(ClassGenerator.isProxy(c));
		assertFalse(c instanceof Proxy);
		assertTrue(c instanceof Component);
		assertEquals("component$java.util.HashMap", c.getClass().getName());
		assertNotNull(c.getClass().getMethod("size" + ClassGenerator.SUPERCALL_POSTFIX, new Class[]{}));
	}
	
	public static class C {
		Object k = new Object();
		static String aap = "aap";
		final String mies = "mies";
		final static String noot = "noot";
	}
	public static class P extends C {
		public Object o = new HashMap();
		int q = 12345;
		private Object r = new Object();
		Object p = r;
	}

	public void testCachingOfClasses() {
		Self self = new Self();
		Object c1 = self.cast(ClassGeneratorTest.class);
		Object c2 = self.cast(ClassGeneratorTest.class);
		assertSame(c1.getClass(), c2.getClass());
	}
	
}
