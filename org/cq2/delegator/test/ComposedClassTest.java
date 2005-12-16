package org.cq2.delegator.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.Self;
import org.cq2.delegator.SelfTest.F1;
import org.cq2.delegator.SelfTest.F2;
import org.cq2.delegator.internal.ComposedClass;
import org.cq2.delegator.internal.ForwardingMethod;
import org.cq2.delegator.internal.ForwardingMethodRegister;

public class ComposedClassTest extends TestCase {

    private Method method;
    private int identifier;
    private ComposedClass composedClass;
    private static boolean methodCalled;
    private Self self;
    
    protected void setUp() throws Exception {
        method = A.class.getDeclaredMethod("add", new Class[]{Object.class});
        identifier = ForwardingMethodRegister.getInstance().getMethodIdentifier(method);
        self = new Self(A.class);
        composedClass = self.composedClass;
        methodCalled = false;
    }
    
    public static class A {
        
        public boolean add(Object o) {
            methodCalled = true;
            return false;
        }
        
        
        public int size() {
            methodCalled = true;
            return 666;
        }
    }
    
    public void testGetMethod() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ForwardingMethod forwardingMethod = composedClass.getMethod(identifier);
        assertFalse(Modifier.isAbstract(forwardingMethod.getClass().getModifiers()));
        Method forwardingMethodMethod = forwardingMethod.getClass().getDeclaredMethod("__invoke_add", new Class[]{Self.class, Object.class});
        assertFalse(Modifier.isAbstract(forwardingMethodMethod.getModifiers()));
        assertEquals(new Boolean(false), forwardingMethodMethod.invoke(forwardingMethod, new Object[]{self, new Integer(5)}));
        assertTrue(methodCalled);
    }
    
    public void test2() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        method = A.class.getDeclaredMethod("size", new Class[]{});
        identifier = ForwardingMethodRegister.getInstance().getMethodIdentifier(method);
        
        ForwardingMethod forwardingMethod = composedClass.getMethod(identifier);
        Method forwardingMethodMethod = forwardingMethod.getClass().getDeclaredMethod("__invoke_size", new Class[]{Self.class});
        Self self = new Self(A.class);
        assertEquals(new Integer(666), forwardingMethodMethod.invoke(forwardingMethod, new Object[]{self}));
        assertTrue(methodCalled);
    }
    
	public void testGetSuffix() {
	    ComposedClass expected = ComposedClass.getEmptyClass().add(Vector.class);
	    ComposedClass larger = ComposedClass.getEmptyClass().add(F1.class).add(F2.class).add(Vector.class);
	    assertEquals(expected, larger.getSuffix(2));
	}

}
