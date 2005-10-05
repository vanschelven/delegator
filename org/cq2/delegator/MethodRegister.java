package org.cq2.delegator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MethodRegister {

    private static MethodRegister instance;
    private Map map;
    private Vector vector;

    private MethodRegister() {
        map = new HashMap();
        vector = new Vector();
    }
    
    public static MethodRegister getInstance() {
        if (instance == null)
            instance = new MethodRegister();
        return instance;
    }
    
    public int getIdentifier(MiniMethod method) {
        Object value = map.get(method);
        if (value != null)
            return ((Integer) value).intValue();
        int result = map.size();
        map.put(method, new Integer(result));
        vector.add(method);
        return result;
    }
    
    public MiniMethod getMethod(int identifier) {
        return (MiniMethod) vector.get(identifier);
    }
    
}
