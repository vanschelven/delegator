package org.cq2.delegator.test;

import junit.framework.TestCase;

import org.cq2.delegator.Self;

public class ThisPointerEncapsulationTest extends TestCase {

    public class SelfPointingClass {
        
        public SelfPointingClass returnThis() {
            return this;
        }
    }
    
    public void testRegularJava() {
        SelfPointingClass selfPointingClass1 = new SelfPointingClass();
        SelfPointingClass selfPointingClass2 = selfPointingClass1.returnThis();
        assertEquals(selfPointingClass1, selfPointingClass2);
    }
    
    public static abstract class DelegatingSelfPointingClass {
        
        public DelegatingSelfPointingClass returnThis() {
            return this;
        }
                
    }
    
    public static class Node {
        
        Document document;
        
        public Node(Document document) {
            this.document = document;
        }
        
        public Document getDocument() {
            return document;
        }
        
    }
    
    public static class Document {
        
        public Node createNode() {
            return new Node(this);
        }
        
        public int getOne() {
            return 1;
        }
        
    }
    
    public void testExampleFromProposalRegularJava() {
        Document document = new Document();
        Node node = document.createNode();
        assertEquals(document, node.getDocument());
    }
    
    public void testExampleFromProposal() {
        Self self = new Self(Document.class);
        Document document = (Document) self.cast(Document.class);
        Node node = document.createNode();
        assertEquals(document, node.getDocument());
    }
    
    public void testExampleFromThesisRegularJava() {
        Document document = new Document();
        Node node = document.createNode();
        assertEquals(1, node.getDocument().getOne());
    }
    
    public void testExampleFromThesis() {
        Self self = new Self(Document.class);
        Document document = (Document) self.cast(Document.class);
        Node node = document.createNode();
        assertEquals(1, node.getDocument().getOne());
    }
    
// TODO Turn this on: the final test
//    public void testDelegation() {
//        DelegatingSelfPointingClass a1 = (DelegatingSelfPointingClass) Delegator.extend(DelegatingSelfPointingClass.class, Vector.class);
//        DelegatingSelfPointingClass a2 = a1.returnThis();
//        assertEquals(a1, a2);
//    }
    
}
