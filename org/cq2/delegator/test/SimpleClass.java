package org.cq2.delegator.test;

import org.cq2.delegator.Self;

public class SimpleClass {

    public void method() {
        
    }
    
    public int return1() {
        return 1;
    }
    
    public Object returnNewObject() {
        return new Object();
    }
    
    public void newObject() {
        new Object();
    }
    
    public SimpleClass returnThis() {
        return this;
    }
    
    public SimpleClass returnSelf(Self self) {
        return (SimpleClass) self.cast(getClass());
    }
    
}
