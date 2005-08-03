package org.cq2.delegator.profiling;

import org.cq2.delegator.MiniMethod;

public class Sandbox {
    
    private static class IntObj {
        
        private int i;
        
        public IntObj(int i) {
            this.i = i;
        }
        
        public boolean equals(Object obj) {
            return (i == ((IntObj) obj).i);
        }
        
    }
    
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        IntObj intObj1 = new IntObj(1);
        IntObj intObj2 = new IntObj(1);
        for (int i = 0; i < 100000; i++) {
            intObj1.equals(intObj2);
        }
        long stopTime = System.currentTimeMillis();
        long result = stopTime - startTime;
        Runtime.getRuntime().gc();
        System.out.println(result);
        main2();
    }

    private static void main2() {
        long startTime = System.currentTimeMillis();
        MiniMethod miniMethod1 = new MiniMethod("aaa", new Class[]{}, new Class[]{}, 0);
        MiniMethod miniMethod2 = new MiniMethod("aaa", new Class[]{}, new Class[]{}, 0);
        for (int i = 0; i < 100000; i++) {
            miniMethod1.equals(miniMethod2);
        }
        long stopTime = System.currentTimeMillis();
        long result = stopTime - startTime;
        Runtime.getRuntime().gc();
        System.out.println(result);
    }
    

}
