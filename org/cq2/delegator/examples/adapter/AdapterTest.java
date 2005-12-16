package org.cq2.delegator.examples.adapter;

import junit.framework.TestCase;

public class AdapterTest extends TestCase {

    public void testOne() {
        OxbridgeBook book = OxbridgeBook.create();
        book.setCode("Foo");
        assertEquals("Foo", book.getSerialNumber());
        book.setSerialNumber("Bar");
        assertEquals("Bar", book.getCode());
    }
    
}
