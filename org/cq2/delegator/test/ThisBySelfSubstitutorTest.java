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
    
    public static class Noot {

        Aap aap;
        
        public Noot(Aap aap) {
            this.aap = aap;
        }

        public Noot(Aap aap, int i) {
            this.aap = aap;
        }
        
    }
    
    public static class Aap {
        
        public int x = 0;
        public Aap thisField;
        
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
        
        public Noot passAsParam() {
            return new Noot(this);
        }
        
        public Aap returnThisViaField() {
            thisField = this;
            return thisField;
        }

        public Noot passAsParams() {
            return new Noot(this, 1);
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
    
    //hhmmm wordt dit gebruikt???? maybe later
    private Object runReplacedMethod(String methodName, Class[] params) throws Exception {
        Aap aap = getAapWithReplacedMethod(methodName);
        Class[] passedParams = new Class[params.length + 1];
        passedParams[0] = Self.class;
        for (int i = 0; i < params.length; i++) {
            passedParams[i + 1] = params[i];
        }
        Method returnThis = aap.getClass().getDeclaredMethod(methodName, passedParams);
        Object result = returnThis.invoke(aap, new Object[]{new Self()});
        return result;
    }
    
    private Object runReplacedMethod(String methodName) throws Exception {
        return runReplacedMethod(methodName, new Class[]{});
    }
    
    public void testFieldAccess() throws Exception {
        runReplacedMethod("fieldAccess");
    }

    public void testReturnThisIndirectly() throws Exception {
        Aap result = (Aap) runReplacedMethod("returnThisIndirectly");
        assertTrue(result instanceof Proxy);
    }
    
    public void testPassAsParam() throws Exception {
        Noot result = (Noot) runReplacedMethod("passAsParam");
        assertTrue(result.aap instanceof Proxy);
    }
    
    public void testReturnThisViaField() throws Exception {
        Aap result = (Aap) runReplacedMethod("returnThisViaField");
        assertTrue(result instanceof Proxy);
    }
    
    public void testPassAsParams() throws Exception {
        Noot result = (Noot) runReplacedMethod("passAsParams");
        assertTrue(result.aap instanceof Proxy);
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
    
    //TODO bepalen welke this vervangen moet worden is misschien helemaal niet triviaal
    //bijv als je eerst meerdere laad op de stack en dan weer wat popt!
    //laten we maar gewoon kijken of we het simpel aan de praat kunnen krijgen.
    
    //TODO exception types
    //TODO replace this pointer with self pointer.
    //TODO Copy stuff with different scopes
    //TODO copy the entire chain (but take the top one)
    //TODO keep in mind special stuff like toSTring
    //TODO put this stuff in the right place - i.e. productioncode i.s.o. tests
    
    //TODO Hoe gaat dit met superclasses? Lijkt me ingewikkelder want
    //de aanroep naar de superclass laat zich weer heel lastig vertalen naar een
    //aanroep naar de self!
    
    //TODO: mogelijk plan van aanpak: gewoon de bestaande code vervangen en kijken waar het allemaal mis gaat
    //dan iig dat eerst werkend krijgen voordat ik aan fantasieproblemen begin.
    
}
