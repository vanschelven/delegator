package org.cq2.delegator.internal;

import java.util.HashMap;
import java.util.List;


public class ComposedClassCache {

    static HashMap map = new HashMap();

    static ComposedClass get(List classes) {
        ComposedClass result = (ComposedClass) map.get(classes);
        if (result != null)
            return result;
        result = new ComposedClass(classes);
        map.put(classes, result);
        return result;
    }

}