/*
 * Created on Jun 3, 2004
 */
package org.cq2.delegator.classgenerator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import junit.framework.TestCase;
import org.cq2.delegator.Self;
import org.cq2.delegator.method.MethodFilterNonFinalNonPrivate;

public class ProxyGeneratorTest extends TestCase {
	ClassInjector injector;
	private final MethodFilterNonFinalNonPrivate filter = new MethodFilterNonFinalNonPrivate();

	protected void setUp() throws Exception {
		super.setUp();
		injector = ClassInjector.create(ClassLoader.getSystemClassLoader());
	}

	public void testCreateProxyForJDKClass() throws Exception {
		Object p = ProxyGenerator.newProxyInstance(injector, HashMap.class, filter, null);
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
		Object p = ProxyGenerator.newProxyInstance(injector, ProxyGeneratorTest.class,
				new MethodFilterNonFinalNonPrivate(), null);
		assertEquals(ProxyGeneratorTest.class.getName() + "$proxy", p.getClass().getName());
	}

	public void testCreateComponent() throws Exception {
		Object c = ProxyGenerator.newComponentInstance(injector, HashMap.class, filter, null);
		assertTrue(ProxyGenerator.isComponent(c));
		assertFalse(ProxyGenerator.isProxy(c));
		assertFalse(c instanceof Proxy);
		assertTrue(c instanceof Component);
		assertEquals("component$java.util.HashMap", c.getClass().getName());
		assertNotNull(c.getClass().getMethod("size", new Class[]{InvocationHandler.class}));
	}

	public static class C {
		Object k = new Object();
	}
	public static class P extends C {
		public Object o = new HashMap();
		int q =12345;
		private Object r = new Object();
		Object p = r;
	}

	public void testDirtyProxyFieldZerod() throws Exception {
		P p = (P) ProxyGenerator.newProxyInstance(injector, P.class, filter, null);
		assertNull(p.o);
		assertNull(p.p);
		Field f =P.class.getDeclaredField("r");
		f.setAccessible(true);
		assertNull(f.get(p));
	}

	public void testCachingOfClasses() {
		Self self = new Self();
		Object c1 = self.cast(ProxyGeneratorTest.class);
		Object c2 = self.cast(ProxyGeneratorTest.class);
		assertSame(c1.getClass(), c2.getClass());
	}
}
