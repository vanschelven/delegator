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
    
    protected void setUp() throws Exception {
        method = Vector.class.getDeclaredMethod("add", new Class[]{Object.class});
        identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(method);
        composedClass = ComposedClass.getEmptyClass().add(Vector.class);
    }
    
    public void testGetMethod() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ProxyMethod proxyMethod = composedClass.getMethod(identifier);
        assertFalse(Modifier.isAbstract(proxyMethod.getClass().getModifiers()));
        Method proxyMethodMethod = proxyMethod.getClass().getDeclaredMethod("invoke", new Class[]{Self.class, Object.class});
        assertFalse(Modifier.isAbstract(proxyMethodMethod.getModifiers()));
        proxyMethodMethod.invoke(proxyMethod, new Object[]{new Self(Vector.class), new Integer(5)});
        //TODO field set to 0, call reaches the vector of a self
        //TODO resolve pakt altijd 0, add
    }
    
    
}
