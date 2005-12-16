package org.cq2.delegator.examples.adapter;

public class ANodeWrapper extends BNode {
    
    ANode delegate;
    
    public ANodeWrapper(ANode node) {
        delegate = node;
    }
    
    public ANodeWrapper(BNode node) {
        delegate = new ANode();
        delegate.setA1(node.getB1());
        delegate.setA2(node.getB2());
        delegate.setA3(node.getB3());
        delegate.setA4(node.getB4());
        setDistinctB1(node.getDistinctB1());
        setDistinctB2(node.getDistinctB2());
    }


    public ANode getDelegate() {
        return delegate;
    }
    
    public void setDelegate(ANode delegate) {
        this.delegate = delegate;
    }
    
    public Object getB1() {
        return delegate.getA1();
    }
    
    public Object getB2() {
        return delegate.getA2();
    }
    
    public Object getB3() {
        return delegate.getA3();
    }
    
    public Object getB4() {
        return delegate.getA4();
    }
    
    public void setB1(Object b1) {
        delegate.setA1(b1);
    }
    
    public void setB2(Object b2) {
        delegate.setA1(b2);
    }
    
    public void setB3(Object b3) {
        delegate.setA1(b3);
    }
    
    public void setB4(Object b4) {
        delegate.setA1(b4);
    }
}
