package org.cq2.delegator.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Self;

public class InstructionListRunner implements Constants {

    private org.apache.bcel.classfile.Method originalMethod;
    private ClassGen classGen;
    private JavaClass superJavaClass;
    private ConstantPoolGen constantPool;
    private InstructionFactory instructionFactory;
    private String superclassName;
    private final Class superclass = Base.class;
    
    public static class Base {
        
        public void mVoid() {}
        public Object mObject() {return null;}
        
    }
    
    public InstructionListRunner() {
        superclassName = superclass.getName();
        superJavaClass = Repository.lookupClass(superclassName);
        constantPool = new ConstantPoolGen(superJavaClass
                        .getConstantPool());
        classGen = new ClassGen(superclassName + "$extramethod",
                        superclassName, "", Modifier.PUBLIC, null, constantPool);

        instructionFactory = new InstructionFactory(classGen, constantPool);
        addDefaultConstructor();
    }
    
    private Object runMethod(String methodName, InstructionList instructionList) throws Exception {
        org.apache.bcel.classfile.Method generatedMethod = generateMethod(methodName, instructionList);
        classGen.addMethod(generatedMethod);
        byte[] classDef = classGen.getJavaClass().getBytes();
        Object object = new SingleClassLoader(classDef).loadClass().newInstance();
        Method method = object.getClass().getDeclaredMethod(methodName, new Class[]{});
        return method.invoke(object, new Object[]{});
    }
    
    public void runVoid(InstructionList instructionList) throws Exception {
        runMethod("mVoid", instructionList);
    }
    
    public Object runObject(InstructionList instructionList) throws Exception {
        return runMethod("mObject", instructionList);
    }
    
    private org.apache.bcel.classfile.Method generateMethod(String methodName, InstructionList instructionList) throws NoSuchMethodException {
        Method method = superclass.getMethod(methodName, new Class[]{});
            
        originalMethod = superJavaClass.getMethod(method);
        MethodGen methodGen = new MethodGen(originalMethod, superclassName, constantPool);
        methodGen.setInstructionList(instructionList);
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        return methodGen.getMethod();
    }
    
    private void addDefaultConstructor() {
        InstructionList instrList = new InstructionList();
        MethodGen methodGen = new MethodGen(ACC_PUBLIC, Type.VOID,
                Type.NO_ARGS, new String[] {}, "<init>", classGen
                        .getClassName(), instrList, constantPool);
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        instrList.append(instructionFactory.createInvoke(classGen.getSuperclassName(),
                "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        instrList.append(InstructionFactory.createReturn(Type.VOID));
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();
    }
    
    public ConstantPoolGen getConstantPool() {
        return constantPool;
    }
    
    public InstructionFactory getInstructionFactory() {
        return instructionFactory;
    }
}
