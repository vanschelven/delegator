/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.classgenerator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;

public class ProxyGeneratorDelegateTest extends TestCase implements InvocationHandler {
	private ClassInjector classInjector;
	private boolean invokeCalled;
	private Object invokeProxy;
	private Object invokeResult;
	private ProxyGeneratorDelegateTestClass proxy;
	private Throwable invokeException;
	private Method invokeMethod;
	private Object[] invokeArgs;

	public ProxyGeneratorDelegateTest(String arg0) {
		super(arg0);
	}

	public void setUp() throws Exception {
		invokeCalled = false;
		invokeProxy = null;
		invokeResult = null;
		invokeException = null;
		invokeMethod = null;
		invokeArgs = null;
		classInjector = ClassInjector.create();
		proxy = (ProxyGeneratorDelegateTestClass) ProxyGenerator.newProxyInstance(classInjector,
				ProxyGeneratorDelegateTestClass.class, Delegator.defaultMethodFilter(), this);
	}

	public void testGenClass() throws Exception {
		Object obj = ProxyGenerator.newProxyInstance(classInjector,
				ProxyGeneratorDelegateTestClass.class, Delegator.defaultMethodFilter(), this);
		assertNotNull(obj);
		Method objectString = obj.getClass().getMethod("objectString", new Class[]{String.class});
		assertNotNull(objectString);
	}

	public void testProxy() throws Exception {
		Object testProxy = ProxyGenerator.newProxyInstance(classInjector,
				ProxyGeneratorDelegateTestClass.class, Delegator.defaultMethodFilter(), this);
		assertTrue(testProxy instanceof ProxyGeneratorDelegateTestClass);
	}

	public void testGenerateParametersNames() {
		String[] args = ProxyGenerator.generateParameterNames(1);
		assertTrue(Arrays.equals(new String[]{"arg0"}, args));
		args = ProxyGenerator.generateParameterNames(0);
		assertTrue(Arrays.equals(new String[]{}, args));
		args = ProxyGenerator.generateParameterNames(2);
		assertTrue(Arrays.equals(new String[]{"arg0", "arg1"}, args));
	}

	public void testDelegate() throws Exception {
		ProxyGeneratorDelegateTestClass testProxy = (ProxyGeneratorDelegateTestClass) ProxyGenerator
				.newProxyInstance(classInjector, ProxyGeneratorDelegateTestClass.class,
						Delegator.defaultMethodFilter(), this);
		Field delegate = testProxy.getClass().getDeclaredField("self");
		assertTrue(Modifier.isTransient(delegate.getModifiers()));
		assertNotNull(delegate);
		delegate.setAccessible(true);
		Object delegateObj = delegate.get(testProxy);
		assertNotNull(delegateObj);
		assertTrue(delegateObj instanceof InvocationHandler);
		assertSame(delegateObj, this);
		testProxy.objectVoid();
		assertTrue(invokeCalled);
		assertSame(invokeProxy, testProxy);
	}

	public void testDelegateResult() throws Exception {
		invokeResult = "AAP";
		assertEquals("AAP", proxy.objectVoid());
		invokeResult = "Noot";
		assertEquals("Noot", proxy.objectVoid());
	}

	public void testObjectVoid() throws Exception {
		invokeResult = new Object();
		assertSame(invokeResult, proxy.objectVoid());
		invokeResult = new HashMap();
		assertEquals(invokeResult, proxy.objectVoid());
		invokeResult = new java.util.Date();
		assertEquals(invokeResult, proxy.objectVoid());
		invokeResult = new StringBuffer();
		assertEquals(invokeResult, proxy.objectVoid());
		invokeResult = System.out;
		assertEquals(invokeResult, proxy.objectVoid());
		invokeResult = null;
		assertEquals(invokeResult, proxy.objectVoid());
	}

	public void testReturnString() throws Exception {
		invokeResult = "Boom";
		assertEquals("Boom", proxy.stringVoid());
		invokeResult = "Vuur";
		assertEquals("Vuur", proxy.stringVoid());
	}

	public void testReturnInteger() throws Exception {
		invokeResult = new Integer(234);
		assertEquals(234, proxy.hashCode());
		invokeResult = new Integer(987);
		assertEquals(987, proxy.hashCode());
	}

	public void testReturnBoolean() {
		invokeResult = new Boolean(false);
		assertEquals(false, proxy.equals(""));
		invokeResult = new Boolean(true);
		assertEquals(true, proxy.equals(""));
	}

	public void testReturnLong() {
		invokeResult = new Long(1987327865197238L);
		assertEquals(1987327865197238L, proxy.longVoid());
		invokeResult = new Long(26);
		assertEquals(26, proxy.longVoid());
	}

	public void testReturnDouble() {
		invokeResult = new Double(123.456);
		assertEquals(123.456, proxy.doubleVoid(), 0.001);
		invokeResult = new Double(123.456);
		assertEquals(123.456, proxy.doubleVoid(), 0.001);
	}

	public void testReturnFloat() {
		invokeResult = new Float(12.3);
		assertEquals(12.3, proxy.floatVoid(), 0.01);
		invokeResult = new Float(9.02);
		assertEquals(9.02, proxy.floatVoid(), 0.01);
	}

	public void testReturnByte() {
		invokeResult = new Byte((byte) 12);
		assertEquals(12, proxy.byteVoid());
		invokeResult = new Byte((byte) 64);
		assertEquals(64, proxy.byteVoid());
	}

	public void testReturnShort() {
		invokeResult = new Short((short) 255);
		assertEquals(255, proxy.shortVoid());
		invokeResult = new Short((short) 32);
		assertEquals(32, proxy.shortVoid());
	}

	public void testReturnChar() {
		invokeResult = new Character('A');
		assertEquals('A', proxy.charVoid());
		invokeResult = new Character('&');
		assertEquals('&', proxy.charVoid());
	}

	public void testReturnIntArray() {
		invokeResult = new int[]{10, 12, 20};
		assertTrue(Arrays.equals((int[]) invokeResult, proxy.intArrayVoid()));
		invokeResult = new double[][]{ {1.1, 2.2}, {2.4, 4.5}};
		assertTrue(Arrays.equals((double[][]) invokeResult, proxy.doubleMatrixVoid()));
	}

	public void testGooiException() {
		invokeException = new IOException("");
		try {
			proxy.throwException();
			fail();
		}
		catch (IOException e) {
			assertEquals(invokeException, e);
		}
		catch (Exception e) {}
		invokeException = new RuntimeException();
		try {
			proxy.throwException();
			fail();
		}
		catch (IOException e) {}
		catch (Exception e) {
			assertEquals(invokeException, e);
		}
	}

	public void testPassMethod() {
		proxy.voidVoid();
		assertEquals("voidVoid", invokeMethod.getName());
		proxy.voidString("Aap");
		assertEquals("voidString", invokeMethod.getName());
		proxy.voidObject(new Object());
		assertEquals("voidObject", invokeMethod.getName());
		proxy.voidInteger(1);
		assertEquals("voidInteger", invokeMethod.getName());
		proxy.voidBoolean(true);
		assertEquals("voidBoolean", invokeMethod.getName());
		proxy.voidLong(798);
		assertEquals("voidLong", invokeMethod.getName());
		proxy.voidDouble(1.234);
		assertEquals("voidDouble", invokeMethod.getName());
		proxy.voidFloat((float) 3.21);
		assertEquals("voidFloat", invokeMethod.getName());
		proxy.voidByte((byte) 3);
		assertEquals("voidByte", invokeMethod.getName());
		proxy.voidShort((short) 23);
		assertEquals("voidShort", invokeMethod.getName());
		proxy.voidChar('D');
		assertEquals("voidChar", invokeMethod.getName());
	}

	public void testSomeArguments() {
		proxy.voidAllArgTypes("Noot", new Object(), 1, true, 2, 1.2, (float) 9.0, (byte) 16,
				(short) 23, 'K');
		assertEquals("voidAllArgTypes", invokeMethod.getName());
		assertEquals(Void.TYPE, invokeMethod.getReturnType());
		Class[] parameterTypes = invokeMethod.getParameterTypes();
		assertEquals(parameterTypes[0], String.class);
		assertEquals(parameterTypes[1], Object.class);
		assertEquals(parameterTypes[2], Integer.TYPE);
		assertEquals(parameterTypes[3], Boolean.TYPE);
		assertEquals(parameterTypes[4], Long.TYPE);
		assertEquals(parameterTypes[5], Double.TYPE);
		assertEquals(parameterTypes[6], Float.TYPE);
		assertEquals(parameterTypes[7], Byte.TYPE);
		assertEquals(parameterTypes[8], Short.TYPE);
		assertEquals(parameterTypes[9], Character.TYPE);
	}

	public void testPassString() {
		proxy.voidString("Aap");
		assertTrue(Arrays.equals(new Object[]{"Aap"}, invokeArgs));
	}

	public void testPassObject() {
		Object obj = new Object();
		proxy.voidObject(obj);
		assertTrue(Arrays.equals(new Object[]{obj}, invokeArgs));
	}

	public void testPassInteger() {
		proxy.voidInteger(10);
		assertTrue(Arrays.equals(new Object[]{new Integer(10)}, invokeArgs));
	}

	public void testPassBoolean() {
		proxy.voidBoolean(true);
		assertTrue(Arrays.equals(new Object[]{new Boolean(true)}, invokeArgs));
	}

	public void testPassLong() {
		proxy.voidLong(123L);
		assertTrue(Arrays.equals(new Object[]{new Long(123)}, invokeArgs));
	}

	public void testPassDouble() {
		proxy.voidDouble(1.99);
		assertTrue(Arrays.equals(new Object[]{new Double(1.99)}, invokeArgs));
	}

	public void testPassFloat() {
		proxy.voidFloat((float) 2.1);
		assertTrue(Arrays.equals(new Object[]{new Float(2.1)}, invokeArgs));
	}

	public void testPassByte() {
		proxy.voidByte((byte) 3);
		assertTrue(Arrays.equals(new Object[]{new Byte((byte) 3)}, invokeArgs));
	}

	public void testPassShort() {
		proxy.voidShort((short) 7);
		assertTrue(Arrays.equals(new Object[]{new Short((short) 7)}, invokeArgs));
	}

	public void testPassChar() {
		proxy.voidChar('F');
		assertTrue(Arrays.equals(new Object[]{new Character('F')}, invokeArgs));
	}

	public void testPassArray() {
		long[] array = new long[]{1, 2, 3};
		proxy.voidArray(array);
		assertTrue(Arrays.equals(new Object[]{array}, invokeArgs));
	}

	public void testAbstractMethod() {
		proxy.abstractMethod();
		assertTrue(invokeCalled);
	}

	//	public void testAbstractMethodOnly() throws Exception {
	//		proxy =
	//			(ProxyGeneratorTestClass) ProxyGenerator.newProxyInstance(
	//		ClassLoader.getSystemClassLoader(),
	//				ProxyGeneratorTestClass.class,
	//				this, true);
	//		proxy.abstractMethod();
	//		assertTrue(invokeCalled);
	//		invokeCalled = false;
	//		proxy.byteVoid();
	//		assertTrue(invokeCalled == false);
	//	}
	public void testProhibitedPackageName() {
		Object testProxy = ProxyGenerator.newProxyInstance(classInjector, HashMap.class, 
				Delegator.defaultMethodFilter(), this);
		assertNotNull(testProxy);
	}

	public void testSetdelegate() {
		Object testProxy = ProxyGenerator.newProxyInstance(classInjector, HashMap.class,
				Delegator.defaultMethodFilter(), this);
		assertSame(this, ProxyGenerator.getInvocationHandler(testProxy));
	}

	public static class MyClass {
		//protected void protectedMethod() {}
	}

	public void testProtectedMethod() {
		Object testProxy = ProxyGenerator.newProxyInstance(classInjector,
				MyClass.class, Delegator.defaultMethodFilter(), this);
		assertNotNull(testProxy);
	}

	interface MyInterface {}

	public void testIf() {
		Object testProxy = Proxy.newProxyInstance(MyClass.class.getClassLoader(),
				new Class[]{MyInterface.class}, this);
		assertEquals(getClass().getPackage(), testProxy.getClass().getPackage());
	}

	public static class WithCtorWithArgs {
		long l;
		String s;

		public WithCtorWithArgs(Long l, String s) {
			this.l = l.longValue();
			this.s = s.toString();
		}
	}

	public Object invoke(Object theProxy, Method method, Object[] args) throws Throwable {
		invokeProxy = theProxy;
		invokeCalled = true;
		invokeMethod = method;
		invokeArgs = args;
		if (invokeException != null) {
			throw invokeException;
		}
		return invokeResult;
	}
}
