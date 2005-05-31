package org.cq2.delegator.classgenerator;

import java.util.HashMap;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantClass;
import org.apache.bcel.classfile.ConstantFieldref;
import org.apache.bcel.classfile.ConstantNameAndType;
import org.apache.bcel.classfile.ConstantUtf8;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
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
            int newIndex = constantPool.addConstant(originalConstantPool.getConstant(i), originalConstantPool);
            constantMap.put(new Integer(i), new Integer(newIndex));
        }
        InstructionList result = original.copy();
        for (int i = 0; i < result.getInstructions().length; i++) {
            if (result.getInstructions()[i] instanceof CPInstruction) {
                CPInstruction instruction = (CPInstruction) result.getInstructions()[i];
//                Constant originalConstant = originalConstantPool.getConstant(instruction.getIndex());
//                instruction.setIndex(find(originalConstant));
                instruction.setIndex(getNewIndex(instruction.getIndex()));
            }
        }
        return result;
    }

    private int getNewIndex(int index) {
        return ((Integer) constantMap.get(new Integer(index))).intValue();
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
        
//        for (int i = FIRSTCONSTANTISRESERVEDBYJVM; i < constantPool.getSize(); i++) {
//            if (constantRepresentation(constantPool.getConstant(i)).equals(constantRepresentation(originalConstant)))
//                return i;
//        }
        throw new RuntimeException("Should not happen...");
        //return -1;
    }
    
    private String constantRepresentation(Constant constant) {
        if (constant instanceof ConstantUtf8) return ((ConstantUtf8)constant).getBytes();
        if (constant instanceof ConstantFieldref) {
            ConstantFieldref fieldref = (ConstantFieldref)constant;
            return constantRepresentation(originalConstantPool.getConstant(fieldref.getClassIndex())) +
              constantRepresentation(originalConstantPool.getConstant(fieldref.getNameAndTypeIndex()));
        }
        if (constant instanceof ConstantClass) 
            return ((ConstantClass)constant).getBytes(originalConstantPool.getConstantPool());
        if (constant instanceof ConstantNameAndType) 
            return ((ConstantNameAndType)constant).getName(originalConstantPool.getConstantPool()) + 
            ((ConstantNameAndType)constant).getSignature(originalConstantPool.getConstantPool());
        throw new RuntimeException("Define more cases, specifically for " + constant);
    }
    
}
