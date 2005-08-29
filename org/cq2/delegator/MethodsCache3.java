package org.cq2.delegator;

import java.lang.reflect.Method;

import org.cq2.delegator.MethodsCache.Tuple;
import org.cq2.delegator.classgenerator.ClassGenerator;

public class MethodsCache3 {

    private Tuple[] tuples = new Tuple[0];
    MethodWrapper[] wrappers = new MethodWrapper[0];

    public Tuple getTuple(int uniqueMethodIdentifier) {
        if (tuples == null || uniqueMethodIdentifier >= tuples.length)
            return null;
        return tuples[uniqueMethodIdentifier];
    }

    public Tuple put(int uniqueMethodIdentifier, Self self, int componentIndex,
            Method delegateMethod) {
        if (tuples == null || tuples.length <= uniqueMethodIdentifier)
            createArray(uniqueMethodIdentifier + 1);
        Tuple result = new Tuple(componentIndex,
                delegateMethod);
        tuples[uniqueMethodIdentifier] = result;
        MiniMethod method = MethodRegister.getInstance().getMethod(uniqueMethodIdentifier);
        if (method.returnType == int.class && method.parameterTypes.length == 0)
            if (self.components[componentIndex] instanceof Component)
                wrappers[uniqueMethodIdentifier] = new MethodWrapperGenerator(self.components[componentIndex].getClass(), method.name + ClassGenerator.SUPERCALL_POSTFIX).generate(uniqueMethodIdentifier);
            else wrappers[uniqueMethodIdentifier] = new MethodWrapperGenerator(self.components[componentIndex].getClass(), method.name).generate(uniqueMethodIdentifier);
        return result;
    }

    private void createArray(int length) {
        Tuple[] oldTuples = tuples;
        tuples = new Tuple[length];
        System.arraycopy(oldTuples, 0, tuples, 0, oldTuples.length);
        MethodWrapper[] oldWrappers = wrappers;
        wrappers = new MethodWrapper[length];
        System.arraycopy(oldWrappers, 0, wrappers, 0, oldWrappers.length);
    }

    public void clear() {
        tuples = null;
    }

}