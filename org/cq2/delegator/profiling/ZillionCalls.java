package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

public class ZillionCalls implements Profilable {

    private Vector v;
    private int numloops = 100000;

    public ZillionCalls() {
        v = new Vector();
    }
    
    public ZillionCalls(int numloops) {
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
