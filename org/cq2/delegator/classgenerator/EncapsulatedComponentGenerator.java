package org.cq2.delegator.classgenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Component;
import org.cq2.delegator.Self;
import org.cq2.delegator.method.MethodFilter;

public class EncapsulatedComponentGenerator extends ClassGenerator {

    private org.apache.bcel.classfile.Method superclassMethod;

    public EncapsulatedComponentGenerator(String className, Class superClass) {
        super(className, superClass, Component.class, true); //duur en nodig om alle constanten te kopieren??!
    }
    
    public byte[] generate() {
        addDelegationMethods(componentMethodFilter, false);
        addCopiesOfSuperMethods(componentMethodFilter);
        return classGen.getJavaClass().getBytes();
    }

    private void addCopiesOfSuperMethods(MethodFilter methodFilter) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();
            int modifiers = method.getModifiers();
            if (methodFilter.filter(method) && !Modifier.isAbstract(modifiers)) {
                addCopyOfSuperMethod(method);
            }
        }
    }

    private void addCopyOfSuperMethod(Method method) {
        // TODO Maak gebruik van addHeader en Trailer
        
        Type returnType = Type.getType(method.getReturnType());
        superclassMethod = findSuperclassMethod(classGen.getSuperclassName(), method);
        List types = new ArrayList();
        types.add(0, Type.getType(Self.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));

        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                | Modifier.PUBLIC;

        InstructionListCopier copier = new InstructionListCopier(new ConstantPoolGen(superclassMethod.getConstantPool()), constPool);
        instrList.append(copier.copy(new InstructionList(superclassMethod.getCode().getCode())));
        instrList.setPositions();
        modifyInstructions();

        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), instrList,
                constPool);

        //add method trailer (met verwijderingen)
        convertReturnValue(returnType);
        try {
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        } catch (Exception e) {throw new RuntimeException(methodGen.getName(), e); }
        
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();

    }

    private org.apache.bcel.classfile.Method findSuperclassMethod(String superclassName, Method method) {
        JavaClass superclass = Repository.lookupClass(superclassName);
        org.apache.bcel.classfile.Method result = superclass.getMethod(method);
        while (result == null) {
            superclass = superclass.getSuperClass();
            if (superclass == null) System.out.println("WRONG222");
            //dit kan fout aflopen maar dan is dat iig op de goede plaats
            result = superclass.getMethod(method);
        }
        return result;
    }

    private void modifyInstructions() {
        InstructionHandle current = instrList.getStart();
        InstructionHandle next = current.getNext();
        while (next != null) {
            reIndexLocalVariables(current);
            replaceThisPointerBySelf(current, next);
            
            current = next;
            next = current.getNext();
        }
    }

    private void reIndexLocalVariables(InstructionHandle current) {
        if (current.getInstruction() instanceof LocalVariableInstruction) {
            LocalVariableInstruction l = (LocalVariableInstruction) current.getInstruction();
            if (l.getIndex() > 0) {
                l.setIndex(l.getIndex() + 1);
            }
        }
    }

    private void replaceThisPointerBySelf(InstructionHandle current, InstructionHandle next) {
        if ((current.getInstruction().getOpcode() == ALOAD_0)
                && ((next.getInstruction().getOpcode() == ARETURN) ||
                    (next.getInstruction() instanceof StoreInstruction) ||
                    //((next.getInstruction() instanceof InvokeInstruction) && !(next.getInstruction() instanceof INVOKESPECIAL)) ||
                    //(next.getInstruction() instanceof InvokeInstruction) ||
                    (next.getInstruction() instanceof PUTFIELD))) {
            InstructionList loadSelfList = getLoadSelfList();
            InstructionHandle firstInstruction = loadSelfList.getStart();
            instrList.insert(current, loadSelfList);
            try {
                instrList.delete(current);
            } catch (TargetLostException e) {
                updateTargets(e, firstInstruction);
            }
        }
    }

    private void updateTargets(TargetLostException e,
            InstructionHandle newTarget) {
        InstructionHandle[] targets = e.getTargets();
        for (int i = 0; i < targets.length; i++) {
            InstructionTargeter[] targeters = targets[i].getTargeters();

            for (int j = 0; j < targeters.length; j++)
                targeters[j].updateTarget(targets[i], newTarget);
        }
    }

    //TODO ev. kan ik hier een zogenaamde compoundinstruction voor gebruiken
    private InstructionList getLoadSelfList() {
        InstructionList result = new InstructionList();
        result.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        result.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        result.append(instrFact.createInvoke(Object.class.getName(),
                "getClass", new ObjectType(Class.class.getName()),
                new Type[] {}, INVOKEVIRTUAL));
        result.append(instrFact.createInvoke(Self.class.getName(),
                "cast", Type.OBJECT, new Type[] { new ObjectType(Class.class
                        .getName()) }, INVOKEVIRTUAL));
        result.append(instrFact.createCheckCast(new ObjectType(
                classGen.getSuperclassName())));
        return result;
    }

    
    //TODO (op de verkeerde plaats maar ja... gebruik eens de Verifier van BCEL)
    
}
