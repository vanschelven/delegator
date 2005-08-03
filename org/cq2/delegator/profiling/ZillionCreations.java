package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

public class ZillionCreations implements Profilable {

    private int numloops = 100000;

    public ZillionCreations() {
    }
    
    public ZillionCreations(int numloops) {
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        new ZillionSelfCreations().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            new Vector();
        }
    }
}
