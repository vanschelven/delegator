package org.cq2.delegator.test;

import java.util.HashMap;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

import junit.framework.TestCase;

/**
 * @author klaas
 *
 * This class contains problems or bugs that are not well understood yet.
 */
public class NotWellUnderstoodTest extends TestCase {
    
	public interface PublicSomeMethod { 
	    public void method();
	}

	public void testPublicInterface() {
	    ISelf map = new Self(HashMap.class);
	    PublicSomeMethod o = (PublicSomeMethod) map.cast(PublicSomeMethod.class);
	}

	interface PackageSomeMethod { 
	    public void method();
	}

	public void testPackageInterface() {
	    ISelf map = new Self(HashMap.class);
	    PackageSomeMethod o = (PackageSomeMethod) map.cast(PackageSomeMethod.class);
	}

	protected interface ProtectedSomeMethod { 
	    public void method();
	}

	public void testProtectedInterface() {
	    ISelf map = new Self(HashMap.class);
	    ProtectedSomeMethod o = (ProtectedSomeMethod) map.cast(ProtectedSomeMethod.class);
	}


}
