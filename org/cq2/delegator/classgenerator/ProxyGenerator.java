/*
 Copyright (C) 2001 Erik J. Groeneveld, http://www.ejgroeneveld.com
 Copyright (C) 2002, 2003, 2004 Seek You Too B.V. the Netherlands. http://www.cq2.nl 
 */
package org.cq2.delegator.classgenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import org.cq2.delegator.ISelf;
import org.cq2.delegator.method.MethodComparator;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.MethodUtil;

public class ProxyGenerator extends ClassLoader implements Constants {
	private static final ObjectType CLASS = new ObjectType("java.lang.Class");
	private final ClassGen classGen;
	private final Class superClass;
	private final InstructionFactory instrFact;
	private final InstructionList instrList;
	private final ConstantPoolGen constPool;
	private final Class proxyClass;
	private ClassInjector classInjector;
	private MethodFilter methodFilter;

	private ProxyGenerator(ClassLoader classLoader, Class superClass, MethodFilter methodFilter) {
		this.superClass = superClass;
		this.methodFilter = methodFilter;
		this.classInjector = ClassInjector.create(classLoader);
		String superClassName = superClass.getName();
		final String proxyClassName = ProxyGenerator.getProxyClassName(superClass);
		String[] extraInterfaces = new String[]{Component.class.getName(), ISelf.class.getName()};
		classGen = new ClassGen(proxyClassName, superClassName, "", (Modifier.isPublic(superClass
				.getModifiers()) ? ACC_PUBLIC : 0)
				| ACC_SUPER, extraInterfaces);
		constPool = classGen.getConstantPool();
		instrFact = new InstructionFactory(classGen, constPool);
		instrList = new InstructionList();
		addSelfField();
		addDefaultConstructor();
		addMethodsFromSuperClass(extraInterfaces);
		byte[] bytes = classGen.getJavaClass().getBytes();
		try {
			proxyClass = classInjector.inject(classGen.getClassName(), bytes, superClass
					.getProtectionDomain());
		}
		catch (ClassFormatError e) {
			System.out.println("class " + classGen.getClassName() + " {");
			printArray(classGen.getMethods());
			System.out.println("}");
			throw e;
		}
	}

	private void printArray(Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			System.out.println("\t" + objects[i].toString() + ";");
		}
	}

	private static String getProxyClassName(Class superClass) {
		String className = superClass.getName();
		if (superClass.getPackage().getName().startsWith("java.")) {
			return "components$" + className;
		}
		else {
			return className + "$component";
		}
	}

	private Component getInstance(InvocationHandler handler, Object[] ctorArgs) {
		return getInstance(proxyClass, handler, ctorArgs);
	}

	private static Component getInstance(Class proxyClass, InvocationHandler delegate,
			Object[] ctorArgs) {
		try {
			Component proxy;
			if (ctorArgs != null) {
				Constructor ctor = proxyClass.getConstructor(argTypes(ctorArgs));
				proxy = (Component) ctor.newInstance(ctorArgs);
			}
			else {
				proxy = (Component) proxyClass.newInstance();
			}
			Field delegateField = getDelegateField(proxy);
			delegateField.set(proxy, delegate);
			return proxy;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Class[] argTypes(Object[] ctorArgs) {
		Class[] types = new Class[ctorArgs.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = ctorArgs[i].getClass();
		}
		return types;
	}

	public static Field getDelegateField(Component proxy) {
		try {
			return proxy.getClass().getDeclaredField("self");
		}
		catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private void addMethodsFromSuperClass(String[] extraInterfaces) {
		//System.out.println("=============" + superClass);
		Set methods = new TreeSet(new MethodComparator());
		MethodUtil.addMethods(superClass, methods, methodFilter);
		for (int i = 0; i < extraInterfaces.length; i++) {
			try {
				MethodUtil.addMethods(Class.forName(extraInterfaces[i]), methods, methodFilter);
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		//printArray(methods);
		for (Iterator iter = methods.iterator(); iter.hasNext();) {
			Method method = (Method) iter.next();
			int modifiers = method.getModifiers();
			// TODO use MethodFilter
			if (!Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
				//System.out.println("Adding " + method);
				addDelegationMethod(method);
				if (!Modifier.isAbstract(modifiers)) {
					addSuperCallMethod(method);
				}
			}
		}
	}

	private void addSelfField() {
		FieldGen fieldGen = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT, new ObjectType(
				"java.lang.reflect.InvocationHandler"), "self", classGen.getConstantPool());
		classGen.addField(fieldGen.getField());
	}

	private void addDefaultConstructor() {
		MethodGen methodGen = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[]{},
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

	private void addDelegationMethod(Method method) {
		Type returnType = Type.getType(method.getReturnType());
		MethodGen methodGen = addMethodHeader(method, returnType, null);
		createCallToInvocationHandler(method);
		addMethodTrailer(returnType, methodGen);
	}

	private void addSuperCallMethod(Method method) {
		Type returnType = Type.getType(method.getReturnType());
		MethodGen methodGen = addMethodHeader(method, returnType, InvocationHandler.class);
		createBindSelf(1);
		createCallToSuper(method, returnType, 2);
		addMethodTrailer(returnType, methodGen);
	}

	private void createBindSelf(int argNr) {
		createLoadThis();
		// arg 'self'
		instrList.append(InstructionFactory.createLoad(Type.OBJECT, argNr));
		// this._self >= self<
		instrList.append(instrFact.createFieldAccess(classGen.getClassName(), "self",
				new ObjectType("java.lang.reflect.InvocationHandler"), Constants.PUTFIELD));
	}

	private MethodGen addMethodHeader(Method method, Type returnType, Class firstArg) {
		List types = new ArrayList();
		types.addAll(Arrays.asList(getArgumentTypes(method)));
		if (firstArg != null) {
			types.add(0, Type.getType(firstArg));
		}
		MethodGen methodGen = new MethodGen(method.getModifiers()
				& ~(Modifier.NATIVE | Modifier.ABSTRACT), returnType, (Type[]) types
				.toArray(new Type[]{}), generateParameterNames(types.size()), method.getName(),
				classGen.getClassName(), instrList, constPool);
		return methodGen;
	}

	private Type[] getArgumentTypes(Method method) {
		return Type.getArgumentTypes(Type.getSignature(method));
	}

	private void addMethodTrailer(Type returnType, MethodGen methodGen) {
		convertReturnValue(returnType);
		methodGen.setMaxStack();
		methodGen.setMaxLocals();
		classGen.addMethod(methodGen.getMethod());
		instrList.dispose();
	}

	private void convertReturnValue(Type returnType) {
		if (returnType.equals(Type.STRING)) {
			instrList.append(instrFact.createCheckCast(Type.STRING));
		}
		else {
			switch (returnType.getType()) {
				case T_VOID :
					break;
				case T_BOOLEAN :
					convertToPrimitive(returnType, "java.lang.Boolean", "booleanValue");
					break;
				case T_INT :
					convertToPrimitive(returnType, "java.lang.Integer", "intValue");
					break;
				case T_LONG :
					convertToPrimitive(returnType, "java.lang.Long", "longValue");
					break;
				case T_DOUBLE :
					convertToPrimitive(returnType, "java.lang.Double", "doubleValue");
					break;
				case T_FLOAT :
					convertToPrimitive(returnType, "java.lang.Float", "floatValue");
					break;
				case T_BYTE :
					convertToPrimitive(returnType, "java.lang.Byte", "byteValue");
					break;
				case T_SHORT :
					convertToPrimitive(returnType, "java.lang.Short", "shortValue");
					break;
				case T_CHAR :
					convertToPrimitive(returnType, "java.lang.Character", "charValue");
					break;
				case T_ARRAY :
				case T_OBJECT :
					instrList.append(instrFact.createCheckCast((ReferenceType) returnType));
					break;
				default :
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

	private void createCallToInvocationHandler(Method method) {
		createLoadThis();
		// this.>delegate<.invoke( ...
		instrList.append(instrFact.createFieldAccess(classGen.getClassName(), "self",
				new ObjectType("java.lang.reflect.InvocationHandler"), Constants.GETFIELD));
		createLoadThis();
		createLoadThis();
		// ... invoke(proxy, super.>getClass()< , ...
		instrList.append(instrFact.createInvoke("java.lang.Object", "getClass", CLASS,
				Type.NO_ARGS, Constants.INVOKESPECIAL));
		// ... getClass().getMethod(>methodName<, ...
		instrList.append(new PUSH(constPool, method.getName()));
		// ... methodName, >Class[]< ...
		Class[] argTypes = method.getParameterTypes();
		createParameterTypeArray(argTypes);
		// ... getClass().>getMethod(..., ...)< ...
		instrList.append(instrFact.createInvoke("java.lang.Class", "getMethod", new ObjectType(
				"java.lang.reflect.Method"), new Type[]{Type.STRING, new ArrayType(CLASS, 1)},
				Constants.INVOKEVIRTUAL));
		// this.delegate.>invoke(proxy, method, >args<)<;
		createParameterArray(argTypes);
		// this.delegate.>invoke(proxy, method, args)<;
		instrList.append(instrFact.createInvoke("java.lang.reflect.InvocationHandler", "invoke",
				Type.OBJECT, new Type[]{Type.OBJECT, new ObjectType("java.lang.reflect.Method"),
						new ArrayType(Type.OBJECT, 1)}, Constants.INVOKEINTERFACE));
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
			}
			else {
				instrList.append(InstructionFactory.createLoad(Type.OBJECT, stackIndex)); // arg i
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
		}
		else if (c.equals(Long.TYPE)) {
			return createPrimitiveTypeClass("java.lang.Long", Type.LONG, stackIndex);
		}
		else if (c.equals(Short.TYPE)) {
			return createPrimitiveTypeClass("java.lang.Short", Type.SHORT, stackIndex);
		}
		else if (c.equals(Character.TYPE)) {
			return createPrimitiveTypeClass("java.lang.Character", Type.CHAR, stackIndex);
		}
		else if (c.equals(Float.TYPE)) {
			return createPrimitiveTypeClass("java.lang.Float", Type.FLOAT, stackIndex);
		}
		else if (c.equals(Double.TYPE)) {
			return createPrimitiveTypeClass("java.lang.Double", Type.DOUBLE, stackIndex);
		}
		else if (c.equals(Byte.TYPE)) {
			return createPrimitiveTypeClass("java.lang.Byte", Type.BYTE, stackIndex);
		}
		else if (c.equals(Boolean.TYPE)) {
			return createPrimitiveTypeClass("java.lang.Boolean", Type.BOOLEAN, stackIndex);
		}
		else
			return 0;
	}

	private int createPrimitiveTypeClass(String className, BasicType primitiveType, int stackIndex) {
		instrList.append(instrFact.createNew(className));
		instrList.append(InstructionConstants.DUP);
		instrList.append(InstructionFactory.createLoad(primitiveType, stackIndex));
		instrList.append(instrFact.createInvoke(className, "<init>", Type.VOID,
				new Type[]{primitiveType}, Constants.INVOKESPECIAL));
		return primitiveType.getSize();
	}

	private void createParameterTypeArray(Class[] args) {
		// ... methodName, new Class[>nrArgs<] { ... }
		instrList.append(new PUSH(constPool, args.length));
		// ... methodName, >new Class[nrArgs]< { ... }
		instrList.append(instrFact.createNewArray(CLASS, (short) 1));
		// ... new Class[nrArgs] >{ ... }< ...
		for (int i = 0; i < args.length; i++) {
			instrList.append(InstructionConstants.DUP); // array
			// pointer
			instrList.append(new PUSH(constPool, i)); // index
			createClassReference(args[i]);
			instrList.append(InstructionConstants.AASTORE);
		}
	}

	private void createClassReference(Class clazz) {
		if (clazz.equals(Integer.TYPE)) {
			createPrimitiveClassAccess("java.lang.Integer");
		}
		else if (clazz.equals(Boolean.TYPE)) {
			createPrimitiveClassAccess("java.lang.Boolean");
		}
		else if (clazz.equals(Long.TYPE)) {
			createPrimitiveClassAccess("java.lang.Long");
		}
		else if (clazz.equals(Double.TYPE)) {
			createPrimitiveClassAccess("java.lang.Double");
		}
		else if (clazz.equals(Float.TYPE)) {
			createPrimitiveClassAccess("java.lang.Float");
		}
		else if (clazz.equals(Short.TYPE)) {
			createPrimitiveClassAccess("java.lang.Short");
		}
		else if (clazz.equals(Character.TYPE)) {
			createPrimitiveClassAccess("java.lang.Character");
		}
		else if (clazz.equals(Byte.TYPE)) {
			createPrimitiveClassAccess("java.lang.Byte");
		}
		else {
			instrList.append(new PUSH(constPool, clazz.getName()));
			instrList.append(instrFact.createInvoke("java.lang.Class", "forName", new ObjectType(
					"java.lang.Class"), new Type[]{Type.STRING}, Constants.INVOKESTATIC));
		}
	}

	private void createPrimitiveClassAccess(String className) {
		instrList.append(instrFact.createFieldAccess(className, "TYPE", new ObjectType(
				"java.lang.Class"), Constants.GETSTATIC));
	}

	static String[] generateParameterNames(int nr) {
		String[] result = new String[nr];
		for (int i = 0; i < nr; i++) {
			result[i] = "arg" + i;
		}
		return result;
	}

	public static Component newProxyInstance(ClassLoader classLoader, Class theInterface,
			InvocationHandler handler, MethodFilter methodFilter, Object[] ctorArgs) {
		return newComponentInstance(classLoader, theInterface, handler, methodFilter, ctorArgs);
	}

	public static Component newComponentInstance(ClassLoader classLoader, Class theInterface,
			InvocationHandler handler, MethodFilter methodFilter, Object[] ctorArgs) {
		if (theInterface.isInterface()) {
			throw new IllegalArgumentException("Interfaces are not supported.");
		}
		try {
			Class proxyClass = classLoader
					.loadClass(ProxyGenerator.getProxyClassName(theInterface));
			Component instance = ProxyGenerator.getInstance(proxyClass, handler, ctorArgs);
			System.err.println("existing class");
			return instance;
		}
		catch (ClassNotFoundException e) {
			return new ProxyGenerator(classLoader, theInterface, methodFilter).getInstance(handler,
					ctorArgs);
		}
	}

	static Object getInvocationHandler(Component proxy) {
		try {
			return getDelegateField(proxy).get(proxy);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isProxy(Object object) {
		return object instanceof Component;
	}
}