/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 *
 * Created on Mar 17, 2004
 */
package org.cq2.delegator.examples.observer;

import junit.framework.TestCase;

public class ObservableTest extends TestCase implements Observer {
	private Object notifier;

	class MockObserver implements Observer {
		public Object myNotifier;

		public void notifyChanged(Object implementation) {
			myNotifier = implementation;
		}
	}
	class MockObservable implements Observable {
		private Observer observer;

		public void addDependent(Observer lobs) {
			this.observer = lobs;
		}

		public void changesomething() {
			observer.notifyChanged(this);
		}
	}

	public void testChanged() {
		ObservableImpl obs = new ObservableImpl();
		obs.addDependent(this);
		MockObserver observer2 = new MockObserver();
		obs.addDependent(observer2);
		obs.changed(this);
		assertSame(this, notifier);
		assertSame(this, observer2.myNotifier);
	}

	public void testSecondObservableClass() {
		Observable obs2 = createObservable();
		obs2.addDependent(this);
		notifier = null;
		changesomething(obs2);
		assertSame(obs2, notifier);
	}

	protected void changesomething(Observable obs2) {
		((MockObservable) obs2).changesomething();
	}

	protected MockObservable createObservable() {
		return new MockObservable();
	}

	public void testRemoveDependent() {
		ObservableImpl obs = new ObservableImpl();
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
