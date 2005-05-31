package org.cq2.delegator.test;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.bcel.Constants;
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
    
    public void testEmptyMethod() throws Exception {
        runReplacedMethod(EmptyMethod.class);
    }
    
    public void testReturnThis() throws Exception {
        Object result = runReplacedMethod(ReturnThis.class);
        assertTrue(result instanceof Proxy);
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
    }
    
    public void testPassAsParam() throws Exception {
        Wrapper result = (Wrapper) runReplacedMethod(PassAsParam.class);
        assertTrue(result.object instanceof Proxy);
    }
    
    //TODO aanzetten
//    public void testPassAsParams() throws Exception {
//        Wrapper result = (Wrapper) runReplacedMethod(PassAsParams.class);
//        System.out.println();
//        assertTrue(result.object instanceof Proxy);
//    }
    
    public void testReturnThisViaField() throws Exception {
        Object result = runReplacedMethod(ReturnThisViaField.class);
        assertTrue(result instanceof Proxy);
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
    
    private Object runReplacedMethod(Class clazz) throws Exception {
        byte[] classDef = new EncapsulatedComponentGenerator(clazz.getName() + "$component", clazz)
        .generate();
        Object object = new SingleClassLoader(classDef).loadClass("intentionally corrupted").newInstance();
                
        Method returnThis = object.getClass().getDeclaredMethod("method", new Class[]{Self.class});
        Object result = returnThis.invoke(object, new Object[]{new Self()});
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
