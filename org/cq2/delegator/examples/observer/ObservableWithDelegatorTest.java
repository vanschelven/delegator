/*
 * This file is part of i-Tor: Internet Tools & Technologies for Open Repositories
 * Copyright (C) 2002, 2003  NIWI-KNAW, The Netherlands, http://www.knaw.nl
 * Copyright (C) 2003 - 2004  i-tor.org, http://i-tor.org
 * 
 * i-Tor is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * i-Tor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * See the COPYING file located in the top-level-directory of 
 * the archive of this library for complete text of license.
 * Created on Mar 17, 2004
 */
package org.cq2.delegator.examples.observer;

import org.cq2.delegator.Delegator;

import junit.framework.TestCase;

public class ObservableWithDelegatorTest extends TestCase implements Observer {
	private Object notifier;

	class MockObserver implements Observer {
		public Object myNotifier;

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
		return (MyObservableImplWithDelegation) new Delegator().createExtension(MyObservableImplWithDelegation.class, ObservableImpl.class);
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
