/*
 * Created on Mar 25, 2004
 */
package org.cq2.delegator.examples.observer;

import org.cq2.delegator.handlers.ISelf;

public abstract class MyObservableImplWithDelegation implements Observable, ISelf {
	abstract public void changed(Object self);
	abstract public void removeDependent(Observer test);
	public void changesomething() {
		changed(this);
	}
}
