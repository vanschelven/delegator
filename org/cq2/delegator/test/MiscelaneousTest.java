package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.util.Vector;

import org.cq2.delegator.Component;
import org.cq2.delegator.Self;

import junit.framework.TestCase;

public class MiscelaneousTest extends TestCase {

    public void testSelfProxy() {
        Self self = new Self();
        Self proxy = (Self) self.cast(Self.class);
        proxy.add(Vector.class);
        assertTrue(self.component(0) instanceof Vector);
    }
    
//TODO dit kan (nog) niet omdat je herhalingen van methodes krijgt. Is onderdeel van een groter probleem!
//    public void testAddSelfToSelf() {
//        Self self = new Self(Self.class);
//        Self proxy = (Self) self.cast(Self.class);
//        proxy.add(Vector.class);
//        assertTrue(self.component(0) instanceof Self);
//        assertTrue(((Self) self.component(0)).component(0) instanceof Vector);
//    }
    
//    public static class HasInvocationHanlderSignature {
//        
//        public void method() {}
//        public void method(InvocationHandler h) {}
//    }
//    
//    public void testMethodDuplication() {
//        new Self(HasInvocationHanlderSignature.class);
//    }
 
}
