package org.cq2.delegator.handlers;

public interface Self {
	Object cast(Class type);

	void become(Class type);

	void add(Self object);

	//void remove(Object object);
	Object getComponent(int nr);
	//	 TODO idea: Self.addFirst(), Self.remove(), Self.addLast, Self.replace()
}
