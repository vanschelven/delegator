package org.cq2.delegator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.cq2.delegator.MethodsCache.Tuple;
import org.cq2.delegator.classgenerator.ClassGenerator;
import org.cq2.delegator.method.MethodUtil;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ComposedClass {

    private Vector classes;

    private ComposedClass[] removeReferences;

    private Map addMap;

    private Map insertMap;

    private ProxyMethod[] componentMethods;

    private static ComposedClass emptyClass;

    private Method[] componentReflectMethods;

    private int[] componentIndexes;

    private ComposedClass() {
        this(new Vector());
    }

    ComposedClass(Vector classes) {
        this.classes = classes;
        removeReferences = new ComposedClass[classes.size()];
        addMap = new HashMap();
        insertMap = new HashMap();
        componentMethods = new ProxyMethod[0];
        componentReflectMethods = new Method[0];
        componentIndexes = new int[0];
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
    
    public ProxyMethod getMethod(int methodIdentifier) {
        if (methodIdentifier < componentMethods.length) {
            ProxyMethod result = componentMethods[methodIdentifier];
            if (result != null)
                return result;
        } else enlargeArrays(methodIdentifier + 1);
        return setMethod(methodIdentifier);
    }

    private Method getDeclaredMethod(Class clazz, String s, Class[] classes) {
        try {
            return clazz.getDeclaredMethod(s, classes);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Tuple resolve(int uniqueIdentifier) {
        Method method = ProxyMethodRegister.getInstance().getMethod(uniqueIdentifier);
        if ((method.getName().equals("equals")) || (method.getName().equals("toString")))
            return new Tuple(classes.size(), getDeclaredMethod(Self.class, "equals", new Class[]{Object.class}));
        //TODO synchronization noodzaak opnieuw checken.
        //TODO equals etc.
        //if ("equals".equals(method.getName()))
       //     return Boolean.valueOf(equals(args[0]));
      //  if ("hashCode".equals(method.getName))
      //      return new Integer(hashCode());
        int i = 0;

        String name = method.getName();
//        if (method.getName().startsWith("__next__")) {
//            while (components[i] != proxy && (i < nrOfComponents))
//                i++;
//            name = name.substring(8);
//            i++;
//            //if(!cmps.hasNext())throw new NoSuchMethodError
//        }

        for (; i < classes.size(); i++) {
            Class clazz = (Class) classes.get(i);
            Tuple result = xxxxxxxx(method, i, name, clazz);
            if (result != null) return result;
        }

        Tuple result = xxxxxxxx(method, classes.size(), name, Self.class);
        if (result != null) return result;
        
        throw new NoSuchMethodError(method.toString());

    }

    private Tuple xxxxxxxx(Method method, int i, String name, Class clazz) {
        Method delegateMethod;
        boolean componentMethodIsProtected = false;
        if (Component.class.isAssignableFrom(clazz)) {
            delegateMethod = MethodUtil.getDeclaredMethod(clazz, name + ClassGenerator.SUPERCALL_POSTFIX, method.getParameterTypes(),
                    method.getExceptionTypes());
            //De superdelegatemethod is feitelijk de methode zoals
            // ingetiept door de programmeur
            //deze bestaat per definitie - als die niet gevonden wordt
            // betekent dat hij protected is
            Method superDelegateMethod = MethodUtil.getDeclaredMethod(
                    clazz.getSuperclass(), name,
                    method.getParameterTypes(), method.getExceptionTypes());

            componentMethodIsProtected = (delegateMethod != null)
                    && (superDelegateMethod == null);
        } else {
            delegateMethod = MethodUtil.getDeclaredMethod(clazz, name, method.getParameterTypes(),
                    method.getExceptionTypes());
            if (delegateMethod != null)  componentMethodIsProtected = Modifier
                    .isProtected(delegateMethod.getModifiers());
        }
        //TODO dit is in duigen gevallen met de toevoeging van package
        // (in forwardees) maar dat lossen we later wel weer op...
        if (delegateMethod != null
                && (!componentMethodIsProtected || Modifier
                        .isProtected(method.getModifiers()))
                || !Modifier.isPublic(method.getModifiers())) {
            return new Tuple(i, delegateMethod);
         }
        return null;
    }

    private ProxyMethod setMethod(int methodIdentifier) {
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
        ProxyMethod result = null;
        try {
            result = (ProxyMethod) clazz.newInstance();
            clazz.getField("componentIndex").set(result, new Integer(componentIndex));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        componentMethods[methodIdentifier] = result;
        componentReflectMethods[methodIdentifier] = delegateMethod;
        componentIndexes[methodIdentifier] = componentIndex;
        return result;
    }

    private void enlargeArrays(int length) {
        ProxyMethod[] oldComponentMethods = componentMethods;
        componentMethods = new ProxyMethod[length];
        System.arraycopy(oldComponentMethods, 0, componentMethods, 0,
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

}