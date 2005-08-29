package org.cq2.delegator.profiling;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Vector;

import org.cq2.delegator.Self;

public class ZillionCompositionChanges implements Profilable {

    private int numloops = 100000;
    private Self self;

    public ZillionCompositionChanges() {
        self = new Self();
    }
    
    public ZillionCompositionChanges(int numloops) {
        this();
        this.numloops = numloops;
    }
    
    public static void main(String[] args) {
        System.out.println("Starting");
        new ZillionCompositionChanges().runBody();
        System.out.println("Done! " + new Date());
    }

    public void runBody() {
        Self component = new Self(Vector.class);
        for (int i = 0; i < numloops; i++) {
            self.add(component);
            self.remove(Vector.class);
        }
    }
    
}
