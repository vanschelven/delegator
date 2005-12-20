/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.internal;

import java.io.IOException;

public abstract class ProxyGeneratorDelegateTestClass {

	public Object objectString(String name) {
		return "WRONG objectString() called!";
	}
	public Object objectVoid() {
		return "WRONG objectVoid() called!";
	}
	public void voidVoid() {}
	public int hashCode() {
		return 123;
	}
	public long longVoid() {
		return 0;
	}
	public float floatVoid() {
		return 0;
	}
	public double doubleVoid() {
		return 0;
	}
	public byte byteVoid() {
		return 0;
	}
	public short shortVoid() {
		return 0;
	}
	public char charVoid() {
		return 0;
	}
	public String stringVoid() {
		return null;
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