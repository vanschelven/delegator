/*
 * Created on Jun 3, 2004
 */
package org.cq2.delegator.classgenerator;

import org.cq2.delegator.handlers.Self;

import junit.framework.TestCase;

public class ProxyGeneratorTest extends TestCase {
	public void testCreateProxy() {
		//ProxyGenerator.newProxyInstance();
		//ProxyGenerator.newComponentInstance();
	}
	
	public void testCachingOfClasses(){
		Self self = new Self();
		self.cast(ProxyGeneratorTest.class);
		self.cast(ProxyGeneratorTest.class);
		self.cast(ProxyGeneratorTest.class);
		self.cast(ProxyGeneratorTest.class);
		self.cast(ProxyGeneratorTest.class);
		self.cast(ProxyGeneratorTest.class);
		self.cast(ProxyGeneratorTest.class);
		fail();
	}
}
