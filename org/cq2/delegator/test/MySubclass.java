package org.cq2.delegator.test;

import org.cq2.delegator.ISelf;


public  abstract class MySubclass implements ISelf {
	
	abstract int myPackageScopeMethod();

	abstract protected int getProtected();

	public int myProtectedMethod() {
		return getProtected();
	} 
}
