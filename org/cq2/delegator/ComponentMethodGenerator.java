package org.cq2.delegator;

import java.lang.reflect.Method;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;

public class ComponentMethodGenerator implements Constants {

    protected final ClassGen classGen;

    private final InstructionFactory instrFact;

    private final InstructionList instrList;

    private final ConstantPoolGen constPool;

    private MethodGen methodGen;

    private Class superclass;

    private int unique = 0;

    private final Class componentClass;

    private int getUnique() {
        return unique++;
    }

    public ComponentMethodGenerator(Class superclass, Class componentClass,
            Method delegateMethod) {
        this.superclass = superclass;
        this.componentClass = componentClass;
        String superclassName = superclass.getName();

        classGen = new ClassGen(superclassName + "_" + getUnique(),
                superclassName, "", ACC_PUBLIC & ~ACC_ABSTRACT, new String[] {});
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        addDefaultConstructor();
        //classGen.addEmptyConstructor(ACC_PUBLIC);
        add_method(delegateMethod);
    }

    public Class generate() {
        ClassLoader parentClassLoader = superclass.getClassLoader();
        if (parentClassLoader == null)
            parentClassLoader = ClassLoader.getSystemClassLoader();
        try {
            return new SingleNamedClassLoader("MethodWrapperYY", classGen
                    .getJavaClass().getBytes(), parentClassLoader).loadClass();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

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

    private void createLoadThis() {
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
    }

    private void add_method(Method method) {
        try {
            Type returnType = Type.getType(method.getReturnType());
            Type[] parameterTypes = insertSelfType(getArgumentTypes(method));

            Method superMethod = superclass.getDeclaredMethod("invoke",
                    insertSelfClass(method.getParameterTypes()));
            methodGen = new MethodGen(superMethod.getModifiers()
                    & ~ACC_ABSTRACT, returnType, parameterTypes,
                    insertSelfString(generateParameterNames(method
                            .getParameterTypes().length)), "invoke", classGen
                            .getClassName(), instrList, constPool);

//            instrList.append(InstructionFactory.createLoad(Type.OBJECT, 1));
//            instrList.append(instrFact.createCheckCast((ReferenceType) Type
//                    .getType(componentClass)));
//            instrList.append(instrFact.createInvoke(componentClass.getName(),
//                    method.getName(), returnType, parameterTypes,
//                    Constants.INVOKEVIRTUAL));
            instrList.append(instrFact.createConstant(new Integer(666)));
            instrList.append(InstructionFactory.createReturn(returnType));

            methodGen.setMaxStack();
            methodGen.setMaxLocals();

            classGen.addMethod(methodGen.getMethod());

            instrList.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class[] insertSelfClass(Class[] input) {
        Class[] result = new Class[input.length + 1];
        result[0] = Self.class;
        System.arraycopy(input, 0, result, 1, input.length);
        return result;
    }

    //copy paste - classGenerator
    private static String[] generateParameterNames(int nr) {
        String[] result = new String[nr];
        for (int i = 0; i < nr; i++) {
            result[i] = "arg" + i;
        }
        return result;
    }

    private static String[] insertSelfString(String[] input) {
        String[] result = new String[input.length + 1];
        result[0] = "self";
        System.arraycopy(input, 0, result, 1, input.length);
        return result;
    }

    private static Type[] getArgumentTypes(Method method) {
        return Type.getArgumentTypes(Type.getSignature(method));
    }

    private static Type[] insertSelfType(Type[] input) {
        Type[] result = new Type[input.length + 1];
        result[0] = Type.getType(Self.class);
        System.arraycopy(input, 0, result, 1, input.length);
        return result;
    }

    public static ProxyMethod getComponentMethodInstance(Class superclass,
            Class componentClass, Method delegateMethod, int componentIndex) {
        Class clazz = new ComponentMethodGenerator(superclass, componentClass,
                delegateMethod).generate();
        try {
            return (ProxyMethod) clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}