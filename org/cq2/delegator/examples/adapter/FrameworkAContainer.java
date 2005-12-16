package org.cq2.delegator.examples.adapter;

public class FrameworkAContainer {
    
    public FrameworkAContainer() {
        node = new ANode();
    }
    
    private ANode node;

    public ANode getNode() {
        return node;
    }

    public void setNode(ANode node) {
        this.node = node;
    }
}
