package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

import org.cq2.delegator.Self;

public class ZillionCallsToComponentTwo implements Profilable {

    private Vector v;
    private int numloops = 100000;

    public ZillionCallsToComponentTwo() {
        Self self = new Self(Date.class);
        self.add(Vector.class);
        v = (Vector) self.cast(Vector.class);
    }
    
    public ZillionCallsToComponentTwo(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        new ZillionProxyCalls().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            v.size();
        }
    }
    
}

