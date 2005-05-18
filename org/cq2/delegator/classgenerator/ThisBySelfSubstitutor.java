package org.cq2.delegator.classgenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Self;

public class ThisBySelfSubstitutor implements Constants {

    private InstructionFactory instructionFactory;

    private ConstantPoolGen constPool;

    private ClassGen classGen;

    private String superclassName;

    public ThisBySelfSubstitutor(ClassGen classGen, ConstantPoolGen constPool,
            InstructionFactory instructionFactory) {
        this.classGen = classGen;
        this.constPool = constPool;
        this.instructionFactory = instructionFactory;
        superclassName = classGen.getSuperclassName();
    }

    public org.apache.bcel.classfile.Method generateMethod(Method method)
            throws TargetLostException {
        Type returnType = Type.getType(method.getReturnType());
        org.apache.bcel.classfile.Method superClassMethod = Repository
                .lookupClass(superclassName).getMethod(method);

        List types = new ArrayList();
        types.add(0, Type.getType(Self.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));

        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                | Modifier.PUBLIC;

        InstructionList myInstrList = new InstructionList(superClassMethod
                .getCode().getCode());
        replaceSomeThisPointersBySelfPointers(myInstrList);

        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList,
                constPool);

        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        return methodGen.getMethod();
    }

    private void replaceSomeThisPointersBySelfPointers(
            InstructionList myInstrList) throws TargetLostException {
        InstructionHandle current = myInstrList.getStart();
        InstructionHandle next = current.getNext();
        while (next != null) {
            if ((current.getInstruction().getOpcode() == ALOAD_0)
                    && ((next.getInstruction().getOpcode() == ARETURN) ||
                        (next.getInstruction() instanceof StoreInstruction) ||
                        (next.getInstruction() instanceof InvokeInstruction) ||
                        (next.getInstruction() instanceof PUTFIELD))) {
                myInstrList.insert(current, getLoadSelfList());
                myInstrList.delete(current);
            }

            current = next;
            next = current.getNext();
        }

    }

    private InstructionList getLoadSelfList() {
        InstructionList result = new InstructionList();
        result.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        result.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        result.append(instructionFactory.createInvoke(Object.class.getName(),
                "getClass", new ObjectType(Class.class.getName()),
                new Type[] {}, INVOKEVIRTUAL));
        result.append(instructionFactory.createInvoke(Self.class.getName(),
                "cast", Type.OBJECT, new Type[] { new ObjectType(Class.class
                        .getName()) }, INVOKEVIRTUAL));
        result.append(instructionFactory.createCheckCast(new ObjectType(
                superclassName)));
        return result;
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