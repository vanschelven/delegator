package org.cq2.delegator.test;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.apache.bcel.Constants;
import org.cq2.delegator.Component;
import org.cq2.delegator.Proxy;
import org.cq2.delegator.Self;
import org.cq2.delegator.classgenerator.EncapsulatedComponentGenerator;

public class EncapsulatedComponentGeneratorTest extends TestCase implements Constants {

    public static class Wrapper {

        Object object;
        
        public Wrapper(Object object) {
            this.object = object;
        }

        public Wrapper(Object object, int i) {
            this.object = object;
        }
        
    }
    
    public static class EmptyMethod{
        
        public void method() {}
        
    }
    
    public static class ReturnThis {

        public Object method() {
            return this;
        }

        
    }
  
    public static class LocalVariableAccess {
        
        public void method() {
            int i = 1;
            int j = i;
            i = j;
        }
    }

    public static class LocalVariableAccess2 {
        
        public void method() {
            int i = 1;
            Object o = this;
        }
    }

    public static class FieldAccess{
        
        public int x = 0;

        public void method() {
            x = 1;
        }
        
    }
    
    public static class ReturnThisIndirectly {
        
        public Object method() {
            Object this2 = this;
            return this2;
        }
    }
    
    public static class PassAsParam {
        
        public Wrapper method() {
            return new Wrapper(this);
        }
        
    }

    public static class PassAsParams {
        
        public Wrapper method() {
            return new Wrapper(this, 1);
        }
    }
    
    public static class ReturnThisViaField {
        
        public Object thisField;
        
        public Object method() {
            thisField = this;
            return thisField;
        }
        
    }
    
    public static class ThisIsWrittenToLocalVariable {
        
        public void method() {
            Object o1 = this;
        }
        
    }
    
    public static class ThisIsAccessedTwoTimes {
        
        public void method() {
            Object o1 = this;
            Object o2 = this;
        }
        
    }
    
    public static class Superclass {
        
        public void someMethod() {}
        
    }
    
    public static class EmptyIntermediate extends Superclass {}
    
    public static class Subclass extends EmptyIntermediate {
        
        public void method() {}
        
    }
    
    public static class SuperclassWithConstants {
        
        public void someMethod() {
            System.out.println("This is never printed");
        }
        
    }
    
    public static class EmtpyIntermediate2 extends SuperclassWithConstants {
        
    }
    
    public static class Subclass2 extends EmtpyIntermediate2 {
        
        public void method() {}
        
    }
    
    public static class IfStatement {
        
        public int i;

        public Object method() {
            if(i == 0) {
                return this;
            }
            return this;
        }
    }
    
    public static class Supercall {
        
        public void method() {
            super.getClass();
        }
    }
    
    public static class PrivateField {
        
        private int i;

        public void method() {
            i = 1;
        }
    }
    
    public static class HowToCallThis {
        
        public void callsPrivate() {
            privateMethod();
        }
        
        private void privateMethod() {}
        
    }
    
    public static class Aap extends HowToCallThis {
        
        public void method() {
            callsPrivate();
        }
        
    }
    
    public static class InitializedField {
        
        public static final int INITIALVALUE = 5;
        public int i = INITIALVALUE;
        
        public Integer method() {
            return new Integer(i);
        }
        
    }
    
    public static class Entry {
        
        public Entry next;
        
    }
    
    public static class FieldOfOtherClass {

        public void method() {
            Entry next = new Entry().next;
        }

    }
    
    public void testEmptyMethod() throws Exception {
        runReplacedMethod(EmptyMethod.class);
    }
    
    public void testReturnThis() throws Exception {
        Object result = runReplacedMethod(ReturnThis.class);
        assertTrue(result instanceof Proxy);
        assertFalse(result instanceof Component);
    }

    public void testLocalVariableAccess() throws Exception {
        runReplacedMethod(LocalVariableAccess.class);
    }
    
    public void testLocalVariableAccess2() throws Exception {
        runReplacedMethod(LocalVariableAccess2.class);
    }
    
    public void testFieldAccess() throws Exception {
        runReplacedMethod(FieldAccess.class);
    }

    public void testReturnThisIndirectly() throws Exception {
        Object result = runReplacedMethod(ReturnThisIndirectly.class);
        assertTrue(result instanceof Proxy);
        assertFalse(result instanceof Component);
    }
    
//TODO aanzetten
//    public void testPassAsParam() throws Exception {
//        Wrapper result = (Wrapper) runReplacedMethod(PassAsParam.class);
//        assertTrue(result.object instanceof Proxy);
//        assertFalse(result instanceof Component);
//    }
    
    //TODO aanzetten
//    public void testPassAsParams() throws Exception {
//        Wrapper result = (Wrapper) runReplacedMethod(PassAsParams.class);
//        System.out.println();
//        assertTrue(result.object instanceof Proxy);
//    }
    
    public void testReturnThisViaField() throws Exception {
        Object result = runReplacedMethod(ReturnThisViaField.class);
        assertTrue(result instanceof Proxy);
        assertFalse(result instanceof Component);
    }
    
    public void testThisIsWrittenToLocalVariable() throws Exception {
        runReplacedMethod(ThisIsWrittenToLocalVariable.class);
    }  
    
    public void testThisIsAccessedTwoTimes() throws Exception {
        runReplacedMethod(ThisIsAccessedTwoTimes.class);
    }
    
    public void testInheritanceChain() throws Exception {
        runReplacedMethod(Subclass.class);
    }
    
    public void testInheritanceChain2() throws Exception {
        runReplacedMethod(Subclass2.class);
    }
    
    public void testIfStatement() throws Exception {
        runReplacedMethod(IfStatement.class);
    }
    
    public void testSupercall() throws Exception {
        runReplacedMethod(Supercall.class);
    }
    
    public void testPrivateField() throws Exception {
        runReplacedMethod(PrivateField.class);
    }
    
    //TODO dit snappen en repareren
//    public void testAap() throws Exception {
//        runReplacedMethod(Aap.class);
//    }
    
    //TODO dit is eigenlijk een speciaal geval (<init>) van supercalls ook kopieren... doen we later.
//    public void testFieldInitialisation() throws Exception {
//        assertEquals(new Integer(InitializedField.INITIALVALUE), runReplacedMethod(InitializedField.class));
//    }
    
    public void testFieldOfOtherClass() throws Exception {
        runReplacedMethod(FieldOfOtherClass.class);
    }
    
    private Object runReplacedMethod(Class clazz) throws Exception {
        byte[] classDef = new EncapsulatedComponentGenerator(clazz.getName() + "$component", clazz)
        .generate();
        Object object = new SingleClassLoader(classDef).loadClass("intentionally corrupted").newInstance();
                
        Method method = object.getClass().getDeclaredMethod("method", new Class[]{Self.class});
        Object result = method.invoke(object, new Object[]{new Self()});
        return result;
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
    
    //TODO: factor this way of testing out and incorporate in ProxyGenereator? Or vise versa: make ProxyGenerator more composed and simpler

    
}
