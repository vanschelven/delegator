package org.cq2.delegator.profiling;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Vector;

import org.cq2.delegator.Self;

public class ZillionCasts implements Profilable {

    private int numloops = 100000;
    private Self self;

    public ZillionCasts() {
        self = new Self(Vector.class);
    }
    
    public ZillionCasts(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        System.out.println("Starting");
        new ZillionCasts().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            Vector v = (Vector) self.cast(Vector.class);
        }
    }
    
}
