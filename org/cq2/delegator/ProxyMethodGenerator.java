package org.cq2.delegator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class ProxyMethodGenerator implements Constants {

    protected final ClassGen classGen;

    private final InstructionFactory instrFact;

    private final InstructionList instrList;

    private final ConstantPoolGen constPool;

    private MethodGen methodGen;

    private Class superclass;

    public ProxyMethodGenerator(int methodIdentifier, Method method) {
        superclass = ProxyMethod.class;
        String superclassName = superclass.getName();

        classGen = new ClassGen(superclassName + methodIdentifier,
                superclassName, "", ACC_PUBLIC + ACC_ABSTRACT, new String[] {});
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        addDefaultConstructor();
        add_method(method);
    }

    public byte[] generate() {
        return classGen.getJavaClass().getBytes();
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
            int modifiers = (ACC_PUBLIC | ACC_ABSTRACT);
            methodGen = new MethodGen(modifiers, Type.getType(method
                    .getReturnType()),
                    insertSelfType(getArgumentTypes(method)),
                    insertSelfString(generateParameterNames(method
                            .getParameterTypes().length)), "__invoke_" + method.getName(), classGen
                            .getClassName(), instrList, constPool);
            for (int i = 0; i < method.getExceptionTypes().length; i++) {
                methodGen.addException(method.getExceptionTypes()[0].getName());
            }
            classGen.addMethod(methodGen.getMethod());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

}