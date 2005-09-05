package org.cq2.delegator;

import java.util.HashMap;
import java.util.Vector;

public class ComposedClassCache {

    static HashMap map = new HashMap();

    static ComposedClass get(Vector classes) {
        ComposedClass result = (ComposedClass) map.get(classes);
        if (result != null)
            return result;
        result = new ComposedClass(classes);
        map.put(classes, result);
        return result;
    }

}