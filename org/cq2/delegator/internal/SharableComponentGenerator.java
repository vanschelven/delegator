package org.cq2.delegator.internal;

import org.cq2.delegator.Component;

public class SharableComponentGenerator extends ClassGenerator {

    public SharableComponentGenerator(String className, Class superClass) {
        super(className, superClass, Component.class);
    }
    
    public byte[] generate() {
        addDelegationMethods(componentMethodFilter, false);
        addSuperCallMethods(componentMethodFilter);
        return classGen.getJavaClass().getBytes();
    }
    
}