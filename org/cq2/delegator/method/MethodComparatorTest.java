/*
 * Created on Apr 1, 2004
 */
package org.cq2.delegator.method;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import junit.framework.TestCase;

public class MethodComparatorTest extends TestCase {
	public void testGetName() throws SecurityException, NoSuchMethodException {
		assertEquals("testGetName", getClass().getMethod("testGetName", null).getName());
	}

	public void testCompareEquals() throws Exception {
		Method a = Collection.class.getMethod("toArray", new Class[]{});
		Method b = List.class.getMethod("toArray", new Class[]{});
		assertEquals(0, new MethodComparator().compare(a, b));
		assertEquals(0, new MethodComparator().compare(b, a));
	}

	public void testCompareNotEquals() throws Exception {
		Method a = Collection.class.getMethod("toArray", new Class[]{Object[].class});
		Method b = List.class.getMethod("toArray", new Class[]{});
		assertTrue(new MethodComparator().compare(b, a) < 0);
		assertTrue(new MethodComparator().compare(a, b) > 0);
	}
}
