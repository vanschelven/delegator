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
        packageMethodCalled = false;
    }

    private String invokedMethod;

    private Object invokeResult;

    private static boolean packageMethodCalled;

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
        //this doesn't work because testPackageMethod fails
        //the names subclass and superclass are not entirely correct
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

    public void testAbstractPackageMethod() throws Exception {
        //this doesn't work because testPackageMethod fails
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

    public abstract static class ProtectedAbstractMethod {
        protected abstract void method();
    }

    public void testProtectedAbstractMethod() throws Exception {
        ProtectedAbstractMethod m = (ProtectedAbstractMethod) ProxyGenerator.newProxyInstance(ProtectedAbstractMethod.class, this);
        Method declaredMethod = m.getClass().getDeclaredMethod("method", null);
        assertNotNull(declaredMethod);
        assertFalse(Modifier.isAbstract(declaredMethod.getModifiers()));
        //assertTrue(Modifier.isPublic(declaredMethod.getModifiers()));
        //declaredMethod.setAccessible(true);
        declaredMethod.invoke(m, null);
        m.method();
    }

    public static class PackageMethod {
 
        void method() {
            packageMethodCalled = true;
        }
        
    }
    
    public static class ExtendedPackageMethod extends PackageMethod {
        
        void method() {
            System.out.println("pop");
        }
        
    }

    public void testPackageMethod() throws Exception {
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(PackageMethod.class, this);
        m.method();
        assertFalse(packageMethodCalled);
        assertEquals("method", invokedMethod);
    }

    public void testPackageMethodUsingReflection() throws Exception {
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(PackageMethod.class, this);
        Method proxyMethod = m.getClass().getDeclaredMethod("method", null);
        proxyMethod.invoke(m, null);
        assertFalse(packageMethodCalled);
        assertEquals("method", invokedMethod);
    }
    
    public void testPackageMethodRegularJava() {
        PackageMethod m = new ExtendedPackageMethod();
        m.method();
        assertFalse(packageMethodCalled);
        //true: extendedPackageMethodCalled
    }
    
    //de volgende test hoort elders thuis...
    public void testMethodSignaturesMatch() throws Exception {
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(PackageMethod.class, this);
        assertEquals("class org.cq2.delegator.test.ScopeTest$PackageMethod$proxy", m
                .getClass().toString());
        assertEquals(this.getClass().getPackage(), m.getClass().getPackage());
        Method proxyMethod = m.getClass().getDeclaredMethod("method", null);
        
        Method originalMethod = PackageMethod.class.getDeclaredMethod("method", null);
        assertEquals(originalMethod.getName(), proxyMethod.getName());
        assertArrayEquals(originalMethod.getParameterTypes(), proxyMethod
                .getParameterTypes());
        assertArrayEquals(originalMethod.getExceptionTypes(), proxyMethod
                .getExceptionTypes());
        assertEquals(originalMethod.getReturnType(), proxyMethod
                .getReturnType());
        assertEquals(originalMethod.isAccessible(), proxyMethod.isAccessible());
        //assertEquals(originalMethod.getModifiers(), proxyMethod.getModifiers()); - this is untrue because some of the modifiers are set to public
    }    

    private void assertArrayEquals(Object[] array1, Object[] array2) {
        assertEquals(array1.length, array2.length);
        for (int i = 0; i < array1.length; i++) {
            assertEquals(array1[i], array2[i]);
        }
    }

    public void testPackages() {
        //See generated compiler messages in the subpackage for the idea behind this test...
        PackageMethod d = new BInSubPackage();
        d.method();
        BInSubPackage d2 = new BInSubPackage();
        d2.method();
    }
    
    

}