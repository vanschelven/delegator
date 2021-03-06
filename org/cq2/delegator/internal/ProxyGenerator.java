package org.cq2.delegator.internal;

import org.cq2.delegator.Proxy;

public class ProxyGenerator extends ClassGenerator {
    
    public ProxyGenerator(String className, Class superClass) {
        super(className, superClass, Proxy.class);
    }
    
    public byte[] generate() {
        addSelfField();
        addDelegationMethods(forwardingMethodFilter, true);
        return classGen.getJavaClass().getBytes();
    }


}
