package org.cq2.delegator.profiling;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Vector;

import org.cq2.delegator.Self;

public class ZillionNewProxyCalls implements Profilable {

    private int numloops = 100000;

    public ZillionNewProxyCalls() {
    }
    
    public ZillionNewProxyCalls(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        System.out.println("Starting");
        new ZillionNewProxyCalls().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            Vector v = (Vector) new Self(Vector.class).cast(Vector.class);
            v.size();
        }
    }
    
}
