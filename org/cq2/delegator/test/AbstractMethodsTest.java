package org.cq2.delegator.test;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.classgenerator.ClassGenerator;

public class AbstractMethodsTest extends InvocationHandlerTest {

    private static boolean called = false;
    
    public abstract static class AbstractPublicMethod {
        public abstract void method();
    }
    
    public static class PublicMethod {
        public void method() {
            called = true;
        }
    }

    public void testAbstractPublicMethod() throws Exception {
        AbstractPublicMethod m = (AbstractPublicMethod) ClassGenerator
                .newProxyInstance(AbstractPublicMethod.class, this);
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
    
    public void testAbstractMethodsAreSkipped() {
        AbstractPublicMethod m = (AbstractPublicMethod) Delegator.extend(AbstractPublicMethod.class, PublicMethod.class);
        m.method();
        assertTrue(called);
    }

    
}
