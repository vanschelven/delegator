package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

public class ZillionNewCalls implements Profilable {

    private int numloops = 100000;

    public ZillionNewCalls() {
    }
    
    public ZillionNewCalls(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        new ZillionNewCalls().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            Vector v = new Vector();
            v.size();
        }
    }
    
}
