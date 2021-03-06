/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.examples.observer;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.cq2.delegator.Self;

public class ObservableImpl {
	private Map observers = new WeakHashMap();

	public void addDependent(Observer observer2) {
		observers.put(observer2, null);
	}

	void changed(Object self) {
		self = Self.self(self);
		for (Iterator iter = observers.keySet().iterator(); iter.hasNext();) {
			Observer observer = (Observer) iter.next();
			observer.notifyChanged(self);
		}
	}

	public void changed() {
		changed(this);
		
	}
	
	public void removeDependent(Observer test) {
		observers.remove(test);
	}
}
