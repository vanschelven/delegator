package org.cq2.delegator.profiling;

import java.util.Date;

public class LoopTime implements Profilable {

    private int numloops = 1000000;

    public LoopTime() {
    }
    
    public LoopTime(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        new LoopTime().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
        }
    }
    
}
