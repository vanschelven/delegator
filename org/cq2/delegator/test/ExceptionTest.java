package org.cq2.delegator.test;

import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.cq2.delegator.DelegatorException;
import org.cq2.delegator.Self;

public class ExceptionTest extends TestCase {

//The following classes demonstrate Java's behavior considering Exceptions
    
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
    
    
//code from here on demonstrates Delegator's behavior...

	public static class TwoExceptions {
	    
	    public void method() throws SQLException, DelegatorException {
	        
	    }
	    
	}
	
	public static class TwoExceptionsReverseOrder {
	    
	    public void method() throws DelegatorException, SQLException {
	        
	    }
	    
	}
	
	public class MoreSpecificDelegatorException extends DelegatorException {

        public MoreSpecificDelegatorException(String string) {
            super(string);
        }
    }
	
	public static class TwoExceptionsMoreSpecific {
	    
	    public void method() throws SQLException, MoreSpecificDelegatorException {}
	}
	
	public static class UncheckedException {
	    
	    public void method() throws ArithmeticException {
	        
	    }
	    
	}
	
	public void testMissingExceptionForClasses() throws DelegatorException, SQLException, SecurityException, NoSuchMethodException {
	    NoException noException = (NoException) new Self(TwoExceptions.class).cast(NoException.class);
	    try {
	        noException.method();
	        fail();
	    } catch (NoSuchMethodError e) {  }
	    
	}

	public void testExceptions() throws DelegatorException, SQLException, SecurityException, NoSuchMethodException {
	    TwoExceptions x = (TwoExceptions) new Self(TwoExceptions.class).cast(TwoExceptions.class);
        x.method();
	}
	
	public void testExceptionsInWrongOrder() throws DelegatorException, SQLException, SecurityException, NoSuchMethodException {
	    TwoExceptionsReverseOrder x = (TwoExceptionsReverseOrder) new Self(TwoExceptions.class).cast(TwoExceptionsReverseOrder.class);
        x.method();
	}
	
	public void testExceptionsMayBeLeftOut() throws DelegatorException, SQLException {
	    TwoExceptions x = (TwoExceptions) new Self(NoException.class).cast(TwoExceptions.class);
        x.method();
	}
	
	public void testExceptionsMayBeMoreSpecific() throws DelegatorException, SQLException {
	    TwoExceptions x = (TwoExceptions) new Self(TwoExceptionsMoreSpecific.class).cast(TwoExceptions.class);
        x.method();
	}
    
	public static abstract class ImplementationMock {

	    public boolean execute(String query) throws SQLException {
			throw new SQLException();
		}

	}
	
	public void testExceptionsArePassedOnCorrectly() {
		Statement statement = (Statement) new Self(ImplementationMock.class).cast(Statement.class);
		try {
			statement.execute("force SQLException");
			fail();
		} catch (SQLException e) {} catch (Throwable e) {
			fail("Invalid exception! " + e);
		}
	}

	public void testUncheckedExceptionIsIrrelevant() {
	    NoException noException = (NoException) new Self(UncheckedException.class).cast(NoException.class);
	    noException.method();
	}
    
}
