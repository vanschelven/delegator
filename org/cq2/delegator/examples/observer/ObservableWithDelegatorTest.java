/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 *
 * Created on Mar 17, 2004
 */
package org.cq2.delegator.examples.observer;

import org.cq2.delegator.Delegator;

import junit.framework.TestCase;

public class ObservableWithDelegatorTest extends TestCase implements Observer {
	private Object	notifier;

	class MockObserver implements Observer {
		public Object	myNotifier;
		public void notifyChanged(Object implementation) {
			myNotifier = implementation;
		}
	}

	public void testChanged() {
		MyObservableImplWithDelegation observable = createObservable();
		observable.addDependent(this);
		MockObserver observer2 = new MockObserver();
		observable.addDependent(observer2);
		observable.changed(this);
		assertSame(this, notifier);
		assertSame(this, observer2.myNotifier);
	}

	public void testSecondObservableClass() {
		MyObservableImplWithDelegation obs2 = createObservable();
		obs2.addDependent(this);
		notifier = null;
		obs2.changesomething();
		assertSame(obs2, notifier);
	}

	private MyObservableImplWithDelegation createObservable() {
		return (MyObservableImplWithDelegation) Delegator.createExtension(
				MyObservableImplWithDelegation.class, ObservableImpl.class);
	}

	public void testRemoveDependent() {
		MyObservableImplWithDelegation obs = createObservable();
		obs.addDependent(this);
		MockObserver observer2 = new MockObserver();
		obs.addDependent(observer2);
		Object self = new Object();
		obs.changed(self);
		assertSame(self, notifier);
		assertSame(self, observer2.myNotifier);
		notifier = null;

		obs.removeDependent(this);
		obs.changed(self);
		assertSame(self, observer2.myNotifier);
		assertNull(notifier);
	}

	public void notifyChanged(Object observable) {
		this.notifier = observable;
	}
}
