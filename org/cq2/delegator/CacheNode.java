package org.cq2.delegator;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CacheNode {

    private Vector classes;
    private CacheNode[] removeReferences;
    private Map addMap;
    private Map insertMap;
    public MethodsCache3 methodsCache;
    
    public CacheNode() {
        this(new Vector());
    }
    
    public CacheNode(Vector classes) {
        this.classes = classes;
        removeReferences = new CacheNode[classes.size()];
        addMap = new HashMap();
        insertMap = new HashMap();
        methodsCache = new MethodsCache3();
    }
    
    public CacheNode remove(int i) {
        CacheNode result = removeReferences[i];
        if (result == null) {
            Vector smallerClasses = new Vector(classes);
            smallerClasses.remove(i);
            result = CacheCache.get(smallerClasses);
            removeReferences[i] = result;
        }
        return result;
    }

    public CacheNode add(Class clazz) {
        CacheNode result = (CacheNode) addMap.get(clazz);
        if (result == null) {
            Vector largerClasses = new Vector(classes);
            largerClasses.add(clazz);
            result = CacheCache.get(largerClasses);
            addMap.put(clazz, result);
        }
        return result;
    }

    public CacheNode insert(Class clazz) {
        CacheNode result = (CacheNode) insertMap.get(clazz);
        if (result == null) {
            Vector largerClasses = new Vector(classes);
            largerClasses.add(0, clazz);
            result = CacheCache.get(largerClasses);
            insertMap.put(clazz, result);
        }
        return result;
    }

    public CacheNode become(int i, Class clazz) {
        //TODO not optimized (yet)
        Vector becomeClasses = new Vector(classes);
        becomeClasses.set(i, clazz);
        return CacheCache.get(becomeClasses);
    }

}
