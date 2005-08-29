package org.cq2.delegator;

import java.util.HashMap;
import java.util.Vector;

public class CacheCache {

    static HashMap map = new HashMap();
    private static CacheNode emptyNode;
    
    public static CacheNode get(Vector classes) {
        CacheNode result = (CacheNode) map.get(classes);
        if (result != null) return result;
        result = new CacheNode(classes);
        map.put(classes, result);
        return result;
    }

    public static CacheNode getEmptyNode() {
        if (emptyNode == null) {
            emptyNode = new CacheNode();
            map.put(new Vector(), emptyNode);
        }
        return emptyNode;
    }

    
    
}
