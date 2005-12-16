package org.cq2.delegator.test;

import org.cq2.delegator.Delegator;

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
    
    public void testAbstractMethodsAreSkipped() {
        AbstractPublicMethod m = (AbstractPublicMethod) Delegator.extend(AbstractPublicMethod.class, PublicMethod.class);
        m.method();
        assertTrue(called);
    }

    
}
