/*
 * Created on Mar 30, 2004
 */
package org.cq2.delegator.examples.mixin;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.examples.state.OutsideTable;
import org.cq2.delegator.examples.state.XMLRenderer;
import org.cq2.delegator.handlers.ISelf;
import org.cq2.delegator.handlers.Self;

public class MixinTest extends TestCase {
	public static class MyClassC {}
	public abstract static class MyClassB implements ISelf {
		public void foo() {}

		public String bla() {
			return "bbbbbb";
		}
	}
	public abstract static class MyClass implements ISelf {
		public String bla() {
			return "multiple inheritance";
		}
	}

	public void testMultipleInheritanceCasting() {
		MyClass my = (MyClass) Delegator.extend(MyClass.class, new Class[]{MyClassB.class,
				MyClassC.class});
		assertEquals("multiple inheritance", my.bla());
		MyClassB renderer = (MyClassB) my.cast(MyClassB.class);
		assertEquals("multiple inheritance", renderer.bla());
		MyClassB myb = (MyClassB) Delegator.extend(MyClassB.class, new Class[]{MyClassC.class,
				MyClass.class});
		assertEquals("bbbbbb", myb.bla());
		renderer = (MyClassB) myb.cast(MyClassB.class);
		assertEquals("bbbbbb", renderer.bla());
	}

	public abstract static class Klaas implements ISelf {}

	public void testSelfInterface() {
		Klaas klaas = (Klaas) Delegator.extend(Klaas.class, new Class[]{MyClassB.class});
		Object a = klaas.cast(MyClassB.class);
		assertTrue(a instanceof MyClassB);
	}

	public void testBA() {
		Self self = new Self(MyClassB.class);
		self.add(MyClass.class);
		//Self self = Delegator.extend(MyClassB.class, new Class[]{MyClass.class});
		MyClassB b = (MyClassB) self.cast(MyClassB.class);
		assertEquals("bbbbbb", b.bla());
		MyClass a = (MyClass) self.cast(MyClass.class);
		assertEquals("bbbbbb", a.bla());
	}

	public void testMultiCast() {
		OutsideTable table = (OutsideTable) (Delegator.extend(OutsideTable.class, new Class[]{
				XMLRenderer.class, MyClass.class}));
		assertNotNull(table);
	}
}
