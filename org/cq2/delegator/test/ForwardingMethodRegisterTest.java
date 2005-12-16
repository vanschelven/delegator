package org.cq2.delegator.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.bcel.generic.Type;
import org.cq2.delegator.Self;
import org.cq2.delegator.internal.ForwardingMethodRegister;

public class ForwardingMethodRegisterTest extends TestCase {

    Method method;
    
    protected void setUp() throws Exception {
        super.setUp();
        method = Vector.class.getDeclaredMethod("add", new Class[]{Object.class});
    }
    
    public void testAddMethod() {
        int identifier = ForwardingMethodRegister.getInstance().getMethodIdentifier(method);
        assertEquals(method, ForwardingMethodRegister.getInstance().getMethod(identifier));
    }
    
    public void testGetForwardingMethodClass() throws SecurityException, NoSuchMethodException {
        int identifier = ForwardingMethodRegister.getInstance().getMethodIdentifier(method);
        Class clazz = ForwardingMethodRegister.getInstance().getForwardingMethodClass(identifier);
        assertTrue(Modifier.isAbstract(clazz.getModifiers()));
        assertEquals("org.cq2.delegator.internal.ForwardingMethod" + identifier, clazz.getName());
        assertEquals("Lorg/cq2/delegator/internal/ForwardingMethod" + identifier + ";", Type.getType(clazz).getSignature());
        Method classMethod = clazz.getDeclaredMethod("__invoke_add", new Class[]{Self.class, Object.class});
        assertTrue(Modifier.isAbstract(classMethod.getModifiers()));
    }
    
    class A {
        
        void throwsException() throws Exception {
            
        }
        
    }
    
    public void testExceptions() throws SecurityException, NoSuchMethodException {
        Method exceptionMethod = A.class.getDeclaredMethod("throwsException", new Class[]{});
        int identifier = ForwardingMethodRegister.getInstance().getMethodIdentifier(exceptionMethod);
        Class clazz = ForwardingMethodRegister.getInstance().getForwardingMethodClass(identifier);
        Method classMethod = clazz.getDeclaredMethod("__invoke_throwsException", new Class[]{Self.class});
        assertEquals(1, classMethod.getExceptionTypes().length);
        assertEquals(Exception.class, classMethod.getExceptionTypes()[0]);
    }
    
}
