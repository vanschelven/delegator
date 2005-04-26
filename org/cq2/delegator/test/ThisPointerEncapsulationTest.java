package org.cq2.delegator.test;

import junit.framework.TestCase;

public class ThisPointerEncapsulationTest extends TestCase {

    public class SelfPointingClass {
        
        public SelfPointingClass returnThis() {
            return this;
        }
    }
    
    public void testRegularJava() {
        SelfPointingClass selfPointingClass1 = new SelfPointingClass();
        SelfPointingClass selfPointingClass2 = selfPointingClass1.returnThis();
        assertEquals(selfPointingClass1, selfPointingClass2);
    }
    
    public static abstract class DelegatingSelfPointingClass {
        
        public DelegatingSelfPointingClass returnThis() {
            return this;
        }
                
    }
    
//TODO Turn this on
//    public void testDelegation() {
//        DelegatingSelfPointingClass a1 = (DelegatingSelfPointingClass) Delegator.extend(DelegatingSelfPointingClass.class, Vector.class);
//        DelegatingSelfPointingClass a2 = a1.returnThis();
//        assertEquals(a1, a2);
//    }
    
}
