package org.cq2.delegator.test;

/*
knipperdeknip uit Object.equals:
The equals method implements an equivalence relation on non-null object references: 
	- It is reflexive: for any non-null reference value x, x.equals(x) should return true. 
	- It is symmetric: for any non-null reference values x and y, x.equals(y) should return true if and only if y.equals(x) 
	 returns true. 
	- It is transitive: for any non-null reference values x, y, and z, if x.equals(y) returns true and y.equals(z) returns true, 
	 then x.equals(z) should return true. 
	- It is consistent: for any non-null reference values x and y, multiple invocations of x.equals(y) consistently return true 
	 or consistently return false, provided no information used in equals comparisons on the objects is modified. 
	- For any non-null reference value x, x.equals(null) should return false.
*/

import java.util.HashMap;
import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.Self;

public class EqualityTest extends TestCase {

    private A a1;
    private A a2;
    private A differentA;
    private Self self1;
    private Self self2;
    private Self differentSelfValue;
    private A proxy1;
    private A proxy2;
    private A differentProxyValue;

    public static class A {
        
        private int i;
        
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof A)) return false;
            A other = (A) obj;
            return (other.i == i);
        }
        
        public A() {}
        
        public A(int i) {
            this.i = i;
        }
        
        public void set(int i) {
            this.i = i;
        }
        
        public int get() {
            return i;
        }
        
    }
    
    public static class B {
    }

    protected void setUp() throws Exception {
        a1 = new A(1);
        a2 = new A(1);
        differentA = new A(2);
        self1 = new Self(A.class);
        proxy1 = (A) self1.cast(A.class);
        proxy1.set(1);
        self2 = new Self(A.class);
        proxy2 = (A) self2.cast(A.class);
        proxy2.set(1);
        differentSelfValue = new Self(A.class);
        differentProxyValue = (A) differentSelfValue.cast(A.class);
        differentProxyValue.set(3);
        
        //dan hebben we nog: b-proxy op a, b-proxy op b, a-proxy op b
    }
    
    public void testA() {
        assertEquals(a1, a2);
        assertEquals(a2, a1);
        assertFalse(a1.equals(null));
        assertFalse(a1.equals(differentA));
    }
    
    public void testEmptySelf() {
        assertEquals(new Self(), new Self());
        assertFalse(new Self().equals(null));
        assertFalse(new Self().equals(self1));
    }
    
    public void testEmptyProxy() {
        assertEquals(new Self().cast(Vector.class), new Self().cast(Vector.class));
        assertEquals(new Self().cast(Vector.class), new Self().cast(HashMap.class));
        assertFalse(new Self().cast(Vector.class).equals(null));
    }
    
    public void testEmptySelfProxy() {
        assertEquals(new Self(), new Self().cast(Vector.class));
        assertEquals(new Self().cast(Vector.class), new Self());
    }
    
    public void testOneElementSelf() {
        assertEquals(self1, self1);
        assertEquals(self1, self2);
        assertFalse(self1.equals(null));
        assertFalse(self1.equals(differentSelfValue));
    }
    
    public void testOneElementProxy() {
        assertEquals(proxy1, proxy1);
        assertEquals(proxy1, proxy2);
        assertFalse(proxy1.equals(null));
        assertFalse(proxy1.equals(differentProxyValue));
    }
    
    public void testOneElementSelfProxy() {
        assertEquals(self1, proxy1);
        assertEquals(proxy1, self1);
        assertFalse(self1.equals(differentProxyValue));
        assertFalse(proxy1.equals(differentSelfValue));
    }
    

    
}


//TODO heeeeeel ergens anders opnemen: hoe zit het met classes zonder defaultconstructor als component??
//TODO heeeeeel ergens anders hoe gaan we om met het toevoegen van bestaande objecten als componenten??
