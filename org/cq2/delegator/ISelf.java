package org.cq2.delegator;


public interface ISelf {
	Object cast(Class type);
	void become(Class componentType);
	// add must become extend()
	void add(Self self);
	void add(Class componentType);
	void add(Component component);
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
