package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Self;

public class ThisPointerEncapsulationTest extends TestCase {

    public class SelfPointingClass {
        
        public SelfPointingClass returnThis() {
            return this;
        }
    }
    
    public void testRegularJava() {
        SelfPointingClass selfPointingClass1 = new SelfPointingClass();
        SelfPointingClass selfPointingClass2 = selfPointingClass1.returnThis();
        assertEquals(selfPointingClass1, selfPointingClass2);
    }
    
    public static abstract class DelegatingSelfPointingClass {
        
        public DelegatingSelfPointingClass returnThis() {
            return this;
        }
                
    }
    
    public static class Node {
        
        Document document;
        
        public Node(Document document) {
            this.document = document;
        }
        
        public Document getDocument() {
            return document;
        }
        
    }
    
    public static class Document {
        
        public Node createNode() {
            return new Node(this);
        }
        
        public int getOne() {
            return 1;
        }
        
    }
    
    public void testExampleFromProposalRegularJava() {
        Document document = new Document();
        Node node = document.createNode();
        assertEquals(document, node.getDocument());
    }
    
    public void testExampleFromProposal() {
        Self self = new Self(Document.class);
        Document document = (Document) self.cast(Document.class);
        Node node = document.createNode();
        assertEquals(document, node.getDocument());
    }
    
    public void testExampleFromThesisRegularJava() {
        Document document = new Document();
        Node node = document.createNode();
        assertEquals(1, node.getDocument().getOne());
    }
    
    public void testExampleFromThesis() {
        Self self = new Self(Document.class);
        Document document = (Document) self.cast(Document.class);
        Node node = document.createNode();
        assertEquals(1, node.getDocument().getOne());
    }
    
// TODO Turn this on: the final test
//    public void testDelegation() {
//        DelegatingSelfPointingClass a1 = (DelegatingSelfPointingClass) Delegator.extend(DelegatingSelfPointingClass.class, Vector.class);
//        DelegatingSelfPointingClass a2 = a1.returnThis();
//        assertEquals(a1, a2);
//    }
    
    public void m() {
        
    }
    
    public void m(InvocationHandler i) {
        
    }
    
    public void testImplementation1() throws SecurityException, NoSuchMethodException {
        printMethods("m");
    }

    private void printMethods(String name) throws NoSuchMethodException {
        printMethod(name, null);
        printMethod(name, new Class[]{InvocationHandler.class});
    }

    private void printMethod(String name, Class[] params) throws NoSuchMethodException {
        System.out.println("---------- " + name);
        JavaClass thisClass = Repository.lookupClass(getClass());
        Method method = getClass().getMethod(name, params);
        org.apache.bcel.classfile.Method methodImpl = thisClass
                .getMethod(method);
        System.out.println(methodImpl.getCode());
    }

    public int i() {
        return 1;
    }
    
    public int i(InvocationHandler iv) {
        return 1;
    }

    public void testImplementation2() throws SecurityException, NoSuchMethodException {
        printMethods("i");
    }

    public Node n(Node nnnn) {
        return nnnn;
    }
    
    public Node n(InvocationHandler iv, Node nnnn) {
        return nnnn;
    }

    public void testImplementation3() throws SecurityException, NoSuchMethodException {
        printMethod("n", new Class[]{Node.class});
        printMethod("n", new Class[]{InvocationHandler.class, Node.class});
    }
   
    public void myMethod() {
        
    }
    
    public class Nodeeeeee {
        
        private ThisPointerEncapsulationTest t;

        public Nodeeeeee(ThisPointerEncapsulationTest t) {
            this.t = t;
        }
        
    }
    
    public Nodeeeeee myCrashingMethod() {
        return new Nodeeeeee(this);
    }
    
    public void testImplementationBigTime() throws SecurityException, NoSuchMethodException {
        String superClassName = getClass().getName();
        ClassGen classGen = new ClassGen(superClassName + "$component", superClassName, "", Modifier.PUBLIC,
                null);
        ConstantPoolGen constPool = classGen.getConstantPool();
        InstructionFactory instrFact = new InstructionFactory(classGen, constPool);
        //InstructionList instrList = new InstructionList();
        
        //the method i'm trying to make in the end
        Method method = getClass().getMethod("myCrashingMethod", null);
        //init & add method header (wel reeds aangepast)
        Type returnType = Type.getType(method.getReturnType());
        
        List types = new ArrayList();
        types.add(0, Type.getType(InvocationHandler.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));
        
        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                    | Modifier.PUBLIC;
        

        //tussenstukje
        JavaClass superClass = Repository.lookupClass(classGen.getSuperclassName());
        org.apache.bcel.classfile.Method superClassMethod = superClass
                .getMethod(method);
        
        InstructionList myInstrList = new InstructionList(superClassMethod.getCode().getCode());
System.out.println(myInstrList);
        //        InstructionList myInstrList = new InstructionList(InstructionFactory.createReturn(returnType));

        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList, constPool);
        System.out.println(methodGen);
        
        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            methodGen.addException(exceptionTypes[i].getName());
        }
        
        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        classGen.addMethod(methodGen.getMethod());
        
        //instrList.dispose();
    }

    public void testJustTryToCopy() throws SecurityException, NoSuchMethodException {
        String superClassName = getClass().getName();
        ClassGen classGen = new ClassGen(superClassName + "$component", superClassName, "", Modifier.PUBLIC,
                null);
        ConstantPoolGen constPool = classGen.getConstantPool();
        InstructionFactory instrFact = new InstructionFactory(classGen, constPool);
        //InstructionList instrList = new InstructionList();
        
        //the method i'm trying to make in the end
        Method method = getClass().getMethod("myCrashingMethod", null);
        //init & add method header (wel reeds aangepast)
        Type returnType = Type.getType(method.getReturnType());
        
        List types = new ArrayList();
        //types.add(0, Type.getType(InvocationHandler.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));
        
        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                    | Modifier.PUBLIC;
        

        //tussenstukje
        JavaClass superClass = Repository.lookupClass(classGen.getSuperclassName());
        org.apache.bcel.classfile.Method superClassMethod = superClass
                .getMethod(method);
        
        InstructionList myInstrList = new InstructionList(superClassMethod.getCode().getCode());
System.out.println(myInstrList);
        //        InstructionList myInstrList = new InstructionList(InstructionFactory.createReturn(returnType));

        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList, constPool);
        System.out.println(methodGen);
        
        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            methodGen.addException(exceptionTypes[i].getName());
        }
        
        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        classGen.addMethod(methodGen.getMethod());
        
        //instrList.dispose();
    }

    public void testJustTryToCopy2() throws SecurityException, NoSuchMethodException {
        String superClassName = getClass().getName();
        ClassGen classGen = new ClassGen(superClassName + "$component", superClassName, "", Modifier.PUBLIC,
                null);
        ConstantPoolGen constPool = classGen.getConstantPool();
        InstructionFactory instrFact = new InstructionFactory(classGen, constPool);
        //InstructionList instrList = new InstructionList();
        
        //the method i'm trying to make in the end
        Method method = getClass().getMethod("myCrashingMethod", null);
        //init & add method header (wel reeds aangepast)
        Type returnType = Type.getType(method.getReturnType());
        
        List types = new ArrayList();
        //types.add(0, Type.getType(InvocationHandler.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));
        
        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                    | Modifier.PUBLIC;
        

        //tussenstukje
        JavaClass superClass = Repository.lookupClass(classGen.getSuperclassName());
        org.apache.bcel.classfile.Method superClassMethod = superClass
                .getMethod(method);
        
        InstructionList myInstrList = new InstructionList(superClassMethod.getCode().getCode());
System.out.println(myInstrList);
        //        InstructionList myInstrList = new InstructionList(InstructionFactory.createReturn(returnType));

        MethodGen methodGen = new MethodGen(superClassMethod, null, constPool);
        System.out.println(methodGen);
        
        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        classGen.addMethod(methodGen.getMethod());
        
        //instrList.dispose();
    }

    
    private Type[] getArgumentTypes(Method method) {
        return Type.getArgumentTypes(Type.getSignature(method));
    }

    static String[] generateParameterNames(int nr) {
        String[] result = new String[nr];
        for (int i = 0; i < nr; i++) {
            result[i] = "arg" + i;
        }
        return result;
    }

    
    
}
