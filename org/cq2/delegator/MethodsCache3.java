package org.cq2.delegator;

import java.lang.reflect.Method;

import org.cq2.delegator.MethodsCache.Tuple;

public class MethodsCache3 {

    private Tuple[] tuples;

    public Tuple getTuple(int uniqueMethodIdentifier) {
        if (tuples == null || uniqueMethodIdentifier >= tuples.length)
            return null;
        return tuples[uniqueMethodIdentifier];
    }

    public void put(int uniqueMethodIdentifier, int componentIndex,
            Method delegateMethod) {
        if (tuples == null || tuples.length <= uniqueMethodIdentifier)
            createArray(uniqueMethodIdentifier + 1);
        tuples[uniqueMethodIdentifier] = new Tuple(componentIndex,
                delegateMethod);
    }

    private void createArray(int length) {
        Tuple[] oldTuples = tuples;
        tuples = new Tuple[length];
        if (oldTuples != null) {
            for (int i = 0; i < oldTuples.length; i++) {
                tuples[i] = oldTuples[i];
            }
        }
    }

    public void clear() {
        tuples = null;
    }

}