package org.cq2.delegator.profiling;

import java.util.Vector;

import org.cq2.delegator.Self;

public class StoreSelfCreations implements MemoryProfilable {

    private int numloops = 100000;

    private Vector v = new Vector();

    public MemoryProfilable newInstance() {
        return new StoreSelfCreations();
    }

    public void runBody() {
        for (int i = 0; i < numloops; i++) {
            v.add(new Self(Object.class));
        }
    }
    
    public Vector getV() {
        return v;
    }

}