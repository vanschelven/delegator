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
import org.cq2.delegator.classgenerator.ProxyGeneratorTest;
import org.cq2.delegator.examples.HashMapExtensionTest;
import org.cq2.delegator.examples.ComposedDocument.ComposedDocumentTest;
import org.cq2.delegator.examples.mixin.MixinTest;
import org.cq2.delegator.examples.observer.CountingObservableTest;
import org.cq2.delegator.handlers.SelfTest;
import org.cq2.delegator.handlers.DynamicBinderTest;
import org.cq2.delegator.handlers.LinkTest;
import org.cq2.delegator.util.MethodComparatorTest;

import org.cq2.delegator.examples.state.StateTest;

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
		suite.addTestSuite(SelfTest.class);
		suite.addTestSuite(LinkTest.class);
		suite.addTestSuite(DelegatorTest.class);
		//$JUnit-END$
		suite.addTestSuite(ComposedDocumentTest.class);
		suite.addTestSuite(StateTest.class);
		suite.addTestSuite(CountingObservableTest.class);
		suite.addTestSuite(MixinTest.class);
		suite.addTestSuite(MethodComparatorTest.class);		
		suite.addTestSuite(DynamicBinderTest.class);
		suite.addTestSuite(ProxyGeneratorTest.class);
		return suite;
	}
}
