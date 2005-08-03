package org.cq2.delegator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodsCache {

    public static class Tuple {
        public Tuple(int index, Method method) {
            this.index = index;
            this.method = method;
        }
        public int index;
        public Method method;
    }
    
    private Map map;
    
    public MethodsCache() {
        
    }
    
    public boolean contains(MiniMethod method) {
        if (map == null) return false;
        return map.containsKey(method);
    }
    
    public int getIndex(MiniMethod method) {
        return ((Tuple) map.get(method)).index;
    }
   
    public Tuple getTuple(MiniMethod method) {
        return ((Tuple) map.get(method));
    }
    
    public Method getMethod(MiniMethod method) {
        return ((Tuple) map.get(method)).method;
    }

//    public void put(Method method, int index, Method delegateMethod) {
//        if (map == null) map = new HashMap();
//        map.put(method, new Tuple(index, delegateMethod));
//    }

    public void clear() {
        map = null;
        //map.clear();
    }

    public void put(MiniMethod method, int index, Method delegateMethod) {
        if (map == null) map = new HashMap();
        map.put(method, new Tuple(index, delegateMethod));
    }
    
}
