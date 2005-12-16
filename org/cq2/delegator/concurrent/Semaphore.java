package org.cq2.delegator.concurrent;


public class Semaphore implements ISemaphore {

    private static ThreadLocal threadLocal = new ThreadLocal() {

        protected Object initialValue() {
            return new Integer(0);
        }
                
    };
    
    private boolean acquired;
    
    public synchronized void acquire() throws InterruptedException {
        tryToAcquire();
        reallyAcquire();
    }
    
    private void tryToAcquire() throws InterruptedException {
        while (acquired && !byMe()) {
            wait();
        }
    }

    private boolean byMe() {
        return get() > 0;
    }

    private int get() {
        return ((Integer) threadLocal.get()).intValue();
    }
    
    private void set(int value) {
        threadLocal.set(new Integer(value));
    }
    
    private void reallyAcquire() {
        acquired = true;
        set(get() + 1);
    }

    public synchronized void release() {
        set(get() - 1);
        if (get() == 0) {
            acquired = false;
            notifyAll();
        }
    }
    
}
