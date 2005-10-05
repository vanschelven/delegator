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

        String name = "";
        
        public Document() {
            
        }
        
        public Document(String name) {
            this.name = name;
        }

        public Node createNode() {
            return new Node(this);
        }

        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }

    public void testExampleFromProposalRegularJava() {
        Document document = new Document("name");
        Node node = document.createNode();
        assertTrue(document.equals(node.getDocument()));
    }

    /**
     * Shows that equality doesn't work for components that leak out of their containing Selves.
     */
    public void testExampleFromProposal() {
        Self self = new Self(Document.class);
        Document document = (Document) self.cast(Document.class);
        document.setName("name");
        Node node = document.createNode();
        assertFalse(document.equals(node.getDocument()));
    }

    public void testExampleFromThesisRegularJava() {
        Document document = new Document("name");
        Node node = document.createNode();
        assertEquals("name", node.getDocument().getName());
    }

    public void testExampleFromThesis() {
        Self self = new Self(Document.class);
        Document document = (Document) self.cast(Document.class);
        document.setName("name");
        Node node = document.createNode();
        assertEquals("name", node.getDocument().getName());
    }

}