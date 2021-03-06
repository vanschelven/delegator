/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.test;

import java.lang.reflect.Constructor;
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

import org.cq2.delegator.Delegator;
import org.cq2.delegator.DelegatorException;
import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

import junit.framework.TestCase;

public class SelfTest extends TestCase {

	private static int originalAddCalled;
	private static int subClassAddCalled;
    private Self self;
    private ImplementationMock implementationMock;
	
	public static abstract class ImplementationMock {

	    private Object keyRef = null;
		private Object valueRef = null;
		private Object returnVal = new Object();
	    
		public Object put(Object key, Object value) {
			keyRef = key;
			valueRef = value;
			return returnVal;
		}

	    
	}
	
	protected void setUp() throws Exception {
	    self = new Self(ImplementationMock.class);
		implementationMock = ((ImplementationMock)self.components[0]);
	    originalAddCalled = 0;
	    subClassAddCalled = 0;
    }
	
	public void testMapDelegator() {
		Map map = (Map) Delegator.proxyFor(Map.class, self);
		Object key = new Object();
		Object value = new Object();
		Object result = map.put(key, value);
        assertEquals(key, implementationMock.keyRef);
		assertEquals(value, implementationMock.valueRef);
		assertEquals(result, implementationMock.returnVal);
	}

	public void testNoSuchMethod() {
		Set set = (Set) Delegator.proxyFor(Set.class, self);
		try {
			set.clear();
			fail("Should throw NoSuchMethodError()");
		} catch (NoSuchMethodError e) {}
	}

	public void testFouteReturnType() {
		Comparator comp = (Comparator) Delegator.proxyFor(Comparator.class, self);
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
		List list = (List) Delegator.proxyFor(List.class, self);
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
		List list = (List) Delegator.proxyFor(List.class, self);
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
		public Object put(Object key, Object value) {
			mymap_key = key;
			return null;
		}
	}

	public void testSelfWithAllKindsofClasses() throws SecurityException, NoSuchMethodException {
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
	
	public abstract static class DoubleCall {

	    public int doubleCall(int aDouble, int bDouble) {
	        return aDouble  + bDouble;
	    }
	
	}
	
	public abstract static class LongParamsNext {
	    
	    public abstract int __next__doubleCall(int aDouble, int bDouble);
	    
	    public int doubleCall(int aDouble, int bDouble) {
	        return 0;
	    }

	    public int entryPoint() {
	        return __next__doubleCall(2, 3);
	    }
	    
	}
	
	//bugtest - werkt gewoon hoor!:
	public void testNextWithLongParams() {
        Self self = new Self(LongParamsNext.class);
        self.add(DoubleCall.class);
	    assertEquals(5, ((LongParamsNext) self.cast(LongParamsNext.class)).entryPoint(), 0);
    }
	
	public void testGetComponentIndex() {
	    Self self = new Self(F1.class);
	    self.add(F2.class);
	    self.add(Vector.class);
        assertEquals(2, self.getComponentIndex(self.components[1]));
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
//		assertNotSame(clone.self(), original.self());
	}
	
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
    
    public class NonStaticInnerClass {
        
        public NonStaticInnerClass() {}
        
    }
    
    public void testInnerClassesMustBeStatic() {
        try {
            new Self(NonStaticInnerClass.class);
            fail();
        } catch (Exception e) { }
    }
    
}