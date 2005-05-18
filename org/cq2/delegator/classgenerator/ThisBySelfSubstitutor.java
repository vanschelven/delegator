package org.cq2.delegator.classgenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Self;

public class ThisBySelfSubstitutor implements Constants {

    private InstructionFactory instructionFactory;

    private ConstantPoolGen constPool;

    private ClassGen classGen;

    public ThisBySelfSubstitutor(ClassGen classGen, ConstantPoolGen constPool,
            InstructionFactory instructionFactory) {
        this.classGen = classGen;
        this.constPool = constPool;
        this.instructionFactory = instructionFactory;
    }

    public org.apache.bcel.classfile.Method generateMethod(Method method) {
        String superclassName = classGen.getSuperclassName();
        JavaClass superJavaClass = Repository.lookupClass(superclassName);

        Type returnType = Type.getType(method.getReturnType());

        List types = new ArrayList();
        types.add(0, Type.getType(Self.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));

        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                | Modifier.PUBLIC;

        org.apache.bcel.classfile.Method superClassMethod = superJavaClass
                .getMethod(method);

        InstructionList superClassInstructionList = new InstructionList(
                superClassMethod.getCode().getCode());
        InstructionList myInstrList;
        if (containsLoadInstruction(superClassInstructionList)) {
            myInstrList = new InstructionList();
            appendLoadSelf(myInstrList, superclassName);
            myInstrList.append(InstructionFactory.createReturn(new ObjectType(
                    superclassName)));
        } else {
            myInstrList = superClassInstructionList;
        }

        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList,
                constPool);

        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        return methodGen.getMethod();
    }

    private void appendLoadSelf(InstructionList myInstrList, String className) {
        myInstrList.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        myInstrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        myInstrList.append(instructionFactory.createInvoke(Object.class
                .getName(), "getClass", new ObjectType(Class.class
                .getName()), new Type[] {}, INVOKEVIRTUAL));
        myInstrList.append(instructionFactory.createInvoke(Self.class
                .getName(), "cast", Type.OBJECT,
                new Type[] { new ObjectType(Class.class.getName()) },
                INVOKEVIRTUAL));
        myInstrList.append(instructionFactory
                .createCheckCast(new ObjectType(className)));
    }

    private boolean containsLoadInstruction(InstructionList instructionList) {
        Instruction[] instructions = instructionList.getInstructions();
        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i].getOpcode() == ALOAD_0)
                return true;
        }
        return false;
    }

    static String[] generateParameterNames(int nr) {
        String[] result = new String[nr];
        for (int i = 0; i < nr; i++) {
            result[i] = "arg" + i;
        }
        return result;
    }

    private Type[] getArgumentTypes(Method method) {
        return Type.getArgumentTypes(Type.getSignature(method));
    }

}