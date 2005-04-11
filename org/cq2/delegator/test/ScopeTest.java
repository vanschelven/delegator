/*
 * Created on Jun 4, 2004
 */
package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    public void setUp() {
        invokedMethod = null;
        invokeResult = null;
        packageMethodCalled = false;
        privateMethodCalled = false;
        publicMethodCalled = false;
    }

    private String invokedMethod;
    private Object invokeResult;
    private static boolean packageMethodCalled;
	private static boolean privateMethodCalled;
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

    public abstract static class B {
        abstract void method();
    }

//    public void testAbstractPackageMethod() throws Exception {
//        //this doesn't work because testPackageMethod fails
//        B b = (B) ProxyGenerator.newProxyInstance(B.class, this);
//        Method declaredMethod = b.getClass().getDeclaredMethod("method", null);
//        assertNotNull(declaredMethod);
//        assertFalse(Modifier.isAbstract(declaredMethod.getModifiers()));
//        //assertTrue(Modifier.isPublic(declaredMethod.getModifiers()));
//        //declaredMethod.setAccessible(true);
//        declaredMethod.invoke(b, null);
//        Method abstractMethod = B.class.getDeclaredMethod("method", null);
//        assertTrue(Modifier.isAbstract(abstractMethod.getModifiers()));
//        assertFalse(Modifier.isPrivate(abstractMethod.getModifiers()));
//        assertFalse(Modifier.isPublic(abstractMethod.getModifiers()));
//        b.method();
//    }
    
    public abstract static class AbstractPublicMethod {
        public abstract void method();
    }
    
    public void testAbstractPublicMethod() throws Exception {
        AbstractPublicMethod m = (AbstractPublicMethod) ProxyGenerator.newProxyInstance(AbstractPublicMethod.class, this);
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

    public static class ExtendedPackageMethod extends PackageMethod {

        void method() {
            System.out.println("pop");
        }

    }

    public void testPackageMethod() throws Exception {
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(
        PackageMethod.class, this);
        m.method();
        assertTrue(packageMethodCalled);
        assertNull(invokedMethod);
    }

    public void testPackageMethodUsingReflection() throws Exception {
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(
        PackageMethod.class, this);
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
        PackageMethod m = (PackageMethod) ProxyGenerator.newProxyInstance(
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
            byte[] classDef = new ProxyGenerator(className, PackageMethod.class, Proxy.class).generateProxy();
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
    

}