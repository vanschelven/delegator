/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 *
 * Created on Mar 17, 2004
 */
package org.cq2.delegator.examples.observer;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;
import org.cq2.delegator.handlers.ISelf;

public class ObservableWithDelegatorTest extends TestCase implements Observer {
	private Object notifier;

	
	class CountingObserver implements Observer {
		int count;
		public void notifyChanged(Object observable) {
			CountingObservableWithDelegation castedObservable = ((CountingObservableWithDelegation) ((ISelf)observable).cast(CountingObservableWithDelegation.class));
			count = castedObservable.getCount();
			
		}
	}

	public void testCounting() {
		//change example to counter, with inc() and count()
		CountingObservableWithDelegation observable = createObservable();
		CountingObserver observer = new CountingObserver();
		observable.addDependent(observer);
		observable.increment();
		assertEquals(1, observer.count);
		observable.increment();
		assertEquals(2, observer.count);
		
	}

	public void testSecondObservableClass() {
		CountingObservableWithDelegation obs2 = createObservable();
		obs2.addDependent(this);
		notifier = null;
		obs2.increment();
		assertSame(((ISelf)obs2).self(), 
				((ISelf)notifier).self());
	}

	private CountingObservableWithDelegation createObservable() {
		return (CountingObservableWithDelegation) Delegator.extend(
				CountingObservableWithDelegation.class, new Class[] {ObservableImpl.class});
	}
	
	public void notifyChanged(Object observable) {
		this.notifier = observable;
		
	}
}
