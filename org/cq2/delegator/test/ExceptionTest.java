package org.cq2.delegator.test;

import java.sql.SQLException;

import junit.framework.TestCase;

public class ExceptionTest extends TestCase {

    public static class NoException {
        
        public void method() {
            
        }
    }
    
//    this code doesn't compile (which is the point of including it here)
//    Exception Exception is not compatible with throws clause in 
//    ExceptionTest.NoException.method()
//    public static class ExtendsNoException extends NoException {
//        
//        public void method() throws Exception {
//            
//        }
//    }
    
    public static class SomeException {
        
        public void method() throws Exception {
            
        }
    }
    
    public static class ExtendsSomeExceptionWithoutException {
        
        public void method() {}
    }
    
    public static class ExtendsSomeExceptionWithMoreSpecificException {
        
        public void method() throws SQLException {
            
        }
    }
    
    public static class ExtendsSomeExceptionWithMoreSpecificExceptions {
        
        public void method() throws SQLException, ClassNotFoundException {
            
        }
    }
    
    public static class SpecificException {
        
        public void method() throws SQLException {
            
        }
    }
    
//    this code doesn't compile (which is the point of including it here)
//    public static class ExtendsSpecificExceptionWithMoreGeneralKind extends SpecificException {
//        
//        public void method() throws Exception {
//            
//        }
//    }
    
    
}
