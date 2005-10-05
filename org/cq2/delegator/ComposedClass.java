package org.cq2.delegator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.cq2.delegator.MethodsCache.Tuple;
import org.cq2.delegator.classgenerator.ClassGenerator;
import org.cq2.delegator.method.MethodUtil;

public class ComposedClass {

    private Vector classes;

    private ComposedClass[] removeReferences;

    private Map addMap;

    private Map insertMap;

    private ProxyMethod[] componentMethods;

    private static ComposedClass emptyClass;

    private Method[] componentReflectMethods;

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
    
    public ProxyMethod getMethod(int methodIdentifier) {
        if (methodIdentifier < componentMethods.length) {
            ProxyMethod result = componentMethods[methodIdentifier];
            if (result != null)
                return result;
        } else enlargeArrays(methodIdentifier + 1);
        return setMethod(methodIdentifier);
    }

    private Tuple resolve(int uniqueIdentifier) {
        Method method = ProxyMethodRegister.getInstance().getMethod(uniqueIdentifier);
        
        //TODO methods that use Proxy may not be cached! Test on this and make it work. Only become uses proxy so perhaps a different mechanism is needed there
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
            Method delegateMethod;
            boolean componentMethodIsProtected;
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
                componentMethodIsProtected = Modifier
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

        }

//TODO dit...
//        Method delegateMethod = MethodUtil.getDeclaredMethod(Self.class, name,
//                method.parameterTypes, method.exceptionTypes);
//        if (delegateMethod != null)
//            return delegateMethod.invoke(this, args);
        throw new NoSuchMethodError(method.toString());

    }

    private ProxyMethod setMethod(int methodIdentifier) {
        Tuple tuple = resolve(methodIdentifier);
        int componentIndex = tuple.componentIndex;
        Class superClass = ProxyMethodRegister.getInstance().getProxyMethodClass(methodIdentifier);
        Class componentClass = (Class) classes.get(componentIndex);
        Method delegateMethod = tuple.method;
        int componentClassIdentifier = ComponentClassRegister.getInstance().getIdentifier(componentClass);
        Class clazz = null;
//        try {
//            System.out.println("ComposedClass.setMethod:" + getClass().getClassLoader());
//            clazz = getClass().getClassLoader().loadClass("org.cq2.delegator.ComponentMethod" + methodIdentifier + "_" + componentClassIdentifier);
//        } catch (ClassNotFoundException e) {
//            try {
//                clazz = new MethodClassLoader(ClassLoader.getSystemClassLoader()).loadClass("org.cq2.delegator.ComponentMethod" + methodIdentifier + "_" + componentClassIdentifier);
//            } catch (ClassNotFoundException e1) {
//               throw new RuntimeException(e1);
//            }
//        }

        try {
            clazz = ClassGenerator.getClassLoader().loadClass("org.cq2.delegator.ComponentMethod" + methodIdentifier + "_" + componentClassIdentifier);
        } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
        }

        
        ProxyMethod result = null;
        try {
            result = (ProxyMethod) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        componentMethods[methodIdentifier] = result;
        componentReflectMethods[methodIdentifier] = delegateMethod;
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
    }

}