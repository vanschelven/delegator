package org.cq2.delegator.examples.adapter;

import junit.framework.TestCase;

public class TestWithoutDelegation extends TestCase {
    
    public void testOne() { 
        FrameworkAContainer frameworkAContainer = new FrameworkAContainer();
        ANode node = frameworkAContainer.getNode();
        node.setA1("1");
        node.setDistinctA1("a1");
        FrameworkBContainer frameworkBContainer = new FrameworkBContainer();
        ANodeWrapper wrappedNode = new ANodeWrapper(node);
        frameworkBContainer.setNode(wrappedNode);
        assertEquals("1", node.getA1());
        assertEquals("a1", node.getDistinctA1());
        assertEquals("1", wrappedNode.getDelegate().getA1());
        assertEquals("1", wrappedNode.getB1());
        assertEquals("a1", wrappedNode.getDelegate().getDistinctA1());
    }
    
    public void testTwo() {
        FrameworkBContainer frameworkBContainer = new FrameworkBContainer();
        BNode node = frameworkBContainer.getNode();
        node.setB1("1");
        node.setDistinctB1("b1");
        FrameworkAContainer frameworkAContainer = new FrameworkAContainer();
        ANodeWrapper wrappedNode = new ANodeWrapper(node);
        frameworkAContainer.setNode(wrappedNode.getDelegate());
        assertEquals("1", node.getB1());
        assertEquals("b1", node.getDistinctB1());
        assertEquals("1", wrappedNode.getDelegate().getA1());
        assertEquals("b1", wrappedNode.getDistinctB1());
    }
    
//    public void testA() {
//        FrameworkAContainer frameworkAContainer = new FrameworkAContainer();
//        ANode node = frameworkAContainer.getNode();
//        node.setA1("1");
//        node.setDistinctA1("a1");
//        FrameworkBContainer frameworkBContainer = new FrameworkBContainer();
//        TwoWayAdapter adapter = TwoWayAdapter.create(node);
//        frameworkBContainer.setNode(adapter.asBNode());
//        assertEquals("1", node.getA1());
//        assertEquals("a1", node.getDistinctA1());
//        assertEquals("1", adapter.asANode().getA1());
//        assertEquals("a1", wrappedNode.getDelegate().getDistinctA1());
//    }
    
    //TODO afmaken
    //TODO: het voordeel onstaat pas zodra je de nodes niet zelf aan kan maken

}
