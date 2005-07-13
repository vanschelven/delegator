package org.cq2.delegator.classgenerator;

import org.cq2.delegator.Component;

public class SingleSelfComponentGenerator extends ClassGenerator {

    public SingleSelfComponentGenerator(String className, Class superClass) {
        super(className, superClass, Component.class);
    }
    
    public byte[] generate() {
        addSelfField();
        addDelegationMethods(componentMethodFilter, true);
        addSuperCallMethods(componentMethodFilter);
        return classGen.getJavaClass().getBytes();
    }
    
}
