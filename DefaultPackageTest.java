import org.cq2.delegator.Delegator;

import junit.framework.TestCase;

public class DefaultPackageTest extends TestCase {

    public void testDelegator() {
  	    Delegator.extend(Object.class, new Class[]{DefaultPackageClass.class});
    }
    
}
