/*
 * Created on Jun 4, 2004
 */
package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.Self;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.test.subpackage.BInSubPackage;

public class ScopeTest extends TestCase implements InvocationHandler {
    public void setUp() {
        invokedMethod = null;
        invokeResult = null;
    }

    private String invokedMethod;

    private Object invokeResult;

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        invokedMethod = method.getName();
        return invokeResult;
    }

    public void testDelegateSubclass() {
        Map map = (Map) Delegator.proxyFor(HashMap.class, this);
        assertNotNull(map);
        invokeResult = Boolean.TRUE;
        assertEquals(true, map.isEmpty());
        invokeResult = Boolean.FALSE;
        assertEquals(false, map.isEmpty());
        assertEquals("isEmpty", invokedMethod);
    }

    public void testDelegateSubclassPackageScopeMethod() {
        Self self = new Self(MySuperClass.class);
        MySuperClass sup = (MySuperClass) self.cast(MySuperClass.class);
        assertEquals(3, sup.myPackageScopeMethod());
        self.add(MySubclass.class);
        MySubclass subclass = (MySubclass) self.cast(MySubclass.class);
        assertEquals(3, subclass.myPackageScopeMethod());
    }

    public void testDelegateSubclassProtectedMethod() {
        Self self = new Self(MySuperClass.class);
        self.add(MySubclass.class);
        MySubclass subclass = (MySubclass) self.cast(MySubclass.class);
        int result = subclass.getProtected();
        assertEquals(5, result);
    }

    public abstract static class B {
        abstract void method();
    }

    public void testAbstractNonPublicMethods() throws Exception {
        B b = (B) ProxyGenerator.newProxyInstance(B.class, this);
        Method declaredMethod = b.getClass().getDeclaredMethod("method", null);
        assertNotNull(declaredMethod);
        assertFalse(Modifier.isAbstract(declaredMethod.getModifiers()));
        //assertTrue(Modifier.isPublic(declaredMethod.getModifiers()));
        //declaredMethod.setAccessible(true);
        declaredMethod.invoke(b, null);
        Method abstractMethod = B.class.getDeclaredMethod("method", null);
        assertTrue(Modifier.isAbstract(abstractMethod.getModifiers()));
        assertFalse(Modifier.isPrivate(abstractMethod.getModifiers()));
        assertFalse(Modifier.isPublic(abstractMethod.getModifiers()));
        b.method();
    }

    public abstract static class C {
        protected abstract void method();// {System.out.println("C.method");}
    }

    public void testAbstractNonPublicMethods2() throws Exception {
        C b = (C) ProxyGenerator.newProxyInstance(C.class, this);
        Method declaredMethod = b.getClass().getDeclaredMethod("method", null);
        assertNotNull(declaredMethod);
        assertFalse(Modifier.isAbstract(declaredMethod.getModifiers()));
        //assertTrue(Modifier.isPublic(declaredMethod.getModifiers()));
        declaredMethod.setAccessible(true);
        declaredMethod.invoke(b, null);
        b.method();
    }

    public static class B2 extends B {
        void method() {
        }
    }

    public void testAbstract() {
        new B2().method();
    }

    public void testAbstract2() {
        B b = new B2();
        b.method();
    }

    public void testMethodLookup() throws Exception {
        B b = (B) ProxyGenerator.newProxyInstance(B2.class, this);
        b.method();
        assertEquals("method", invokedMethod);
    }

    public static class D {
        protected void method() {
            System.out.println("d.method");
        }
        
    }

    public void testMethodLookup2() throws Exception {
        D d = (D) ProxyGenerator.newProxyInstance(D.class, this);
        d.method();
        assertEquals("method", invokedMethod);
    }

    public void testMethodLookup3() throws Exception {
        D d = (D) ProxyGenerator.newProxyInstance(D.class, this);
        assertEquals("class org.cq2.delegator.test.ScopeTest$D$proxy", d
                .getClass().toString());
        assertEquals(this.getClass().getPackage(), d.getClass().getPackage());
        Method proxyMethod = d.getClass().getDeclaredMethod("method", null);
        proxyMethod.invoke(d, null);
        assertEquals("method", invokedMethod);
        
        Method originalMethod = D.class.getDeclaredMethod("method", null);
        assertEquals(originalMethod.getName(), proxyMethod.getName());
        assertArrayEquals(originalMethod.getParameterTypes(), proxyMethod
                .getParameterTypes());
        assertArrayEquals(originalMethod.getExceptionTypes(), proxyMethod
                .getExceptionTypes());
        assertEquals(originalMethod.getReturnType(), proxyMethod
                .getReturnType());
        assertEquals(originalMethod.isAccessible(), proxyMethod.isAccessible());
        invokedMethod = null;
        d.method();
        assertEquals("method", invokedMethod);
        
        //assertEquals(originalMethod.getModifiers(), proxyMethod.getModifiers());
        //en dat dit werkt snap ik dus niet!!
    }

    private void assertArrayEquals(Object[] array1, Object[] array2) {
        assertEquals(array1.length, array2.length);
        for (int i = 0; i < array1.length; i++) {
            assertEquals(array1[i], array2[i]);
        }
    }

    //public void testMethodLookup4() throws Exception {
    //dit kan niet ProxyGenerator.newProxyInstance(D.class, this).method();
    //assertEquals("method", invokedMethod);
    //}

    public void testHowDoesThisWork() {
        int oldMods = Modifier.STATIC + Modifier.NATIVE;
        int newMods = oldMods
                & ~(Modifier.NATIVE | Modifier.ABSTRACT | Modifier.PRIVATE | Modifier.PROTECTED)
                | Modifier.PUBLIC;
        assertEquals(Modifier.STATIC | Modifier.PUBLIC, newMods);
    }
    
    public void testPackages() {
        D d = new BInSubPackage();
        d.method();
        BInSubPackage d2 = new BInSubPackage();
        d2.method();
    }
    
    

}