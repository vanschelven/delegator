package org.cq2.delegator.internal;

import java.lang.reflect.Method;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.Type;
import org.cq2.delegator.Self;

public class Generator implements Constants{

    protected Type[] appendIntType(Type[] input) {
        Type[] result = new Type[input.length + 1];
        result[result.length - 1] = Type.INT;
        System.arraycopy(input, 0, result, 0, input.length);
        return result;
    }

    protected static String[] generateParameterNames(int nr) {
        String[] result = new String[nr];
        for (int i = 0; i < nr; i++) {
            result[i] = "arg" + i;
        }
        return result;
    }

    protected static String[] insertSelfString(String[] input) {
        String[] result = new String[input.length + 1];
        result[0] = "self";
        System.arraycopy(input, 0, result, 1, input.length);
        return result;
    }

    protected static Type[] getArgumentTypes(Method method) {
        return Type.getArgumentTypes(Type.getSignature(method));
    }

    protected static Type[] insertSelfType(Type[] input) {
        Type[] result = new Type[input.length + 1];
        result[0] = Type.getType(Self.class);
        System.arraycopy(input, 0, result, 1, input.length);
        return result;
    }

    
}
