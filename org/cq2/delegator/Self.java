/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import org.cq2.delegator.classgenerator.ProxyGenerator;

public class Self implements InvocationHandler, ISelf {
	public static ThreadLocal self = new ThreadLocal() {
		protected Object initialValue() {
			return new Stack();
		}
	};
	private List components;
	private transient Object caller; // TODO threadsafe!

	private Self(Component object) {
		this();
		components.add(object);
	}

	public Self() {
		this.components = new ArrayList();
	}

	public Self(Class firstComponentClass) {
		this();
		components.add(newComponent(firstComponentClass));
	}

	/**
	 * @see InvocationHandler#invoke(Object, Method, Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		this.caller = proxy;
		String name = method.getName();
		if ("equals".equals(name))
			return Boolean.valueOf(equals(args[0]));
		if ("hashCode".equals(name))
			return new Integer(hashCode());
		Iterator cmps = components.iterator();
		if(name.startsWith("__next__")) {
			Object component = null;
			while(component!=proxy && cmps.hasNext()){
				component = cmps.next();
			}
			name = name.substring(8);
			//if(!cmps.hasNext())throw new NoSuchMethodError
		}
		List argTypeList = new ArrayList();
		argTypeList.add(InvocationHandler.class);
		argTypeList.addAll(Arrays.asList(method.getParameterTypes()));
		while (cmps.hasNext()) {
			Object component = cmps.next();
			try {
				Method delegateMethod = component.getClass().getDeclaredMethod(name,
						(Class[]) argTypeList.toArray(new Class[]{}));
				delegateMethod.setAccessible(true);
				Stack stack = ((Stack) self.get());
				stack.push(this);
				try {
					return delegateMethod.invoke(component, mapArgs(args));
				}
				finally {
					stack.pop();
				}
			}
			catch (NoSuchMethodException e) {
				continue;
			}
			catch (InvocationTargetException e) {
				throw e.getTargetException();
			}
		}
		if ("cast".equals(name))
			return cast((Class) args[0]);
		else if ("add".equals(name)) {
			if (args[0] instanceof Class)
				add((Class) args[0]);
			else if (args[0] instanceof Self)
				add((Self) args[0]);
			else if (args[0] instanceof Component)
				add((Component) args[0]);
			return null;
		}
		else if ("become".equals(name)) {
			become((Class) args[0]);
			return Void.TYPE;
		}
		else if ("toString".equals(name))
			return toString();
		else if ("respondsTo".equals(name)) {
			if (args[0] instanceof Method)
				return Boolean.valueOf(respondsTo((Method) args[0]));
			else
				return Boolean.valueOf(respondsTo((Class) args[0]));
		}
		else if ("component".equals(name))
			return component(((Number) args[0]).intValue());
		else if ("self".equals(name))
			return self();
		throw new NoSuchMethodError(method.toString());
	}

	private Object[] mapArgs(Object[] args) {
		List argList = new ArrayList();
		argList.add(this);
		if (args != null)
			argList.addAll(Arrays.asList(args));
		return argList.toArray();
	}

	public Object cast(Class clas) {
		return Delegator.proxyFor(clas, this);
	}

	public void become(Class clas) {
		Object newComponent = newComponent(clas);
		for (ListIterator iter = components.listIterator(); iter.hasNext();) {
			if (iter.next() == caller)
				iter.set(newComponent);
		}
	}

	public void add(Self object) {
		for (Iterator c = object.components.iterator(); c.hasNext();)
			components.add(c.next());
	}

	public Object component(int component) {
		return components.get(component);
	}

	public Object component(InvocationHandler h, int component) {
		return component(component);
	}

	public void add(Class clas) {
		add(newComponent(clas));
	}

	public void add(Component component) {
		components.add(component);
	}

	public void add(InvocationHandler s, Class clas) {
		add(clas);
	}

	private Component newComponent(Class clas) {
		return ProxyGenerator.newComponentInstance(clas);
	}

	public Self extend(Class class1) {
		Self newSelf = new Self(newComponent(class1));
		newSelf.add(this);
		return newSelf;
	}

	public Self self() {
		return this;
	}

	public boolean equals(Object arg0) {
		return arg0 instanceof Self ? super.equals(arg0) : arg0 != null ? arg0.equals(this) : false;
	}

	private Method findMethod(Method m) {
		Iterator c = components.iterator();
		while (c.hasNext()) {
			Object component = c.next();
			try {
				return component.getClass().getSuperclass().getMethod(m.getName(),
						m.getParameterTypes());
			}
			catch (NoSuchMethodException e) {
				continue;
			}
		}
		return null;
	}

	public boolean respondsTo(Method m) {
		return findMethod(m) != null;
	}

	public boolean respondsTo(Class clazz) {
		Method m[] = clazz.getMethods();
		boolean result = true;
		for (int i = 0; i < m.length && result; i++) {
			result = findMethod(m[i]) != null;
		}
		return result;
	}

	public static Object self(Object obj) {
		return obj instanceof Component ? ((ISelf)obj).cast(obj.getClass().getSuperclass()) : obj;
	}
}