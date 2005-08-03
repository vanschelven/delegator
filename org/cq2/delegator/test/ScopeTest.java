/*
 * Created on Jun 4, 2004
 */
package org.cq2.delegator.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;
import org.cq2.delegator.classgenerator.ClassGenerator;
import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.classgenerator.SingleSelfComponentGenerator;

public class ScopeTest extends InvocationHandlerTest {

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
        PrivateMethod m = (PrivateMethod) ClassGenerator.newProxyInstance(
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
    
    private static class PrivateClass {
        
    }
    
    public void testPrivateClass() {
        try {
            new Self(PrivateClass.class);
            fail();
        } catch (IllegalAccessError e) { }
    }
    
    public static class PackageMethod {

        private boolean called;

        void method() {
            packageMethodCalled = true;
            called = true;
        }

        public boolean isCalled() {
            return called;
        }
        
    }

    public void testPackageMethodsCanBeAccessedRegularJava() {
        PackageMethod m = new PackageMethod();
        m.method();
        assertTrue(packageMethodCalled);
        assertTrue(m.called);
    }

    /**
     * Test showing a limitation of Delegator: The fact that proxies don't work
     * with package methods. Generates a proxy for PackageMethod and calls
     * method(). This test shows that firstly the proxy has not acted as a
     * proxy. If it had invokedMethod would not have been null. Secondly
     * packageMethodCalled is true. This means the proxy has indeed executed the
     * code it semantically could be expected to override.
     */
    public void testPackageMethodProxy() {
        PackageMethod m = (PackageMethod) ClassGenerator.newProxyInstance(
                PackageMethod.class, this);
        m.method();
        assertNull(invokedMethod);
        assertTrue(packageMethodCalled);
    }
   
    /**
     * Test showing a limitation of Delegator: The fact that Delegator doesn't
     * work with package methods (because proxies don't work with package
     * methods). If a package method is called on a proxy the proxy doesn't
     * forward this to self (which is indicated by m.isCalled() being false) but
     * instead executes it itself (which is indicated by m.called being true).
     */
    public void testPackageMethod() {
        PackageMethod m = (PackageMethod) new Self(PackageMethod.class)
                .cast(PackageMethod.class);
        m.method();
        assertFalse(m.isCalled());
        assertTrue(packageMethodCalled);
        assertTrue(m.called);
    }
    
    /**
     * Shows that Self's method lookup cannot find a package method although it exists. 
     * This is fine because the method actually called on the proxy is public.
     */
    public void testPackageMethodsArentCalled() {
        ISelf result = new Self(PackageMethod.class);
        PublicMethod m = (PublicMethod) result.cast(PublicMethod.class);
        try {
            m.method();
            fail();
        } catch (NoSuchMethodError e) {}
    }
   
    //Other package scope properties are also the same as private scope
    //in fact they are totally analogyous!!
    
    public static class CallsPackageMethod {
        
        private boolean called;
        
        public void call() {
            method();
        }

        void method() {
            called = true;
        }
        
        public boolean isCalled() {
            return called;
        }
        
    }
    
    /**
     * Test showing a limitation of Delegator: The fact that Delegator doesn't
     * have the self-problem solved for package methods. If a package method is called 
     * within a component the component doesn't forward this to self (which is indicated by 
     * packageMethodCalled being false) but instead executes it itself (which is indicated by 
     * m.isCalled() being true).
     */
    public void testPackageMethodsHaveSelfProblem() {
        Self self = new Self(PackageMethod.class);
        self.add(CallsPackageMethod.class);
        CallsPackageMethod m = (CallsPackageMethod) self.cast(CallsPackageMethod.class);
        m.call();
        assertFalse(packageMethodCalled);
        self.remove(PackageMethod.class);
        assertTrue(m.isCalled());
    }
    
    private static class PackageClass {
        
    }
    
    public void testPackageClass() {
        try {
            new Self(PackageClass.class);
            fail();
        } catch (IllegalAccessError e) { }
    }
    

    
    public static class ProtectedMethod {

        protected void method() {
            protectedMethodCalled = true;
        }

    }

    /**
     * Shows that proxies work fine with protected methods. Generates a proxy for 
     * ProtectedMethod and calls method(). This test shows that firstly the proxy 
     * has acted as a proxy, because invokedMethod is "method". Secondly
     * protectedMethodCalled is false. This means the proxy has not executed the
     * code it semantically is expected to override.
     */
    public void testProtectedMethodProxy() {
        ProtectedMethod m = (ProtectedMethod) ClassGenerator.newProxyInstance(
                ProtectedMethod.class, this);
        m.method();
        assertEquals("method", invokedMethod);
        assertFalse(protectedMethodCalled);
    }

    //some other analogyous tests have been left out.
    
    /**
     * Shows that Self's method lookup cannot find a protected method although it exists. 
     * This is fine because the method actually called on the proxy is public.
     */
    public void testProtectedMethodsArentCalled() {
        ISelf result = new Self(ProtectedMethod.class);
        PublicMethod m = (PublicMethod) result.cast(PublicMethod.class);
        try {
            m.method();
            fail();
        } catch (NoSuchMethodError e) {}
    }
   
    
    public static class PublicMethod {

        public void method() {
            publicMethodCalled = true;
        }

    }
    
    public void testPublicMethodProxy() {
        PublicMethod m = (PublicMethod) ClassGenerator.newProxyInstance(
                PublicMethod.class, this);
        m.method();
        assertEquals("method", invokedMethod);
        assertFalse(publicMethodCalled);
    }

    
    
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

    //exactly the same as packagemethod but we need something to circumvent the ClassLoader
    public static class PackageMethod2 {

        private boolean called;

        void method() {
            packageMethodCalled = true;
            called = true;
        }

        public boolean isCalled() {
            return called;
        }
        
    }
    
    public void testPackageMethodWithSavedClass() {
        generateProxyClassFile(PackageMethod2.class);
        generateComponentClassFile(PackageMethod2.class);
        PackageMethod2 m = (PackageMethod2) new Self(PackageMethod2.class)
          .cast(PackageMethod2.class);
        m.method();
        assertTrue(m.isCalled());
        assertTrue(packageMethodCalled);
        assertFalse(m.called);
    }
    
    private String packageToPath(String packageName) {
        return packageName.replaceAll("\\.", getSeparator())  + getSeparator();
    }
    
    private String getSeparator() {
        return System.getProperty("file.separator");
    }

    private void generateClassFile(Class clazz, String postfix, byte[] generate) {
        byte[] classDef = generate;
        String classLocation = guessClassLocation(clazz);
        if (classLocation == null) throw new RuntimeException("No location found for " + clazz);
        saveToFile(classDef, classLocation + classNameOnly(clazz) + "$" + postfix + ".class");
    }

    private void generateProxyClassFile(Class clazz) {
        String className = clazz.getName() + "$proxy";
        byte[] classDef = new ProxyGenerator(className, clazz).generate();
        generateClassFile(clazz, "proxy", classDef);
    }

    private void generateComponentClassFile(Class clazz) {
        String className = clazz.getName() + "$component";
        byte[] classDef = new SingleSelfComponentGenerator(className, clazz).generate();
        generateClassFile(clazz, "component", classDef);
    }

    private String classNameOnly(Class clazz) {
        return clazz.getName().substring(clazz.getPackage().getName().length() + 1);
    }

    private String guessClassLocation(Class clazz) {
        String classPath = System.getProperty("java.class.path");
        StringTokenizer tokenizer = new StringTokenizer(classPath, ":");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            String possiblePath = token + getSeparator() + packageToPath(clazz.getPackage().getName());
            File file = new File(token);
            if (file.exists() && file.isDirectory())
                return possiblePath;
        }
        return null;
    }

    private void saveToFile(byte[] bytes, String fileName) {
        OutputStream o;
        try {
            o = new FileOutputStream(fileName);
            o.write(bytes);
            o.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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

    protected void setUp() throws Exception {
        super.setUp();
        packageMethodCalled = false;
        privateMethodCalled = false;
        protectedMethodCalled = false;
        publicMethodCalled = false;
    }


    private static boolean packageMethodCalled;

    private static boolean privateMethodCalled;

    private static boolean protectedMethodCalled;

    private static boolean publicMethodCalled;

    interface PrivateInterface {
    }

    public void testPrivateInterface() {
        try {
            PrivateInterface o = (PrivateInterface) new Self()
                    .cast(PrivateInterface.class);
            fail();
        } catch (IllegalAccessError e) {
        }
    }

    interface PackageInterface {
    }

    public void testPackageInterface() {
        try {
            PackageInterface o = (PackageInterface) new Self()
                    .cast(PackageInterface.class);
            fail();
        } catch (IllegalAccessError e) {
        }
    }

    protected interface ProtectedInterface {
        public void method();
    }

    public void testProtectedInterface() {
        ProtectedInterface o = (ProtectedInterface) new Self()
                .cast(ProtectedInterface.class);
    }

    public interface PublicInterface {
        public void method();
    }

    public void testPublicInterface() {
        PublicInterface o = (PublicInterface) new Self()
                .cast(PublicInterface.class);
    }
    
    //TODO schrijf: write documentation on interfaces
}