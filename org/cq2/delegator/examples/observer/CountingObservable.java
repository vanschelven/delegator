/*
 * Created on Mar 25, 2004
 */
package org.cq2.delegator.examples.observer;

import org.cq2.delegator.handlers.ISelf;

public abstract class CountingObservable implements Observable, ISelf {
	private int count;
	abstract public void changed();
	
	
	public void increment() {
		count++;
		changed();
	}
	
	public int getCount() {
		return count;
	}
}
