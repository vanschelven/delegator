/*
 * Created on Mar 25, 2004
 */
package org.cq2.delegator.examples.observer;
public abstract class MyObservableImplWithDelegation implements Observable {
	abstract public void changed(Object self);
	abstract public void removeDependent(Observer test);


	public void changesomething() {
		changed(this);
	}
	

	
}
