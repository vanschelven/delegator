package org.cq2.delegator;

import java.util.HashMap;
import java.util.Map;

public class MethodRegister {

    private static MethodRegister instance;
    private Map map;

    private MethodRegister() {
        map = new HashMap();
    }
    
    public static MethodRegister getInstance() {
        if (instance == null)
            instance = new MethodRegister();
        return instance;
    }
    
    public int getUnique(MiniMethod method) {
        Object value = map.get(method);
        if (value != null)
            return ((Integer) value).intValue();
        int result = map.size();
        map.put(method, new Integer(result));
        return result;
    }
    
}
