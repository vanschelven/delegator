/*
 * Created on Jun 4, 2004
 */
package org.cq2.delegator.classgenerator;

import junit.framework.TestCase;

public class ClassInjectorTest extends TestCase {
	public static class A{
		
	}
	
	public void testCreateProxyClass() throws Exception{
		ClassLoader loader = ClassInjector.create();
		Class c = loader.loadClass("org.cq2.delegator.classgenerator.ClassInjectorTest$A$component");
		assertTrue(Component.class.isAssignableFrom(c));
		assertTrue(A.class.isAssignableFrom(c));

		
	}
}
