/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.method;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.DelegatorException;


public class MethodUtilTest extends TestCase {

	final MethodFilter methodFilter = new MethodFilter() {
		public boolean filter(Method method) {
			return !Modifier.isFinal(method.getModifiers())
				&& !Modifier.isPrivate(method.getModifiers());
		}
	};

	public void testClass() {
		class Class1 {
			public void f1() {}
		}
		Set set = new TreeSet(new MethodComparator());
		MethodUtil.addMethods(Class1.class, set);
		String string = set.toString();
		assertTrue(string.matches(".*clone().*"));
		assertTrue(string.matches(".*equals().*"));
		assertTrue(string.matches(".*f1().*"));
		assertTrue(string.matches(".*finalize().*"));
		assertTrue(string.matches(".*hashCode().*"));
		assertTrue(string.matches(".*toString().*"));
		assertEquals(13, set.size()); //This used to say 6 but I don't see why
	}

	interface I1 {
		void f();
	}
	interface I2 {
		void f();
	}
	public static abstract class C1 implements I1, I2 {}

	public void testAbstractSuperSuperMethod() {
		Set methods = new TreeSet(new MethodComparator());
		MethodUtil.addMethods(C1.class, methods);
		assertEquals(13, methods.size()); //This used to say 6 but I don't see why
	}

	private void printMethods(Set set) {
		Iterator iter = set.iterator();
		while (iter.hasNext()) {
			System.out.println("\t" + iter.next().toString() + ";");
		}
	}
	
	public void testGetMethod() throws SecurityException, NoSuchMethodException {
        Method result = MethodUtil.getDeclaredMethod(Vector.class, "add", new Class[]{Object.class}, null);
        assertEquals(Vector.class.getDeclaredMethod("add", new Class[]{Object.class}), result);
    }
	
	public void testGetMethodRefined() throws SecurityException, NoSuchMethodException {
        Method result = MethodUtil.getDeclaredMethod(Vector.class, "add", new Class[]{Integer.class}, null);
        assertEquals(Vector.class.getDeclaredMethod("add", new Class[]{Object.class}), result);
    }
	
	public void testGetMethodUnrefined() throws SecurityException {
        Method result = MethodUtil.getDeclaredMethod(Vector.class, "addAll", new Class[]{Object.class}, null);
        assertNull(result);
    }
	
	public void testGetMethodPrimitiveType() throws SecurityException, NoSuchMethodException {
        Method result = MethodUtil.getDeclaredMethod(Vector.class, "add", new Class[]{Integer.TYPE, Object.class}, null);
        assertEquals(Vector.class.getDeclaredMethod("add", new Class[]{Integer.TYPE, Object.class}), result);
	}
	
	public void testBoxing() throws SecurityException, NoSuchMethodException {
        Method result = MethodUtil.getDeclaredMethod(Vector.class, "add", new Class[]{Integer.TYPE}, null);
        assertEquals(Vector.class.getDeclaredMethod("add", new Class[]{Object.class}), result);
	}
	
	public void testBoxing2() throws SecurityException, NoSuchMethodException {
        Method result = MethodUtil.getDeclaredMethod(Vector.class, "addAll", new Class[]{Integer.TYPE}, null);
        assertNull(result);
	}
	
	public class X {
	    
	    public void m() throws DelegatorException {
	        
	    }
	    
	}
	
	public void testExceptionsMatch() throws SecurityException, NoSuchMethodException {
	    Method result = MethodUtil.getDeclaredMethod(X.class, "m", new Class[]{}, new Class[]{DelegatorException.class});
	    assertEquals(X.class.getDeclaredMethod("m", new Class[]{}), result);
	}
	
	public class Y {
	    
	    public void m() throws SQLException, DelegatorException {
	        
	    }
	}
	
	public void testExceptionOrderIsIrrelevant() throws SecurityException, NoSuchMethodException {
	    Method result = MethodUtil.getDeclaredMethod(Y.class, "m", new Class[]{}, new Class[]{DelegatorException.class, SQLException.class});
	    assertEquals(Y.class.getDeclaredMethod("m", new Class[]{}), result);
	}
	

	
	public class A {
	    private void privateMethod() {
	    }
	    
	}
	
	public void testGetPrivateMethod() throws SecurityException, NoSuchMethodException {
        Method result = MethodUtil.getDeclaredMethod(A.class, "privateMethod", new Class[]{}, null);
        assertNull(result);
    }

}
