package org.cq2.delegator.examples.mystate;

public abstract class TCPClosed extends TCPState {
    
    public void activeOpen() {
        doWork("TCPClosed.activeOpen()");
        changeState(TCPEstablished.class);
    }
    
    public void passiveOpen() {
        doWork("TCPClosed.passiveOpen()");
        changeState(TCPListen.class);
    }

}
