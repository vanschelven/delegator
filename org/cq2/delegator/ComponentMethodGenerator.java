package org.cq2.delegator;

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
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.classgenerator.ClassGenerator;

public class ComponentMethodGenerator implements Constants {

    protected final ClassGen classGen;

    private final InstructionFactory instrFact;

    private final InstructionList instrList;

    private final ConstantPoolGen constPool;

    private MethodGen methodGen;

    private Class superclass;

    private int unique = 0;

    private final Class componentClass;

    public ComponentMethodGenerator(int methodIdentifier, int componentIdentifier) {
        superclass = ProxyMethodRegister.getInstance().getProxyMethodClass(methodIdentifier);
        componentClass = ComponentClassRegister.getInstance().getComponentClass(componentIdentifier);
        String superclassName = superclass.getName();

        classGen = new ClassGen("org.cq2.delegator.ComponentMethod" +  methodIdentifier + "_" + componentIdentifier,
                superclassName, "", ACC_PUBLIC & ~ACC_ABSTRACT, new String[] {});
        constPool = classGen.getConstantPool();
        instrFact = new InstructionFactory(classGen, constPool);
        instrList = new InstructionList();
        addComponentIndexField();
        addDefaultConstructor();
        //classGen.addEmptyConstructor(ACC_PUBLIC);
        add_method();
    }
    
    private void addComponentIndexField() {
        FieldGen fieldGen = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT, Type.INT, "componentIndex", classGen.getConstantPool());
        classGen.addField(fieldGen.getField());
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

    private void add_method() {
        try {
            Method superMethod = getInvokeMethod(superclass); 

            Type returnType = Type.getType(superMethod.getReturnType());
            Type[] parameterTypes = getArgumentTypes(superMethod);

            methodGen = new MethodGen(superMethod.getModifiers()
                    & ~ACC_ABSTRACT, returnType, parameterTypes,
                    generateParameterNames(superMethod
                            .getParameterTypes().length), superMethod.getName(), classGen
                            .getClassName(), instrList, constPool);
            
//            instrList.append(instrFact.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"),
//	                Constants.GETSTATIC));
//			instrList.append((instrFact.createGetField(classGen.getClassName(), "componentIndex", Type.INT)));
//
//	        instrList.append(instrFact.createInvoke("java.io.PrintStream", "println", Type.VOID, 
//	         new Type[] { Type.INT },
//	         Constants.INVOKEVIRTUAL));

            instrList.append(InstructionFactory.createLoad(Type.getType(Self.class), 1));
            instrList.append(instrFact.createGetField(Self.class.getName(), "components", new ArrayType(Type.getType(Object.class), 1)));
			instrList.append(InstructionFactory.createThis());
			instrList.append((instrFact.createGetField(classGen.getClassName(), "componentIndex", Type.INT)));
			instrList.append(InstructionConstants.AALOAD);
			
			instrList.append(instrFact.createCheckCast((ReferenceType) Type
                    .getType(componentClass)));
            final int SKIP_THIS_POINTER = 1;
            int pointer = SKIP_THIS_POINTER + 1; //don't use self
			for (int i = 1; i < methodGen.getArgumentTypes().length; i++) {
                instrList.append(InstructionFactory.createLoad(methodGen.getArgumentTypes()[i], pointer));
                pointer += methodGen.getArgumentTypes()[i].getSize();
            }

            String methodName = extractOriginalMethodName(superMethod.getName());
            if (Component.class.isAssignableFrom(componentClass) &&( (!methodName.equals("equals")) && (!methodName.equals("toString")))) //TODO uitbreiden
                methodName += ClassGenerator.SUPERCALL_POSTFIX;
            instrList.append(instrFact.createInvoke(componentClass.getName(),
                    methodName, returnType, removeFirst(getArgumentTypes(superMethod)),
                    Constants.INVOKEVIRTUAL));
            
            instrList.append(InstructionFactory.createReturn(returnType));

            methodGen.setMaxStack();
            methodGen.setMaxLocals();
            
 //           printMethod(methodGen);
            addFakeLineNumbers(methodGen);

            classGen.addMethod(methodGen.getMethod());

            instrList.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void addFakeLineNumbers(MethodGen methodGen) {
        InstructionHandle[] instructionHandles = methodGen.getInstructionList().getInstructionHandles();
        for (int i = 0; i < instructionHandles.length; i++) {
            methodGen.addLineNumber(instructionHandles[i], i);
        }
    }
    
    private void printMethod(MethodGen methodGen) {
        System.out.println("code of " + methodGen.getName());
        InstructionHandle[] instructionHandles = methodGen.getInstructionList().getInstructionHandles();
        for (int i = 0; i < instructionHandles.length; i++) {
            System.out.println(i + ": " + instructionHandles[i]);
        }
    }
    
    private Type[] removeFirst(Type[] input) {
        Type[] result = new Type[input.length - 1];
        System.arraycopy(input, 1, result, 0, result.length);
        return result;
    }

    private String extractOriginalMethodName(String name) {
        return name.substring("__invoke_".length());
    }

    private Method getInvokeMethod(Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("__invoke")) return methods[i];
        }
        return null;
    }
//    private void add_method() {
//        try {
//            Type returnType = Type.getType(method.getReturnType());
//            Type[] parameterTypes = insertSelfType(getArgumentTypes(method));
//
//            Method superMethod = superclass.getDeclaredMethod("invoke",
//                    insertSelfClass(method.getParameterTypes()));
//            methodGen = new MethodGen(superMethod.getModifiers()
//                    & ~ACC_ABSTRACT, returnType, parameterTypes,
//                    insertSelfString(generateParameterNames(method
//                            .getParameterTypes().length)), "invoke", classGen
//                            .getClassName(), instrList, constPool);
//            
//            //(componentClass) self.components[componentIndex]
//            instrList.append(InstructionFactory.createLoad(Type.getType(Self.class), 1));
//            instrList.append(instrFact.createGetField(Self.class.getName(), "components", new ArrayType(Type.getType(Object.class), 1)));
//			instrList.append(InstructionFactory.createThis());
//			instrList.append((instrFact.createGetField(classGen.getClassName(), "componentIndex", Type.INT)));
//			instrList.append(InstructionConstants.AALOAD);
//            instrList.append(instrFact.createCheckCast((ReferenceType) Type
//                    .getType(componentClass)));
//            
//            for (int i = 0; i < method.getParameterTypes().length; i++) {
//                instrList.append(InstructionFactory.createLoad(Type.getType(method.getParameterTypes()[i]), i + 2)); //TODO dit nog door het echte lijstje vervangen
//            }
//
//            instrList.append(instrFact.createInvoke(componentClass.getName(),
//                    method.getName(), returnType, getArgumentTypes(method),
//                    Constants.INVOKEVIRTUAL));
//            
//            instrList.append(InstructionFactory.createReturn(returnType));
//
//            methodGen.setMaxStack();
//            methodGen.setMaxLocals();
//
//            classGen.addMethod(methodGen.getMethod());
//
//            instrList.dispose();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }


    //copy paste - classGenerator
    private static String[] generateParameterNames(int nr) {
        String[] result = new String[nr];
        for (int i = 0; i < nr; i++) {
            result[i] = "arg" + i;
        }
        return result;
    }

    private static Type[] getArgumentTypes(Method method) {
        return Type.getArgumentTypes(Type.getSignature(method));
    }

}