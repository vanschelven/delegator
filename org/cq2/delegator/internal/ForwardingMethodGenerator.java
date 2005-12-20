package org.cq2.delegator.internal;

import java.lang.reflect.Method;

import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

public class ForwardingMethodGenerator extends Generator {

    protected final ClassGen classGen;

    private final InstructionList instrList;

    private final ConstantPoolGen constPool;

    private MethodGen methodGen;

    private Class superclass;

    public ForwardingMethodGenerator(int methodIdentifier, Method method) {
        superclass = ForwardingMethod.class;
        String superclassName = superclass.getName();

        classGen = new ClassGen(superclassName + methodIdentifier,
                superclassName, "", ACC_PUBLIC + ACC_ABSTRACT, new String[] {});
        constPool = classGen.getConstantPool();
        instrList = new InstructionList();
        classGen.addEmptyConstructor(ACC_PUBLIC);
        add_method(method, getArgumentTypes(method), "__invoke_");
        add_method(method, appendIntType(getArgumentTypes(method)), "__offset_");
    }

    public byte[] generate() {
        return classGen.getJavaClass().getBytes();
    }

    private void add_method(Method method, Type[] argumentTypes, String prefix) {
        try {
            int modifiers = (ACC_PUBLIC | ACC_ABSTRACT);
            methodGen = new MethodGen(modifiers, Type.getType(method
                    .getReturnType()),
                    insertSelfType(argumentTypes),
                    insertSelfString(generateParameterNames(argumentTypes.length)), prefix + method.getName(), classGen
                            .getClassName(), instrList, constPool);
            for (int i = 0; i < method.getExceptionTypes().length; i++) {
                methodGen.addException(method.getExceptionTypes()[0].getName());
            }
            classGen.addMethod(methodGen.getMethod());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}