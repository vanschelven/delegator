package org.cq2.delegator.classgenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.Repository;
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
import org.cq2.delegator.method.MethodFilter;

public class EncapsulatedComponentGenerator extends ClassGenerator {

    public EncapsulatedComponentGenerator(String className, Class superClass, Class marker) {
        super(className, superClass, marker, true); //duur om alle constanten te kopieren??!
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
        org.apache.bcel.classfile.Method superClassMethod = Repository
                .lookupClass(classGen.getSuperclassName()).getMethod(method);

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
       //hier mist een regel convertReturnValue ofzo
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();

    }

    private void replaceSomeThisPointersBySelfPointers(
            InstructionList myInstrList) {
        InstructionHandle current = myInstrList.getStart();
        InstructionHandle next = current.getNext();
        while (next != null) {
            if ((current.getInstruction().getOpcode() == ALOAD_0)
                    && ((next.getInstruction().getOpcode() == ARETURN) ||
                        (next.getInstruction() instanceof StoreInstruction) ||
                        (next.getInstruction() instanceof InvokeInstruction) ||
                        (next.getInstruction() instanceof PUTFIELD))) {
                myInstrList.insert(current, getLoadSelfList());
                try {
                    myInstrList.delete(current);
                } catch (Exception e) {throw new RuntimeException(e);}
            }

            current = next;
            next = current.getNext();
        }

    }

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

    
}
