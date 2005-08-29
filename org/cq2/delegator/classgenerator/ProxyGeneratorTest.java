/*
 * Created on Jun 3, 2004
 */

//TODO deze hele klasse lijkt een beetje weird te zijn!!! duplicaat met classgeneratortest
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

//	Dit is niet echt nodig - zero is as good as anything else... en kost wel veel tijd.
//	public void testDirtyProxyFieldZerod() throws Exception {
//		P p = (P) ClassGenerator.newProxyInstance(P.class, null);
//		assertNull(p.o);
//		assertNull(p.p);
//		Field f = P.class.getDeclaredField("r");
//		f.setAccessible(true);
//		assertNull(f.get(p));
//		assertEquals("aap", C.aap);
//		assertEquals("mies", p.mies);
//		assertEquals("noot", C.noot);
//	}

	public void testCachingOfClasses() {
		Self self = new Self();
		Object c1 = self.cast(ProxyGeneratorTest.class);
		Object c2 = self.cast(ProxyGeneratorTest.class);
		assertSame(c1.getClass(), c2.getClass());
	}
	
}
