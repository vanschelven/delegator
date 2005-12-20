package org.cq2.delegator.internal;

import org.cq2.delegator.SingleSelfComponent;

public class SingleSelfComponentGenerator extends ClassGenerator {

    public SingleSelfComponentGenerator(String className, Class superClass) {
        super(className, superClass, SingleSelfComponent.class);
    }
    
    public byte[] generate() {
        addSelfField();
        addDelegationMethods(componentMethodFilter, true);
        addSuperCallMethods(componentMethodFilter);
        return classGen.getJavaClass().getBytes();
    }
    
}
