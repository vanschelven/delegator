package org.cq2.delegator.profiling;

import java.io.PrintStream;

public class Profiler {

    Profilable profilable;
    private long min;
    private long max;
    private long avg;
    private PrintStream reportTo;
    private int runs;
    
    public Profiler(Profilable profilable, PrintStream reportTo) {
        this.profilable = profilable;
        this.reportTo = reportTo;
    }
    
    public void run(int runs) {
        this.runs = runs;
        warmup();
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        int sum = 0;
        for (int i = 0; i < runs; i++) {
            long result = singleRun();
            min = Math.min(result, min);
            max = Math.max(max, result);
            sum += result;
        }
        avg = sum / runs;
        printReport();
    }

    private void printReport() {
        reportTo.println(profilable.getClass().getName() + " " + runs + " runs " + min + " " + avg + " " + max);
    }

    private void warmup() {
        singleRun();
    }

    private long singleRun() {
        long startTime = System.currentTimeMillis();
        profilable.runBody();
        long stopTime = System.currentTimeMillis();
        long result = stopTime - startTime;
        Runtime.getRuntime().gc();
        return result;
    }
    
    public long getAvg() {
        return avg;
    }
    
    public long getMax() {
        return max;
    }
    
    public long getMin() {
        return min;
    }
    
}
