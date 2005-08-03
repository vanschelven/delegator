package org.cq2.delegator.test;

import junit.framework.TestCase;

import org.cq2.delegator.Self;

public class InnerClassTest extends TestCase {

  public static class ClassWithInnerClass {
    public class InnerClass {
    }

    public Object createInnerClass() {
      try {
        return new InnerClass();
      } catch (NoClassDefFoundError e) {
        e.printStackTrace();
        e.getCause().printStackTrace();
        throw e;
      }
    }
  }

  public void testInnerClassLoader() {
    Self self = new Self(ClassWithInnerClass.class);
    ClassWithInnerClass c = (ClassWithInnerClass) self
        .cast(ClassWithInnerClass.class);
    Object innerObject = c.createInnerClass();
    assertNotNull(innerObject);
    assertEquals(
        "org.cq2.delegator.test.InnerClassTest$ClassWithInnerClass$InnerClass",
        innerObject.getClass().getName());
  }
}