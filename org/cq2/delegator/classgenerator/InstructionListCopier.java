package org.cq2.delegator.classgenerator;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantFieldref;
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
                Constant originalConstant = originalConstantPool.getConstant(instruction.getIndex());
                instruction.setIndex(find(originalConstant));
            }
        }
        return result;
    }

    private int find(Constant originalConstant) {
//        constantPool.lookupClass("");
//        constantPool.lookupDouble(1.0);
//        if (originalConstant instanceof ConstantFieldref) {
//            ConstantFieldref constant = (ConstantFieldref) originalConstant;
//            return constantPool.lookupFieldref(constant.getClass(originalConstantPool.getConstantPool()), constant., "");
//        }
//        constantPool.lookupFloat((float) 1.0);
//        constantPool.lookupInteger(0);
//        constantPool.lookupInterfaceMethodref("", "", "");
//        constantPool.lookupLong(1);
//        constantPool.lookupMethodref("", "", "");
//        constantPool.lookupNameAndType("", "");
//        constantPool.lookupString("");
//        constantPool.lookupUtf8("");
        
//        if (Constant instanceof Fi)
//        constantPool.look
//        for (int i = FIRSTCONSTANTISRESERVEDBYJVM; i < constantPool.getSize(); i++) {
//            if (constantPool.getConstant(i).equals(originalConstant))
//                return i;
//        }
        throw new RuntimeException("Should not happen...");
        //return -1;
    }
    
}
