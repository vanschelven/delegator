package org.cq2.delegator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import junit.framework.TestCase;

public class ComposedClassTest extends TestCase {

    private Method method;
    private int identifier;
    private ComposedClass composedClass;
    private static boolean methodCalled;
    
    protected void setUp() throws Exception {
        method = A.class.getDeclaredMethod("add", new Class[]{Object.class});
        identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(method);
        composedClass = ComposedClass.getEmptyClass().add(A.class);
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
        ProxyMethod proxyMethod = composedClass.getMethod(identifier);
        assertFalse(Modifier.isAbstract(proxyMethod.getClass().getModifiers()));
        Method proxyMethodMethod = proxyMethod.getClass().getDeclaredMethod("invoke", new Class[]{Self.class, Object.class});
        assertFalse(Modifier.isAbstract(proxyMethodMethod.getModifiers()));
        Self self = new Self(A.class);
        assertEquals(new Boolean(false), proxyMethodMethod.invoke(proxyMethod, new Object[]{self, new Integer(5)}));
        assertTrue(methodCalled);
        //TODO field set to 0, call reaches the vector of a self
        //TODO resolve pakt altijd 0, add
    }
    
    public void test2() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        method = A.class.getDeclaredMethod("size", new Class[]{});
        identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(method);
        
        ProxyMethod proxyMethod = composedClass.getMethod(identifier);
        Method proxyMethodMethod = proxyMethod.getClass().getDeclaredMethod("invoke", new Class[]{Self.class});
        Self self = new Self(A.class);
        assertEquals(new Integer(666), proxyMethodMethod.invoke(proxyMethod, new Object[]{self}));
        assertTrue(methodCalled);
    }
    
    
}
