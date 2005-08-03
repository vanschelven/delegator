package org.cq2.delegator.profiling;

import java.util.Date;
import java.util.Vector;

public class ZillionParameterCalls implements Profilable {

    private Vector v;
    private int numloops = 100000;

    public ZillionParameterCalls() {
        v = new Vector();
    }
    
    public ZillionParameterCalls(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        new ZillionParameterCalls().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        String string = "aaa";
        for (int i = 0; i < numloops; i++) {
            v.contains(string);
        }
    }
    
}
