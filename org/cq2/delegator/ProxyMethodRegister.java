package org.cq2.delegator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ProxyMethodRegister {

    private static ProxyMethodRegister instance;
    private Map reverse;
    private Vector methods;
    private Class[] proxyMethodClasses;

    private ProxyMethodRegister() {
        reverse = new HashMap();
        methods = new Vector();
        proxyMethodClasses = new Class[0];
    }
    
    public static ProxyMethodRegister getInstance() {
        if (instance == null)
            instance = new ProxyMethodRegister();
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
    
    public Class getProxyMethodClass(int methodIdentifier) {
        if (methodIdentifier >= proxyMethodClasses.length)
            enlargeProxyMethodClasses(proxyMethodClasses.length + 1);
        Class result = proxyMethodClasses[methodIdentifier];
        if (result == null) {
            result = new ProxyMethodGenerator(methodIdentifier, getMethod(methodIdentifier)).generate();
            proxyMethodClasses[methodIdentifier] = result;
        }
        return result;
    }
    
    private void enlargeProxyMethodClasses(int length) {
        Class[] oldProxyMethodClasses = proxyMethodClasses;
        proxyMethodClasses = new Class[length];
        System.arraycopy(oldProxyMethodClasses, 0, proxyMethodClasses, 0, oldProxyMethodClasses.length);
    }    
    
}
