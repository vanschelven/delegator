/*
 * Created on Apr 3, 2004
 */
package org.cq2.delegator.examples.observer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;

public class ObservableList extends Observable implements List {
	private final List innerList;
	public ObservableList create() {
		//innerList = new ArrayList();
		ObservableList list = new ObservableList(innerList);
		//list.addObserver(...);
		return list;
	}
	private ObservableList(List list) {
		this.innerList = list;
	}
	public boolean add(Object arg0) {
		notifyObservers();
		return innerList.add(arg0);
	}

	public int size() {
		return 0;
	}

	public void clear() {}

	public boolean isEmpty() {
		return false;
	}

	public Object[] toArray() {
		return null;
	}

	public Object get(int arg0) {
		return null;
	}

	public Object remove(int arg0) {
		return null;
	}

	public void add(int arg0, Object arg1) {}

	public int indexOf(Object arg0) {
		return 0;
	}

	public int lastIndexOf(Object arg0) {
		return 0;
	}

	public boolean contains(Object arg0) {
		return false;
	}

	public boolean remove(Object arg0) {
		return false;
	}

	public boolean addAll(int arg0, Collection arg1) {
		return false;
	}

	public boolean addAll(Collection arg0) {
		return false;
	}

	public boolean containsAll(Collection arg0) {
		return false;
	}

	public boolean removeAll(Collection arg0) {
		return false;
	}

	public boolean retainAll(Collection arg0) {
		return false;
	}

	public Iterator iterator() {
		return null;
	}

	public List subList(int arg0, int arg1) {
		return null;
	}

	public ListIterator listIterator() {
		return null;
	}

	public ListIterator listIterator(int arg0) {
		return null;
	}

	public Object set(int arg0, Object arg1) {
		return null;
	}

	public Object[] toArray(Object[] arg0) {
		return null;
	}
}
