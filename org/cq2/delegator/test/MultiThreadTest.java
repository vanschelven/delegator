package org.cq2.delegator.test;

import java.util.HashMap;
import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.IMonitor;
import org.cq2.delegator.ISelf;
import org.cq2.delegator.ISemaphore;
import org.cq2.delegator.Monitor;
import org.cq2.delegator.Self;

public class MultiThreadTest extends TestCase {

    private final static int MAXBECOMES = 200;

    protected void setUp() throws Exception {
        becomeCounter = 0;
    }

    public class MyThread extends Thread {

        private int counter;

        private final static int NUMLOOPS = 200;

        private boolean locksAreReleased;

        public synchronized void waitFor() throws InterruptedException {
            if (!locksAreReleased)
                wait();
        }

        public void run() {
            for (int i = 0; i < NUMLOOPS; i++) {
                counter = i;
                interestingBit();
            }
            releaseLocks();
        }

        protected void interestingBit() {
            // hook
        }

        protected synchronized void releaseLocks() {
            locksAreReleased = true;
            notifyAll();
        }

        public int getCounter() {
            return counter;
        }
    }

    public void testUseThreadsInTests() throws InterruptedException {
        MyThread myThread = new MyThread();
        myThread.start();
        myThread.waitFor();
        assertEquals(MyThread.NUMLOOPS - 1, myThread.counter);
    }

    public class VectorComponentUser extends MyThread {

        private Self self;

        public VectorComponentUser(Self self) {
            this.self = self;
        }

        protected void interestingBit() {
            Vector vector = (Vector) self.cast(Vector.class);
            vector.add("a");
        }

    }

    public void testComponentUser() throws InterruptedException {
        Self self = new Self(Vector.class);
        VectorComponentUser thread1 = new VectorComponentUser(self);
        thread1.start();
        VectorComponentUser thread2 = new VectorComponentUser(self);
        thread2.start();
        thread1.waitFor();
        thread2.waitFor();
        assertEquals(MyThread.NUMLOOPS - 1, thread1.getCounter());
    }

    public interface A {

        public void m();

    }

    private static int becomeCounter;

    public abstract static class A1 implements A, ISelf {

        public void m() {
            becomeCounter++;
            if (becomeCounter < MAXBECOMES) {
                become(A2.class);
                m();
            }
        }
    }

    public abstract static class A2 implements A, ISelf {

        public void m() {
            becomeCounter++;
            if (becomeCounter < MAXBECOMES) {
                become(A1.class);
                m();
            }
        }
    }

    public static class B {

        public void n() {
        }

    }

    public class BComponentUser extends MyThread {

        private ISelf self;

        public BComponentUser(ISelf self) {
            this.self = self;
        }

        protected void interestingBit() {
            B b = (B) self.cast(B.class);
            b.n();
        }

    }

    public class CComponentUser extends MyThread {

        private ISelf self;

        private Exception exception;

        public CComponentUser(ISelf self) {
            this.self = self;
        }

        public void run() {
            try {
                C c = (C) self.cast(C.class);
                c.n(20);
            } catch (Exception e) {
                exception = e;
            }
            releaseLocks();
        }

    }

    public static class C {

        public void n(int i) {
            if (i > 0)
                n(i - 1);
        }

    }

    //is deze manier van werken niet veel handiger dan de eerste manier?!
    public class SelfManipulator extends Thread {

        private boolean stopped = false;

        private Self self;

        public SelfManipulator(Self self) {
            this.self = self;
        }

        public void run() {
            while (!stopped) {
                self.add(Vector.class);
                self.remove(Vector.class);
            }
        }

        public void stopRunning() {
            stopped = true;
        }

    }

    public void testManipulatedSelf() {
        Self self = new Self(HashMap.class);
        HashMap map = (HashMap) self.cast(HashMap.class);
        SelfManipulator thread = new SelfManipulator(self);
        thread.start();
        for (int i = 0; i < 1000; i++) {
            map.put("key" + i, "value" + i);
        }
        thread.stopRunning();
    }

    public class SelfManipulator2 extends Thread {

        private boolean stopped = false;

        private Self self;

        private Class clas;

        public SelfManipulator2(Self self, Class clas) {
            this.self = self;
            this.clas = clas;
        }

        public void run() {
            while (!stopped) {
                self.decorate(clas);
                self.remove(clas);
            }
        }

        public void stopRunning() {
            stopped = true;
        }

    }

    public void testManipulatedSelf2() {
        Self self = new Self(HashMap.class);
        HashMap map = (HashMap) self.cast(HashMap.class);
        SelfManipulator2 thread = new SelfManipulator2(self, Vector.class);
        thread.start();
        for (int i = 0; i < 1000; i++) {
            map.put("key" + i, "value" + i);
        }
        thread.stopRunning();
    }

    public static class X {

        public void m() {
        }

    }

    public static class Y {

        public void m() {
        }

    }

   //TODO valt deze soms om?
    public void testSameMethodRemoveOne() {
        Self self = new Self(X.class);
        X x = (X) self.cast(X.class);
        SelfManipulator2 thread = new SelfManipulator2(self, Y.class);
        thread.start();
        for (int i = 0; i < 1000; i++) {
            x.m();
        }
        thread.stopRunning();
    }

    //dit zou wel kunnen met synchronized spul
    public void testSemaphore() throws InterruptedException {
        java.util.concurrent.Semaphore semaphore = new java.util.concurrent.Semaphore(
                1);
        //semaphore.acquire();
        semaphore.acquire();
        //semaphore.release();
        semaphore.release();
    }

    private void n(org.cq2.delegator.Semaphore s, int i)
            throws InterruptedException {
        s.acquire();
        if (i > 0)
            n(s, i - 1);
        s.release();
    }

    public void testDelegatorSemaphoreCanDealWithRecursiveStuff()
            throws InterruptedException {
        org.cq2.delegator.Semaphore semaphore = new org.cq2.delegator.Semaphore();
        n(semaphore, 10);
    }

    private org.cq2.delegator.Semaphore semaphore;

    private boolean b = false;

    public class BlaaaaThread extends Thread {

        private boolean stopped = false;

        public void run() {
            while (!stopped) {
                try {
                    semaphore.acquire();
                    assertFalse(b);
                    b = true;
                    b = false;
                    semaphore.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void stopRunning() {
            stopped = true;
        }

    }

    public void testDelegatorSemaphoreReallyIsASemaphore() {
        semaphore = new org.cq2.delegator.Semaphore();
        BlaaaaThread thread1 = new BlaaaaThread();
        thread1.start();
        BlaaaaThread thread2 = new BlaaaaThread();
        thread2.start();
        BlaaaaThread thread3 = new BlaaaaThread();
        thread3.start();
        for (int i = 0; i < 10000; i++) {
            //do nothing but wait for the other threads;
        }
        thread1.stopRunning();
        thread2.stopRunning();
        thread3.stopRunning();
    }

    public abstract static class Counter implements ISemaphore {

        private int value = 0;

        public int inc() throws InterruptedException {
            acquire();
            value++;
            int localValue = value;
            release();
            return localValue;
        }

        protected void setValue(int i) {
            value = i;
        }

        public int getValue() {
            return value;
        }

    }

    public abstract static class ForwardingCounter implements ISemaphore {

        public abstract int getValue();

        public abstract void setValue(int i);

        public int doubleValue() throws InterruptedException {
            acquire();
            setValue(getValue() * 2);
            int localValue = getValue(); //aaaaarrggg!! dit is erg lelijk.
            release();
            return localValue;
        }

    }

    private Vector list;

    private Counter counter;

    public class CounterThread extends MyThread {

        protected void interestingBit() {
            try {
                list.add(new Integer(counter.inc()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void testExampleFromProposal1() throws InterruptedException {
        list = new Vector();
        Self self = new Self(Counter.class);
        self.add(org.cq2.delegator.Semaphore.class);
        counter = (Counter) self.cast(Counter.class);
        CounterThread thread1 = new CounterThread();
        thread1.start();
        CounterThread thread2 = new CounterThread();
        thread2.start();
        thread1.waitFor();
        thread2.waitFor();
        for (int i = 1; i <= 400; i++) {
            assertTrue(list.contains(new Integer(i)));
        }
    }

    public abstract static class MonitorCounter implements IMonitor {

        private int value = 0;

        public int inc() throws InterruptedException {
            synchronized (getMonitor()) {
                value++;
                return value;
            }
        }

        protected void setValue(int i) {
            value = i;
        }

        public int getValue() {
            return value;
        }

    }

    MonitorCounter monitorCounter;

    public class MonitorCounterThread extends MyThread {

        protected void interestingBit() {
            try {
                list.add(new Integer(monitorCounter.inc()));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void testExampleFromProposalWithMonitor()
            throws InterruptedException {
        list = new Vector();
        Self self = new Self(MonitorCounter.class);
        self.add(Monitor.class);
        monitorCounter = (MonitorCounter) self.cast(MonitorCounter.class);
        MonitorCounterThread thread1 = new MonitorCounterThread();
        thread1.start();
        MonitorCounterThread thread2 = new MonitorCounterThread();
        thread2.start();
        thread1.waitFor();
        thread2.waitFor();
        for (int i = 1; i <= 400; i++) {
            assertTrue(list.contains(new Integer(i)));
        }
    }

}