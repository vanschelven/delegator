package org.cq2.delegator.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.bcel.generic.Type;
import org.cq2.delegator.ProxyMethodRegister;
import org.cq2.delegator.Self;

public class ProxyMethodRegisterTest extends TestCase {

    Method method;
    
    protected void setUp() throws Exception {
        super.setUp();
        method = Vector.class.getDeclaredMethod("add", new Class[]{Object.class});
    }
    
    public void testAddMethod() {
        int identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(method);
        assertEquals(method, ProxyMethodRegister.getInstance().getMethod(identifier));
    }
    
    public void testGetProxyMethodClass() throws SecurityException, NoSuchMethodException {
        int identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(method);
        Class clazz = ProxyMethodRegister.getInstance().getProxyMethodClass(identifier);
        assertTrue(Modifier.isAbstract(clazz.getModifiers()));
        assertEquals("org.cq2.delegator.ProxyMethod" + identifier, clazz.getName());
        assertEquals("Lorg/cq2/delegator/ProxyMethod" + identifier + ";", Type.getType(clazz).getSignature());
        Method classMethod = clazz.getDeclaredMethod("__invoke_add", new Class[]{Self.class, Object.class});
        assertTrue(Modifier.isAbstract(classMethod.getModifiers()));
    }
    
    class A {
        
        void throwsException() throws Exception {
            
        }
        
    }
    
    public void testExceptions() throws SecurityException, NoSuchMethodException {
        Method exceptionMethod = A.class.getDeclaredMethod("throwsException", new Class[]{});
        int identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(exceptionMethod);
        Class clazz = ProxyMethodRegister.getInstance().getProxyMethodClass(identifier);
        Method classMethod = clazz.getDeclaredMethod("__invoke_throwsException", new Class[]{Self.class});
        assertEquals(1, classMethod.getExceptionTypes().length);
        assertEquals(Exception.class, classMethod.getExceptionTypes()[0]);
    }
    
}
