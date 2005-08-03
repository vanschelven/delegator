package org.cq2.delegator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.cq2.delegator.MethodsCache.Tuple;

public class MethodsCache2 {

    private Map map;

    public Tuple getTuple(int uniqueMethodIdentifier) {
        return (Tuple) map.get(new Integer(uniqueMethodIdentifier));
    }

    public void put(int uniqueMethodIdentifier, int componentIndex, Method delegateMethod) {
        if (map == null) map = new HashMap();
        map.put(new Integer(uniqueMethodIdentifier), new Tuple(componentIndex, delegateMethod));
    }

    public boolean contains(int index) {
        if (map == null) return false;
        return map.containsKey(new Integer(index));
    }

    public void clear() {
        map = null;
    }

}
