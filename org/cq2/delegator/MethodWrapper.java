package org.cq2.delegator;

public class MethodWrapper {

    public int i_invoke(Object component) {
        throw new RuntimeException("MethodWrapper code shouldn't be reached");
    }

}
