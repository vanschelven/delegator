package org.cq2.delegator.classgenerator;

import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;

public class InstructionListCopier {

    private ConstantPoolGen constantPool;
    private ConstantPoolGen originalConstantPool;
    private static final int FIRSTCONSTANTISRESERVEDBYJVM = 1;
    
    public InstructionListCopier(ConstantPoolGen originalConstantPool, ConstantPoolGen constantPool) {
        this.originalConstantPool = originalConstantPool;
        this.constantPool = constantPool;
    }
    
    public InstructionList copy(InstructionList original) {
        for (int i = FIRSTCONSTANTISRESERVEDBYJVM; i < originalConstantPool.getSize(); i++) {
            constantPool.addConstant(originalConstantPool.getConstant(i), originalConstantPool);
        }
        InstructionList result = original.copy();
        for (int i = 0; i < result.getInstructions().length; i++) {
            if (result.getInstructions()[i] instanceof CPInstruction) {
                CPInstruction instruction = (CPInstruction) result.getInstructions()[i];
//                (instruction.getIndex())
            }
        }
        return result;
    }
    
}
