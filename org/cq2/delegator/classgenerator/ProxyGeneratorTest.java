/*
 * Created on Jun 3, 2004
 */
package org.cq2.delegator.classgenerator;

import java.lang.reflect.InvocationHandler;
import java.util.HashMap;
import junit.framework.TestCase;
import org.cq2.delegator.Self;
import org.cq2.delegator.method.MethodFilterNonFinalNonPrivate;

public class ProxyGeneratorTest extends TestCase {
	ClassInjector injector;

	protected void setUp() throws Exception {
		super.setUp();
		injector = ClassInjector.create(ClassLoader.getSystemClassLoader());
	}

	public void testCreateProxy() throws Exception {
		Object p = ProxyGenerator.newProxyInstance(injector, HashMap.class,
				new MethodFilterNonFinalNonPrivate(), null);
		assertTrue(ProxyGenerator.isProxy(p));
		assertFalse(ProxyGenerator.isComponent(p));
		assertTrue(p instanceof Proxy);
		assertFalse(p instanceof Component);
		assertEquals("proxy$java.util.HashMap", p.getClass().getName());
		assertNull(p.getClass().getMethod("size", new Class[]{InvocationHandler.class}));
	}

	public void testCreateProxy2() throws Exception {
		Object p = ProxyGenerator.newProxyInstance(injector, ProxyGeneratorTest.class,
				new MethodFilterNonFinalNonPrivate(), null);
		assertEquals(ProxyGeneratorTest.class.getName() + "$proxy", p.getClass().getName());
	}

	// TODO ProxyGenerator.newComponentInstance();
	public void testCachingOfClasses() {
		Self self = new Self();
		Object c1 = self.cast(ProxyGeneratorTest.class);
		Object c2 = self.cast(ProxyGeneratorTest.class);
		assertSame(c1.getClass(), c2.getClass());
	}
}
