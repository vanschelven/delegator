package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

import org.cq2.delegator.Self;

public class ZillionProxyCalls implements Profilable {

    private Vector v;
    private int numloops = 100000;

    public ZillionProxyCalls() {
        Self self = new Self(Vector.class);
        v = (Vector) self.cast(Vector.class);
    }
    
    public ZillionProxyCalls(int numloops) {
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
