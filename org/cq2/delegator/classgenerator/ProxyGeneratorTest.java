/*
 * Created on Jun 3, 2004
 */
package org.cq2.delegator.classgenerator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import junit.framework.TestCase;
import org.cq2.delegator.Component;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.Self;

public class ProxyGeneratorTest extends TestCase {

	public void testCreateProxyForJDKClass() throws Exception {
		Object p = ProxyGenerator.newProxyInstance(HashMap.class, null);
		assertTrue(ProxyGenerator.isProxy(p));
		assertFalse(ProxyGenerator.isComponent(p));
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
		Object p = ProxyGenerator.newProxyInstance(ProxyGeneratorTest.class, null);
		assertEquals(ProxyGeneratorTest.class.getName() + "$proxy", p.getClass().getName());
	}

	public static class A {}

	public void testCreateProxyClass() throws Exception {
		ClassLoader loader = ProxyGenerator.configureClassLoader(ClassLoader.getSystemClassLoader());
		Class c = loader
				.loadClass("org.cq2.delegator.classgenerator.ProxyGeneratorTest$A$component");
		assertTrue(Component.class.isAssignableFrom(c));
		assertTrue(A.class.isAssignableFrom(c));
	}

	public void testCreateComponent() throws Exception {
		Object c = ProxyGenerator.newComponentInstance(HashMap.class);
		assertTrue(ProxyGenerator.isComponent(c));
		assertFalse(ProxyGenerator.isProxy(c));
		assertFalse(c instanceof Proxy);
		assertTrue(c instanceof Component);
		assertEquals("component$java.util.HashMap", c.getClass().getName());
		assertNotNull(c.getClass().getMethod("size", new Class[]{InvocationHandler.class}));
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

	public void testDirtyProxyFieldZerod() throws Exception {
		P p = (P) ProxyGenerator.newProxyInstance(P.class, null);
		assertNull(p.o);
		assertNull(p.p);
		Field f = P.class.getDeclaredField("r");
		f.setAccessible(true);
		assertNull(f.get(p));
		assertEquals("aap", C.aap);
		assertEquals("mies", p.mies);
		assertEquals("noot", C.noot);
	}

	public void testCachingOfClasses() {
		Self self = new Self();
		Object c1 = self.cast(ProxyGeneratorTest.class);
		Object c2 = self.cast(ProxyGeneratorTest.class);
		assertSame(c1.getClass(), c2.getClass());
	}
}
