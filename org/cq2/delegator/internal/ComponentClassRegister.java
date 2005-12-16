package org.cq2.delegator.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ComponentClassRegister {

    private Map reverse;
    private Vector componentClasses;
    private static ComponentClassRegister instance;
    
    public ComponentClassRegister() {
        reverse = new HashMap();
        componentClasses = new Vector();
    }

    public static ComponentClassRegister getInstance() {
        if (instance == null) instance = new ComponentClassRegister();
        return instance;
    }
    
    public int getIdentifier(Class componentClass) {
        Object value = reverse.get(componentClass);
        if (value != null)
            return ((Integer) value).intValue();
        int result = componentClasses.size();
        reverse.put(componentClass, new Integer(result));
        componentClasses.add(componentClass);
        return result;
    }
    
    public Class getComponentClass(int identifier) {
        return (Class) componentClasses.get(identifier);
    }
    
}
