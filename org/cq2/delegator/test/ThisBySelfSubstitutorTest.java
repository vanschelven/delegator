package org.cq2.delegator.test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.Self;
import org.cq2.delegator.classgenerator.ThisBySelfSubstitutor;

public class ThisBySelfSubstitutorTest extends TestCase implements Constants {

    private org.apache.bcel.classfile.Method originalMethod;
    private ClassGen classGen;
    private JavaClass superJavaClass;
    private ConstantPoolGen constantPool;
    private InstructionFactory instructionFactory;

    protected void setUp() {
        String superclassName = Aap.class.getName();
        superJavaClass = Repository.lookupClass(superclassName);
        constantPool = new ConstantPoolGen(superJavaClass
                        .getConstantPool());
        classGen = new ClassGen(superclassName + "$component",
                        superclassName, "", Modifier.PUBLIC, null, constantPool);

        instructionFactory = new InstructionFactory(classGen, constantPool);
        addDefaultConstructor(classGen, constantPool, instructionFactory);
    }
    
    public static class Aap {
        
        public int x = 0;
        
        public void emptyMethod() {
            
        }
        
        public void localVariableAccess() {
            int i = 1;
            int j = i;
            i = j;
        }
        
        public Aap returnThis() {
            return this;
        }
        
        public void fieldAccess() {
            x = 1; 
        }
        
        public Aap returnThisIndirectly() {
            Aap this2 = this;
            return this2;
        }
        
    }
    
    public void testEmptyMethod() throws Exception {
        org.apache.bcel.classfile.Method result = generateMethod("emptyMethod");
        assertTrue(Arrays.equals(originalMethod.getCode().getCode(), result.getCode().getCode()));
    }
    
    public void testLocalVariableAccess() throws Exception {
        org.apache.bcel.classfile.Method result = generateMethod("localVariableAccess");
        assertTrue(Arrays.equals(originalMethod.getCode().getCode(), result.getCode().getCode()));
    }

    //Some simple code understanding: expand this!!!
    public void testReturnThisCodeInspection() throws Exception {
        org.apache.bcel.classfile.Method result = generateMethod("returnThis");
        assertTrue(originalMethod.getCode().getLength() < result.getCode().getLength());
    }
    
    public void testReturnThis() throws Exception {
        Aap result = (Aap) runReplacedMethod("returnThis");
        assertTrue(result instanceof Proxy);
    }
    
    private Object runReplacedMethod(String methodName) throws Exception {
        Aap aap = getAapWithReplacedMethod(methodName);
        Method returnThis = aap.getClass().getDeclaredMethod(methodName, new Class[]{Self.class});
        Object resulteeee = returnThis.invoke(aap, new Object[]{new Self()});
        return resulteeee;
    }
    
    public void testFieldAccess() throws Exception {
        runReplacedMethod("fieldAccess");
    }

    public void testReturnThisIndirectly() throws Exception {
        Aap result = (Aap) runReplacedMethod("returnThisIndirectly");
        assertTrue(result instanceof Proxy);
    }
    
    private org.apache.bcel.classfile.Method generateMethod(String methodName) throws NoSuchMethodException, TargetLostException {
        Class[] methodParams = new Class[]{};
        Method method = Aap.class.getMethod(methodName, methodParams);
            
        originalMethod = superJavaClass
                .getMethod(method);
        ThisBySelfSubstitutor substitutor = new ThisBySelfSubstitutor(classGen, constantPool, instructionFactory);
        org.apache.bcel.classfile.Method result = substitutor.generateMethod(method);
        return result;
    }
    
    public Aap getAapWithReplacedMethod(String methodName) throws Exception {
        classGen.addMethod(generateMethod(methodName));
        byte[] bytes = classGen.getJavaClass().getBytes();
        return (Aap) new SingleClassLoader(bytes).loadClass(
                "intentionallyCorruptedString").newInstance();
    }
    
    private void addDefaultConstructor(ClassGen classGen,
            ConstantPoolGen constPool, InstructionFactory instrFact) {
        InstructionList instrList = new InstructionList();
        MethodGen methodGen = new MethodGen(ACC_PUBLIC, Type.VOID,
                Type.NO_ARGS, new String[] {}, "<init>", classGen
                        .getClassName(), instrList, constPool);
        instrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        instrList.append(instrFact.createInvoke(classGen.getSuperclassName(),
                "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
        instrList.append(InstructionFactory.createReturn(Type.VOID));
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());
        instrList.dispose();
    }


    //TODO fields must be copied too
    
}
