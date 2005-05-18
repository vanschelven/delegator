package org.cq2.delegator.classgenerator;

import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.ObjectType;
import org.cq2.delegator.method.MethodFilter;
import org.cq2.delegator.method.ProxyMethodFilter;

public class ProxyGenerator extends ClassGenerator {

    private static final MethodFilter proxyMethodFilter = new ProxyMethodFilter();

    public ProxyGenerator(String className, Class superClass, Class marker) {
        super(className, superClass, marker);
    }

    public byte[] generate() {
        addSelfField();
        addDelegationMethods(proxyMethodFilter, true);
        return classGen.getJavaClass().getBytes();
    }
    
    private void addSelfField() {
        FieldGen fieldGen = new FieldGen(ACC_PUBLIC | ACC_TRANSIENT, new ObjectType(
                "java.lang.reflect.InvocationHandler"), "self", classGen.getConstantPool());
        classGen.addField(fieldGen.getField());
    }
    
}
