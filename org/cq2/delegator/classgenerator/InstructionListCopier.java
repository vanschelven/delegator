package org.cq2.delegator.classgenerator;

import java.util.HashMap;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

public class InstructionListCopier {

    private ConstantPoolGen constantPool;
    private ConstantPoolGen originalConstantPool;
    private static final int FIRSTCONSTANTISRESERVEDBYJVM = 1;
    private HashMap constantMap;
    
    public InstructionListCopier(ConstantPoolGen originalConstantPool, ConstantPoolGen constantPool) {
        this.originalConstantPool = originalConstantPool;
        this.constantPool = constantPool;
        constantMap = new HashMap();
    }
    
    public InstructionList copy(InstructionList original) {
        for (int i = FIRSTCONSTANTISRESERVEDBYJVM; i < originalConstantPool.getSize(); i++) {
            Constant constant = originalConstantPool.getConstant(i);
            if (constant != null) { //TODO apart testen?? echwel!
                int newIndex = constantPool.addConstant(constant, originalConstantPool);
                constantMap.put(new Integer(i), new Integer(newIndex));
            }
        }
        InstructionList result = original.copy();
        InstructionHandle current = result.getStart();
        while (current != null) {
            if (current.getInstruction() instanceof CPInstruction) {
                CPInstruction instruction = (CPInstruction) current.getInstruction();
                instruction.setIndex(getNewIndex(instruction.getIndex()));
            }
            current = current.getNext();
        }
        return result;
    }

    private int getNewIndex(int index) {
        return ((Integer) constantMap.get(new Integer(index))).intValue();
    }
    
}
