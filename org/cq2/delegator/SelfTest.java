/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.io.DataInput;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import org.cq2.delegator.classgenerator.ClassInjector;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.MethodFilterNonFinalNonPrivate;

/**
 * @author ejgroene
 *
 */
public class SelfTest extends TestCase {
	private Object keyRef = null;
	private Object valueRef = null;
	private Object returnVal = new Object();

	public Object put(Object key, Object value) {
		keyRef = key;
		valueRef = value;
		return returnVal;
	}

	public void testMapDelegator() {
		Map map = (Map) Delegator.forInterface(Map.class, this);
		Object key = new Object();
		Object value = new Object();
		Object result = map.put(key, value);
		assertEquals(key, keyRef);
		assertEquals(value, valueRef);
		assertEquals(result, returnVal);
	}

	public void testAddComponent() {
		ClassInjector injector = new ClassInjector(ClassLoader.getSystemClassLoader());
		MethodFilter filter = new MethodFilterNonFinalNonPrivate();
		Self self = new Self();
		Component c = ProxyGenerator.newComponentInstance(injector, ArrayList.class, filter, null);
		self.add(c);
		assertSame(c, self.component(0));
	}

	public void testNoSuchMethod() {
		Set set = (Set) Delegator.forInterface(Set.class, this);
		try {
			set.clear();
			fail("Should throw NoSuchMethodError()");
		}
		catch (NoSuchMethodError e) {}
	}

	public boolean execute(String query) throws SQLException {
		throw new SQLException();
	}

	public void testException() throws SQLException {
		Statement statement = (Statement) Delegator.forInterface(Statement.class, this);
		try {
			statement.execute("niks");
			fail();
		}
		catch (SQLException e) {}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Invalid exception!");
		}
	}

	public void testFouteReturnType() {
		Comparator comp = (Comparator) Delegator.forInterface(Comparator.class, this);
		try {
			comp.compare(null, null);
			fail();
		}
		catch (NoSuchMethodError e) {}
	}

	/**
	 * @see Comparator.compare()
	 */
	public long compare(Object a, Object b) {
		return 0;
	}

	public void testMissingExceptionType() throws Exception {
		DataInput input = (DataInput) Delegator.forInterface(DataInput.class, this);
		try {
			input.readByte();
			fail("exception types should not match");
		}
		catch (NoSuchMethodError e) {}
	}

	/**
	 * @see DataInput.readByte()  Intentionally without throws IOException
	 */
	public byte readByte() {
		return 0;
	}

	public void testPublicOnly() {
		List list = (List) Delegator.forInterface(List.class, this);
		try {
			list.add(new Object());
			fail();
		}
		catch (NoSuchMethodError e) {}
	}

	public void addBatch(String query) { // should throws SQLException
	}

	public int hashCode() {
		return 1;
	}

	public void testListHasNoHashCode() {
		List list = (List) Delegator.forInterface(List.class, this);
		try {
			list.hashCode();
			fail();
		}
		catch (Error e) {}
	}

	public interface I extends ISelf {}

	public void testCast() {
		Self c = new Self();
		I i = (I) c.cast(I.class);
		assertNotNull(i);
		assertNotNull(i.cast(Map.class));
	}

	public static abstract class TestCastISelf {
		ISelf self = (ISelf) this;

		public Object testCast() {
			return ((ISelf) this).self();
		}
	}

	public void testCastISelf() {
		Self self = new Self(TestCastISelf.class);
		TestCastISelf obj = (TestCastISelf) self.cast(TestCastISelf.class);
		assertSame(self, obj.testCast());
		// or:
		assertSame(self, ((ISelf) obj).self());
	}

	public void testToString() {
		Self self = newModifiedSelf(Object.class);
		assertEquals("modifiedSelf", self.toString());
	}

	private Self newModifiedSelf(Class a) {
		Self self = new Self(a) {
			public String toString() {
				return "modifiedSelf";
			}

			public int hashCode() {
				return 99;
			}
		};
		return self;
	}

	public static class A {
		public String toString() {
			return "A";
		}

		// hashCode cannot be redefined
		public int hashCode() {
			fail();
			return 28;
		}

		public boolean equals(Object arg0) {
			fail();
			return true;
		}
	}
	public static class B extends A {}
	public static class C {}

	public void testToString2() {
		Object objA = newModifiedSelf(A.class).cast(Object.class);
		assertEquals("A", objA.toString());
		Object objB = newModifiedSelf(B.class).cast(Object.class);
		assertEquals("A", objB.toString());
		Object objC = newModifiedSelf(C.class).cast(Object.class);
		assertEquals("modifiedSelf", objC.toString());
	}

	public void testHashCodeCannotBeRedefined() {
		Object objA = newModifiedSelf(A.class).cast(Object.class);
		assertEquals(99, objA.hashCode());
		Object objB = newModifiedSelf(B.class).cast(Object.class);
		assertEquals(99, objB.hashCode());
		Object objC = newModifiedSelf(C.class).cast(Object.class);
		assertEquals(99, objC.hashCode());
	}

	public void testEqualsCannotBeRedifined() {
		Self self1 = new Self();
		Self self2 = new Self();
		assertFalse(self1.equals(self2));
		assertTrue(self1.equals(self1));
		self1.add(A.class);
		self2.add(A.class);
		assertFalse(self1.equals(self2));
		assertTrue(self1.equals(self1));
		A a1 = (A) self1.cast(A.class);
		A a2 = (A) self2.cast(A.class);
		assertFalse(a1.equals(a2));
		assertTrue(a1.equals(a1));
		assertFalse(a1.equals(null));
		assertFalse(a1.equals(new Object()));
		assertFalse(new Object().equals(a1));
		assertTrue(a1.equals(self1.cast(HashMap.class)));
	}
}