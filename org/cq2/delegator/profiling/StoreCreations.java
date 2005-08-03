package org.cq2.delegator.profiling;

import java.util.Vector;

public class StoreCreations implements MemoryProfilable {

    private int numloops = 100000;
    private Vector v = new Vector();
    
    public MemoryProfilable newInstance() {
        return new StoreCreations();
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            v.add(new Object());
        }
    }

    public Vector getV() {
        return v;
    }

}
