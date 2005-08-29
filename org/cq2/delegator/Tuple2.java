package org.cq2.delegator;

public class Tuple2 {

    public Object component;
    public MethodWrapper wrapper;
    
    public Tuple2(Object component, MethodWrapper wrapper) {
        this.component = component;
        this.wrapper = wrapper;
    }
    
}
