/*
 * Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com Copyright
 * (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl
 */

package org.cq2.delegator.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Component;
import org.cq2.delegator.DelegatorException;
import org.cq2.delegator.ISelf;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.Self;
import org.cq2.delegator.method.ForwardingMethodFilter;
import org.cq2.delegator.method.MethodComparator;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.MethodUtil;

public abstract class ClassGenerator extends Generator {

    protected static final MethodFilter forwardingMethodFilter = new ForwardingMethodFilter();

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
            String prefix = ForwardingMethod.class.getName();
            if (classname.startsWith(prefix) && !classname.equals(prefix)) {
                String postfix = classname.substring(prefix.length());
                int identifier = Integer.parseInt(postfix);
                byte[] bytes = new ForwardingMethodGenerator(identifier,
                        ForwardingMethodRegister.getInstance().getMethod(identifier))
                        .generate();
                return defineClass(classname, bytes, 0, bytes.length);
            }
            prefix = "org.cq2.delegator.ComponentMethod";
            if (classname.startsWith(prefix) && !classname.equals(prefix)) {
                String postfix = classname.substring(prefix.length());
                int methodIdentifier = Integer.parseInt(postfix.substring(0,
                        postfix.indexOf('_')));
                int componentIdentifier = Integer.parseInt(postfix.substring(
                        postfix.indexOf('_') + 1, postfix.length()));
                byte[] bytes = new ImplementingMethodGenerator(methodIdentifier,
                        componentIdentifier).generate();
                return defineClass(classname, bytes, 0, bytes.length);
            }
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

    private Class superClass;

    ClassGenerator(String className, Class superClass, Class marker) {
        String[] extraInterfaces;
        if (superClass.isInterface()) {
            this.superClass = Object.class;
            extraInterfaces = new String[] { superClass.getName(), marker.getName(),
                    ISelf.class.getName() };
        } else {
            this.superClass = superClass;
            extraInterfaces = new String[] { marker.getName(),
                    ISelf.class.getName() };
        }
        
        int modifiers = (Modifier.isPublic(this.superClass.getModifiers()) ? ACC_PUBLIC
                : 0)
                | ACC_SUPER;
        classGen = new ClassGen(className, this.superClass.getName(), "", modifiers,
                extraInterfaces);
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        classGen.addEmptyConstructor(ACC_PUBLIC);
        methods = collectMethods(this.superClass, extraInterfaces);
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

    private MethodGen methodGen;

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

    private void addDelegationMethod(Method method, boolean useSelf) {
        Type returnType = Type.getType(method.getReturnType());
        addMethodHeader(method, returnType, "", false);
        createCallToInvocationHandler(method, useSelf);
        addMethodTrailer(returnType);
    }

    private void addSuperCallMethod(Method method) {
        Type returnType = Type.getType(method.getReturnType());
        addMethodHeader(method, returnType, SUPERCALL_POSTFIX, true);
        createCallToSuper(method, returnType, 1);
        addMethodTrailer(returnType);
    }

    private void addMethodHeader(Method method, Type returnType,
            String postfix, boolean publicAccessor) {
        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        if (publicAccessor)
            newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                    | Modifier.PUBLIC;
        methodGen = new MethodGen(newMods, returnType,
                getArgumentTypes(method), generateParameterNames(method
                        .getParameterTypes().length), method.getName()
                        + postfix, classGen.getClassName(), instrList,
                constPool);

        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            methodGen.addException(exceptionTypes[i].getName());
        }
    }

    private void addMethodTrailer(Type returnType) {
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();
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

    private void createCallToInvocationHandler(Method method, boolean useSelf) {
        // this.self.composedClass.getMethod(identifier)).invoke(self,
        // [...args...]);
        LocalVariableGen nextMethodComponentOffset = methodGen.addLocalVariable("nextMethodOffset", Type.INT, null, null);
        boolean nextMethod = false;
        String name = method.getName();
        if (name.startsWith("__next__")) {
            nextMethod = true;
            name = name.substring(8);
            try {
                method = superClass.getDeclaredMethod(name, method.getParameterTypes());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        LocalVariableGen stackLocal = null;

        createLoadThis();

        // ((Stack)(org.cq2.delegator.Self.self.get())
        instrList.append(instrFact.createFieldAccess(
                "org.cq2.delegator.Self", "self", new ObjectType(
                        "java.lang.ThreadLocal"), Constants.GETSTATIC));
        instrList.append(instrFact.createInvoke("java.lang.ThreadLocal",
                "get", Type.OBJECT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
        instrList.append(instrFact.createCheckCast(new ObjectType(
                "java.util.Stack")));

        stackLocal = methodGen.addLocalVariable("stack", Type
                .getType(Stack.class), null, null);
        instrList.append(InstructionFactory.createStore(Type.OBJECT,
                stackLocal.getIndex()));

        if (useSelf) {
            // this.>delegate<.invoke( ...
            instrList.append(instrFact.createFieldAccess(classGen
                    .getClassName(), "self", new ObjectType(Self.class
                    .getName()), Constants.GETFIELD));
        } else {
            instrList.append(InstructionFactory.createLoad(Type
                    .getType(Stack.class), stackLocal.getIndex()));
            instrList.append(instrFact.createInvoke("java.util.Stack", "peek",
                    Type.OBJECT, Type.NO_ARGS, Constants.INVOKEVIRTUAL));
            instrList.append(instrFact.createCheckCast(new ObjectType(
                    Self.class.getName())));
        }
        //store self locally
        LocalVariableGen selfLocal = methodGen.addLocalVariable("self",
                new ObjectType(Self.class.getName()), null, null);

        instrList.append(InstructionFactory.createDup(1));
        instrList.append(InstructionFactory.createStore(Type.OBJECT, selfLocal
                .getIndex()));
        
        //obtain monitor on self
        instrList.append(InstructionFactory.createDup(1));
        instrList.append(InstructionConstants.MONITORENTER);

        //stack.push(self);
        instrList.append(InstructionFactory.createLoad(Type
                .getType(Stack.class), stackLocal.getIndex()));
        instrList.append(InstructionFactory.createLoad(Type
                .getType(Self.class), selfLocal.getIndex()));
        instrList.append(instrFact.createInvoke("java.util.Stack", "push",
                Type.OBJECT, new Type[] { Type.OBJECT },
                Constants.INVOKEVIRTUAL));
        instrList.append(InstructionConstants.POP);

        //get composedClass
        instrList.append(instrFact.createGetField(Self.class.getName(),
                "composedClass", Type.getType(ComposedClass.class)));
        if (nextMethod) {
            instrList.append(InstructionFactory.createLoad(Type
                    .getType(Self.class), selfLocal.getIndex()));
           
            instrList.append(InstructionFactory.createThis());
            instrList.append(instrFact.createInvoke(Self.class.getName(),
                    "getComponentIndex", Type.INT,
                    new Type[] { Type.OBJECT }, INVOKEVIRTUAL));
            instrList.append(InstructionFactory.createDup(1));
            instrList.append(InstructionFactory.createStore(Type.INT, nextMethodComponentOffset.getIndex()));
            instrList.append(instrFact.createInvoke(ComposedClass.class.getName(), "getSuffix", Type.getType(ComposedClass.class), new Type[]{Type.INT}, INVOKEVIRTUAL));
        }
        //load identifier
        int identifier = ForwardingMethodRegister.getInstance().getMethodIdentifier(
                method);
        instrList.append(new PUSH(constPool, identifier));
        //call method "getMethod"
        instrList.append(instrFact.createInvoke(ComposedClass.class.getName(),
                "getMethod", Type.getType(ForwardingMethod.class),
                new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
        //cast to ForwardingMethod_[identifier]

        String forwardingMethodSignature = "Lorg/cq2/delegator/internal/ForwardingMethod"
                + identifier + ";";
        instrList.append(instrFact.createCheckCast((ReferenceType) Type
                .getType(forwardingMethodSignature)));
        //load self again...
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, selfLocal
                .getIndex()));
        
        //exit monitor
        instrList.append(InstructionFactory.createDup(1));
        instrList.append(InstructionConstants.MONITOREXIT);
        //load paramters

        final int SKIP_THIS_POINTER = 1;
        int pointer = SKIP_THIS_POINTER;
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            instrList.append(InstructionFactory.createLoad(Type.getType(method
                    .getParameterTypes()[i]), pointer));
            pointer += Type.getType(method.getParameterTypes()[i]).getSize();
        }

        //call invoke
        if (!nextMethod) {
            instrList.append(instrFact.createInvoke("org.cq2.delegator.internal.ForwardingMethod"
                + identifier, "__invoke_" + name, Type.getType(method
                .getReturnType()), insertSelfType(getArgumentTypes(method)),
                Constants.INVOKEVIRTUAL));
        } else {
	        instrList.append(InstructionFactory.createLoad(Type.INT, nextMethodComponentOffset.getIndex()));
            instrList.append(instrFact.createInvoke("org.cq2.delegator.internal.ForwardingMethod"
	                + identifier, "__offset_" + name, Type.getType(method
	                .getReturnType()), appendIntType(insertSelfType(getArgumentTypes(method))),
	                Constants.INVOKEVIRTUAL));
        }

	    //stack.pop();
        instrList.append(InstructionFactory.createLoad(Type
                .getType(Stack.class), stackLocal.getIndex()));
        instrList.append(instrFact.createInvoke("java.util.Stack", "pop",
                Type.OBJECT, new Type[] {}, Constants.INVOKEVIRTUAL));
        instrList.append(InstructionConstants.POP);

        //return
        instrList.append(InstructionFactory.createReturn(Type.getType(method
                .getReturnType())));

        //Het is de vraag wat er gebeurt met de stack als je een exceptie krijgt hier - door het ontbreken van een finally is dit een memoryleak, maar heeft verder geen desastreuse gevolgen.
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
            Component result;
            try {
                result = (Component) componentClass.newInstance();
            } catch (NoSuchMethodError e) {
                throw new DelegatorException("Class " + componentClass + " cannot be instantiated, for lack of an empty constructor", e);
            }
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