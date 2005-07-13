package org.cq2.delegator.test;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.Self;

import junit.framework.TestCase;

public class InnerClassTest extends TestCase {
  private ClassLoader loggingLoader = new ClassLoader() {
    public Class findClass(String className) throws ClassNotFoundException {
      //System.out.println(className);
      return super.getParent().loadClass(className);
    }
  };

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

//TODO uitgezet omdat ik het nog niet snap... waar is dit voor nodig?
//  public void testInnerClassLoader() {
//    Delegator.configureClassLoader(loggingLoader);
//    Self self = new Self(ClassWithInnerClass.class);
//    assertSame(loggingLoader, self.component(0).getClass().getClassLoader()
//        .getParent());
//    ClassWithInnerClass c = (ClassWithInnerClass) self
//        .cast(ClassWithInnerClass.class);
//    Object innerObject = c.createInnerClass();
//    assertNotNull(innerObject);
//    assertEquals(
//        "org.cq2.delegator.test.InnerClassTest$ClassWithInnerClass$InnerClass",
//        innerObject.getClass().getName());
//  }
}