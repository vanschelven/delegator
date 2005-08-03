package org.cq2.delegator.profiling;

import java.io.PrintStream;
import java.util.Date;

public class Report {

    private final static int RUNS = 5;
    
    public static void main(String[] args) {
        PrintStream out = System.out;
        
        out.println("Start...");
        out.println("Speed:");
        Profiler calls = new Profiler(new ZillionCalls(), out);
        calls.run(RUNS);
        Profiler proxyCalls = new Profiler(new ZillionProxyCalls(), out);
        proxyCalls.run(RUNS);
        out.println("1:" + (proxyCalls.getAvg() / calls.getAvg()));
        
        Profiler callsToComponentTwo = new Profiler(new ZillionCallsToComponentTwo(), out);
        callsToComponentTwo.run(RUNS);
        out.println("1:" + (callsToComponentTwo.getAvg() / calls.getAvg()));

        Profiler mixedCalls = new Profiler(new ZillionMixedCalls(), out);
        mixedCalls.run(RUNS);
        Profiler mixedProxyCalls = new Profiler(new ZillionMixedProxyCalls(), out);
        mixedProxyCalls.run(RUNS);
        out.println("1:" + (mixedProxyCalls.getAvg() / mixedCalls.getAvg()));
        
        Profiler creations = new Profiler(new ZillionCreations(), out);
        creations.run(RUNS);
        Profiler selfCreations = new Profiler(new ZillionSelfCreations(), out);
        selfCreations.run(RUNS);
        out.println("1:" + (selfCreations.getAvg() / creations.getAvg()));
        
        out.println("Memory:");
        MemoryProfiler creationsMemory = new MemoryProfiler(new StoreCreations(), out);
        creationsMemory.run();
        MemoryProfiler selfCreationsMemory = new MemoryProfiler(new StoreSelfCreations(), out);
        selfCreationsMemory.run();
        out.println("1:" + (selfCreationsMemory.getResult() / creationsMemory.getResult()));
        
        out.println("Done... " + new Date());
    }

    //TODO dit neemt nog niet mee: class creation times en class memory overhead...
}
