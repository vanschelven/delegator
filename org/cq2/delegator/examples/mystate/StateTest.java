package org.cq2.delegator.examples.mystate;

import junit.framework.TestCase;

public class StateTest extends TestCase {
    
    public void testCreation() {
        TCPConnection tcpConnection = TCPConnection.create();
        assertEquals("[]", tcpConnection.getWork());
    }

    public void testSingleCall() {
        TCPConnection tcpConnection = TCPConnection.create();
        tcpConnection.activeOpen();
        assertEquals("[TCPClosed.activeOpen()]", tcpConnection.getWork());
    }

    public void testSeries1() {
        TCPConnection tcpConnection = TCPConnection.create();
        tcpConnection.activeOpen();
        tcpConnection.transmit("hallo");
        tcpConnection.close();
        assertEquals("[TCPClosed.activeOpen(), " +
        		     "TCPEstablished.transmit(hallo), " +
        		     "TCPEstablished.close()]", tcpConnection.getWork());
    }
    
    public void testSeries2() {
        TCPConnection tcpConnection = TCPConnection.create();
        tcpConnection.passiveOpen();
        tcpConnection.send();
        tcpConnection.transmit("hallo");
        tcpConnection.close();
        assertEquals("[TCPClosed.passiveOpen(), " +
        		     "TCPListen.send(), " +
        		     "TCPEstablished.transmit(hallo), " +
        		     "TCPEstablished.close()]", tcpConnection.getWork());
    }

}
