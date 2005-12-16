package org.cq2.delegator.concurrent;

public interface ISemaphore {

    public void acquire() throws InterruptedException;
    public void release() throws InterruptedException;
    
}
