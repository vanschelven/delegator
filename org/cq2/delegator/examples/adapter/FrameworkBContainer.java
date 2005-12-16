package org.cq2.delegator.examples.adapter;

public class FrameworkBContainer {
    
    public FrameworkBContainer() {
        node = new BNode();
    }
    
    private BNode node;

    public BNode getNode() {
        return node;
    }

    public void setNode(BNode node) {
        this.node = node;
    }
}
