package org.cq2.delegator;

import org.cq2.delegator.handlers.ISelf;

public  abstract class MySubclass implements ISelf {
	
	abstract int myPackageScopeMethod();

	abstract protected int getProtected();

	public int myProtectedMethod() {
		return getProtected();
	} 
}
