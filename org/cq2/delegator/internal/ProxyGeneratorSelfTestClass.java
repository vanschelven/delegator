/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.internal;

import java.io.IOException;

public abstract class ProxyGeneratorSelfTestClass {
	public boolean voidVoidCalled = false;
	public void voidVoid() {
		voidVoidCalled = true;
	}
	public Object objectVoid() {
		return "TestClass2";
	}
	public int hashCode() {
		return 87654;
	}
	public long longVoid() {
		return 98765678;
	}
	public float floatVoid() {
		return 123.987F;
	}
	public double doubleVoid() {
		return 765.890;
	}
	public byte byteVoid() {
		return 87;
	}
	public short shortVoid() {
		return 789;
	}
	public char charVoid() {
		return 'B';
	}
	public String stringVoid() {
		return "Are you there?";
	}
	public void throwException() throws IOException, RuntimeException {}
	public void voidString(String string) {}
	public void voidObject(Object object) {}
	public void voidInteger(int i) {}
	public void voidBoolean(boolean b) {}
	public void voidDouble(double d) {}
	public void voidLong(long i) {}
	public void voidFloat(float f) {}
	public void voidByte(byte b) {}
	public void voidShort(short s) {}
	public void voidChar(char c) {}
	public void voidAllArgTypes(
		String string,
		Object object,
		int i,
		boolean b,
		long l,
		double d,
		float f,
		byte b1,
		short s,
		char c) {
		}
	public int[] intArrayVoid() {
		return null;
	}
	public double[][] doubleMatrixVoid() {
		return null;
	}
	public void voidArray(long[] l) {}
	public abstract void abstractMethod();
}
