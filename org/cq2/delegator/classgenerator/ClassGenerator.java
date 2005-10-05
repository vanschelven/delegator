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
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Component;
import org.cq2.delegator.ComponentMethodFilter;
import org.cq2.delegator.ComponentMethodGenerator;
import org.cq2.delegator.ComposedClass;
import org.cq2.delegator.ISelf;
import org.cq2.delegator.MethodRegister;
import org.cq2.delegator.MiniMethod;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.ProxyMethod;
import org.cq2.delegator.ProxyMethodGenerator;
import org.cq2.delegator.ProxyMethodRegister;
import org.cq2.delegator.Self;
import org.cq2.delegator.method.MethodComparator;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.MethodUtil;
import org.cq2.delegator.method.ProxyMethodFilter;

import com.sun.org.apache.bcel.internal.generic.LoadInstruction;

public abstract class ClassGenerator implements Constants {

    protected static final MethodFilter proxyMethodFilter = new ProxyMethodFilter();

    protected static final MethodFilter componentMethodFilter = new ComponentMethodFilter();

    static class ClassInjector extends ClassLoader {

        private ClassInjector(ClassLoader parent) {
            super(parent);
        }

        private Class inject(String className, byte[] classDef,
                ProtectionDomain domain) {
            return defineClass(className, classDef, 0, classDef.length, domain);
        }

        public static ClassLoader create() {
            return ClassInjector.create(getSystemClassLoader());
        }

        public static ClassLoader create(ClassLoader parent) {
            if (parent instanceof ClassInjector) {
                return (ClassInjector) parent;
            }
            return new ClassInjector(parent);
        }

        protected Class findClass(String classname)
                throws ClassNotFoundException {
            if (classname.endsWith("$sharablecomponent")) {
                return injectSharableComponentClass(loadClass(classname
                        .substring(0, classname.length() - 18)));
            } else if (classname.startsWith("sharablecomponent$")) {
                return injectSharableComponentClass(loadClass(classname
                        .substring(18)));
            } else if (classname.endsWith("$component")) {
                return injectComponentClass(loadClass(classname.substring(0,
                        classname.length() - 10)));
            } else if (classname.startsWith("component$")) {
                return injectComponentClass(loadClass(classname.substring(10)));
            } else if (classname.endsWith("$proxy")) {
                return injectProxyClass(loadClass(classname.substring(0,
                        classname.length() - 6)));
            } else if (classname.startsWith("proxy$")) {
                return injectProxyClass(loadClass(classname.substring(6)));
            }
            String prefix = "org.cq2.delegator.ProxyMethod";
            if (classname.startsWith(prefix) && !classname.equals(prefix)) {
                String postfix = classname.substring(prefix.length());
                int identifier = Integer.parseInt(postfix);
                byte[] bytes = new ProxyMethodGenerator(identifier,
                        ProxyMethodRegister.getInstance().getMethod(identifier))
                        .generate();
                return defineClass(classname, bytes, 0, bytes.length);
            }
            prefix = "org.cq2.delegator.ComponentMethod";
            if (classname.startsWith(prefix) && !classname.equals(prefix)) {
                String postfix = classname.substring(prefix.length());
                int methodIdentifier = Integer.parseInt(postfix.substring(0, postfix.indexOf('_')));
                int componentIdentifier = Integer.parseInt(postfix.substring(postfix.indexOf('_') + 1, postfix.length()));
                byte[] bytes =  new ComponentMethodGenerator(methodIdentifier, componentIdentifier).generate();
                return defineClass(classname, bytes, 0, bytes.length);
            }
         //   System.out.println("fail");
            throw new ClassNotFoundException(classname);
        }

        private Class injectProxyClass(Class clazz) {
            String className = getClassName(clazz, "proxy");
            return injectClass(new ProxyGenerator(className, clazz), clazz,
                    className);
        }

        private Class injectComponentClass(Class clazz) {
            String className = getClassName(clazz, "component");
            return injectClass(new SingleSelfComponentGenerator(className,
                    clazz), clazz, className);
        }

        private Class injectSharableComponentClass(Class clazz) {
            String className = getClassName(clazz, "sharablecomponent");
            return injectClass(
                    new SharableComponentGenerator(className, clazz), clazz,
                    className);
        }

        private Class injectClass(ClassGenerator generator, Class clazz,
                String className) {
            if (clazz.isInterface()) {
                throw new IllegalArgumentException(
                        "Interfaces are not supported, use java.lang.reflect.Proxy.");
            }
            byte[] classDef = generator.generate();
            return inject(className, classDef, clazz.getProtectionDomain());
        }
    }

    public abstract byte[] generate();

    private static ClassLoader injector = ClassInjector.create(ClassLoader
            .getSystemClassLoader());

    protected final ClassGen classGen;

    private final InstructionFactory instrFact;

    private final InstructionList instrList;

    private final ConstantPoolGen constPool;

    private Set methods;

    private static Cache componentsClassCache = new Cache("component");

    private static Cache sharableComponentsClassCache = new Cache(
            "sharablecomponent");

    private static Cache proxyClassCache = new Cache("proxy");

    public static final String SUPERCALL_POSTFIX = "__super";

    ClassGenerator(String className, Class superClass, Class marker) {
        String[] extraInterfaces = new String[] { marker.getName(),
                ISelf.class.getName() };
        int modifiers = (Modifier.isPublic(superClass.getModifiers()) ? ACC_PUBLIC
                : 0)
                | ACC_SUPER;
        classGen = new ClassGen(className, superClass.getName(), "", modifiers,
                extraInterfaces);
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        addDefaultConstructor();
        methods = collectMethods(superClass, extraInterfaces);
    }

    static String getClassName(Class superClass, String marker) {
        String className = superClass.getName();
        if (superClass.getPackage() != null
                && superClass.getPackage().getName().startsWith("java.")) {
            return marker + "$" + className;
        }
        return className + "$" + marker;
    }

    private static Map delegateFieldCache = new HashMap();

    public static Field getDelegateField(Object proxy) {
        try {
            Class clazz = proxy.getClass();
            Object value = delegateFieldCache.get(clazz);
            if (value != null)
                return (Field) value;
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
                MethodUtil.addMethods(Class.forName(extraInterfaces[i]),
                        methods);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        MethodUtil.addMethods(superClass, methods);
        return methods;
    }

    protected void addDelegationMethods(MethodFilter methodFilter,
            boolean useSelf) {
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
        FieldGen fieldGen = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT,
                new ObjectType(Self.class.getName()), "self", classGen
                        .getConstantPool());
        classGen.addField(fieldGen.getField());
    }

    private void addDefaultConstructor() {
        MethodGen methodGen = new MethodGen(ACC_PUBLIC, Type.VOID,
                Type.NO_ARGS, new String[] {}, "<init>", classGen
                        .getClassName(), instrList, constPool);
        createLoadThis();
        instrList.append(instrFact.createInvoke(classGen.getSuperclassName(),
                "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
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
        MethodGen methodGen = addMethodHeader(method, returnType,
                SUPERCALL_POSTFIX, true);
        createCallToSuper(method, returnType, 1);
        addMethodTrailer(returnType, methodGen);
    }

    private MethodGen addMethodHeader(Method method, Type returnType,
            String postfix, boolean publicAccessor) {
        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        if (publicAccessor)
            newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                    | Modifier.PUBLIC;
        MethodGen methodGen = new MethodGen(newMods, returnType,
                getArgumentTypes(method), generateParameterNames(method
                        .getParameterTypes().length), method.getName()
                        + postfix, classGen.getClassName(), instrList,
                constPool);

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
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
     //   printMethod(methodGen);
        addFakeLineNumbers(methodGen);
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();
    }
    
    private void printMethod(MethodGen methodGen) {
        System.out.println("code of " + methodGen.getName());
        InstructionHandle[] instructionHandles = methodGen.getInstructionList().getInstructionHandles();
        for (int i = 0; i < instructionHandles.length; i++) {
            System.out.println(i + ": " + instructionHandles[i]);
        }
    }
    
    private void addFakeLineNumbers(MethodGen methodGen) {
        InstructionHandle[] instructionHandles = methodGen.getInstructionList().getInstructionHandles();
        for (int i = 0; i < instructionHandles.length; i++) {
            methodGen.addLineNumber(instructionHandles[i], i);
        }
    }

    private void createCallToSuper(Method method, Type returnType,
            int stackIndex) {
        createLoadThis();
        Class[] argClasses = method.getParameterTypes();
        for (int i = 0; i < argClasses.length; i++) {
            Type type = Type.getType(argClasses[i]);
            instrList.append(InstructionFactory.createLoad(type, stackIndex));
            stackIndex += type.getSize();
        }
        // this.super.>method<(
        instrList.append(instrFact.createInvoke(method.getDeclaringClass()
                .getName(), method.getName(), returnType,
                getArgumentTypes(method), Constants.INVOKESPECIAL));
        instrList.append(InstructionFactory.createReturn(returnType));
    }

    private void createLoadThis() {
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    }

    //TODO refactor gelijkenissen tussen de verschillende code generators
    private void createCallToInvocationHandler(Method method, boolean useSelf) {

        //System.out.println(method);
        //instrList.append(instrFact.createPrintln(method.toString()));

        //((ProxyMethod_[identifier])
        // this.self.composedClass.getMethod(identifier)).invoke(self,
        // [...args...]);
        createLoadThis();
        if (useSelf) {
            // this.>delegate<.invoke( ...
            instrList.append(instrFact.createFieldAccess(classGen
                    .getClassName(), "self", new ObjectType(Self.class
                    .getName()), Constants.GETFIELD));
        } else {
            // ((Stack)(org.cq2.delegator.Self.self.get()).peek()
            instrList.append(instrFact.createFieldAccess(
                    "org.cq2.delegator.Self", "self", new ObjectType(
                            "java.lang.ThreadLocal"), Constants.GETSTATIC));
            instrList.append(instrFact.createInvoke("java.lang.ThreadLocal",
                    "get", Type.OBJECT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
            instrList.append(instrFact.createCheckCast(new ObjectType(
                    "java.util.Stack")));
            instrList.append(instrFact.createInvoke("java.util.Stack", "peek",
                    Type.OBJECT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        }
        //store self locally
        instrList.append(InstructionFactory.createDup(1));
        instrList.append(InstructionFactory.createStore(Type.OBJECT, method.getParameterTypes().length + 2));

        //get composedClass Field -- done
        instrList.append(instrFact.createGetField(Self.class.getName(),
                "composedClass", Type.getType(ComposedClass.class)));
        //load identifier --done
        int identifier = ProxyMethodRegister.getInstance().getMethodIdentifier(
                method);
        instrList.append(new PUSH(constPool, identifier));
        //call method "getMethod" 
        instrList.append(instrFact.createInvoke(ComposedClass.class.getName(),
                "getMethod", Type.getType(ProxyMethod.class), new Type[] { Type.INT },
                Constants.INVOKEVIRTUAL));
        //cast to ProxyMethod_[identifier] 

        String proxyMethodSignature = "Lorg/cq2/delegator/ProxyMethod"
                + identifier + ";";
        instrList.append(instrFact.createCheckCast((ReferenceType) Type
                .getType(proxyMethodSignature)));
        //load self again...
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, method.getParameterTypes().length + 2));
        //load paramters
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            instrList.append(InstructionFactory.createLoad(Type.getType(method
                    .getParameterTypes()[i]), i + 1));
        }
        //call invoke
        instrList.append(instrFact.createInvoke("org.cq2.delegator.ProxyMethod" + identifier, "__invoke_" + method.getName(),
                Type.getType(method.getReturnType()),
                insertSelfType(getArgumentTypes(method)),
                Constants.INVOKEVIRTUAL));
        //return
        instrList.append(InstructionFactory.createReturn(Type.getType(method
                .getReturnType())));
    }

    private static Type[] insertSelfType(Type[] input) {
        Type[] result = new Type[input.length + 1];
        result[0] = Type.getType(Self.class);
        System.arraycopy(input, 0, result, 1, input.length);
        return result;
    }

    static String[] generateParameterNames(int nr) {
        String[] result = new String[nr];
        for (int i = 0; i < nr; i++) {
            result[i] = "arg" + i;
        }
        return result;
    }

    public static Proxy newProxyInstance(Class clazz, Self self) {
        try {
            Class proxyClass = proxyClassCache.getClass(injector, clazz);
            Object proxy = proxyClass.newInstance();
            getDelegateField(proxy).set(proxy, self);
            return (Proxy) proxy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Component newComponentInstance(Class clazz, Self self) {
        try {
            Class componentClass = componentsClassCache.getClass(injector,
                    clazz);
            Component result = (Component) componentClass.newInstance();
            getDelegateField(result).set(result, self);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Component newSharableComponentInstance(Class clazz) {
        try {
            Class componentClass = sharableComponentsClassCache.getClass(
                    injector, clazz);
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

        Class getClass(ClassLoader loader, Class clazz)
                throws ClassNotFoundException {
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

    public static Self getSelf(Object proxy) {
        try {
            return (Self) getDelegateField(proxy).get(proxy);
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