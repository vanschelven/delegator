/*
 * Created on Jan 22, 2004
 */
package org.cq2.delegator.handlers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.cq2.delegator.handlers.Binder.Binding;

public class DynamicBinderTest extends TestCase {
	public String testMethod(Object arg) {
		return "Object";
	}
	public String testMethod(String arg) {
		return "Monkey";
	}
	public String testMethod(List arg) {
		return "List";
	}
	public void testCreate() throws Throwable {
		Method method = getClass().getMethod("testMethod", new Class[] { Object.class });
		Binder binder = new DynamicBinder();
		Binding binding = binder.bind(method, this);
		assertNotNull(binding);
		Object result1 = binding.invoke(new Object[] { new Object()});
		assertEquals("Object", result1);
		Object result2 = binding.invoke(new Object[] { "Mies" });
		assertEquals("Monkey", result2);
		Object result3 = binding.invoke(new Object[] { new ArrayList()});
		assertEquals("List", result3);
	}
}
