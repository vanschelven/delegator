/*
 * Created on Apr 3, 2004
 */
package org.cq2.delegator.examples.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;

import org.cq2.delegator.Delegator;

abstract class ObservableList2 {
	public ObservableList2 create() {
		ObservableList2 list = (ObservableList2) Delegator.extend(ObservableList2.class, Observable.class, ArrayList.class);
		list.addObserver(...);
		return list;
	}
	private ObservableList2() {}
	public boolean add(Object arg0) {
		notifyObservers();
		return proto().add(arg0);
	}
	abstract void notifyObservers();
	abstract ArrayList proto();
}


public class ObservableListTest extends TestCase implements Observer{
	public void testCreate() {
		List innerList = new ArrayList();
		List list = new ObservableList(innerList);
		Observable observable = (Observable) list;
		observable.addObserver(this);
		assertNotNull(observable);
		assertNotNull(list);
	}

	public void update(Observable arg0, Object arg1) {}
}
