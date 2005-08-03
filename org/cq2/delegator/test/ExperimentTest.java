package org.cq2.delegator.test;

import org.cq2.delegator.Self;

import junit.framework.TestCase;

public class ExperimentTest extends TestCase {

    final public static class FinalClass {
        
        public void method() {
            
        }
        
    }
    
    public void testFinalClass() {
        try {
            Self self = new Self(FinalClass.class);
            fail();
        } catch (VerifyError e) {}
    }

    public void testArray() {
        try {
            new Self(Object[].class);
            fail();
        } catch (ClassFormatError e) {}
    }
    
}
