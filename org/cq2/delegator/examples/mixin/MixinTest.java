/*
 * Created on Mar 30, 2004
 */
package org.cq2.delegator.examples.mixin;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.examples.state.OutsideTable;
import org.cq2.delegator.examples.state.XMLRenderer;
import org.cq2.delegator.handlers.Self;

public class MixinTest extends TestCase {
	public static class MyClassC {
		
	}

	public static class MyClassB {
		public void foo() {}
		public String bla() {
			return "bbbbbb";
		}
	}

	public static class MyClass {
		public String bla() {
			return "multiple inheritance";
		}
		
	}

	public void testMultipleInheritanceCasting() {
		Self self = Delegator.extend(MyClass.class, new Class[]{MyClassB.class,
				MyClassC.class});
		MyClass my = (MyClass) self.cast(MyClass.class);
		assertEquals("multiple inheritance", my.bla());
		MyClassB renderer = (MyClassB) self.cast(MyClassB.class);
		assertEquals("multiple inheritance", renderer.bla());
		
	   self = Delegator.extend(MyClassB.class, new Class[]{MyClassC.class,
				MyClass.class});
		my = (MyClass) self.cast(MyClass.class);
		assertEquals("bbbbbb", my.bla());
		renderer = (MyClassB) self.cast(MyClassB.class);
		assertEquals("bbbbbb", renderer.bla());	
	}

	public abstract static class Klaas implements Self {}
	public void testSelfInterface() {
		Klaas klaas = (Klaas) Delegator.extend(Klaas.class, new Class[] {MyClassB.class}).cast(Klaas.class);
		Object a = klaas.cast(MyClassB.class);
		assertTrue(a instanceof MyClassB);
	}
	
	public void testBA() {
		Self self = Delegator.extend(MyClassB.class, new Class[]{MyClass.class});
		MyClassB b = (MyClassB) self.cast(MyClassB.class);
		assertEquals("bbbbbb", b.bla());
		MyClass a = (MyClass) self.cast(MyClass.class);
		assertEquals("bbbbbb", a.bla());
	}

	public void testMultiCast() {
		OutsideTable table = (OutsideTable) (Delegator.extend(XMLRenderer.class, new Class[]{
				OutsideTable.class, MyClass.class})).cast(OutsideTable.class);
		assertNotNull(table);
	}
}
