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
import org.cq2.delegator.classgenerator.ClassGenerator;
import org.cq2.delegator.classgenerator.ProxyGenerator;

public class ScopeTest extends TestCase implements InvocationHandler {
    public void setUp() {
        invokedMethod = null;
        invokeResult = null;
        packageMethodCalled = false;
        privateMethodCalled = false;
        protectedMethodCalled = false;
        publicMethodCalled = false;
        m1Called = false;
        m2Called = false;
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

	public class InnerClass {
		private void privateMethod() {
		}
	}
	
	public void testPrivateMethodsCanBeAccessedRegularJava() {
		new InnerClass().privateMethod();
	}
	
	public static class PublicMethod {
	    
	    public void method() {
	        publicMethodCalled = true;
	    }
	    
	}
	
	public static class PrivateMethod {
	    
	    private void method() {
			privateMethodCalled = true;
	    }
	    
	}
	
	public void testPublicMethodExtendsPrivateMethod() {
	    PublicMethod m = (PublicMethod) Delegator.extend(PublicMethod.class, PrivateMethod.class);
	    m.method();
	    assertFalse(privateMethodCalled);
	    assertTrue(publicMethodCalled);
	}

	public void testPrivateMethodExtendsPublicMethod() {
	    PrivateMethod m = (PrivateMethod) Delegator.extend(PrivateMethod.class, PublicMethod.class);
	    m.method();
	    assertTrue(privateMethodCalled);
	    assertFalse(publicMethodCalled);
	}

	public void testPrivateMethodExtendsPublicMethodCastToPublicMethod() {
		ISelf result = new Self(PrivateMethod.class);
		result.add(PublicMethod.class);
		PublicMethod m = (PublicMethod) result.cast(PublicMethod.class);
		m.method();
		assertFalse(privateMethodCalled);
		assertTrue(publicMethodCalled);
	}
	
	public static class PrivateSelfCallingClass {
	    
	    private void m1() {
	        m1Called = true;
	        m2();
	    }

        private void m2() {
            m2Called = true;
        }
	    
	}
	
	public void testPrivateSelfCall() {
        Self self = new Self(PrivateSelfCallingClass.class);
        PrivateSelfCallingClass s = (PrivateSelfCallingClass) self.cast(PrivateSelfCallingClass.class);
        s.m1();
        assertTrue(m1Called);
        assertTrue(m2Called);
    }
	
	public static class ComplicatedOnPurpose {
	    
	    private boolean called = false;

        private void m1() {
	        //deze twee regels kunnen wel samen in de Delegator (of is dat te loos en moet het in de tests?) worden bijgeplaatst
	        Self self = new Self(ComplicatedOnPurpose.class);
	        ComplicatedOnPurpose complicated = (ComplicatedOnPurpose) self.cast(ComplicatedOnPurpose.class);
	        complicated.m2();
	        assertTrue(complicated.called);
	        assertTrue(complicated.isCalled());
	    }

        private void m2() {
            called = true;
        }
        
        public boolean isCalled() {
            return called;
        }
	    
	}
	
// This is to show what doesn't work!
//	public void testIReallyWantToMakeThingsComplicated() {
//        ComplicatedOnPurpose complicated = new ComplicatedOnPurpose();
//	    complicated.m1();
//        assertFalse(complicated.isCalled());
//	}
	
	public static class ForwardingClass {
	    
	    private boolean called = false;

        private void m1() {
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
	
	public void testForwardingClass() {
        Self self = new Self(ForwardingClass.class);
        ForwardingClass clazz = (ForwardingClass) self.cast(ForwardingClass.class);
        clazz.m1();
        assertFalse(clazz.isCalled());
	}
	
	
	public void testPublicMethodExtendsProtectedMethod() {
	    PublicMethod m = (PublicMethod) Delegator.extend(PublicMethod.class, ProtectedMethod.class);
	    m.method();
	    assertFalse(protectedMethodCalled);
	    assertTrue(publicMethodCalled);
	}

	public void testProtectedMethodExtendsPublicMethod() {
	    ProtectedMethod m = (ProtectedMethod) Delegator.extend(ProtectedMethod.class, PublicMethod.class);
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
		ProtectedMethod m = (ProtectedMethod) result.cast(ProtectedMethod.class);
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
//        AbstractPackageMethod b = (AbstractPackageMethod) ProxyGenerator.newProxyInstance(AbstractPackageMethod.class, this);
//        Method declaredMethod = b.getClass().getDeclaredMethod("method", null);
//        assertNotNull(declaredMethod);
//        assertFalse(Modifier.isAbstract(declaredMethod.getModifiers()));
//        //assertTrue(Modifier.isPublic(declaredMethod.getModifiers()));
//        //declaredMethod.setAccessible(true);
//        declaredMethod.invoke(b, null);
//        Method abstractMethod = AbstractPackageMethod.class.getDeclaredMethod("method", null);
//        assertTrue(Modifier.isAbstract(abstractMethod.getModifiers()));
//        assertFalse(Modifier.isPrivate(abstractMethod.getModifiers()));
//        assertFalse(Modifier.isPublic(abstractMethod.getModifiers()));
//        b.method();
//    }
    
    public abstract static class AbstractPublicMethod {
        public abstract void method();
    }
    
    public void testAbstractPublicMethod() throws Exception {
        AbstractPublicMethod m = (AbstractPublicMethod) ClassGenerator.newProxyInstance(AbstractPublicMethod.class, this);
        m.method();
        assertEquals("method", invokedMethod);
    }

    public abstract static class AbstractProtectedMethod {
        protected abstract void method();
    }

    public void testAbstractProtectedMethod() throws Exception {
        AbstractProtectedMethod m = (AbstractProtectedMethod) ClassGenerator
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

    public void testPackageMethod() throws Exception {
        PackageMethod m = (PackageMethod) ClassGenerator.newProxyInstance(
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
        PackageMethod m = (PackageMethod) ClassGenerator.newProxyInstance(
                PackageMethod.class, this);
        assertEquals(
                "class org.cq2.delegator.test.ScopeTest$PackageMethod$proxy", m
                        .getClass().toString());
        assertTrue(PackageMethod.class.isAssignableFrom(m.getClass()));
        assertEquals(PackageMethod.class.getPackage(), m.getClass().getPackage());
        
//        assertEquals(PackageMethod.class.getClassLoader(), m.getClass().getClassLoader());
        
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

        private Class inject(String className, byte[] classDef, ProtectionDomain domain) {
            return defineClass(className, classDef, 0, classDef.length, domain);
        }

        protected Class findClass(String name) throws ClassNotFoundException {
            String className = "org.cq2.delegator.test.ScopeTest$PackageMethod$proxy";
            byte[] classDef = new ProxyGenerator(className, PackageMethod.class).generate();
            return inject(null, classDef, PackageMethod.class.getProtectionDomain());
        }
                
    }

    public void testClassLoader() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        Class clazz = new MyClassLoader().loadClass("SomeOtherClassNameToMakeSureTheOriginalClassLoaderIsNotUsed");
        PackageMethod m = (PackageMethod) clazz.newInstance();
        m.method();
        assertTrue(packageMethodCalled);
    }
    
//    public void testGenerateClassFile() {
//        String className = "org.cq2.delegator.test.ScopeTest$PackageMethod$proxy";
//        byte[] classDef = new ProxyGenerator(className, PackageMethod.class, Proxy.class).generateProxy();
//        OutputStream o;
//      try {
//          o = new FileOutputStream("/home/klaas/Documents/eclipse/delegator/classes/org/cq2/delegator/test/" +
//          		"ScopeTest$PackageMethod$proxy.class");
//          o.write(classDef);
//          o.close();
//      } catch (Exception e) {
//          e.printStackTrace();
//      }  
//    }

    private static boolean m1Called;
    private static boolean m2Called;
    
    //is dit apart noodzakelijk? Is er verschil? zijn meer tests nodig? voorlopig niet!
    public static class ProtectedSelfCallingClass {
        
        protected void m1() {
            m1Called = true;
            m2();
        }
        
        protected void m2() {
            m2Called = true;
        }
        
    }
    
    public void testScopingWithinComponent() {
        Self self = new Self(ProtectedSelfCallingClass.class);
        ProtectedSelfCallingClass s = (ProtectedSelfCallingClass) self.cast(ProtectedSelfCallingClass.class);
        s.m1();
        assertTrue(m1Called);
        assertTrue(m2Called);
    }
    
   

}