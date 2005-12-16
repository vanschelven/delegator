package org.cq2.delegator.examples.mystate;

import java.util.Vector;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

public abstract class TCPConnection implements ITCPState, ISelf {

    private Vector work;
   
    public TCPConnection() {
        work = new Vector();
    }
    
    public void doWork(String s) {
        work.add(s);
    }
    
    public String getWork() {
        return work.toString();
    }

    public static TCPConnection create() {
        Self self = new Self();
        self.add(TCPClosed.class);
        self.add(TCPConnection.class);
        return (TCPConnection) self.cast(TCPConnection.class);
    }
    
    public void changeState(Class clazz) {
        remove(TCPState.class);
        insert(clazz);
    }
    
}
