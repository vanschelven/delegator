/*
 * Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com Copyright
 * (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl
 */

package org.cq2.delegator.classgenerator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Component;
import org.cq2.delegator.ComponentMethodFilter;
import org.cq2.delegator.ISelf;
import org.cq2.delegator.MethodRegister;
import org.cq2.delegator.MiniMethod;
import org.cq2.delegator.MyInvocationHandler;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.method.MethodComparator;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.MethodUtil;
import org.cq2.delegator.method.ProxyMethodFilter;

public abstract class ClassGenerator implements Constants {

    protected static final MethodFilter proxyMethodFilter = new ProxyMethodFilter();

    protected static final MethodFilter componentMethodFilter = new ComponentMethodFilter();

    static class ClassInjector extends ClassLoader {

        public ClassInjector(ClassLoader parent) {
            super(parent);
        }

        private Class inject(String className, byte[] classDef, ProtectionDomain domain) {
            return defineClass(className, classDef, 0, classDef.length, domain);
        }

        public static ClassInjector create() {
            return ClassInjector.create(getSystemClassLoader());
        }

        public static ClassInjector create(ClassLoader parent) {
            if (parent instanceof ClassInjector) {
                return (ClassInjector) parent;
            }
            return new ClassInjector(parent);
        }

        protected Class findClass(String classname) throws ClassNotFoundException {
            if (classname.endsWith("$sharablecomponent")) {
                return injectSharableComponentClass(loadClass(classname.substring(0,
                        classname.length() - 18)));
            } else if (classname.startsWith("sharablecomponent$")) {
                return injectSharableComponentClass(loadClass(classname.substring(18)));
            } else if (classname.endsWith("$component")) {
                return injectComponentClass(loadClass(classname.substring(0,
                            classname.length() - 10)));
            } else if (classname.startsWith("component$")) {
                return injectComponentClass(loadClass(classname.substring(10)));
            } else if (classname.endsWith("$proxy")) {
                return injectProxyClass(loadClass(classname.substring(0, classname.length() - 6)));
            } else if (classname.startsWith("proxy$")) {
                return injectProxyClass(loadClass(classname.substring(6)));
            }
            throw new ClassNotFoundException(classname);
        }

        private Class injectProxyClass(Class clazz) {
            String className = getClassName(clazz, "proxy");
            return injectClass(new ProxyGenerator(className, clazz), clazz, className);
        }

        private Class injectComponentClass(Class clazz) {
            String className = getClassName(clazz, "component");
            return injectClass(new SingleSelfComponentGenerator(className, clazz), clazz, className);
        }
        
        private Class injectSharableComponentClass(Class clazz) {
            String className = getClassName(clazz, "sharablecomponent");
            return injectClass(new SharableComponentGenerator(className, clazz), clazz, className);
        }

        private Class injectClass(ClassGenerator generator, Class clazz, String className) {
            if (clazz.isInterface()) {
                throw new IllegalArgumentException(
                        "Interfaces are not supported, use java.lang.reflect.Proxy.");
            }
            byte[] classDef = generator.generate();
            return inject(className, classDef, clazz.getProtectionDomain());
        }
    }
    
    public abstract byte[] generate();

    private static ClassInjector injector = ClassInjector
            .create(ClassLoader.getSystemClassLoader());

    protected final ClassGen classGen;

    private final InstructionFactory instrFact;

    private final InstructionList instrList;

    private final ConstantPoolGen constPool;

    private Set methods;

    private static Cache componentsClassCache = new Cache("component");

    private static Cache sharableComponentsClassCache = new Cache("sharablecomponent");
    
    private static Cache proxyClassCache = new Cache("proxy");

    public static final String SUPERCALL_POSTFIX = "__super";

    ClassGenerator(String className, Class superClass, Class marker) {
        String[] extraInterfaces = new String[] { marker.getName(), ISelf.class.getName() };
        int modifiers = (Modifier.isPublic(superClass.getModifiers()) ? ACC_PUBLIC : 0) | ACC_SUPER;
        classGen = new ClassGen(className, superClass.getName(), "", modifiers, extraInterfaces);
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        addDefaultConstructor();
        methods = collectMethods(superClass, extraInterfaces);
    }

    static String getClassName(Class superClass, String marker) {
        String className = superClass.getName();
        if (superClass.getPackage() != null && superClass.getPackage().getName().startsWith("java.")) {
            return marker + "$" + className;
        }
        return className + "$" + marker;
    }

    private static Map delegateFieldCache = new HashMap();

    private boolean aaaaa;

    public static Field getDelegateField(Object proxy) {
        try {
            Class clazz = proxy.getClass();
            Object value = delegateFieldCache.get(clazz);
            if (value != null) return (Field) value;
            Field result = proxy.getClass().getDeclaredField("self");
            delegateFieldCache.put(clazz, result);
            return result;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Set collectMethods(Class superClass, String[] extraInterfaces) {
        methods = new TreeSet(new MethodComparator());
        for (int i = 0; i < extraInterfaces.length; i++) {
            try {
                MethodUtil.addMethods(Class.forName(extraInterfaces[i]), methods);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        MethodUtil.addMethods(superClass, methods);
        return methods;
    }

    protected void addDelegationMethods(MethodFilter methodFilter, boolean useSelf) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();
            if (methodFilter.filter(method)) {
                addDelegationMethod(method, useSelf);
            }
        }
    }

    protected void addSuperCallMethods(MethodFilter methodFilter) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();
            int modifiers = method.getModifiers();
            if (methodFilter.filter(method) && !Modifier.isAbstract(modifiers)) {
                addSuperCallMethod(method);
            }
        }
    }

    protected void addSelfField() {
        FieldGen fieldGen = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT, new ObjectType(
                MyInvocationHandler.class.getName()), "self", classGen.getConstantPool());
        classGen.addField(fieldGen.getField());
    }

    private void addDefaultConstructor() {
        MethodGen methodGen = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {},
                "<init>", classGen.getClassName(), instrList, constPool);
        createLoadThis();
        instrList.append(instrFact.createInvoke(classGen.getSuperclassName(), "<init>", Type.VOID,
                Type.NO_ARGS, Constants.INVOKESPECIAL));
        instrList.append(InstructionFactory.createReturn(Type.VOID));
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();
    }

    private void addDelegationMethod(Method method, boolean useSelf) {
        Type returnType = Type.getType(method.getReturnType());
        MethodGen methodGen = addMethodHeader(method, returnType, "", false);
        createCallToInvocationHandler(method, useSelf);
        addMethodTrailer(returnType, methodGen);
    }

    private void addSuperCallMethod(Method method) {
        Type returnType = Type.getType(method.getReturnType());
        MethodGen methodGen = addMethodHeader(method, returnType, SUPERCALL_POSTFIX, true);
        createCallToSuper(method, returnType, 1);
        addMethodTrailer(returnType, methodGen);
    }
    
    private MethodGen addMethodHeader(Method method, Type returnType, String postfix, boolean publicAccessor) {
        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        if (publicAccessor) newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED) | Modifier.PUBLIC;        
        MethodGen methodGen = new MethodGen(newMods, returnType, getArgumentTypes(method), generateParameterNames(method.getParameterTypes().length), method.getName() + postfix,
                classGen.getClassName(), instrList, constPool);
        
        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            methodGen.addException(exceptionTypes[i].getName());
        }
        return methodGen;
    }

    private static Type[] getArgumentTypes(Method method) {
        return Type.getArgumentTypes(Type.getSignature(method));
    }

    private void addMethodTrailer(Type returnType, MethodGen methodGen) {
        if (aaaaa) convertReturnValue(returnType);
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();
    }

    private void convertReturnValue(Type returnType) {
        if (returnType.equals(Type.STRING)) {
            instrList.append(instrFact.createCheckCast(Type.STRING));
        } else {
            switch (returnType.getType()) {
            case T_VOID:
                break;
            case T_BOOLEAN:
                convertToPrimitive(returnType, "java.lang.Boolean", "booleanValue");
                break;
            case T_INT:
                convertToPrimitive(returnType, "java.lang.Integer", "intValue");
                break;
            case T_LONG:
                convertToPrimitive(returnType, "java.lang.Long", "longValue");
                break;
            case T_DOUBLE:
                convertToPrimitive(returnType, "java.lang.Double", "doubleValue");
                break;
            case T_FLOAT:
                convertToPrimitive(returnType, "java.lang.Float", "floatValue");
                break;
            case T_BYTE:
                convertToPrimitive(returnType, "java.lang.Byte", "byteValue");
                break;
            case T_SHORT:
                convertToPrimitive(returnType, "java.lang.Short", "shortValue");
                break;
            case T_CHAR:
                convertToPrimitive(returnType, "java.lang.Character", "charValue");
                break;
            case T_ARRAY:
            case T_OBJECT:
                instrList.append(instrFact.createCheckCast((ReferenceType) returnType));
                break;
            default:
                throw new RuntimeException("Unknown return type: " + returnType);
            }
        }
        instrList.append(InstructionFactory.createReturn(returnType));
    }

    private void convertToPrimitive(Type type, String typeName, String methodName) {
        instrList.append(instrFact.createCheckCast(new ObjectType(typeName)));
        instrList.append(instrFact.createInvoke(typeName, methodName, type, Type.NO_ARGS,
                Constants.INVOKEVIRTUAL));
    }

    private void createCallToSuper(Method method, Type returnType, int stackIndex) {
        createLoadThis();
        Class[] argClasses = method.getParameterTypes();
        for (int i = 0; i < argClasses.length; i++) {
            Type type = Type.getType(argClasses[i]);
            instrList.append(InstructionFactory.createLoad(type, stackIndex));
            stackIndex += type.getSize();
        }
        // this.super.>method<(
        instrList.append(instrFact.createInvoke(method.getDeclaringClass().getName(), method
                .getName(), returnType, getArgumentTypes(method), Constants.INVOKESPECIAL));
        instrList.append(InstructionFactory.createReturn(returnType));
    }

    private void createLoadThis() {
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    }

    private void createCallToInvocationHandler(Method method, boolean useSelf) {
        // createSystemOutPrintln(method.toString());
         createLoadThis();
         if (useSelf) {
             // this.>delegate<.invoke( ...
             instrList.append(instrFact.createFieldAccess(classGen.getClassName(), "self",
                     new ObjectType(MyInvocationHandler.class.getName()), Constants.GETFIELD));
         } else {
             // ((Stack)(org.cq2.delegator.Self.self.get()).peek()
             instrList.append(instrFact.createFieldAccess("org.cq2.delegator.Self", "self",
                     new ObjectType("java.lang.ThreadLocal"), Constants.GETSTATIC));
             instrList.append(instrFact.createInvoke("java.lang.ThreadLocal", "get", Type.OBJECT,
                     Type.NO_ARGS, Constants.INVOKEVIRTUAL));
             instrList.append(instrFact.createCheckCast(new ObjectType("java.util.Stack")));
             instrList.append(instrFact.createInvoke("java.util.Stack", "peek", Type.OBJECT,
                     Type.NO_ARGS, Constants.INVOKEVIRTUAL));
         }
         
         createLoadThis();
         instrList.append(new PUSH(constPool, MethodRegister.getInstance().getIdentifier(new MiniMethod(method))));
         if ((method.getReturnType() != int.class || method.getParameterTypes().length != 0) ||
                 (isSpecialMethod(method.getName()))) {
             createParameterArray(method.getParameterTypes());

             // this.delegate.>invoke(...., ........, args)<;
             instrList.append(instrFact.createInvoke(MyInvocationHandler.class.getName(), "invoke",
                     Type.OBJECT, new Type[] { Type.OBJECT, Type.INT, new ArrayType(Type.OBJECT, 1) }, Constants.INVOKEINTERFACE));
             aaaaa = true;
         } else {
             aaaaa = false;
             instrList.append(instrFact.createInvoke(MyInvocationHandler.class.getName(), "i_invoke",
                     Type.INT, new Type[]{ Type.OBJECT, Type.INT }, Constants.INVOKEINTERFACE));
             instrList.append(InstructionFactory.createReturn(Type.getType(method.getReturnType())));
         }             
    }
    
    //TODO is dit echt nodig?
    private boolean isSpecialMethod(String name) {
        if (name.startsWith("__next__")) return true;
        if (name.equals("equals")) return true;
        if (name.equals("hashCode")) return true;
        return false;
    }

    private void createParameterArray(Class[] argTypes) {
        instrList.append(new PUSH(constPool, argTypes.length)); // array
        // size
        instrList.append(instrFact.createNewArray(Type.OBJECT, (short) 1));
        int stackIndex = 1;
        for (int i = 0; i < argTypes.length; i++) {
            Class c = argTypes[i];
            instrList.append(InstructionConstants.DUP); // array
            // ptr
            instrList.append(new PUSH(constPool, i)); // array
            // index
            if (c.isPrimitive()) {
                stackIndex += convertPrimitiveToObject(c, stackIndex);
            } else {
                instrList.append(InstructionFactory.createLoad(Type.OBJECT, stackIndex)); // arg
                // i
                stackIndex += 1;
            }
            instrList.append(InstructionConstants.AASTORE); // set
            // array
            // element
        }
    }

    private int convertPrimitiveToObject(Class c, int stackIndex) {
        if (c.equals(Integer.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Integer", Type.INT, stackIndex);
        } else if (c.equals(Long.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Long", Type.LONG, stackIndex);
        } else if (c.equals(Short.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Short", Type.SHORT, stackIndex);
        } else if (c.equals(Character.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Character", Type.CHAR, stackIndex);
        } else if (c.equals(Float.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Float", Type.FLOAT, stackIndex);
        } else if (c.equals(Double.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Double", Type.DOUBLE, stackIndex);
        } else if (c.equals(Byte.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Byte", Type.BYTE, stackIndex);
        } else if (c.equals(Boolean.TYPE)) {
            return createPrimitiveTypeClass("java.lang.Boolean", Type.BOOLEAN, stackIndex);
        } else
            return 0;
    }

    private int createPrimitiveTypeClass(String className, BasicType primitiveType, int stackIndex) {
        instrList.append(instrFact.createNew(className));
        instrList.append(InstructionConstants.DUP);
        instrList.append(InstructionFactory.createLoad(primitiveType, stackIndex));
        instrList.append(instrFact.createInvoke(className, "<init>", Type.VOID,
                new Type[] { primitiveType }, Constants.INVOKESPECIAL));
        return primitiveType.getSize();
    }

    static String[] generateParameterNames(int nr) {
        String[] result = new String[nr];
        for (int i = 0; i < nr; i++) {
            result[i] = "arg" + i;
        }
        return result;
    }

    public static Proxy newProxyInstance(Class clazz, MyInvocationHandler handler) {
        try {
            Class proxyClass = proxyClassCache.getClass(injector, clazz);
            Object proxy = proxyClass.newInstance();
            getDelegateField(proxy).set(proxy, handler);
            return (Proxy) proxy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Component newComponentInstance(Class clazz, MyInvocationHandler handler) {
        try {
            Class componentClass = componentsClassCache.getClass(injector, clazz);
            Component result = (Component) componentClass.newInstance();
            getDelegateField(result).set(result, handler);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Component newSharableComponentInstance(Class clazz) {
        try {
            Class componentClass = sharableComponentsClassCache.getClass(injector, clazz);
            Component result = (Component) componentClass.newInstance();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class Cache {

        private Map loaders = new HashMap();
        private final String postFix;

        public Cache(String postFix) {
            this.postFix = postFix;
        }

        Class getClass(ClassLoader loader, Class clazz) throws ClassNotFoundException {
            Map cache = (Map) loaders.get(loader);
            if (cache == null) {
                cache = new HashMap();
                loaders.put(loader, cache);
            }
            Class componentClass = (Class) cache.get(clazz);
            if (componentClass == null) {
                componentClass = loader.loadClass(getClassName(clazz, postFix));
                cache.put(clazz, componentClass);
            }
            return componentClass;
        }
    }

    public static MyInvocationHandler getInvocationHandler(Object proxy) {
        try {
            return (MyInvocationHandler) getDelegateField(proxy).get(proxy);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isProxy(Object object) {
        return object instanceof Proxy;
    }

    public static boolean isComponent(Object object) {
        return object instanceof Component;
    }

    private static Map injectorCache = new HashMap();

    public static ClassLoader configureClassLoader(ClassLoader loader) {
        injector = (ClassInjector) injectorCache.get(loader);
        if (injector == null) {
            injector = ClassInjector.create(loader);
            injectorCache.put(loader, injector);
        }
        return injector;
    }

    public static ClassLoader getClassLoader() {
        return injector;
    }
}