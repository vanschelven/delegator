package org.cq2.delegator.test;

import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

/**
 * @author klaas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MultiThreadTest extends TestCase {

    private final static int MAXBECOMES = 200;

    protected void setUp() throws Exception {
        becomeCounter = 0;
    }

    // dit ding laten crashen kan veel simpeler
    //    public class SelfManipulator extends Thread {
    //        
    //        private boolean stopped = false;
    //        private Self self;
    //
    //        public SelfManipulator(Self self) {
    //            this.self = self;
    //        }
    //        
    //        public void run() {
    //            while (!stopped) {
    //                self.add("");
    //            }
    //        }
    //        
    //        public void stopRunning() {
    //            stopped = true;
    //        }
    //        
    //    }

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

        private synchronized void releaseLocks() {
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
            //System.out.println(this + " " + vector.toString());
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
            System.out.println(this.toString() + "    " + becomeCounter);
            becomeCounter++;
            if (becomeCounter < MAXBECOMES) {
                become(A2.class);
                m();
            }
        }
    }

    public abstract static class A2 implements A, ISelf {

        public void m() {
            System.out.println(this.toString() + "    " + becomeCounter);
            becomeCounter++;
            if (becomeCounter < MAXBECOMES) {
                become(A1.class);
                m();
            }
        }
    }

    public static class B {

        public void n() {
            //nothing here.
            System.out.println(this + "   n");
        }

    }

    public void testBecomeUsingSingleThread() {
        ISelf self = new Self(A1.class);
        self.add(B.class);
        A a = (A) self.cast(A.class);
        a.m();

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

    public void testBecomeUsingTwoThreads() throws InterruptedException {
        ISelf self = new Self(A1.class);
        self.add(B.class);
        A a = (A) self.cast(A.class);
        BComponentUser thread = new BComponentUser(self);
        thread.start();
        a.m();
        thread.waitFor();
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
        }

    }

    public static class C {
        
        public void n(int i) {
            System.out.println(i);
            if (i > 0)
                n(i - 1);
        }
        
    }

    public void testBecomeUsingTwoThreads2() throws InterruptedException {
        ISelf self = new Self(A1.class);
        self.add(C.class);
        A a = (A) self.cast(A.class);
        CComponentUser thread = new CComponentUser(self);
        thread.start();
        a.m();
        thread.waitFor();
        assertNull(thread.exception);
    }

}