package org.cq2.delegator;

public class DelegatorException extends RuntimeException {

    public DelegatorException(String string) {
        super(string);
    }
    
    public DelegatorException(String message, Throwable cause) {
        super(message, cause);
    }

}
