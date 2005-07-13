package org.cq2.delegator;

import java.lang.reflect.Method;


public interface ISelf {
	Object cast(Class type);
	void become(Class componentType) throws DelegatorException;
	void add(Self self);
	void add(Class componentType);
	void insert(Class componentType);
	void remove(Class c);
	Self getComponent(int component);
	Self getComponent(Class c);
	Self self();
	boolean respondsTo(Method method);
	boolean respondsTo(Class clazz);
	void decorate(Class decorator);

}
