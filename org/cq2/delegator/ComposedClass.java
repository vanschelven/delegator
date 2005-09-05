package org.cq2.delegator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.cq2.delegator.MethodsCache.Tuple;

public class ComposedClass {

    private Vector classes;

    private ComposedClass[] removeReferences;

    private Map addMap;

    private Map insertMap;

    private ProxyMethod[] componentMethods;

    private static ComposedClass emptyClass;

    private ComposedClass() {
        this(new Vector());
    }

    ComposedClass(Vector classes) {
        this.classes = classes;
        removeReferences = new ComposedClass[classes.size()];
        addMap = new HashMap();
        insertMap = new HashMap();
        componentMethods = new ProxyMethod[0];
    }

    public static ComposedClass getEmptyClass() {
        if (emptyClass == null) {
            emptyClass = ComposedClassCache.get(new Vector());
        }
        return emptyClass;
    }

    public ComposedClass remove(int i) {
        ComposedClass result = removeReferences[i];
        if (result == null) {
            Vector smallerClasses = new Vector(classes);
            smallerClasses.remove(i);
            result = ComposedClassCache.get(smallerClasses);
            removeReferences[i] = result;
        }
        return result;
    }

    public ComposedClass add(Class clazz) {
        ComposedClass result = (ComposedClass) addMap.get(clazz);
        if (result == null) {
            Vector largerClasses = new Vector(classes);
            largerClasses.add(clazz);
            result = ComposedClassCache.get(largerClasses);
            addMap.put(clazz, result);
        }
        return result;
    }

    public ComposedClass insert(Class clazz) {
        ComposedClass result = (ComposedClass) insertMap.get(clazz);
        if (result == null) {
            Vector largerClasses = new Vector(classes);
            largerClasses.add(0, clazz);
            result = ComposedClassCache.get(largerClasses);
            insertMap.put(clazz, result);
        }
        return result;
    }

    public ProxyMethod getMethod(int methodIdentifier) {
        if (methodIdentifier < componentMethods.length) {
            ProxyMethod result = componentMethods[methodIdentifier];
            if (result != null)
                return result;
        }
        enlargeComponentMethods(methodIdentifier + 1);
        return setMethod(methodIdentifier);
    }

    private Tuple resolve(int i) {
        try {
            return new Tuple(0, Vector.class.getDeclaredMethod("add",
                    new Class[] { Object.class }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ProxyMethod setMethod(int methodIdentifier) {
        Tuple tuple = resolve(methodIdentifier);
        Method delegateMethod = tuple.method;
        int componentIndex = tuple.componentIndex;
        ProxyMethod result = ComponentMethodGenerator
                .getComponentMethodInstance(ProxyMethodRegister.getInstance()
                        .getProxyMethodClass(methodIdentifier), (Class) classes
                        .get(componentIndex), delegateMethod, componentIndex);
        componentMethods[methodIdentifier] = result;
        return result;
    }

    private void enlargeComponentMethods(int length) {
        ProxyMethod[] oldComponentMethods = componentMethods;
        componentMethods = new ProxyMethod[length];
        System.arraycopy(oldComponentMethods, 0, componentMethods, 0,
                oldComponentMethods.length);
    }

}