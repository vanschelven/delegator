package org.cq2.delegator.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Self;

public class ThisPointerEncapsulationTest extends TestCase implements Constants {

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

        public Node() {
            //TODO misschien tijdelijk
        }
        
        public Node(Document document) {
            this.document = document;
        }

        public Node(ThisPointerEncapsulationTest test) {
            // TODO Auto-generated constructor stub, misschien tijdelijk
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
    //        DelegatingSelfPointingClass a1 = (DelegatingSelfPointingClass)
    // Delegator.extend(DelegatingSelfPointingClass.class, Vector.class);
    //        DelegatingSelfPointingClass a2 = a1.returnThis();
    //        assertEquals(a1, a2);
    //    }

    public void m() {

    }

    public void m(InvocationHandler i) {

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

    //    public void testImplementationBigTime() throws SecurityException,
    // NoSuchMethodException, InstantiationException, IllegalAccessException,
    // ClassNotFoundException {
    //        String superClassName = getClass().getName();
    //        ClassGen classGen = new ClassGen(superClassName + "$component",
    // superClassName, "", Modifier.PUBLIC,
    //                null);
    //        ConstantPoolGen constPool = classGen.getConstantPool();
    //        InstructionFactory instrFact = new InstructionFactory(classGen,
    // constPool);
    //        //InstructionList instrList = new InstructionList();
    //        addDefaultConstructor(classGen, constPool, instrFact);
    //       
    //        //the method i'm trying to make in the end
    //        Method method = getClass().getMethod("myCrashingMethod", null);
    //        //init & add method header (wel reeds aangepast)
    //        Type returnType = Type.getType(method.getReturnType());
    //        
    //        List types = new ArrayList();
    //        types.add(0, Type.getType(InvocationHandler.class));
    //        types.addAll(Arrays.asList(getArgumentTypes(method)));
    //        
    //        int newMods = method.getModifiers()
    //                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
    //        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
    //                    | Modifier.PUBLIC;
    //        
    //
    //        //tussenstukje
    //        JavaClass superClass =
    // Repository.lookupClass(classGen.getSuperclassName());
    //        org.apache.bcel.classfile.Method superClassMethod = superClass
    //                .getMethod(method);
    //        
    //        InstructionList myInstrList = new
    // InstructionList(superClassMethod.getCode().getCode());
    //System.out.println(myInstrList);
    //        // InstructionList myInstrList = new
    // InstructionList(InstructionFactory.createReturn(returnType));
    //
    //        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
    //                .toArray(new Type[] {}), generateParameterNames(types.size()),
    //                method.getName(), classGen.getClassName(), myInstrList, constPool);
    //        System.out.println(methodGen);
    //        
    //        Class[] exceptionTypes = method.getExceptionTypes();
    //        for (int i = 0; i < exceptionTypes.length; i++) {
    //            methodGen.addException(exceptionTypes[i].getName());
    //        }
    //        
    //        //add method trailer (met verwijderingen)
    //        methodGen.setMaxStack(superClassMethod.getCode().getMaxStack());
    //        final int INVOCATIONHANDLERISLOCAL = 1;
    //        methodGen.setMaxLocals(superClassMethod.getCode().getMaxLocals() +
    // INVOCATIONHANDLERISLOCAL);
    //
    //        classGen.addMethod(methodGen.getMethod());
    //        byte[] bytes = classGen.getJavaClass().getBytes();
    //        Object aap = new SingleClassLoader(bytes).loadClass("aap").newInstance();
    //        System.out.println(aap);
    //        
    //        //instrList.dispose();
    //    }
    //
    //    public void testJustTryToCopy() throws SecurityException,
    // NoSuchMethodException, InstantiationException, IllegalAccessException,
    // ClassNotFoundException {
    //        String superClassName = getClass().getName();
    //        ClassGen classGen = new ClassGen(superClassName + "$component",
    // superClassName, "", Modifier.PUBLIC,
    //                null);
    //        ConstantPoolGen constPool = classGen.getConstantPool();
    //        InstructionFactory instrFact = new InstructionFactory(classGen,
    // constPool);
    //        //InstructionList instrList = new InstructionList();
    //        addDefaultConstructor(classGen, constPool, instrFact);
    //        
    //        //the method i'm trying to make in the end
    //        Method method = getClass().getMethod("myCrashingMethod", null);
    //        //init & add method header (wel reeds aangepast)
    //        Type returnType = Type.getType(method.getReturnType());
    //        
    //        List types = new ArrayList();
    //        //types.add(0, Type.getType(InvocationHandler.class));
    //        types.addAll(Arrays.asList(getArgumentTypes(method)));
    //        
    //        int newMods = method.getModifiers()
    //                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
    //        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
    //                    | Modifier.PUBLIC;
    //        
    //
    //        //tussenstukje
    //        JavaClass superClass =
    // Repository.lookupClass(classGen.getSuperclassName());
    //        org.apache.bcel.classfile.Method superClassMethod = superClass
    //                .getMethod(method);
    //        
    //        InstructionList myInstrList = new
    // InstructionList(superClassMethod.getCode().getCode());
    //System.out.println(myInstrList);
    //        // InstructionList myInstrList = new
    // InstructionList(InstructionFactory.createReturn(returnType));
    //
    //        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
    //                .toArray(new Type[] {}), generateParameterNames(types.size()),
    //                method.getName(), classGen.getClassName(), myInstrList, constPool);
    //        System.out.println(methodGen);
    //        
    //        Class[] exceptionTypes = method.getExceptionTypes();
    //        for (int i = 0; i < exceptionTypes.length; i++) {
    //            methodGen.addException(exceptionTypes[i].getName());
    //        }
    //        
    //        //add method trailer (met verwijderingen)
    //        methodGen.setMaxStack(superClassMethod.getCode().getMaxStack());
    //        final int INVOCATIONHANDLERISLOCAL = 1;
    //        methodGen.setMaxLocals(superClassMethod.getCode().getMaxLocals() +
    // INVOCATIONHANDLERISLOCAL);
    //
    //        classGen.addMethod(methodGen.getMethod());
    //        byte[] bytes = classGen.getJavaClass().getBytes();
    //        Object aap = new SingleClassLoader(bytes).loadClass("aap").newInstance();
    //        System.out.println(aap);
    //        
    //        //instrList.dispose();
    //    }
    //
    //    public void testJustTryToCopy2() throws SecurityException,
    // NoSuchMethodException, InstantiationException, IllegalAccessException,
    // ClassNotFoundException {
    //        String superClassName = getClass().getName();
    //        ClassGen classGen = new ClassGen(superClassName + "$component",
    // superClassName, "", Modifier.PUBLIC,
    //                null);
    //        ConstantPoolGen constPool = classGen.getConstantPool();
    //        InstructionFactory instrFact = new InstructionFactory(classGen,
    // constPool);
    //        //InstructionList instrList = new InstructionList();
    //        addDefaultConstructor(classGen, constPool, instrFact);
    //        //the method i'm trying to make in the end
    //        Method method = getClass().getMethod("myCrashingMethod", null);
    //        //init & add method header (wel reeds aangepast)
    //        Type returnType = Type.getType(method.getReturnType());
    //        
    //        List types = new ArrayList();
    //        //types.add(0, Type.getType(InvocationHandler.class));
    //        types.addAll(Arrays.asList(getArgumentTypes(method)));
    //        
    //        int newMods = method.getModifiers()
    //                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
    //        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
    //                    | Modifier.PUBLIC;
    //        
    //
    //        //tussenstukje
    //        JavaClass superClass =
    // Repository.lookupClass(classGen.getSuperclassName());
    //        org.apache.bcel.classfile.Method superClassMethod = superClass
    //                .getMethod(method);
    //        
    //        InstructionList myInstrList = new
    // InstructionList(superClassMethod.getCode().getCode());
    //System.out.println(myInstrList);
    //        // InstructionList myInstrList = new
    // InstructionList(InstructionFactory.createReturn(returnType));
    //
    //        MethodGen methodGen = new MethodGen(superClassMethod, null, constPool);
    //        System.out.println(methodGen);
    //        
    //        //add method trailer (met verwijderingen)
    //        methodGen.setMaxStack(superClassMethod.getCode().getMaxStack());
    //        methodGen.setMaxLocals(superClassMethod.getCode().getMaxLocals());
    //
    //        classGen.addMethod(methodGen.getMethod());
    //        byte[] bytes = classGen.getJavaClass().getBytes();
    //        Object aap = new SingleClassLoader(bytes).loadClass("aap").newInstance();
    //        System.out.println(aap);
    //        //instrList.dispose();
    //    }

    //deze methode is alleen een subclass met defaultconstructor
    public void testLetsStartSimple() throws SecurityException,
            NoSuchMethodException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        Method method = SimpleClass.class.getMethod("method", null);
        String superClassName = SimpleClass.class.getName();
        ClassGen classGen = new ClassGen(superClassName + "$component",
                superClassName, "", Modifier.PUBLIC, null);
        ConstantPoolGen constPool = classGen.getConstantPool();
        InstructionFactory instrFact = new InstructionFactory(classGen,
                constPool);
        addDefaultConstructor(classGen, constPool, instrFact);

        //hier is ruimte voor ingewikkelder dingen

        byte[] bytes = classGen.getJavaClass().getBytes();
        Object aap = new SingleClassLoader(bytes).loadClass("aap")
                .newInstance();
        ((SimpleClass) (aap)).method();
    }

    //in deze test proberen we een simpele kopie te maken van SimpleClass met
    // een kopie van method
    public void testCopyVoidMethod() throws SecurityException,
            NoSuchMethodException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        Object aap = createSubclassWithCopiedMethod(SimpleClass.class, "method");
        ((SimpleClass) (aap)).method();
    }

    public void testCopyMethodWithReturnStatement() throws SecurityException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException {
        Object aap = createSubclassWithCopiedMethod(SimpleClass.class,
                "return1");
        assertEquals(1, ((SimpleClass) (aap)).return1());
    }

    public void testCopyMethodWithNewStatement() throws SecurityException,
            InstantiationException, IllegalAccessException,
            ClassNotFoundException, NoSuchMethodException {
        Object aap = createSubclassWithCopiedMethod(SimpleClass.class,
                "newObject");
        ((SimpleClass) (aap)).newObject();
    }

    //    public void testCopyMethodWithNewStatementManually() throws
    // SecurityException, NoSuchMethodException, InstantiationException,
    // IllegalAccessException, ClassNotFoundException {
    //        Class superclass = SimpleClass.class;
    //        String methodName = "newObject";
    //        
    //        Method method = superclass.getMethod(methodName, null);
    //        String superclassName = SimpleClass.class.getName();
    //        ClassGen classGen = new ClassGen(superclassName + "$component",
    // superclassName, "", Modifier.PUBLIC,
    //                null);
    //        ConstantPoolGen constPool = classGen.getConstantPool();
    //        InstructionFactory instrFact = new InstructionFactory(classGen,
    // constPool);
    //        addDefaultConstructor(classGen, constPool, instrFact);
    //        
    //        //hier is de ruimte voor ingewikkelder dingen benut:
    //        Type returnType = Type.getType(method.getReturnType());
    //        
    //        List types = new ArrayList();
    //        types.addAll(Arrays.asList(getArgumentTypes(method)));
    //        
    //        int newMods = method.getModifiers()
    //                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
    //        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
    //                    | Modifier.PUBLIC;
    //
    //        JavaClass superClass =
    // Repository.lookupClass(classGen.getSuperclassName());
    //        org.apache.bcel.classfile.Method superClassMethod = superClass
    //                .getMethod(method);
    //        
    //        InstructionList myInstrList = new InstructionList();
    //        myInstrList.append(instrFact.createNew(ObjectType.OBJECT));
    //        myInstrList.append(instrFact.createInvoke(Object.class.getName(),
    // "<init>", ObjectType.VOID, new Type[]{}, INVOKESPECIAL));
    //        myInstrList.append(InstructionFactory.createReturn(ObjectType.VOID));
    //        myInstrList.setPositions(); //niet per se nodig maar dit leest lekkerder
    //        System.out.println(myInstrList);
    //        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
    //                .toArray(new Type[] {}), generateParameterNames(types.size()),
    //                method.getName(), classGen.getClassName(), myInstrList, constPool);
    //        
    //        //add method trailer (met verwijderingen)
    //        methodGen.setMaxStack(superClassMethod.getCode().getMaxStack());
    //        methodGen.setMaxLocals(superClassMethod.getCode().getMaxLocals());
    //        classGen.addMethod(methodGen.getMethod());
    //        
    //        byte[] bytes = classGen.getJavaClass().getBytes();
    //        Object aap = new
    // SingleClassLoader(bytes).loadClass("intentionallyCorruptedString").newInstance();
    //        ((SimpleClass) (aap)).newObject();
    //    }

    public void testCopyMethodWithReturnAndNewStatement()
            throws SecurityException, InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException {
        Object aap = createSubclassWithCopiedMethod(SimpleClass.class,
                "returnNewObject");
        ((SimpleClass) (aap)).returnNewObject();
    }

    private Object createSubclassWithCopiedMethod(Class superclass,
            String methodName) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SecurityException,
            NoSuchMethodException {
        Method method = superclass.getMethod(methodName, null);
        String superclassName = superclass.getName();
        JavaClass superJavaClass = Repository.lookupClass(superclassName);
        ConstantPoolGen constPool = new ConstantPoolGen(superJavaClass
                .getConstantPool()); //dit leidt wellicht tot overbodige
                                     // constanten maar dat lossen we later wel
                                     // op

        ClassGen classGen = new ClassGen(superclassName + "$component",
                superclassName, "", Modifier.PUBLIC, null, constPool);
        InstructionFactory instrFact = new InstructionFactory(classGen,
                constPool);
        addDefaultConstructor(classGen, constPool, instrFact);

        //hier is de ruimte voor ingewikkelder dingen benut:
        Type returnType = Type.getType(method.getReturnType());

        List types = new ArrayList();
        types.addAll(Arrays.asList(getArgumentTypes(method)));

        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                | Modifier.PUBLIC;

        org.apache.bcel.classfile.Method superClassMethod = superJavaClass
                .getMethod(method);

        InstructionList myInstrList = new InstructionList(superClassMethod
                .getCode().getCode());
        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList,
                constPool);

        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());

        byte[] bytes = classGen.getJavaClass().getBytes();
        return new SingleClassLoader(bytes).loadClass(
                "intentionallyCorruptedString").newInstance();
    }

    //in deze test veranderen we de signature van method...
    public void testCopyMethodWithReturnAndNewStatementWithDifferentSignature()
            throws SecurityException, InstantiationException,
            ClassNotFoundException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Object aap = createSubclassWithCopiedMethodWithModifiedSignature(
                SimpleClass.class, "returnNewObject");
        Method m = aap.getClass().getMethod("returnNewObject",
                new Class[] { InvocationHandler.class });
        assertNotNull(m);
        m.invoke(aap, new Object[] { null });
    }

    private Object createSubclassWithCopiedMethodWithModifiedSignature(
            Class superclass, String methodName) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SecurityException,
            NoSuchMethodException {
        Method method = superclass.getMethod(methodName, null);
        String superclassName = superclass.getName();
        JavaClass superJavaClass = Repository.lookupClass(superclassName);
        ConstantPoolGen constPool = new ConstantPoolGen(superJavaClass
                .getConstantPool()); //dit leidt wellicht tot overbodige
                                     // constanten maar dat lossen we later wel
                                     // op

        ClassGen classGen = new ClassGen(superclassName + "$component",
                superclassName, "", Modifier.PUBLIC, null, constPool);
        InstructionFactory instrFact = new InstructionFactory(classGen,
                constPool);
        addDefaultConstructor(classGen, constPool, instrFact);

        //hier is de ruimte voor ingewikkelder dingen benut:
        Type returnType = Type.getType(method.getReturnType());

        List types = new ArrayList();
        types.add(0, Type.getType(InvocationHandler.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));

        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                | Modifier.PUBLIC;

        org.apache.bcel.classfile.Method superClassMethod = superJavaClass
                .getMethod(method);

        InstructionList myInstrList = new InstructionList(superClassMethod
                .getCode().getCode());
        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList,
                constPool);

        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());

        byte[] bytes = classGen.getJavaClass().getBytes();
        return new SingleClassLoader(bytes).loadClass(
                "intentionallyCorruptedString").newInstance();
    }

    //in deze test veranderen we de signature van method...
    public void testCopyMethodWithReturnAndNewStatementWithDifferentSignatureAndReplacedThis()
            throws SecurityException, InstantiationException,
            ClassNotFoundException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        Object noot = createSubclassWithCopiedMethodWithModifiedSignatureAndReplacedThis1(
                SimpleClass.class, "returnSelf");
        Method n = noot.getClass().getMethod("returnSelf",
                new Class[] { Self.class });
        assertNotNull(n);
        Self self = new Self();
        // self.add(SimpleClass.class);
        // n.invoke(noot, new Object[]{self});

        //het gaat natuurlijk om deze
        Object aap = createSubclassWithCopiedMethodWithModifiedSignatureAndReplacedThis2(
                SimpleClass.class, "returnThis");
        Method m = aap.getClass().getMethod("returnThis",
                new Class[] { Self.class });
        assertNotNull(m);
        self = new Self();
        //  self.add(SimpleClass.class);
        //  m.invoke(aap, new Object[]{self});
    }

    //we beginnen met het weggooien van de gehele methode en het invullen van
    // de nieuwe goede code
    // nr.1 is zonder extra param en gaat uit van het bestaan van die param.
    private Object createSubclassWithCopiedMethodWithModifiedSignatureAndReplacedThis1(
            Class superclass, String methodName) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SecurityException,
            NoSuchMethodException {
        Method method = superclass.getMethod(methodName,
                new Class[] { Self.class });
        String superclassName = superclass.getName();
        JavaClass superJavaClass = Repository.lookupClass(superclassName);
        ConstantPoolGen constPool = new ConstantPoolGen(superJavaClass
                .getConstantPool()); //dit leidt wellicht tot overbodige
                                     // constanten maar dat lossen we later wel
                                     // op

        ClassGen classGen = new ClassGen(superclassName + "$component",
                superclassName, "", Modifier.PUBLIC, null, constPool);
        InstructionFactory instrFact = new InstructionFactory(classGen,
                constPool);
        addDefaultConstructor(classGen, constPool, instrFact);

        //hier is de ruimte voor ingewikkelder dingen benut:
        Type returnType = Type.getType(method.getReturnType());

        List types = new ArrayList();
        //types.add(0, Type.getType(Self.class));
        types.addAll(Arrays.asList(getArgumentTypes(method)));

        int newMods = method.getModifiers()
                & ~(Modifier.NATIVE | Modifier.ABSTRACT);
        newMods = newMods & ~(Modifier.PRIVATE | Modifier.PROTECTED)
                | Modifier.PUBLIC;

        org.apache.bcel.classfile.Method superClassMethod = superJavaClass
                .getMethod(method);

        InstructionList myInstrList = new InstructionList();
        myInstrList.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        myInstrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        myInstrList.append(instrFact.createInvoke(Object.class.getName(),
                "getClass", new ObjectType(Class.class.getName()),
                new Type[] {}, INVOKEVIRTUAL));
        myInstrList.append(instrFact.createInvoke(Self.class.getName(), "cast",
                Type.OBJECT,
                new Type[] { new ObjectType(Class.class.getName()) },
                INVOKEVIRTUAL));
        myInstrList.append(instrFact.createCheckCast(new ObjectType(
                superclassName)));
        myInstrList.append(InstructionFactory.createReturn(new ObjectType(
                superclassName)));
        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList,
                constPool);

        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());

        byte[] bytes = classGen.getJavaClass().getBytes();
        return new SingleClassLoader(bytes).loadClass(
                "intentionallyCorruptedString").newInstance();
    }

    private Object createSubclassWithCopiedMethodWithModifiedSignatureAndReplacedThis2(
            Class superclass, String methodName) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SecurityException,
            NoSuchMethodException {
        Method method = superclass.getMethod(methodName, new Class[] {});
        String superclassName = superclass.getName();
        JavaClass superJavaClass = Repository.lookupClass(superclassName);
        ConstantPoolGen constPool = new ConstantPoolGen(superJavaClass
                .getConstantPool()); //dit leidt wellicht tot overbodige
                                     // constanten maar dat lossen we later wel
                                     // op

        ClassGen classGen = new ClassGen(superclassName + "$component",
                superclassName, "", Modifier.PUBLIC, null, constPool);
        InstructionFactory instrFact = new InstructionFactory(classGen,
                constPool);
        addDefaultConstructor(classGen, constPool, instrFact);

        //hier is de ruimte voor ingewikkelder dingen benut:
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

        InstructionList myInstrList = new InstructionList();
        myInstrList.append(InstructionFactory.createLoad(Type.OBJECT, 1));
        myInstrList.append(InstructionFactory.createLoad(Type.OBJECT, 0));
        myInstrList.append(instrFact.createInvoke(Object.class.getName(),
                "getClass", new ObjectType(Class.class.getName()),
                new Type[] {}, INVOKEVIRTUAL));
        myInstrList.append(instrFact.createInvoke(Self.class.getName(), "cast",
                Type.OBJECT,
                new Type[] { new ObjectType(Class.class.getName()) },
                INVOKEVIRTUAL));
        myInstrList.append(instrFact.createCheckCast(new ObjectType(
                superclassName)));
        myInstrList.append(InstructionFactory.createReturn(new ObjectType(
                superclassName)));
        MethodGen methodGen = new MethodGen(newMods, returnType, (Type[]) types
                .toArray(new Type[] {}), generateParameterNames(types.size()),
                method.getName(), classGen.getClassName(), myInstrList,
                constPool);

        //add method trailer (met verwijderingen)
        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        classGen.addMethod(methodGen.getMethod());

        byte[] bytes = classGen.getJavaClass().getBytes();
        return new SingleClassLoader(bytes).loadClass(
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

    public void testXXXX() throws NoSuchMethodException {
        printMethod("differentUsesOfThis", null);
    }
    
    private void printMethod(String name, Class[] params)
            throws NoSuchMethodException {
        System.out.println("---------- " + name);
        JavaClass thisClass = Repository.lookupClass(getClass());
        Method method = getClass().getMethod(name, params);
        org.apache.bcel.classfile.Method methodImpl = thisClass
                .getMethod(method);
        System.out.println(methodImpl.getCode());
    }
    
    private int one = 1;
    
    public void acceptThis(ThisPointerEncapsulationTest t) {}
    
    public ThisPointerEncapsulationTest differentUsesOfThis() {
        ThisPointerEncapsulationTest that = new ThisPointerEncapsulationTest();
        ThisPointerEncapsulationTest this2 = this;
        m();
        this.m();
        int i = one;
        toString();
        getName();
        one = 2;
        new Node();
        new Node(this);
        acceptThis(this);
        acceptThis(that);
        acceptThis(this2);
        return this;
        //TODO Hoe gaat dit met superclasses? Lijkt me ingewikkelder want
        //de aanroep naar de superclass laat zich weer heel lastig vertalen naar een
        //aanroep naar de self!
    }

    //TODO bepalen welke this vervangen moet worden is misschien helemaal niet triviaal
    //bijv als je eerst meerdere laad op de stack en dan weer wat popt!
    //laten we maar gewoon kijken of we het simpel aan de praat kunnen krijgen.
    
    //TODO exception types
    //TODO replace this pointer with self pointer.
    //TODO Copy stuff with different scopes
    //TODO copy the entire chain (but take the top one)
    //TODO keep in mind special stuff like toSTring
    //TODO put this stuff in the right place - i.e. productioncode i.s.o. tests

}