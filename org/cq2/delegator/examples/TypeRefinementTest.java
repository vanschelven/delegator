/*
 * Created on Apr 2, 2004
 */
package org.cq2.delegator.examples;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.accessibility.AccessibleIcon;

import junit.framework.TestCase;

import org.cq2.delegator.ISelf;
import org.cq2.delegator.Self;

public class TypeRefinementTest extends TestCase {
    
	public static ISelf createMap() {
		return new Self(HashMap.class);
	}

	public interface DateMap {
		void put(String key, Date value);
		Date get(String key);
	}

	public void testPutAndGetWithMoreSpecificTypes() {
		DateMap map = (DateMap) createMap().cast(DateMap.class);
		map.put("birthday", new Date(2345690));
		assertEquals(new Date(2345690), map.get("birthday"));
	}

	public interface IntMap {
		void put(int key, int value);
		int get(int key);
	}

	public void testBoxing() {
		IntMap map = (IntMap) createMap().cast(IntMap.class);
		map.put(9, 16);
		assertEquals(16, map.get(9));
	}
	
	public void test1() {
		Map map = (Map) createMap().cast(Map.class);
	}

	public void test2() {
	    List o = (List) createMap().cast(List.class);
	}
	
	public interface SomeMethod { //als je hier geen public voor zet doet ie het niet
	    public void method();
	}

	public void test3() {
	    ISelf map = createMap();
	    SomeMethod o = (SomeMethod) map.cast(SomeMethod.class);
	}
	
	public void test4() {
	    AccessibleIcon o = (AccessibleIcon) createMap().cast(AccessibleIcon.class);
	}
	
}
