package org.cq2.delegator.test;

import java.util.Vector;

import junit.framework.TestCase;

import org.cq2.delegator.Self;

/**
 * @author klaas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MultiThreadTest extends TestCase {

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
    
//    public class ComponentUser extends Thread {
//    
//        private Self self;
//        private final static int NUMLOOPS = 1000;
//        private Vector vector;
//
//        public ComponentUser(Self self) {
//            this.self = self;
//            this.vector = (Vector) self.cast(Vector.class);
//        }
//        
//        public void run() {
//            for (int i = 0; i < NUMLOOPS; i++) {
//                System.out.println(vector.toString());
//            }
//
//        }
//    }
//    
//    public synchronized void testSingleThread() throws InterruptedException {
//        Self self = new Self(Vector.class);
//        ComponentUser componentUser1 = new ComponentUser(self);
//        componentUser1.start();
//        wait();
//    }
}
