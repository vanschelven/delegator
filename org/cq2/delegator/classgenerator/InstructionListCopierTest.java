package org.cq2.delegator.classgenerator;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;

import sun.awt.OrientableFlowLayout;

import junit.framework.TestCase;

public class InstructionListCopierTest extends TestCase {

    //TODO refactor...
    public void testEmptyList() {
        ConstantPoolGen originalConstantPool = new ConstantPoolGen();
        InstructionFactory originalFactory = new InstructionFactory(originalConstantPool);
        InstructionList originalInstructionList = new InstructionList();
//        originalInstructionList.append(originalFactory.createPrintln("blabla"));
        originalInstructionList.setPositions();
        
        ConstantPoolGen newConstantPool = new ConstantPoolGen();
        
        InstructionListCopier copier = new InstructionListCopier(originalConstantPool, newConstantPool);
        InstructionList result = copier.copy(originalInstructionList);
        
        result.setPositions();
        assertEquals(originalInstructionList.toString(), result.toString());
        assertEquals(originalConstantPool.toString(), newConstantPool.toString());
    }
    
    public void testPrintln() {
        ConstantPoolGen originalConstantPool = new ConstantPoolGen();
        InstructionFactory originalFactory = new InstructionFactory(originalConstantPool);
        InstructionList originalInstructionList = new InstructionList();
        originalInstructionList.append(originalFactory.createPrintln("blabla"));
        originalInstructionList.setPositions();
        
        ConstantPoolGen newConstantPool = new ConstantPoolGen();
        
        InstructionListCopier copier = new InstructionListCopier(originalConstantPool, newConstantPool);
        InstructionList result = copier.copy(originalInstructionList);
        
        result.setPositions();
        assertEquals(originalInstructionList.toString(), result.toString());
        assertEquals(originalConstantPool.toString(), newConstantPool.toString());
    }
    
    public void testOriginalConstantPoolHasConstantsAlready() {
        ConstantPoolGen originalConstantPool = new ConstantPoolGen();
        originalConstantPool.addString("Someconstant that was already present");
        InstructionFactory originalFactory = new InstructionFactory(originalConstantPool);
        InstructionList originalInstructionList = new InstructionList();
        originalInstructionList.append(originalFactory.createPrintln("blabla"));

        ConstantPoolGen newConstantPool = new ConstantPoolGen();
        
        InstructionListCopier copier = new InstructionListCopier(originalConstantPool, newConstantPool);
        InstructionList result = copier.copy(originalInstructionList);
        
        //een of andere slimme test
    }
    

    
}
