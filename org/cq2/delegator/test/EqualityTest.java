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
    private Self selfAB1;
    private Self selfAB2;
    private Self selfBA;
    private Self selfABDifferentValue;

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
        
        public void setI(int i) {
            this.i = i;
        }
        
        public int getI() {
            return i;
        }
        
    }
    
    public static class B {
        
        private int j;
        
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof B)) return false;
            B other = (B) obj;
            return (other.j == j);
        }
        
        public B() {}
        
        public B(int j) {
            this.j = j;
        }
        
        public void setJ(int j) {
            this.j = j;
        }
        
        public int getJ() {
            return j;
        }
        
    }

    protected void setUp() throws Exception {
        a1 = new A(1);
        a2 = new A(1);
        differentA = new A(2);
        self1 = new Self(A.class);
        proxy1 = (A) self1.cast(A.class);
        proxy1.setI(1);
        self2 = new Self(A.class);
        proxy2 = (A) self2.cast(A.class);
        proxy2.setI(1);
        differentSelfValue = new Self(A.class);
        differentProxyValue = (A) differentSelfValue.cast(A.class);
        differentProxyValue.setI(3);
        
        selfAB1 = new Self(A.class);
        selfAB1.add(B.class);
        ((A) selfAB1.cast(A.class)).setI(1);
        selfAB2 = new Self(A.class);
        selfAB2.add(B.class);
        ((A) selfAB2.cast(A.class)).setI(1);
        selfABDifferentValue = new Self(A.class);
        selfABDifferentValue.add(B.class);
        ((A) selfABDifferentValue.cast(A.class)).setI(3);
        selfBA = new Self(B.class);
        selfBA.add(A.class);

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
        assertEquals(new Self().cast(A.class), new Self().cast(A.class));
        assertEquals(new Self().cast(A.class), new Self().cast(B.class));
        assertFalse(new Self().cast(A.class).equals(null));
    }
    
    public void testEmptySelfProxy() {
        assertEquals(new Self(), new Self().cast(A.class));
        assertEquals(new Self().cast(A.class), new Self());
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
    
    public void testObjectsWithoutEquals() {
        Self selfX = new Self(Object.class);
        assertEquals(selfX, selfX);
    }
    
    public void testTwoElementsSelf() {
        assertEquals(selfAB1, selfAB1);
        assertEquals(selfAB1, selfAB2);
        assertFalse(selfAB1.equals(null));
        assertFalse(selfAB1.equals(selfABDifferentValue));
        assertFalse(selfAB1.equals(selfBA));
    }

    public void testTwoElementsProxy() {
        assertEquals(selfAB1.cast(A.class), selfAB1.cast(A.class));
        assertEquals(selfAB1.cast(A.class), selfAB1.cast(B.class));
        assertFalse(selfAB1.cast(A.class).equals(null));
    }
    
    public void testEqualsFilter() {
        selfAB1.setEqualsComponents(new Class[]{A.class});
        assertEquals(selfAB1, self1);
        selfAB1.setEqualsComponents(new Class[]{});
        assertEquals(selfAB1, new Self());
    }
    

    public void testWeirdEqualityBetweenOriginalsAndProxies() {
        assertEquals(new A(), proxy1);
        assertEquals(1, proxy1.getI());
        assertEquals(0,new A().getI());
        assertFalse(proxy1.equals(new A()));
        a1 = new A();
        a1.setI(1);
        assertFalse(a1.equals(proxy1));
        assertEquals(1, proxy1.getI());
        assertEquals(1, a1.getI());
    }
    
    //this can be used to make sure Object.equals isn't the method really used, because even though fields may differ....
    public static class OddOrEven {
        
        private int k;
        
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (!(obj instanceof OddOrEven)) return false;
            OddOrEven other = (OddOrEven) obj;
            return (other.k % 2 == k % 2);
        }
        
        public OddOrEven() {}
        
        public OddOrEven(int k) {
            this.k = k;
        }
        
        public void setK(int k) {
            this.k = k;
        }
        
        public int getK() {
            return k;
        }
        
    }

    public void testEqualityBetweenWrappedForwardees() {
        Self selfForwardee1 = new Self(new A(1));
        Self selfForwardee2 = new Self(new A(1));
        Self differentForwardeeSelf = new Self(new A(2));
        assertEquals(selfForwardee1, selfForwardee1);
        assertEquals(selfForwardee1, selfForwardee2);
        assertFalse(selfForwardee1.equals(null));
        assertFalse(selfForwardee1.equals(differentForwardeeSelf));
        
        assertEquals(new Self(new OddOrEven(1)), new Self(new OddOrEven(3)));
    }
    
    public void testEqualityBetweenDelegatesAndForwardees() {
        Self selfForwardee1 = new Self(new A(1));
        assertEquals(selfForwardee1, self1);
        assertEquals(self1, selfForwardee1);
        assertFalse(selfForwardee1.equals(differentSelfValue));
        assertFalse(differentSelfValue.equals(selfForwardee1));
    }
    
}


//TODO heeeeeel ergens anders opnemen: hoe zit het met classes zonder defaultconstructor als component??
