package org.cq2.delegator.internal;

import java.lang.reflect.Method;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Component;
import org.cq2.delegator.Self;
import org.cq2.delegator.classgenerator.ClassGenerator;

public class ImplementingMethodGenerator extends Generator implements Constants {

    protected final ClassGen classGen;

    private final InstructionFactory instrFact;

    private final InstructionList instrList;

    private final ConstantPoolGen constPool;

    private MethodGen methodGen;

    private Class superclass;

    private final Class componentClass;

    public ImplementingMethodGenerator(int methodIdentifier, int componentIdentifier) {
        superclass = ForwardingMethodRegister.getInstance().getForwardingMethodClass(methodIdentifier);
        componentClass = ComponentClassRegister.getInstance().getComponentClass(componentIdentifier);
        String superclassName = superclass.getName();

        classGen = new ClassGen("org.cq2.delegator.ComponentMethod" +  methodIdentifier + "_" + componentIdentifier,
                superclassName, "", ACC_PUBLIC & ~ACC_ABSTRACT, new String[] {});
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        addComponentIndexField();
        classGen.addEmptyConstructor(ACC_PUBLIC);
        add_method("__invoke_", false);
        add_method("__offset_", true);
    }
    
    private void addComponentIndexField() {
        FieldGen fieldGen = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT, Type.INT, "componentIndex", classGen.getConstantPool());
        classGen.addField(fieldGen.getField());
    }

    public byte[] generate() {
        return classGen.getJavaClass().getBytes();
   }

    private void add_method(String prefix, boolean hasOffset) {
        try {
            Method superMethod = getSuperMethod(prefix); 

            Type returnType = Type.getType(superMethod.getReturnType());
            Type[] argumentTypes = getArgumentTypes(superMethod);

            methodGen = new MethodGen(superMethod.getModifiers()
                    & ~ACC_ABSTRACT, returnType, argumentTypes,
                    generateParameterNames(superMethod
                            .getParameterTypes().length), superMethod.getName(), classGen
                            .getClassName(), instrList, constPool);
             
            instrList.append(InstructionFactory.createLoad(Type.getType(Self.class), 1));
            instrList.append(instrFact.createGetField(Self.class.getName(), "components", new ArrayType(Type.getType(Object.class), 1)));
			instrList.append(InstructionFactory.createThis());
			instrList.append((instrFact.createGetField(classGen.getClassName(), "componentIndex", Type.INT)));
			if (hasOffset) {
				argumentTypes = removeLast(argumentTypes);
				instrList.append(InstructionFactory.createLoad(Type.INT, superMethod.getParameterTypes().length));
				instrList.append(InstructionConstants.IADD);
			}
			instrList.append(InstructionConstants.AALOAD);
			
			instrList.append(instrFact.createCheckCast((ReferenceType) Type
                    .getType(componentClass)));

			final int SKIP_THIS_POINTER = 1;
			final int SKIP_SELF_POINTER = 1;
            int pointer = SKIP_THIS_POINTER + SKIP_SELF_POINTER;
            for (int i = 1; i < argumentTypes.length; i++) {
                instrList.append(InstructionFactory.createLoad(argumentTypes[i], pointer));
                pointer += argumentTypes[i].getSize();
            }

            String methodName = extractOriginalMethodName(superMethod.getName(), prefix);
            if (Component.class.isAssignableFrom(componentClass) &&( (!methodName.equals("equals"))))
                methodName += ClassGenerator.SUPERCALL_POSTFIX;
            instrList.append(instrFact.createInvoke(componentClass.getName(),
                    methodName, returnType, removeFirst(argumentTypes),
                    Constants.INVOKEVIRTUAL));
            
            instrList.append(InstructionFactory.createReturn(returnType));

            methodGen.setMaxStack();
            methodGen.setMaxLocals();
            
            classGen.addMethod(methodGen.getMethod());

            instrList.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Type[] removeFirst(Type[] input) {
        Type[] result = new Type[input.length - 1];
        System.arraycopy(input, 1, result, 0, result.length);
        return result;
    }

    private Type[] removeLast(Type[] input) {
        Type[] result = new Type[input.length - 1];
        System.arraycopy(input, 0, result, 0, result.length);
        return result;
    }

    private String extractOriginalMethodName(String name, String prefix) {
        return name.substring(prefix.length());
    }

    private Method getSuperMethod(String prefix) {
        Method[] methods = superclass.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith(prefix)) return methods[i];
        }
        return null;
    }

}