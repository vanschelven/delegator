package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

import org.cq2.delegator.Self;

public class ZillionMixedProxyCalls implements Profilable {

    private Vector v;
    private int numloops = 100000;

    public ZillionMixedProxyCalls() {
        Self self = new Self(Vector.class);
        v = (Vector) self.cast(Vector.class);
    }
    
    public ZillionMixedProxyCalls(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        new ZillionMixedProxyCalls().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            v.capacity();
            v.size();
        }
    }
    
}
