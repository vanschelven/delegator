package org.cq2.delegator.classgenerator;

import junit.framework.TestCase;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.RETURN;
import org.cq2.delegator.test.InstructionListRunner;

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
    
    public void testOriginalConstantPoolHasConstantsAlready() throws Exception {
        InstructionListRunner runner = new InstructionListRunner();
        
        ConstantPoolGen originalConstantPool = new ConstantPoolGen();
        originalConstantPool.addString("Someconstant that was already present");
        InstructionFactory originalFactory = new InstructionFactory(originalConstantPool);
        InstructionList originalInstructionList = new InstructionList();
        originalInstructionList.append(originalFactory.createPrintln("blabla"));

        ConstantPoolGen newConstantPool = runner.getConstantPool();
        
        InstructionListCopier copier = new InstructionListCopier(originalConstantPool, newConstantPool);
        InstructionList result = copier.copy(originalInstructionList);
        result.append(new RETURN());
        
        runner.runVoid(result);
    }
    
}
