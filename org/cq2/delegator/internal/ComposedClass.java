package org.cq2.delegator.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cq2.delegator.Component;
import org.cq2.delegator.Self;
import org.cq2.delegator.internal.MethodsCache.Tuple;
import org.cq2.delegator.method.MethodUtil;

public class ComposedClass {

    private List classes;

    private ComposedClass[] removeReferences;

    private Map addMap;

    private Map insertMap;

    private ForwardingMethod[] implementingMethods;

    private static ComposedClass emptyClass;

    private Method[] componentReflectMethods;

    private int[] componentIndexes;

    private ComposedClass() {
        this(new ArrayList());
    }

    ComposedClass(List classes) {
        this.classes = classes;
        removeReferences = new ComposedClass[classes.size()];
        addMap = new HashMap();
        insertMap = new HashMap();
        implementingMethods = new ForwardingMethod[0];
        componentReflectMethods = new Method[0];
        componentIndexes = new int[0];
    }

    public static ComposedClass getEmptyClass() {
        if (emptyClass == null) {
            emptyClass = ComposedClassCache.get(new ArrayList());
        }
        return emptyClass;
    }

    public ComposedClass remove(int i) {
        ComposedClass result = removeReferences[i];
        if (result == null) {
            List smallerClasses = new ArrayList(classes);
            smallerClasses.remove(i);
            result = ComposedClassCache.get(smallerClasses);
            removeReferences[i] = result;
        }
        return result;
    }

    public ComposedClass add(Class clazz) {
        ComposedClass result = (ComposedClass) addMap.get(clazz);
        if (result == null) {
            List largerClasses = new ArrayList(classes);
            largerClasses.add(clazz);
            result = ComposedClassCache.get(largerClasses);
            addMap.put(clazz, result);
        }
        return result;
    }

    public ComposedClass insert(Class clazz) {
        ComposedClass result = (ComposedClass) insertMap.get(clazz);
        if (result == null) {
            List largerClasses = new ArrayList(classes);
            largerClasses.add(0, clazz);
            result = ComposedClassCache.get(largerClasses);
            insertMap.put(clazz, result);
        }
        return result;
    }

    public Method getReflectMethod(int methodIdentifier) {
        if (methodIdentifier < componentReflectMethods.length) {
            Method result = componentReflectMethods[methodIdentifier];
            if (result != null)
                return result;
        } else enlargeArrays(methodIdentifier + 1);
        setMethod(methodIdentifier);
        return componentReflectMethods[methodIdentifier];
    }
    
    public int getComponentIndex(int methodIdentifier) {
        if (methodIdentifier < componentReflectMethods.length) {
            int result = componentIndexes[methodIdentifier];
            if (result != -1)
                return result;
        } else enlargeArrays(methodIdentifier + 1);
        setMethod(methodIdentifier);
        return componentIndexes[methodIdentifier];
    }
    
    public ForwardingMethod getMethod(int methodIdentifier) {
        if (methodIdentifier < implementingMethods.length) {
            ForwardingMethod result = implementingMethods[methodIdentifier];
            if (result != null)
                return result;
        } else enlargeArrays(methodIdentifier + 1);
        return setMethod(methodIdentifier);
    }

    private Tuple resolve(int uniqueIdentifier) {
        Method method = ForwardingMethodRegister.getInstance().getMethod(uniqueIdentifier);
        //TODO synchronization noodzaak opnieuw checken.
        if ("hashCode".equals(method.getName()) || "equals".equals(method.getName()))
            return resolve(method, classes.size(), Self.class);
        int i = 0;

        for (; i < classes.size(); i++) {
            Class clazz = (Class) classes.get(i);
            Tuple result = resolve(method, i, clazz);
            if (result != null) return result;
        }

        Tuple result = resolve(method, classes.size(), Self.class);
        if (result != null) return result;
        
        throw new NoSuchMethodError(method.toString());

    }

    private Tuple resolve(Method method, int i, Class clazz) {
        Method delegateMethod;
        boolean componentMethodIsProtected = false;
        if (Component.class.isAssignableFrom(clazz)) {
            delegateMethod = MethodUtil.getDeclaredMethod(clazz, method.getName() + ClassGenerator.SUPERCALL_POSTFIX, method.getParameterTypes(),
                    method.getExceptionTypes());
            //De superdelegatemethod is feitelijk de methode zoals
            // ingetiept door de programmeur
            //deze bestaat per definitie - als die niet gevonden wordt
            // betekent dat hij protected of package is
            Method superDelegateMethod = MethodUtil.getDeclaredMethod(
                    clazz.getSuperclass(), method.getName(),
                    method.getParameterTypes(), method.getExceptionTypes());

            componentMethodIsProtected = (delegateMethod != null)
                    && (superDelegateMethod == null);
        } else {
            delegateMethod = MethodUtil.getDeclaredMethod(clazz, method.getName(), method.getParameterTypes(),
                    method.getExceptionTypes());
            if (delegateMethod != null)  componentMethodIsProtected = Modifier
                    .isProtected(delegateMethod.getModifiers());
        }

        if (delegateMethod != null
                && (!componentMethodIsProtected || Modifier
                        .isProtected(method.getModifiers()))
                || !Modifier.isPublic(method.getModifiers())) {
            return new Tuple(i, delegateMethod);
         }
        return null;
    }

    private ForwardingMethod setMethod(int methodIdentifier) {
        Tuple tuple = resolve(methodIdentifier);
        int componentIndex = tuple.componentIndex;
        Class componentClass;
        if (componentIndex == classes.size()) componentClass = Self.class;
        else componentClass = (Class) classes.get(componentIndex);
        
        Method delegateMethod = tuple.method;
        int componentClassIdentifier = ComponentClassRegister.getInstance().getIdentifier(componentClass);
        Class clazz = null;


        try {
            clazz = ClassGenerator.getClassLoader().loadClass("org.cq2.delegator.ComponentMethod" + methodIdentifier + "_" + componentClassIdentifier);
        } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
        }
        ForwardingMethod result = null;
        try {
            result = (ForwardingMethod) clazz.newInstance();
            clazz.getField("componentIndex").set(result, new Integer(componentIndex));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        implementingMethods[methodIdentifier] = result;
        componentReflectMethods[methodIdentifier] = delegateMethod;
        componentIndexes[methodIdentifier] = componentIndex;
        return result;
    }

    private void enlargeArrays(int length) {
        ForwardingMethod[] oldComponentMethods = implementingMethods;
        implementingMethods = new ForwardingMethod[length];
        System.arraycopy(oldComponentMethods, 0, implementingMethods, 0,
                oldComponentMethods.length);

        Method[] oldComponentReflectMethods = componentReflectMethods;
        componentReflectMethods = new Method[length];
        System.arraycopy(oldComponentReflectMethods, 0, componentReflectMethods, 0,
                oldComponentReflectMethods.length);
        
        int[] oldComponentIndexes = componentIndexes;
        componentIndexes = new int[length];
        System.arraycopy(oldComponentIndexes, 0, componentIndexes, 0,
                oldComponentIndexes.length);
        Arrays.fill(componentIndexes, oldComponentIndexes.length, componentIndexes.length - 1, -1);
        
    }
    
    public String toString() {
        return "ComposedClass: " + classes.toString();
    }
    
    public ComposedClass getSuffix(int i) {
        if (i == 0) return this;
        return remove(0).getSuffix(i - 1);
    }

}