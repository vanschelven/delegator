/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.cq2.delegator.classgenerator.ProxyGeneratorDelegateTest;
import org.cq2.delegator.classgenerator.ProxyGeneratorSelfTest;
import org.cq2.delegator.examples.HashMapExtensionTest;
import org.cq2.delegator.handlers.ComposerTest;
import org.cq2.delegator.handlers.LinkTest;

public class AllDelegatorTests {

	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("");
		//$JUnit-BEGIN$
		suite.addTestSuite(ProxyGeneratorDelegateTest.class);
		suite.addTestSuite(ProxyGeneratorSelfTest.class);
		suite.addTestSuite(HashMapExtensionTest.class);
		suite.addTestSuite(ComposerTest.class);
		suite.addTestSuite(LinkTest.class);
		suite.addTestSuite(DelegatorTest.class);
		//$JUnit-END$
		return suite;
	}
}
