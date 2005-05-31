package org.cq2.delegator.test;

import junit.framework.TestCase;

import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.InstructionList;

public class InstructionListRunnerTest extends TestCase {

    public void testEmtpyVoid() throws Exception {
        InstructionListRunner runner = new InstructionListRunner();
        InstructionList instructionList = new InstructionList();
        instructionList.append(new RETURN());
        runner.runVoid(instructionList);
    }
    
}
