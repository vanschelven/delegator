package org.cq2.delegator.internal;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.cq2.delegator.classgenerator.ClassGenerator;

public class ForwardingMethodRegister {

    private static ForwardingMethodRegister instance;
    private Map reverse;
    private Vector methods;
    private Class[] forwardingMethodClasses;

    private ForwardingMethodRegister() {
        reverse = new HashMap();
        methods = new Vector();
        forwardingMethodClasses = new Class[0];
    }
    
    public static ForwardingMethodRegister getInstance() {
        if (instance == null)
            instance = new ForwardingMethodRegister();
        return instance;
    }
    
    public int getMethodIdentifier(Method method) {
        Object value = reverse.get(method);
        if (value != null)
            return ((Integer) value).intValue();
        int result = methods.size();
        reverse.put(method, new Integer(result));
        methods.add(method);
        return result;
    }
    
    public Method getMethod(int methodIdentifier) {
        return (Method) methods.get(methodIdentifier);
    }
    
    public Class getForwardingMethodClass(int methodIdentifier) {
        if (methodIdentifier >= forwardingMethodClasses.length)
            enlargeForwardingMethodClasses(methodIdentifier + 1);
        Class result = forwardingMethodClasses[methodIdentifier];
        if (result == null) {
            try {
                result = ClassGenerator.getClassLoader().loadClass("org.cq2.delegator.internal.ForwardingMethod" + methodIdentifier);
            } catch (ClassNotFoundException e) {
               throw new RuntimeException(e);
            }
            forwardingMethodClasses[methodIdentifier] = result;
        }
        return result;
    }
    
    private void enlargeForwardingMethodClasses(int length) {
        Class[] oldForwardingMethodClasses = forwardingMethodClasses;
        forwardingMethodClasses = new Class[length];
        System.arraycopy(oldForwardingMethodClasses, 0, forwardingMethodClasses, 0, oldForwardingMethodClasses.length);
    }    
    
}
