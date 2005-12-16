//TODO schrijf: polymorphism in de scriptie opnemen...

package org.cq2.delegator.test;

import junit.framework.TestCase;

import org.cq2.delegator.Self;

import sun.security.krb5.internal.ccache.as;

public class PolymorphismTest extends TestCase {

    private static Class reachedClass;

    public static class L1 {
    }

    public static class L2 extends L1 {
    }

    public static class L3 extends L2 {
    }

    public static class ThreeMethods {
        public void method(L1 o) {
            reachedClass = L1.class;
        }

        public void method(L2 o) {
            reachedClass = L2.class;
        }

        public void method(Object o) {
            reachedClass = Object.class;
        }

    }

    public static class ThreeMethodsReverse extends ThreeMethods {
        
        public void method(L2 o) {
            reachedClass = L2.class;
        }

        public void method(L1 o) {
            reachedClass = L1.class;
        }

        public void method(Object o) {
            reachedClass = Object.class;
        }
    }

    public void testRegularJava() {
        ThreeMethods m = new ThreeMethods();
        doTests(m);
    }

    public void testDelegation() {
        //deze test slaagt in sommige situaties ook als self.invoke slecht is geimplementeerd... zie mijn bookmark thuis
        ThreeMethods m = (ThreeMethods) new Self(ThreeMethods.class).cast(ThreeMethods.class);
        doTests(m);
    }
    
    public void testDelegation2() {
        //idem als bovenstaand - dit heeft te maken met de volgorde waarin getDeclaredMethods werkt
        ThreeMethods m = (ThreeMethods) new Self(ThreeMethodsReverse.class).cast(ThreeMethodsReverse.class);
    }

   private void doTests(ThreeMethods m) {
        m.method(new Object());
        assertEquals(Object.class, reachedClass);
        m.method(new L1());
        assertEquals(L1.class, reachedClass);
        m.method(new L2());
        assertEquals(L2.class, reachedClass);

        Object l2 = new L2();
        m.method(l2);
        assertEquals(Object.class, reachedClass);

        L1 o = new L2();
        m.method(o);
        assertEquals(L1.class, reachedClass);
   }
   
   private static String SET_OO = "set(Object key, Object value)";
   private static String GET_O = "get(Object key)";
   private static String SET_IO = "set(int position, Object value)";
   private static String GET_I = "get(int position)";

   //TODO (ergens anders) waarom moeten inner classes static zijn? Omdat ze een extra thispointer genereren. En wat is de foutmelding?
   
   public static class PHPArray {
       
       private String invokedMethod = "";
             
       public void set(Object key, Object value) {
           invokedMethod = SET_OO;
       }
       
       public Object get(Object key) {
           invokedMethod = GET_O;
           return null;
       }
       
       public void set(int position, Object value) {
           invokedMethod = SET_IO;
       }
       
       public Object get(int position) {
           invokedMethod = GET_I;
           return null;
       }
       
       public String getInvokedMethod() {
           return invokedMethod;
       }
       
   }
   
   public void testBoxingProblems() {
       PHPArray array = (PHPArray) new Self(PHPArray.class).cast(PHPArray.class);
       array.set(0, "value");
       assertEquals(SET_IO, array.getInvokedMethod());
       array.set("key", "value");
       assertEquals(SET_OO, array.getInvokedMethod());
       array.get(0);
       assertEquals(GET_I, array.getInvokedMethod());
       array.get("key");
       assertEquals(GET_O, array.getInvokedMethod());
   }

}