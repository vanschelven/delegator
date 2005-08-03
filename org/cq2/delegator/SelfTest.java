/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.classgenerator.ClassGenerator;

public class SelfTest extends TestCase {
	private Object keyRef = null;
	private Object valueRef = null;
	private Object returnVal = new Object();
	private static boolean component2MethodCalled;

	protected void setUp() throws Exception {
	    originalAddCalled = 0;
	    subClassAddCalled = 0;
	    component2MethodCalled = false;
    }
	
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

	public void testNoSuchMethod() {
		Set set = (Set) Delegator.forInterface(Set.class, this);
		try {
			set.clear();
			fail("Should throw NoSuchMethodError()");
		} catch (NoSuchMethodError e) {}
	}

	public void testFouteReturnType() {
		Comparator comp = (Comparator) Delegator.forInterface(Comparator.class, this);
		try {
			comp.compare(null, null);
			fail();
		} catch (NoSuchMethodError e) {}
	}

	/**
	 * @see Comparator.compare()
	 */
	public long compare(Object a, Object b) {
		return 0;
	}

	public void testPublicOnly() {
		List list = (List) Delegator.forInterface(List.class, this);
		try {
			list.add(new Object());
			fail();
		} catch (NoSuchMethodError e) {}
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
		} catch (Error e) {}
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

	public void testEqualsCannotBeRedefined() {
//		Self self1 = new Self();
//		Self self2 = new Self();
//		assertFalse(self1.equals(self2));
//		assertTrue(self1.equals(self1));
//		self1.add(A.class);
//		self2.add(A.class);
//		assertFalse(self1.equals(self2));
//		assertTrue(self1.equals(self1));
//		A a1 = (A) self1.cast(A.class);
//		A a2 = (A) self2.cast(A.class);
//		assertFalse(a1.equals(a2));
//		assertTrue(a1.equals(a1));
//		assertFalse(a1.equals(null));
//		assertFalse(a1.equals(new Object()));
//		assertFalse(new Object().equals(a1));
//		assertTrue(a1.equals(self1.cast(HashMap.class)));
	}

	public static class Nr1 {
		public String aMethod() {
			return "nr1";
		}
	}
	public static class Nr2 {
		public String aMethod() {
			return "nr2";
		}

		public String method2() {
			return "nr2";
		}
	}
	public static interface Nr3 {
		String aMethod();
	}

	public void testRespondsTo() throws Exception {
		Self self = new Self(Nr1.class);
		Nr1 nr1 = (Nr1) self.cast(Nr1.class);
		Nr2 nr2 = (Nr2) self.cast(Nr2.class);
		Method m1 = Nr1.class.getMethod("aMethod", null);
		assertTrue(self.respondsTo(m1));
		assertTrue(((ISelf) nr1).respondsTo(m1));
		Method m2 = Nr2.class.getMethod("aMethod", null);
		assertTrue(self.respondsTo(m2));
		assertTrue(((ISelf) nr2).respondsTo(m1));
		assertTrue(((ISelf) nr2).respondsTo(m2));
		Method m3 = Nr2.class.getMethod("method2", null);
		assertFalse(self.respondsTo(m3));
		assertFalse(((ISelf) nr2).respondsTo(m3));
		//This test is the same as:
		try {
			nr2.method2();
			fail();
		} catch (NoSuchMethodError nsme) {}
	}

	public void testRespondsToClass() {
		Self self = new Self(Nr2.class);
		assertTrue(self.respondsTo(Nr2.class));
		assertTrue(self.respondsTo(Object.class));
		assertFalse(self.respondsTo(String.class));
		assertTrue(self.respondsTo(Nr1.class));
		self = new Self(Nr1.class);
		Nr2 nr2 = (Nr2) self.cast(Nr2.class);
		assertTrue(((ISelf) nr2).respondsTo(Nr1.class));
		assertFalse(((ISelf) nr2).respondsTo(Nr2.class));
		assertTrue(((ISelf) nr2).respondsTo(Nr3.class));
	}

	public static Object mymap_key;

	public static class MyMap {
		public void put(Object key, Object value) {
			mymap_key = key;
		}
	}

	public void testSelfWithAllKindsofClasses() {
		mymap_key = null;
		Self self = new Self();
		self.add(HashMap.class);
		self.add(MyMap.class);
		MyMap mymap = (MyMap) self.cast(MyMap.class);
		mymap.put("duck", "erik");
		assertNull(mymap_key);
		Map map = (Map) self.cast(Map.class);
		assertEquals("erik", map.get("duck"));
	}

	public static class DeclaredException extends Exception {}
	public static class ThrowException {
		protected void throwException() throws DeclaredException {
			throw new DeclaredException();
		}

		protected void throwUnexpectedException() {
			throw new Error("oops");
		}
	}
	public static abstract class ehhh {
		protected abstract void throwException() throws DeclaredException;

		protected abstract void throwUnexpectedException();
	}

	public void testTargetException() {
		Self self = new Self(ThrowException.class);
		ehhh te = (ehhh) self.cast(ehhh.class);
		//		ThrowException te = (ThrowException) self.cast(ThrowException.class);
		try {
			te.throwException();
			fail();
		} catch (DeclaredException de) {}
		try {
			te.throwUnexpectedException();
			fail();
		} catch (Error e) {
			assertEquals("oops", e.getMessage());
		}
	}

	public static class R1 {
		public String f(R2 other) {
			return "R1.f()/" + other.g() + "/" + h();
		}

		public String h() {
			return "R1.h()";
		}
	}
	public static class R2 {
		public String g() {
			return "R2.g()/" + h();
		}

		public String h() {
			return "R2.h()";
		}
	}

	public void testCrossRefs() {
		Self r1 = new Self();
		r1.addSharableComponent(R1.class);
		Self r2 = new Self();
		r2.addSharableComponent(R2.class);
		Self self = new Self();
		self.add(r2);
		self.add(r1);
		Self other = new Self();
		other.add(r1);
		other.add(r2);
		R1 r = (R1) self.cast(R1.class);
		String result = r.f((R2) other.cast(R2.class));
		assertEquals(result, "R1.f()/R2.g()/R1.h()/R2.h()", result);
	}

	public abstract static class F1 {
		public abstract String __next__method();

		public abstract int __next__plus(int i);

		public String method() {
			return "you're my " + __next__method();
		}

		public int plus(int i) {
			return 1 + __next__plus(1 + i);
		}
	}
	public static abstract class F2 implements ISelf {
		public String method() {
			return "hero!";
		}

		public abstract int plus(int i);

		public int calc() {
			return plus(4);
		}
	}
	public static class F3 {
		public int plus(int i) {
			return 3 * i;
		}
	}

	public void testForward() {
		F2 f = (F2) new Self(F1.class).cast(F2.class);
		f.add(F2.class);
		f.add(F3.class);
		assertEquals("you're my hero!", f.method());
		assertEquals(16, f.calc());
	}

	public void testDecorate() {
		ISelf self = new Self(F2.class);
		F2 f2 = (F2) self.cast(F2.class);
		assertEquals("hero!", f2.method());
		self.decorate(F1.class);
		assertEquals("you're my hero!", f2.method());
	}

	public void testDecorateAlias() {
		F2 f2 = (F2) new Self(F2.class).cast(F2.class);
		assertEquals("hero!", f2.method());
		Self.decorate(f2, F1.class);
		assertEquals("you're my hero!", f2.method());
	}

	public void testClone() {
		Self original = new Self(F2.class);
		original.add(HashMap.class);
		original.add(ArrayList.class);
		ISelf base = (ISelf) original.cast(ArrayList.class);
		ISelf clone = Self.clone(base);
		assertTrue(clone instanceof ArrayList);
//		assertSame(clone.component(0), original.component(0));
//		assertSame(clone.component(1), original.component(1));
//		assertSame(clone.component(2), original.component(2));
		assertEquals(clone, original);
		assertNotSame(clone.self(), original.self());
	}
	
	private static int originalAddCalled;
	private static int subClassAddCalled;
	
	public static abstract class OriginalList {
	    
	    public void addAll(Collection c) {
	        for (Iterator iter = c.iterator(); iter.hasNext();) {
                add(iter.next());
            }
        }
	    
	    public void add(Object o) {
	        originalAddCalled++;
	    }
	    
	}
	
	public static abstract class PlaceholderList implements ISelf {
	    
	    public static PlaceholderList create() {
	        Self result = new Self(PlaceholderList.class);
	        result.add(OriginalList.class);
	        return (PlaceholderList) result.cast(PlaceholderList.class);
	    }
	    
	    public void add(Object o) {
	        subClassAddCalled++;
	    }
	    
	    public abstract void addAll(Collection c); 
	    
	}
	
	public void testPlaceholderList() {
	    PlaceholderList list = PlaceholderList.create();
	    list.add("");
	    assertEquals(1, subClassAddCalled);
	    assertEquals(0, originalAddCalled);
	}
	
	
	public void testSelfProblem() {
	    PlaceholderList list = PlaceholderList.create();
	    Collection twoItems = new Vector();
	    twoItems.add("one");
	    twoItems.add("two");
        list.addAll(twoItems);
	    assertEquals(2, subClassAddCalled);
	    assertEquals(0, originalAddCalled);
	}
	
	public abstract static class Component1 implements ISelf {
	    
	    public void method() {
	        become(Component2.class);
	        method();
	    }
	    
	}
	
	public static class Component2 {
	    
	    public void method() {
	        component2MethodCalled = true;
	    }
	    
	}

	public void testBecome() {
	    Self self = new Self(Component1.class);
	    Component1 component = (Component1) self.cast(Component1.class);
	    component.method();
	    assertTrue(component2MethodCalled);
	}
	
	public void testBecomeCanOnlyBeCalledFromWithin() {
	    Self self = new Self(Component1.class);
	    try {
	        self.become(Component2.class);
	        fail();
	    } catch (Exception e) {
	    }
	}

	public void testBecomeCanOnlyBeCalledFromWithin2() {
	    Self self = new Self(Component1.class);
	    Self proxy = (Self) self.cast(Self.class);
	    try {
	        proxy.become(Component2.class);
	        fail();
	    } catch (Exception e) {
	    }
	}
	
	public void testRemove() {
	    Self self = new Self();
	    self.add(Vector.class);
	    self.add(HashMap.class);
	    assertTrue(self.respondsTo(Vector.class));
	    assertTrue(self.respondsTo(HashMap.class));
	    self.remove(Vector.class);
	    assertFalse(self.respondsTo(Vector.class));
	    self.remove(HashMap.class);
	    assertFalse(self.respondsTo(HashMap.class));
	}
	
	public static class ResemblingSelf {
	    
	    public void add(){}
	    
	}
	
	public void testSelfResemblingStuffBug() {
        Self self = new Self();
        ResemblingSelf resemblingSelf = (ResemblingSelf) self.cast(ResemblingSelf.class);
        try {
        	resemblingSelf.add();
        	fail();
        } catch (NoSuchMethodError e) { 
        } catch (Exception e) {
            fail(e + " thrown in stead of a NoSuchMethodError");
        }
    }
	
	public void testCastingIsUnsafe() {
        new Self().cast(Vector.class);
    }
	

    private ClassLoader loggingLoader = new ClassLoader() {
        public Class findClass(String className) throws ClassNotFoundException {
            //System.out.println(className);
            return super.getParent().loadClass(className);
        }
    };

    public void testConfigureClassLoader() {
        Delegator.configureClassLoader(loggingLoader);
        Self self = new Self(Object.class);
        assertSame(loggingLoader, self.component(0).getClass().getClassLoader()
    .getParent());
    }
	
}