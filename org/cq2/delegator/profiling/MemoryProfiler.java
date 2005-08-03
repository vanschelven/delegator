package org.cq2.delegator.profiling;

import java.io.PrintStream;

public class MemoryProfiler {

    MemoryProfilable profilable;
    private PrintStream reportTo;
    private long result;
    
    public MemoryProfiler(MemoryProfilable profilable, PrintStream reportTo) {
        this.profilable = profilable;
        this.reportTo = reportTo;
    }
    
    public void run() {
        warmup();
        result = singleRun();
        printReport();
    }

    private void printReport() {
        reportTo.println(profilable.getClass().getName() + " " + result);
    }

    private void warmup() {
        singleRun();
    }

    private long singleRun() {
        Runtime.getRuntime().gc();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        MemoryProfilable newInstance = profilable.newInstance();
        newInstance.runBody();
        Runtime.getRuntime().gc();
        newInstance.toString(); //forces this ref. to exist after gc
        long stopMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return stopMemory - startMemory;
    }
        
    public long getResult() {
        return result;
    }
}
