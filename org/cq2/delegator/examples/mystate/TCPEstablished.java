package org.cq2.delegator.examples.mystate;

public abstract class TCPEstablished extends TCPState {
    
    public void close() {
        doWork("TCPEstablished.close()");
        changeState(TCPListen.class);
    }
    
    public void transmit(String data) {
        doWork("TCPEstablished.transmit(" + data + ")");
    }
       
}
