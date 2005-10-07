package org.cq2.delegator.test;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.Self;

public class AutomaticForwardingTest extends TestCase {

    public static class A {
        
        public void method() {  }
        
    }
    
    public static class B {
        
        private boolean called;

        public void method() {
            called = true;
        }
        
        public boolean isCalled() {
            return called;
        }
        
    }
    
    public static class C {
        
        private boolean called;

        public void callMethod() {
            method();
        }
        
        public void method() {
            called = true;
        }
        
        public boolean isCalled() {
            return called;
        }
        
    }
      
    public void testSimpleCase() {
        Self self = new Self();
        self.addForwardee(new A());
        ((A) self.cast(A.class)).method();
    }
  
    public void testConstructor() {
        Self self = new Self(new A());
        ((A) self.cast(A.class)).method();
    }
    
    public void testSelfProblemNotSolved() {
        Self self = new Self();
        self.add(B.class);
        C c = new C();
        self.addForwardee(c);
        C proxy = (C) self.cast(C.class);
        proxy.callMethod();
        assertTrue(c.isCalled());
        assertFalse(((B) self.cast(B.class)).isCalled());
    }
    
    public interface IString {
        public int length();
    }
    
//  TODO dit werkt inderdaad nog niet - op meerdere plaatsen moet een aanroep komen zonder self argument
    public void testFinalTypesCanBeUsed() {
        String string = "String is a final class";
        Self self = new Self(string);
        assertEquals(string.length(), ((IString) self.cast(IString.class)).length());
    }
    
    //TODO dit werkt inderdaad nog niet - op meerdere plaatsen moet een aanro
    public void testLiveObjectsCanBeWrapped() {
        Vector existingVector = new Vector();
        existingVector.add("0");
        existingVector.add("1");
        Self self = new Self(existingVector);
        List list = (List) self.cast(List.class);
        assertEquals(2, list.size());
    }
    
}
