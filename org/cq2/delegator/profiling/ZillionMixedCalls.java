package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

public class ZillionMixedCalls implements Profilable {

    private Vector v;
    private int numloops = 100000;

    public ZillionMixedCalls() {
        v = new Vector();
    }
    
    public ZillionMixedCalls(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        new ZillionMixedCalls().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            v.capacity();
            v.size();
        }
    }
    
}
