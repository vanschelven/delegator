package org.cq2.delegator.classgenerator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Component;
import org.cq2.delegator.method.MethodFilter;

public class ComponentGenerator extends ClassGenerator {

    public ComponentGenerator(String className, Class superClass) {
        super(className, superClass, Component.class);
    }
    
    public byte[] generate() {
        addDelegationMethods(componentMethodFilter, false);
        addSuperCallMethods(componentMethodFilter);
        return classGen.getJavaClass().getBytes();
    }

    private void addSuperCallMethods(MethodFilter methodFilter) {
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            Method method = (Method) iter.next();
            int modifiers = method.getModifiers();
            if (methodFilter.filter(method) && !Modifier.isAbstract(modifiers)) {
                addSuperCallMethod(method);
            }
        }
    }

    private void addSuperCallMethod(Method method) {
        Type returnType = Type.getType(method.getReturnType());
        MethodGen methodGen = addMethodHeader(method, returnType, InvocationHandler.class, true);
        //createBindSelf(1);
        createCallToSuper(method, returnType, 2);
        //copySuperCodeAndReplaceThisBySelf(method, returnType);
        addMethodTrailer(returnType, methodGen);
    }
    
    private void createCallToSuper(Method method, Type returnType, int stackIndex) {
        createLoadThis();
        Class[] argClasses = method.getParameterTypes();
        for (int i = 0; i < argClasses.length; i++) {
            Type type = Type.getType(argClasses[i]);
            instrList.append(InstructionFactory.createLoad(type, stackIndex));
            stackIndex += type.getSize();
        }
        // this.super.>method<(
        instrList.append(instrFact.createInvoke(method.getDeclaringClass().getName(), method
                .getName(), returnType, getArgumentTypes(method), Constants.INVOKESPECIAL));
        instrList.append(InstructionFactory.createReturn(returnType));
    }
    
//    private void copySuperCodeAndReplaceThisBySelf(Method method, Type returnType) {
//        Method newMethod = new ThisBySelfSubstitutor(classGen, constPool, instrFact).generateMethod(method);
//        classGen.addMethod(newMethod);
//    }




    
}
