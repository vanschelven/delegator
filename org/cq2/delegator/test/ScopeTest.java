/*
 * Created on Jun 4, 2004
 */
package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.ISelf;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.Self;
import org.cq2.delegator.classgenerator.ProxyGenerator;

public class ScopeTest extends TestCase implements InvocationHandler {

    public static class PrivateMethod {

        private boolean called = false;

        private void method() {
            privateMethodCalled = true;
            called = true;
        }

        public boolean isCalled() {
            return called;
        }
    }

    public void testPrivateMethodsCanBeAccessedRegularJava() {
        PrivateMethod m = new PrivateMethod();
        m.method();
        assertTrue(privateMethodCalled);
        assertTrue(m.called);
    }

    /**
     * Test showing a limitation of Delegator: The fact that proxies don't work
     * with private methods. Generates a proxy for PrivateMethod and calls
     * method(). This test shows that firstly the proxy has not acted as a
     * proxy. If it had invokedMethod would not have been null. Secondly
     * privateMethodCalled is true. This means the proxy has indeed executed the
     * code it semantically could be expected to override.
     */
    public void testPrivateMethodProxy() {
        PrivateMethod m = (PrivateMethod) ProxyGenerator.newProxyInstance(
                PrivateMethod.class, this);
        m.method();
        assertNull(invokedMethod);
        assertTrue(privateMethodCalled);
    }

    /**
     * Test showing a limitation of Delegator: The fact that Delegator doesn't
     * work with private methods (because proxies don't work with private
     * methods). If a private method is called on a proxy the proxy doesn't
     * forward this to self (which is indicated by m.isCalled() being false) but
     * instead executes it itself (which is indicated by m.called being true).
     */
    public void testPrivateMethod() {
        PrivateMethod m = (PrivateMethod) new Self(PrivateMethod.class)
                .cast(PrivateMethod.class);
        m.method();
        assertFalse(m.isCalled());
        assertTrue(privateMethodCalled);
        assertTrue(m.called);
    }

    /**
     * Shows that Self's method lookup cannot find a private method although it exists. 
     * This is fine because the method actually called on the proxy is public.
     */
    public void testPrivateMethodsArentCalled() {
        ISelf result = new Self(PrivateMethod.class);
        PublicMethod m = (PublicMethod) result.cast(PublicMethod.class);
        try {
            m.method();
            fail();
        } catch (NoSuchMethodError e) {}
    }

    public static class PrivateSelfCallingClass {

        private boolean m1Called;

        private boolean m2Called;

        public void m1() {
            m1Called = true;
            m2();
        }

        private void m2() {
            m2Called = true;
        }

        public boolean isM1Called() {
            return m1Called;
        }

        public boolean isM2Called() {
            return m2Called;
        }

    }

    /**
     * Shows that it possible to have calls to private methods within code that
     * is executed via delegation.
     */
    public void testPrivateSelfCall() {
        Self self = new Self(PrivateSelfCallingClass.class);
        PrivateSelfCallingClass s = (PrivateSelfCallingClass) self
                .cast(PrivateSelfCallingClass.class);
        s.m1();
        assertTrue(s.isM1Called());
        assertTrue(s.isM2Called());
    }

    public static class ForwardingClass {

        private boolean called = false;

        public void m1() {
            ForwardingClass forwardingClass = new ForwardingClass();
            forwardingClass.m2();
            assertTrue(forwardingClass.isCalled());
        }

        private void m2() {
            called = true;
        }

        public boolean isCalled() {
            return called;
        }

    }

    /**
     *  Shows that a private call to another instance of the same class can be executed in
     * the context of Delegator without any problems.
     * @see testPrivateSelfCall
     */
    public void testForwardingClass() {
        Self self = new Self(ForwardingClass.class);
        ForwardingClass m = (ForwardingClass) self.cast(ForwardingClass.class);
        m.m1();
        assertFalse(m.isCalled());
    }

    public static class ForwardsToProxy {

        private boolean called = false;

        public void m1() {
            ForwardsToProxy m = (ForwardsToProxy) new Self(
                    ForwardsToProxy.class).cast(ForwardsToProxy.class);
            m.m2();
            assertTrue(m.called);
            assertFalse(m.isCalled());
        }

        private void m2() {
            called = true;
        }

        public boolean isCalled() {
            return called;
        }

    }

    /** 
     * Similar to testPrivateMethod, with the difference that the private method is called
     * within the code of the class itself, but on an instance created using Delegator.
     * The limitation is the same as with testPrivateMethod.
     */
    public void testForwardsToProxy() {
        ForwardsToProxy m = new ForwardsToProxy();
        m.m1();
        assertFalse(m.isCalled());
    }
    
    
    //TODO ook voor deze andere methods is er de vraag of dit voldoende is...
    public void testPublicMethodExtendsProtectedMethod() {
        PublicMethod m = (PublicMethod) Delegator.extend(PublicMethod.class,
                ProtectedMethod.class);
        m.method();
        assertFalse(protectedMethodCalled);
        assertTrue(publicMethodCalled);
    }

    public void testProtectedMethodExtendsPublicMethod() {
        ProtectedMethod m = (ProtectedMethod) Delegator.extend(
                ProtectedMethod.class, PublicMethod.class);
        m.method();
        assertTrue(protectedMethodCalled);
        assertFalse(publicMethodCalled);
    }

    public void testProtectedExtendsPublicCastToPublic() {
        ISelf result = new Self(ProtectedMethod.class);
        result.add(PublicMethod.class);
        PublicMethod m = (PublicMethod) result.cast(PublicMethod.class);
        m.method();
        assertFalse(protectedMethodCalled);
        assertTrue(publicMethodCalled);
    }

    public void testPublicExtendsProtectedCastToProtected() {
        ISelf result = new Self(PublicMethod.class);
        result.add(ProtectedMethod.class);
        ProtectedMethod m = (ProtectedMethod) result
                .cast(ProtectedMethod.class);
        m.method();
        assertFalse(protectedMethodCalled);
        assertTrue(publicMethodCalled);
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

    //    public void testDelegateSubclassPackageScopeMethod() {
    //        //this doesn't work because testPackageMethod fails
    //        //the names subclass and superclass are not entirely correct
    //        Self self = new Self(MySuperClass.class);
    //        MySuperClass sup = (MySuperClass) self.cast(MySuperClass.class);
    //        assertEquals(3, sup.myPackageScopeMethod());
    //        self.add(MySubclass.class);
    //        MySubclass subclass = (MySubclass) self.cast(MySubclass.class);
    //        assertEquals(3, subclass.myPackageScopeMethod());
    //    }

    public void testDelegateSubclassProtectedMethod() {
        Self self = new Self(MySuperClass.class);
        self.add(MySubclass.class);
        MySubclass subclass = (MySubclass) self.cast(MySubclass.class);
        int result = subclass.getProtected();
        assertEquals(5, result);
    }

    public abstract static class AbstractPackageMethod {
        abstract void method();
    }

    //    public void testAbstractPackageMethod() throws Exception {
    //        //this doesn't work because testPackageMethod fails
    //        AbstractPackageMethod b = (AbstractPackageMethod)
    // ProxyGenerator.newProxyInstance(AbstractPackageMethod.class, this);
    //        Method declaredMethod = b.getClass().getDeclaredMethod("method", null);
    //        assertNotNull(declaredMethod);
    //        assertFalse(Modifier.isAbstract(declaredMethod.getModifiers()));
    //        //assertTrue(Modifier.isPublic(declaredMethod.getModifiers()));
    //        //declaredMethod.setAccessible(true);
    //        declaredMethod.invoke(b, null);
    //        Method abstractMethod =
    // AbstractPackageMethod.class.getDeclaredMethod("method", null);
    //        assertTrue(Modifier.isAbstract(abstractMethod.getModifiers()));
    //        assertFalse(Modifier.isPrivate(abstractMethod.getModifiers()));
    //        assertFalse(Modifier.isPublic(abstractMethod.getModifiers()));
    //        b.method();
    //    }

    public abstract static class AbstractPublicMethod {
        public abstract void method();
    }

    public void testAbstractPublicMethod() throws Exception {
        AbstractPublicMethod m = (AbstractPublicMethod) ProxyGenerator
                .newProxyInstance(AbstractPublicMethod.class, this);
        m.method();
        assertEquals("method", invokedMethod);
    }

    public abstract static class AbstractProtectedMethod {
        protected abstract void method();
    }

    public void testAbstractProtectedMethod() throws Exception {
        AbstractProtectedMethod m = (AbstractProtectedMethod) ProxyGenerator
                .newProxyInstance(AbstractProtectedMethod.class, this);
        m.method();
        assertEquals("method", invokedMethod);
    }

    public static class PackageMethod {

        void method() {
            packageMethodCalled = true;
        }

    }

    public static class ProtectedMethod {

        protected void method() {
            protectedMethodCalled = true;
        }

    }

    public static class PublicMethod {

        public void method() {
            publicMethodCalled = true;
        }

    }

    public void testPackageMethod() throws Exception {
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(
                PackageMethod.class, this);
        m.method();
        assertTrue(packageMethodCalled);
        assertNull(invokedMethod);
    }

    //    public void testPackageMethodUsingReflection() throws Exception {
    //        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(
    //        PackageMethod.class, this);
    //        Method proxyMethod = m.getClass().getDeclaredMethod("method", null);
    //        proxyMethod.invoke(m, null);
    //        assertFalse(packageMethodCalled);
    //        assertEquals("method", invokedMethod);
    //    }

    //de volgende test hoort elders thuis...
    public void testMethodSignaturesMatch() throws Exception {
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(
                PackageMethod.class, this);
        assertEquals(
                "class org.cq2.delegator.test.ScopeTest$PackageMethod$proxy", m
                        .getClass().toString());
        assertTrue(PackageMethod.class.isAssignableFrom(m.getClass()));
        assertEquals(PackageMethod.class.getPackage(), m.getClass()
                .getPackage());

        //        assertEquals(PackageMethod.class.getClassLoader(),
        // m.getClass().getClassLoader());

        Method proxyMethod = m.getClass().getDeclaredMethod("method", null);

        Method originalMethod = PackageMethod.class.getDeclaredMethod("method",
                null);
        assertEquals(originalMethod.getName(), proxyMethod.getName());
        assertArrayEquals(originalMethod.getParameterTypes(), proxyMethod
                .getParameterTypes());
        assertArrayEquals(originalMethod.getExceptionTypes(), proxyMethod
                .getExceptionTypes());
        assertEquals(originalMethod.getReturnType(), proxyMethod
                .getReturnType());
        assertEquals(originalMethod.isAccessible(), proxyMethod.isAccessible());
        //assertEquals(originalMethod.getModifiers(),
        // proxyMethod.getModifiers()); - this is untrue because some of the
        // modifiers are set to public
    }

    private void assertArrayEquals(Object[] array1, Object[] array2) {
        assertEquals(array1.length, array2.length);
        for (int i = 0; i < array1.length; i++) {
            assertEquals(array1[i], array2[i]);
        }
    }

    static class MyClassLoader extends ClassLoader {

        private Class inject(String className, byte[] classDef,
                ProtectionDomain domain) {
            return defineClass(className, classDef, 0, classDef.length, domain);
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            String className = "org.cq2.delegator.test.ScopeTest$PackageMethod$proxy";
            byte[] classDef = new ProxyGenerator(className,
                    PackageMethod.class, Proxy.class).generateProxy();
            return inject(null, classDef, PackageMethod.class
                    .getProtectionDomain());
        }

    }

    public void testClassLoader() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        Class clazz = new MyClassLoader()
                .loadClass("SomeOtherClassNameToMakeSureTheOriginalClassLoaderIsNotUsed");
        PackageMethod m = (PackageMethod) clazz.newInstance();
        m.method();
        assertTrue(packageMethodCalled);
    }

    //    public void testGenerateClassFile() {
    //        String className =
    // "org.cq2.delegator.test.ScopeTest$PackageMethod$proxy";
    //        byte[] classDef = new ProxyGenerator(className, PackageMethod.class,
    // Proxy.class).generateProxy();
    //        OutputStream o;
    //      try {
    //          o = new
    // FileOutputStream("/home/klaas/Documents/eclipse/delegator/classes/org/cq2/delegator/test/"
    // +
    //          		"ScopeTest$PackageMethod$proxy.class");
    //          o.write(classDef);
    //          o.close();
    //      } catch (Exception e) {
    //          e.printStackTrace();
    //      }
    //    }

    //is dit apart noodzakelijk? Is er verschil? zijn meer tests nodig?
    // voorlopig niet!
    public static class ProtectedSelfCallingClass {

        private boolean m1Called;

        private boolean m2Called;

        protected void m1() {
            m1Called = true;
            m2();
        }

        protected void m2() {
            m2Called = true;
        }

        public boolean isM1Called() {
            return m1Called;
        }

        public boolean isM2Called() {
            return m2Called;
        }

    }

    public void testScopingWithinComponent() {
        Self self = new Self(ProtectedSelfCallingClass.class);
        ProtectedSelfCallingClass s = (ProtectedSelfCallingClass) self
                .cast(ProtectedSelfCallingClass.class);
        s.m1();
        assertTrue(s.isM1Called());
        assertTrue(s.isM2Called());
    }

    public void setUp() {
        invokedMethod = null;
        invokeResult = null;
        packageMethodCalled = false;
        privateMethodCalled = false;
        protectedMethodCalled = false;
        publicMethodCalled = false;
    }

    private String invokedMethod;

    private Object invokeResult;

    private static boolean packageMethodCalled;

    private static boolean privateMethodCalled;

    private static boolean protectedMethodCalled;

    private static boolean publicMethodCalled;

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        invokedMethod = method.getName();
        return invokeResult;
    }

}