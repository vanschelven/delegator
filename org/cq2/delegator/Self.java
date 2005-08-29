/*
 * Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com Copyright
 * (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl
 */
package org.cq2.delegator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Stack;

import org.cq2.delegator.MethodsCache.Tuple;
import org.cq2.delegator.classgenerator.ClassGenerator;
import org.cq2.delegator.method.MethodUtil;

public class Self implements MyInvocationHandler, ISelf {
    public static ThreadLocal self = new ThreadLocal() {
        protected Object initialValue() {
            return new Stack();
        }
    };

    Object[] components;

    private int nrOfComponents = 0;

    private Class[] equalsComponents;

    private MethodsCache3 methodsCache;
    
    private CacheNode cacheNode;

    public Self(Object component) {
        this();
        addComponent(component);
    }

    public Self() {
        components = new Object[4];
        cacheNode = CacheCache.getEmptyNode();
        methodsCache = cacheNode.methodsCache;
    }

    public Self(Class firstComponentClass) {
        this();
        addComponent(newComponent(firstComponentClass));
    }

    private Object invokeViaCache(Tuple tuple, Object[] args)
            throws Throwable {
        Object component = components[tuple.index];
        Method delegateMethod = tuple.method;
        //copy paste
        Stack stack = ((Stack) self.get());
        stack.push(this);
        try {
            try {
                return delegateMethod.invoke(component, args);
            } finally {
                stack.pop();
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    //TODO methods that use Proxy may not be cached! Test on this and make it work. Only become uses proxy so perhaps a different mechanism is needed there
    //TODO synchronization noodzaak opnieuw checken.
    public Object invokeNewMethod(Object proxy, MiniMethod method, Object[] args)
            throws Throwable {
        if ("equals".equals(method.name))
            return Boolean.valueOf(equals(args[0]));
        if ("hashCode".equals(method.name))
            return new Integer(hashCode());
        int i = 0;

        String name = method.name;
        if (method.name.startsWith("__next__")) {
            while (components[i] != proxy && (i < nrOfComponents))
                i++;
            name = name.substring(8);
            i++;
            //if(!cmps.hasNext())throw new NoSuchMethodError
        }

        for (; i < nrOfComponents; i++) {
            Object component = components[i];
            Method delegateMethod;
            boolean componentMethodIsProtected;
            if (component instanceof Component) {
                delegateMethod = MethodUtil.getDeclaredMethod(component
                        .getClass(), name + ClassGenerator.SUPERCALL_POSTFIX, method.parameterTypes,
                        method.exceptionTypes);
                //De superdelegatemethod is feitelijk de methode zoals
                // ingetiept door de programmeur
                //deze bestaat per definitie - als die niet gevonden wordt
                // betekent dat hij protected is
                Method superDelegateMethod = MethodUtil.getDeclaredMethod(
                        component.getClass().getSuperclass(), name,
                        method.parameterTypes, method.exceptionTypes);

                componentMethodIsProtected = (delegateMethod != null)
                        && (superDelegateMethod == null);
            } else {
                delegateMethod = MethodUtil.getDeclaredMethod(component
                        .getClass(), name, method.parameterTypes,
                        method.exceptionTypes);
                componentMethodIsProtected = Modifier
                        .isProtected(delegateMethod.getModifiers());
            }
            //TODO dit is in duigen gevallen met de toevoeging van package
            // (in forwardees) maar dat lossen we later wel weer op...
            if (delegateMethod != null
                    && (!componentMethodIsProtected || Modifier
                            .isProtected(method.modifiers))
                    || !Modifier.isPublic(method.modifiers)) {
                methodsCache.put(
                        MethodRegister.getInstance().getUnique(method), this, i,
                        delegateMethod);
                delegateMethod.setAccessible(true);
                Stack stack = ((Stack) self.get());
                stack.push(this);
                try {
                    try {
                        return delegateMethod.invoke(component, args);
                    } finally {
                        stack.pop();
                    }
                } catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }

        }
        if ("become".equals(name)) {
            become((Class) args[0], proxy);
            return null;
        }

        Method delegateMethod = MethodUtil.getDeclaredMethod(Self.class, name,
                method.parameterTypes, method.exceptionTypes);
        if (delegateMethod != null)
            return delegateMethod.invoke(this, args);
        throw new NoSuchMethodError(method.toString());

    }

    public synchronized Object invoke(Object proxy, int uniqueIdentifier, Object[] args) throws Throwable {
        Tuple tuple = methodsCache.getTuple(uniqueIdentifier);
        if (tuple != null) {
            return invokeViaCache(tuple, args);
        }
        return invokeNewMethod(proxy, MethodRegister.getInstance().getMethod(uniqueIdentifier), args);
    }
    
    public synchronized int i_invoke(Object proxy, int uniqueIdentifier) throws Throwable {
        Tuple tuple = methodsCache.getTuple(uniqueIdentifier);
        if (tuple != null) {
            return methodsCache.wrappers[uniqueIdentifier].i_invoke(components[tuple.index]);
        }
        return ((Integer) invokeNewMethod(proxy, MethodRegister.getInstance().getMethod(uniqueIdentifier), new Class[]{})).intValue();
    }
    
    //TODO wordt de stack al wel gebruikt meerdere keren achter elkaar??!
    //TODO onderstaand: become 2x, equals, self-calls verwijderd
   public Tuple initCaches(MiniMethod method)
            throws Throwable {
        String name = method.name;
        for (int i = 0; i < nrOfComponents; i++) {
            Object component = components[i];
            Method delegateMethod;
            boolean componentMethodIsProtected;
            if (component instanceof Component) {
                delegateMethod = MethodUtil.getDeclaredMethod(component
                        .getClass(), name + ClassGenerator.SUPERCALL_POSTFIX,
                        method.parameterTypes, method.exceptionTypes);
                //De superdelegatemethod is feitelijk de methode zoals
                // ingetiept door de programmeur
                //deze bestaat per definitie - als die niet gevonden wordt
                // betekent dat hij protected is
                Method superDelegateMethod = MethodUtil.getDeclaredMethod(
                        component.getClass().getSuperclass(), name,
                        method.parameterTypes, method.exceptionTypes);

                componentMethodIsProtected = (delegateMethod != null)
                        && (superDelegateMethod == null);
            } else {
                delegateMethod = MethodUtil.getDeclaredMethod(component
                        .getClass(), name, method.parameterTypes,
                        method.exceptionTypes);
                componentMethodIsProtected = Modifier
                        .isProtected(delegateMethod.getModifiers());
            }
            //TODO dit is in duigen gevallen met de toevoeging van package
            // (in forwardees) maar dat lossen we later wel weer op...
            if (delegateMethod != null
                    && (!componentMethodIsProtected || Modifier
                            .isProtected(method.modifiers))
                    || !Modifier.isPublic(method.modifiers)) {
                return methodsCache.put(
                        MethodRegister.getInstance().getUnique(method), this,
                        i, delegateMethod);
            }

        }
        throw new NoSuchMethodError(method.toString());
    }


    
    public synchronized Tuple2 getTuple2(int uniqueIdentifier) throws Throwable {
        Tuple tuple = methodsCache.getTuple(uniqueIdentifier);
        if (tuple == null)
            tuple = initCaches(MethodRegister.getInstance().getMethod(uniqueIdentifier));
        return new methodsCache.wrappers[uniqueIdentifier].i_invoke(components[tuple.index]);
        return ((Integer) invokeNewMethod(proxy, MethodRegister.getInstance().getMethod(uniqueIdentifier), new Class[]{})).intValue();
    }

    public Object cast(Class clas) {
        return Delegator.proxyFor(clas, this);
    }

    public void become(Class clas) throws DelegatorException {
        throw new DelegatorException(
                "Become may only be called from within components of self");
    }

    private void become(Class clas, Object caller) {
        Component newComponent = newComponent(clas);
        for (int i = 0; i < nrOfComponents; i++) {
            if (components[i] == caller) {
                components[i] = newComponent;
                cacheNode = cacheNode.become(i, clas);
                methodsCache = cacheNode.methodsCache;
                return;
            }
        }
        throw new DelegatorException(
                "Become may only be called from within components of self");
    }

    public void add(Self object) {
        for (int i = 0; i < object.nrOfComponents; i++)
            addComponent(object.components[i]);
    }

    public Self getComponent(int i) {
        return new Self(component(i));
    }

    public Self getComponent(Class clazz) {
        Object result = component(clazz);
        if (result instanceof SingleSelfComponent)
            throw new DelegatorException(
                    "Component may not be shared between different instances of Self");
        return new Self(result);
    }

    Object component(int i) {
        return components[i];
    }

    Object component(Class clazz) {
        for (int i = 0; i < nrOfComponents; i++) {
            if ((components[i] instanceof Component && components[i].getClass()
                    .getSuperclass().equals(clazz))
                    || components[i].getClass().equals(clazz))
                return components[i];
        }
        return null;
    }

    public void add(Class clas) {
        addComponent(newComponent(clas));
    }

    private synchronized void addComponent(Object component) {
        if (nrOfComponents >= components.length) {
            Object[] newComponents = new Object[components.length * 2];
            System
                    .arraycopy(components, 0, newComponents, 0,
                            components.length);
            components = newComponents;
        }
        components[nrOfComponents++] = component;
        cacheNode = cacheNode.add(component.getClass());
        methodsCache = cacheNode.methodsCache;
    }

    public synchronized void insert(Class componentType) {
        Component[] newComponents = new Component[components.length + 1];
        newComponents[0] = newComponent(componentType);
        System.arraycopy(components, 0, newComponents, 1, nrOfComponents);
        components = newComponents;
        nrOfComponents++;
        cacheNode = cacheNode.insert(componentType);
        methodsCache = cacheNode.methodsCache;
    }

    private Component newComponent(Class clas) {
        return ClassGenerator.newComponentInstance(clas, this);
    }

    public Self extend(Class class1) {
        Self newSelf = new Self(newComponent(class1));
        newSelf.add(this);
        return newSelf;
    }

    public Self self() {
        return this;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Self || obj instanceof Proxy))
            return false;
        Self other;
        if (obj instanceof Self)
            other = (Self) obj;
        else
            other = getSelfFromProxy((Proxy) obj);

        Object[] thisComponents = getEqualsComponents();
        Object[] otherComponents = other.getEqualsComponents();
        if (thisComponents.length != otherComponents.length)
            return false;

        try {
            for (int i = 0; i < thisComponents.length; i++) {
                try {
                    Method m = thisComponents[i].getClass().getDeclaredMethod(
                            "equals" + ClassGenerator.SUPERCALL_POSTFIX,
                            new Class[] { Object.class });
                    Boolean result = (Boolean) m.invoke(thisComponents[i],
                            new Object[] { otherComponents[i] });
                    if (!result.booleanValue())
                        return false;
                } catch (NoSuchMethodException e) {
                    if (!thisComponents[i].equals(otherComponents[i]))
                        return false;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private static Self getSelfFromProxy(Proxy proxy) {
        try {
            Field selfField = proxy.getClass().getDeclaredField("self");
            return (Self) selfField.get(proxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Method m) {
        for (int i = 0; i < nrOfComponents; i++) {
            Method result = MethodUtil.getDeclaredMethod(components[i]
                    .getClass().getSuperclass(), m.getName(), m
                    .getParameterTypes(), m.getExceptionTypes());
            if (result != null)
                return result;
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

    public synchronized void remove(Class c) {
        for (int i = 0; i < nrOfComponents; i++) {
            if (components[i].getClass().getSuperclass().equals(c)) {
                for (int j = i + 1; j < nrOfComponents; j++)
                    components[j - 1] = components[j];
                nrOfComponents--;
                cacheNode = cacheNode.remove(i);
                methodsCache = cacheNode.methodsCache;
                return;
            }
        }
    }

    public void setEqualsComponents(Class[] classes) {
        this.equalsComponents = classes;
    }

    private Object[] getEqualsComponents() {
        Object[] result;
        if (equalsComponents == null) {
            result = new Object[nrOfComponents];
            for (int i = 0; i < result.length; i++) {
                result[i] = components[i];
            }
        } else {
            result = new Component[equalsComponents.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = component(equalsComponents[i]);
            }
        }
        return result;
    }

    public void addSharableComponent(Class componentClass) {
        Component component = ClassGenerator
                .newSharableComponentInstance(componentClass);
        addComponent(component);
    }

    public void addForwardee(Object forwardee) {
        addComponent(forwardee);
    }
    
    //TODO (HEEL ergens anders, finalize documenteren)

}