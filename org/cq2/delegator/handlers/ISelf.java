package org.cq2.delegator.handlers;

public interface ISelf {
	Object cast(Class type);
	void become(Class componentType);
	// add must become extend()
	void add(ISelf component);
	void add(Class componentType);
	void add(Class componentType, Object[] ctorArgs);
	//void remove();
	//void remove(Object object);
	//void remove(int component)
	//void remove(Class c)
	Object component(int component);
	// Object component(Class c);
	// Object component();
	Self self();
	//	 TODO idea: Self.addFirst(), Self.remove(), Self.addLast, Self.replace()
}
