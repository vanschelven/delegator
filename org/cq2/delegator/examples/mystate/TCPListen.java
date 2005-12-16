package org.cq2.delegator.examples.mystate;

public abstract class TCPListen extends TCPState {
    
    public void send() {
        doWork("TCPListen.send()");
        changeState(TCPEstablished.class);
    }

}
