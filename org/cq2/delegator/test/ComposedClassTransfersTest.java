package org.cq2.delegator.test;

import java.util.HashMap;
import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.internal.ComposedClass;

public class ComposedClassTransfersTest extends TestCase {

    public void testGetEmptyClass() {
        assertNotNull(ComposedClass.getEmptyClass());
    }
    
    public void testTransfers() {
        ComposedClass empty1 = ComposedClass.getEmptyClass();
        ComposedClass v1 = empty1.add(Vector.class);
        ComposedClass vm1 = v1.add(HashMap.class);
        ComposedClass v2 = vm1.remove(1);
        ComposedClass mv1 = v1.insert(HashMap.class);
        ComposedClass v3 = mv1.remove(0);
        ComposedClass empty2 = v1.remove(0);
        assertEquals(empty1, empty2);
        assertEquals(v1, v2);
        assertEquals(v2, v3);
        assertEquals(v1, v3);
        assertFalse(empty1.equals(v1));
    }
    
    public void testTransfersSecondTime() {
        testTransfers(); //Caches should now be initialized and still working
    }

    
}
