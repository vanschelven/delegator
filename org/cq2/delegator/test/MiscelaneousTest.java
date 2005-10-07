package org.cq2.delegator.test;

import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.DelegatorException;
import org.cq2.delegator.Self;

public class MiscelaneousTest extends TestCase {

    public void testSelfProxy() {
        Self self = new Self();
        Self proxy = (Self) self.cast(Self.class);
        proxy.add(Vector.class);
        assertTrue(self.getComponent(0).respondsTo(Vector.class));
    }

    public void testAddSelfToSelf() {
        Self self = new Self(Self.class);
        Self proxy = (Self) self.cast(Self.class);
        proxy.add(Vector.class);
        assertTrue(self.component(0) instanceof Self);
        assertTrue(((Self) self.component(0)).component(0) instanceof Vector);
    }

    public void testSingleComponentsMayNotBeShared() {
        Self self = new Self(Object.class);
        try {
            self.getComponent(Object.class);
            fail();
        } catch (DelegatorException e) {
        }
    }
    
    public void testArraysMayNotBeUsed() {
        Self self = new Self(new Object[]{new Object()});
        self.cast(Object.class);
        try {
            self.cast(Object[].class);
            fail();
        } catch (Error e) { }
        try {
            self = new Self(Object[].class);
            fail();
        } catch (Error e) { }
    }
    
}