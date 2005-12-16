package org.cq2.delegator.examples.adapter;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

public abstract class TwoWayAdapter implements ISelf {
    
    public static TwoWayAdapter create(Object node) {
        Self self = new Self(node);
        self.add(TwoWayAdapter.class);
        return (TwoWayAdapter) self.cast(TwoWayAdapter.class);
    }
    
    public ANode asANode() {
        return (ANode) cast(ANode.class);       
    }
    
    public BNode asBNode() {
        return (BNode) cast(BNode.class);
    }
    
    

}
