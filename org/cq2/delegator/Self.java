/*
 * Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com Copyright
 * (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl
 */
package org.cq2.delegator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.cq2.delegator.classgenerator.ProxyGenerator;
import org.cq2.delegator.method.MethodUtil;

public class Self implements InvocationHandler, ISelf {
    public static ThreadLocal self = new ThreadLocal() {
        protected Object initialValue() {
            return new Stack();
        }
    };

    private Object[] components;

    private transient Object caller; // TODO threadsafe!

    private int nrOfComponents = 0;

    private Self(Component object) {
        this();
        addComponent(object);
    }

    public Self() {
        this.components = new Object[4];
    }

    public Self(Class firstComponentClass) {
        this();
        addComponent(newComponent(firstComponentClass));
    }

    /**
     * @see InvocationHandler#invoke(Object, Method, Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        this.caller = proxy;
        String name = method.getName();
        if ("equals".equals(name))
            return Boolean.valueOf(equals(args[0]));
        if ("hashCode".equals(name))
            return new Integer(hashCode());
        int i = 0;
        if (name.startsWith("__next__")) {
            while (components[i] != proxy && (i < nrOfComponents))
                i++;
            name = name.substring(8);
            i++;
            //if(!cmps.hasNext())throw new NoSuchMethodError
        }
        List argTypeList = new ArrayList();
        argTypeList.add(InvocationHandler.class);
        argTypeList.addAll(Arrays.asList(method.getParameterTypes()));
        
        List argTypeListExludingInvocationHandler = new ArrayList();
        argTypeListExludingInvocationHandler.addAll(Arrays.asList(method.getParameterTypes()));

        for (; i < nrOfComponents; i++) {
            try {
                Method delegateMethod = MethodUtil.getDeclaredMethod(
                        components[i].getClass(), name, (Class[]) argTypeList
                                .toArray(new Class[] {}));
                Method superDelegateMethod = MethodUtil.getDeclaredMethod(
                        components[i].getClass().getSuperclass(), name, (Class[]) argTypeListExludingInvocationHandler.toArray(new Class[] {}));
                boolean componentMethodIsProtected = (delegateMethod != null) && (superDelegateMethod == null);
                if (delegateMethod != null && (!componentMethodIsProtected || Modifier.isProtected(method.getModifiers()))) {
                    delegateMethod.setAccessible(true);
                    Stack stack = ((Stack) self.get());
                    stack.push(this);
                    try {
                        return delegateMethod.invoke(components[i],
                                mapArgs(args));
                    } finally {
                        stack.pop();
                    }
                }
            } catch (InvocationTargetException e) {
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
        } else if ("become".equals(name)) {
            become((Class) args[0]);
            return null;
        } else if ("toString".equals(name))
            return toString();
        else if ("respondsTo".equals(name)) {
            if (args[0] instanceof Method)
                return Boolean.valueOf(respondsTo((Method) args[0]));
            return Boolean.valueOf(respondsTo((Class) args[0]));
        } else if ("component".equals(name))
            return component(((Number) args[0]).intValue());
        else if ("self".equals(name))
            return self();
        else if ("insert".equals(name) && args[0] instanceof Class) {
            insert((Class) args[0]);
            return null;
        } else if ("decorate".equals(name) && args[0] instanceof Class) {
            decorate((Class) args[0]);
            return null;
        }
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
        System.out.println(caller);
        Object newComponent = newComponent(clas);
        for (int i = 0; i < components.length; i++) {
            if (components[i] == caller)
                components[i] = newComponent;
            // break?
        }
    }

    public void add(Self object) {
        for (int i = 0; i < object.nrOfComponents; i++)
            addComponent(object.components[i]);
    }

    public Object component(int component) {
        return components[component];
    }

    public Object component(InvocationHandler h, int component) {
        return component(component);
    }

    public void add(Class clas) {
        add(newComponent(clas));
    }

    public void add(Component component) {
        addComponent(component);
    }

    private void addComponent(Object component) {
        if (nrOfComponents >= components.length) {
            Object[] newComponents = new Object[components.length * 2];
            System
                    .arraycopy(components, 0, newComponents, 0,
                            components.length);
            components = newComponents;
        }
        components[nrOfComponents++] = component;
    }

    public void add(InvocationHandler s, Class clas) {
        add(clas);
    }

    public void insert(Class componentType) {
        Object[] newComponents = new Object[components.length + 1];
        newComponents[0] = newComponent(componentType);
        System.arraycopy(components, 0, newComponents, 1, nrOfComponents);
        components = newComponents;
        nrOfComponents++;
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
        return arg0 instanceof Self ? super.equals(arg0) : arg0 != null ? arg0
                .equals(this) : false;
    }

    private Method findMethod(Method m) {
        for (int i = 0; i < nrOfComponents; i++) {
            try {
                return components[i].getClass().getSuperclass().getMethod(
                        m.getName(), m.getParameterTypes());
            } catch (NoSuchMethodException e) {
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
        return obj instanceof Component ? ((ISelf) obj).cast(obj.getClass()
                .getSuperclass()) : obj;
    }

    public void decorate(Class decorator) {
        insert(decorator);
    }

    public static void decorate(Object object, Class decorator) {
        ((ISelf) object).decorate(decorator);
    }

    public static ISelf clone(Object object) {
        Self clone = new Self();
        clone.add(((ISelf) object).self());
        return (ISelf) clone.cast(object.getClass().getSuperclass());
    }
}