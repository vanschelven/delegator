package org.cq2.delegator.examples.mystate;

public interface ITCPState {

    public void doWork(String s);
    public void transmit(String data);
    public void activeOpen();
    public void passiveOpen();
    public void close();
    public void synchronize();
    public void acknowledge();
    public void send();
    public void changeState(Class clazz);
    
}
