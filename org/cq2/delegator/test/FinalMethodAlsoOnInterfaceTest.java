/*
 * Created on Jun 8, 2004
 */
package org.cq2.delegator.test;

import junit.framework.TestCase;

import org.cq2.delegator.Self;

public class FinalMethodAlsoOnInterfaceTest extends TestCase {
	public void testSecondClass(){
		new Self(SecondClass.class);
	}
	
	public static interface SecondInterface{
		boolean fromInterface();
	}
	
	public static class SecondClass implements SecondInterface{
		public final boolean fromInterface() {
			return false;
		}
	}
	
	public static class A {
		public void doSomething(){}
		public void startDo(){
			doSomething();
		}
	}
	public static class B extends A {
		public final void doSomething() {
			super.doSomething();
			doSomethingElse();
		}
		protected void doSomethingElse(){
			
		}
	}
	public static class C extends B{
		protected void doSomethingElse(){
			//different
		}
		
	}
	public void testNowWithClasses(){
		new Self(B.class);
	}
	public void testC(){
		Self self = new Self(C.class);
		C c = (C) self.cast(C.class);
		c.startDo();
	}
}
